package it.wldt.adapter.http.digital.adapter;

import io.undertow.Undertow;
import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.adapter.http.digital.server.HttpDigitalAdapterRequestListener;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.state.*;
import it.wldt.exception.EventBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.stream.Collectors;

import static it.wldt.adapter.http.digital.server.HttpDigitalAdapterHandlersFactory.createDefaultRoutingHandler;

/**
 * HTTP Digital Adapter class extending {@link DigitalAdapter} and implementing {@link HttpDigitalAdapterRequestListener}.
 * This class provides functionality for handling HTTP requests and managing the digital twin state.
 *
 * @author Marco Picone, Ph.D. - picone.m@gmail.com, Marta Spadoni University of Bologna
 */
public class HttpDigitalAdapter extends DigitalAdapter<HttpDigitalAdapterConfiguration> implements HttpDigitalAdapterRequestListener {

    private final static Logger logger = LoggerFactory.getLogger(HttpDigitalAdapter.class);

    /**
     * Reference to the current DT instance, used to describe the structure of the DT in terms of adapters
     */
    private final DigitalTwin digitalTwinInstance;

    /**
     * The latest updated DT State
     */
    private DigitalTwinState updatedDigitalTwinState = null;

    /**
     * The previous computed DT State
     */
    private DigitalTwinState previousDigitalTwinState = null;

    /**
     * The list of changes that are associated to the latest DT State update
     */
    private ArrayList<DigitalTwinStateChange> latestDigitalTwinStateChangeList = null;

    /**
     * List of received Event Notifications
     */
    private final List<DigitalTwinStateEventNotification<?>> eventNotificationList = new LinkedList<>();

    /**
     * The reference to the Undertow server used by the Adapter
     */
    private Undertow server;

    /**
     * Constructs an HTTP Digital Adapter instance with the given configuration and digital twin instance.
     *
     * @param configuration The configuration for the HTTP Digital Adapter.
     * @param digitalTwinInstance The Digital Twin instance associated with this adapter.
     */
    public HttpDigitalAdapter(HttpDigitalAdapterConfiguration configuration, DigitalTwin digitalTwinInstance) {
        super(configuration.getId(), configuration);
        this.digitalTwinInstance = digitalTwinInstance;
    }

    /**
     * Callback method invoked when the state of the Digital Twin is updated.
     *
     * @param newDigitalTwinState The updated Digital Twin state.
     * @param previousDigitalTwinState The previous Digital Twin state.
     * @param digitalTwinStateChangeList The list of changes in the Digital Twin state.
     */
    @Override
    protected void onStateUpdate(DigitalTwinState newDigitalTwinState, DigitalTwinState previousDigitalTwinState, ArrayList<DigitalTwinStateChange> digitalTwinStateChangeList) {

        // In newDigitalTwinState we have the new DT State
        logger.debug("New DT State: {} - Previous DT State: {}", newDigitalTwinState, previousDigitalTwinState);

        // Update DT State
        this.updatedDigitalTwinState = newDigitalTwinState;

        // Keep track of the previous DT State
        this.previousDigitalTwinState = previousDigitalTwinState;

        // Keep track of the list of changes on the DT State that triggered the variation
        this.latestDigitalTwinStateChangeList = digitalTwinStateChangeList;

    }

