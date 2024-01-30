package it.wldt.adapter.http.digital;

import io.undertow.Undertow;
import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.state.*;
import it.wldt.exception.EventBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.stream.Collectors;

import static it.wldt.adapter.http.digital.HttpDigitalAdapterHandlersFactory.createDefaultRoutingHandler;

public class HttpDigitalAdapter extends DigitalAdapter<HttpDigitalAdapterConfiguration> implements HttpDigitalAdapterRequestListener {

    private final static Logger logger = LoggerFactory.getLogger(HttpDigitalAdapter.class);

    // Reference to the Digital Twin Instance
    private final DigitalTwin digitalTwinInstance;

    private DigitalTwinState updatedDigitalTwinState = null;

    private DigitalTwinState previousDigitalTwinState = null;

    private ArrayList<DigitalTwinStateChange> latestDigitalTwinStateChangeList = null;


    private final List<DigitalTwinStateEventNotification<?>> eventNotificationList = new LinkedList<>();

    private Undertow server;

    public HttpDigitalAdapter(HttpDigitalAdapterConfiguration configuration, DigitalTwin digitalTwinInstance) {
        super(configuration.getId(), configuration);
        this.digitalTwinInstance = digitalTwinInstance;
    }

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

    @Override
    protected void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) {
        logger.debug("HTTP Digital Adapter receive event: {}", digitalTwinStateEventNotification);
        this.eventNotificationList.add(digitalTwinStateEventNotification);
    }

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

    @Override
    public void onAdapterStop() {
        this.server.stop();
    }

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

    private <T> void handleComponent(String componentKey,
                                     T component,
                                     Map<String, T> componentMap,
                                     List<String> componentFilter){
        if (componentFilter.isEmpty() || componentFilter.contains(componentKey)) {
            componentMap.put(componentKey, component);
        }
    }

    @Override
    public void onDigitalTwinUnSync(DigitalTwinState digitalTwinState) {

    }

    @Override
    public void onDigitalTwinCreate() {

    }

    @Override
    public void onDigitalTwinStart() {

    }

    @Override
    public void onDigitalTwinStop() {

    }

    @Override
    public void onDigitalTwinDestroy() {

    }

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

    @Override
    public List<DigitalTwinStateEventNotification<?>> onEventNotificationGet() {
        return this.eventNotificationList;
    }

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

    @Override
    public DigitalTwin onInstanceRequest() {
        return this.digitalTwinInstance;
    }

}