    /**
     * Callback method invoked when an event notification is received for the Digital Twin state.
     *
     * @param digitalTwinStateEventNotification The received event notification.
     */
    @Override
    protected void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) {
        logger.debug("HTTP Digital Adapter receive event: {}", digitalTwinStateEventNotification);
        this.eventNotificationList.add(digitalTwinStateEventNotification);
    }

    /**
     * Callback method invoked when the adapter starts.
     */
    @Override
    public void onAdapterStart() {
        this.server = Undertow.builder()
                .addHttpListener(getConfiguration().getPort(), getConfiguration().getHost())
                .setHandler(createDefaultRoutingHandler(this))
                .build();
        this.server.start();
        logger.info("HTTP Digital Adapter Started");
        this.notifyDigitalAdapterBound();
    }

    /**
     * Callback method invoked when the adapter stops.
     */
    @Override
    public void onAdapterStop() {
        this.server.stop();
    }

    /**
     * Callback method invoked when the Digital Twin is synchronized.
     *
     * @param currentDigitalTwinState The current state of the Digital Twin.
     */
    @Override
    public void onDigitalTwinSync(DigitalTwinState currentDigitalTwinState) {
        try {
            if(currentDigitalTwinState != null){

                //Update DT State
                this.updatedDigitalTwinState = currentDigitalTwinState;

                // Observer Existing Digital Twin Events Notifications
                currentDigitalTwinState.getEventList()
                        .map(events -> events.stream()
                                .map(DigitalTwinStateEvent::getKey)
                                .collect(Collectors.toList()))
                        .ifPresent(l -> {
                            try {
                                observeDigitalTwinEventsNotifications(l);
                            } catch (EventBusException e) {
                                e.printStackTrace();
                            }
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Callback method invoked when the Digital Twin is unsynchronized. This method is called when the
     * association between the HTTP Digital Adapter and the Digital Twin is terminated.
     *
     * @param digitalTwinState The last known state of the Digital Twin before unsynchronization.
     */
    @Override
    public void onDigitalTwinUnSync(DigitalTwinState digitalTwinState) {

    }

    /**
     * Callback method invoked when a new Digital Twin is created. This method is called when a new
     * Digital Twin instance is created and associated with the HTTP Digital Adapter.
     */
    @Override
    public void onDigitalTwinCreate() {

    }

    /**
     * Callback method invoked when the Digital Twin is started. This method is called when the
     * Digital Twin instance associated with the HTTP Digital Adapter is started.
     */
    @Override
    public void onDigitalTwinStart() {

    }

    /**
     * Callback method invoked when the Digital Twin is stopped. This method is called when the
     * Digital Twin instance associated with the HTTP Digital Adapter is stopped.
     */
    @Override
    public void onDigitalTwinStop() {

    }

    /**
     * Callback method invoked when the Digital Twin is destroyed. This method is called when the
     * Digital Twin instance associated with the HTTP Digital Adapter is destroyed.
     */
    @Override
    public void onDigitalTwinDestroy() {

    }

    /**
     * Retrieves the current state of the Digital Twin. This method is invoked when a request is made
     * to obtain the current state of the associated Digital Twin.
     *
     * @return An {@code Optional} containing the current Digital Twin state, or empty if the state
     * is not available or an error occurs during the retrieval.
     */
    @Override
    public Optional<DigitalTwinState> onStateGet() {
        try {

            if(this.updatedDigitalTwinState == null)
                return Optional.empty();

            return Optional.of(this.updatedDigitalTwinState);

        } catch (Exception e) {
            logger.error("Error loading DT State: {} ! Error: {}", updatedDigitalTwinState, e.toString());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the previous state of the Digital Twin. This method is invoked when a request is made
     * to obtain the previous state of the associated Digital Twin.
     *
     * @return An {@code Optional} containing the previous Digital Twin state, or empty if the state
     * is not available or an error occurs during the retrieval.
     */
    @Override
    public Optional<DigitalTwinState> onPreviousStateGet() {
        try {

            if(this.previousDigitalTwinState == null)
                return Optional.empty();

            return Optional.of(this.previousDigitalTwinState);

        } catch (Exception e) {
            logger.error("Error loading Previous DT State: {} ! Error: {}", previousDigitalTwinState, e.toString());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the list of state changes that occurred in the Digital Twin. This method is invoked
     * when a request is made to obtain the list of changes in the associated Digital Twin's state.
     *
     * @return An {@code Optional} containing the list of Digital Twin state changes, or empty if the
     * list is not available or an error occurs during the retrieval.
     */
    @Override
    public Optional<Collection<DigitalTwinStateChange>> onStateChangesListGet() {
        try {

            if(this.latestDigitalTwinStateChangeList == null)
                return Optional.empty();

            return Optional.of(this.latestDigitalTwinStateChangeList);

        } catch (Exception e) {
            logger.error("Error loading DT State Change List: {} ! Error: {}", latestDigitalTwinStateChangeList, e.toString());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the Digital Twin state property with the specified key. This method is invoked
     * when a request is made to obtain the value of a specific property in the Digital Twin state.
     *
     * @param propertyKey The key of the property to retrieve.
     * @return An {@code Optional} containing the Digital Twin state property, or empty if the property
     * is not available or an error occurs during the retrieval.
     */
    @Override
    public Optional<DigitalTwinStateProperty<?>> onPropertyGet(String propertyKey) {
        try {

            if(this.updatedDigitalTwinState == null)
                return Optional.empty();

            return this.updatedDigitalTwinState.getProperty(propertyKey);

        } catch (Exception e) {
           logger.error("Error loading property: {} ! Error: {}", propertyKey, e.toString());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the Digital Twin state action with the specified key. This method is invoked
     * when a request is made to obtain information about a specific action in the Digital Twin state.
     *
     * @param actionKey The key of the action to retrieve.
     * @return An {@code Optional} containing the Digital Twin state action, or empty if the action
     * is not available or an error occurs during the retrieval.
     */
    @Override
    public Optional<DigitalTwinStateAction> onActionGet(String actionKey) {
        try {

            if(this.updatedDigitalTwinState == null)
                return Optional.empty();

            return this.updatedDigitalTwinState.getAction(actionKey);

        } catch (Exception e) {
            logger.error("Error loading action: {} ! Error: {}", actionKey, e.toString());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the Digital Twin state event with the specified key. This method is invoked
     * when a request is made to obtain information about a specific event in the Digital Twin state.
     *
     * @param eventKey The key of the event to retrieve.
     * @return An {@code Optional} containing the Digital Twin state event, or empty if the event
     * is not available or an error occurs during the retrieval.
     */
    @Override
    public Optional<DigitalTwinStateEvent> onEventGet(String eventKey) {
        try {

            if(this.updatedDigitalTwinState == null)
                return Optional.empty();

            return this.updatedDigitalTwinState.getEvent(eventKey);

        } catch (Exception e) {
            logger.error("Error loading Event: {} ! Error: {}", eventKey, e.toString());
            return Optional.empty();
        }
    }

    /**
     * Retrieves the Digital Twin state relationship with the specified name. This method is invoked
     * when a request is made to obtain information about a specific relationship in the Digital Twin state.
     *
     * @param relationshipName The name of the relationship to retrieve.
     * @return An {@code Optional} containing the Digital Twin state relationship, or empty if the
     * relationship is not available or an error occurs during the retrieval.
     */
    @Override
    public Optional<DigitalTwinStateRelationship<?>> onRelationshipGet(String relationshipName) {
        try {

            if(this.updatedDigitalTwinState == null)
                return Optional.empty();

            return this.updatedDigitalTwinState.getRelationship(relationshipName);

        } catch (Exception e) {
            logger.error("Error loading Relationship: {} ! Error: {}", relationshipName, e.toString());
            return Optional.empty();
        }
    }

    /**
     * Retrieves a collection of Digital Twin state properties. This method is invoked
     * when a request is made to obtain all properties in the Digital Twin state.
     *
     * @return A collection of Digital Twin state properties, or an empty list if no properties
     * are available or an error occurs during the retrieval.
     */
    @Override
    public Collection<DigitalTwinStateProperty<?>> onPropertiesGet() {
        try {

            if(this.updatedDigitalTwinState != null && this.updatedDigitalTwinState.getPropertyList().isPresent())
                return this.updatedDigitalTwinState.getPropertyList().get();
            else
                return new ArrayList<>();

        } catch (Exception e) {
            logger.error("Error loading Properties ! Error: {}", e.toString());
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves a collection of Digital Twin state actions. This method is invoked
     * when a request is made to obtain all actions in the Digital Twin state.
     *
     * @return A collection of Digital Twin state actions, or an empty list if no actions
     * are available or an error occurs during the retrieval.
     */
    @Override
    public Collection<DigitalTwinStateAction> onActionsGet() {
        try {

            if(this.updatedDigitalTwinState != null && this.updatedDigitalTwinState.getActionList().isPresent())
                return this.updatedDigitalTwinState.getActionList().get();
            else
                return new ArrayList<>();

        } catch (Exception e) {
            logger.error("Error loading Actions ! Error: {}", e.toString());
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves a collection of Digital Twin state events. This method is invoked
     * when a request is made to obtain all events in the Digital Twin state.
     *
     * @return A collection of Digital Twin state events, or an empty list if no events
     * are available or an error occurs during the retrieval.
     */
    @Override
    public Collection<DigitalTwinStateEvent> onEventsGet() {
        try {

            if(this.updatedDigitalTwinState != null && this.updatedDigitalTwinState.getEventList().isPresent())
                return this.updatedDigitalTwinState.getEventList().get();
            else
                return new ArrayList<>();

        } catch (Exception e) {
            logger.error("Error loading Events ! Error: {}", e.toString());
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves a collection of Digital Twin state relationships. This method is invoked
     * when a request is made to obtain all relationships in the Digital Twin state.
     *
     * @return A collection of Digital Twin state relationships, or an empty list if no relationships
     * are available or an error occurs during the retrieval.
     */
    @Override
    public Collection<DigitalTwinStateRelationship<?>> onRelationshipsGet() {
        try {

            if(this.updatedDigitalTwinState != null && this.updatedDigitalTwinState.getRelationshipList().isPresent())
                return this.updatedDigitalTwinState.getRelationshipList().get();
            else
                return new ArrayList<>();

        } catch (Exception e) {
            logger.error("Error loading Relationships ! Error: {}", e.toString());
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves the value of a specific property in the Digital Twin state. This method is invoked
     * when a request is made to read the value of a property identified by the provided key.
     *
     * @param propertyKey The key of the property to be read.
     * @return An optional containing the property value if found, or an empty optional if the property
     * is not found or an error occurs during the retrieval.
     */
    @Override
    public Optional<String> onReadProperty(String propertyKey) {
        try {

            if(this.updatedDigitalTwinState != null && this.updatedDigitalTwinState.getProperty(propertyKey).isPresent()){
                return Optional.ofNullable(this.updatedDigitalTwinState.getProperty(propertyKey).get().getValue().toString());
            }
            else
                return Optional.empty();

        } catch (Exception e) {
            logger.error("Error loading property value key: {} ! Error: {}", propertyKey, e.toString());
            return Optional.empty();
        }
    }

    /**
     * Retrieves a list of event notifications in the Digital Twin state. This method is invoked
     * when a request is made to obtain a list of event notifications.
     *
     * @return A list of Digital Twin state event notifications.
     */
    @Override
    public List<DigitalTwinStateEventNotification<?>> onEventNotificationGet() {
        return this.eventNotificationList;
    }

    /**
     * Retrieves a list of instances for a specific relationship in the Digital Twin state. This method
     * is invoked when a request is made to obtain instances associated with a given relationship.
     *
     * @param relationshipName The name of the relationship for which to retrieve instances.
     * @return An optional containing the list of relationship instances if found, or an empty optional
     * if the relationship is not found or an error occurs during the retrieval.
     */
    @Override
    public Optional<List<DigitalTwinStateRelationshipInstance<?>>> onRelationshipInstancesGet(String relationshipName) {

        try {

            if(this.updatedDigitalTwinState != null && this.updatedDigitalTwinState.getRelationship(relationshipName).isPresent()){
                return Optional.ofNullable(this.updatedDigitalTwinState.getRelationship(relationshipName).get().getInstances());
            }
            else
                return Optional.empty();

        } catch (Exception e) {
            logger.error("Error loading relationship instances rel-name: {} ! Error: {}", relationshipName, e.toString());
            return Optional.empty();
        }
    }

    /**
     * Processes an action request in the Digital Twin state. This method is invoked when a request
     * is made to perform a specific action identified by the provided key.
     *
     * @param actionKey The key of the action to be performed.
     * @param bodyRequest The request body containing additional data for the action.
     * @return An HTTP status code indicating the result of the action request. 202 indicates success,
     * while 400 indicates failure or an error during processing.
     */
    @Override
    public Integer onActionRequest(String actionKey, String bodyRequest) {

        try {

            if(this.updatedDigitalTwinState == null || !this.updatedDigitalTwinState.containsAction(actionKey))
                return 400;

            publishDigitalActionWldtEvent(actionKey, bodyRequest);

        } catch (Exception e) {
            logger.error("Error sending Action to the DT ! ActionKey: {} - BodyRequest: {} ! Error: {}", actionKey, bodyRequest, e.toString());
            return 400;
        }
        return 202;
    }

    /**
     * Retrieves the Digital Twin instance associated with the Digital Adapter. This method is invoked
     * when a request is made to obtain the Digital Twin instance represented by the adapter.
     *
     * @return The Digital Twin instance associated with the adapter.
     */
    @Override
    public DigitalTwin onInstanceRequest() {
        return this.digitalTwinInstance;
    }

}
