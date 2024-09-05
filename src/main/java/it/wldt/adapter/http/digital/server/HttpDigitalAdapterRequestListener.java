package it.wldt.adapter.http.digital.server;

import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.state.*;
import it.wldt.storage.model.StorageStats;
import it.wldt.storage.query.QueryRequest;
import it.wldt.storage.query.QueryResult;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Interface defining the contract for handling various requests in an HTTP Digital Adapter.
 * Implement this interface to provide custom behavior for handling state, properties, actions,
 * events, relationships, and other interactions with a digital twin through the HTTP Digital Adapter.
 *
 *
 * @author Marco Picone, Ph.D. - picone.m@gmail.com, Marta Spadoni University of Bologna
 */
public interface HttpDigitalAdapterRequestListener {

    /**
     * Handles the request to retrieve the current state of the digital twin.
     *
     * @return An {@code Optional} containing the current state, or empty if not available.
     */
    Optional<DigitalTwinState> onStateGet();

    /**
     * Handles the request to retrieve the previous state of the digital twin.
     *
     * @return An {@code Optional} containing the previous state, or empty if not available.
     */
    Optional<DigitalTwinState> onPreviousStateGet();

    /**
     * Handles the request to retrieve a list of state changes of the digital twin.
     *
     * @return An {@code Optional} containing a collection of state changes, or empty if not available.
     */
    Optional<Collection<DigitalTwinStateChange>> onStateChangesListGet();

    /**
     * Handles the request to retrieve a specific property of the digital twin.
     *
     * @param propertyKey The key of the property to retrieve.
     * @return An {@code Optional} containing the requested property, or empty if not available.
     */
    Optional<DigitalTwinStateProperty<?>> onPropertyGet(String propertyKey);

    /**
     * Handles the request to retrieve a specific action of the digital twin.
     *
     * @param actionKey The key of the action to retrieve.
     * @return An {@code Optional} containing the requested action, or empty if not available.
     */
    Optional<DigitalTwinStateAction> onActionGet(String actionKey);

    /**
     * Handles the request to retrieve a specific event of the digital twin.
     *
     * @param eventKey The key of the event to retrieve.
     * @return An {@code Optional} containing the requested event, or empty if not available.
     */
    Optional<DigitalTwinStateEvent> onEventGet(String eventKey);

    /**
     * Handles the request to retrieve a specific relationship of the digital twin.
     *
     * @param relationshipName The name of the relationship to retrieve.
     * @return An {@code Optional} containing the requested relationship, or empty if not available.
     */
    Optional<DigitalTwinStateRelationship<?>> onRelationshipGet(String relationshipName);

    /**
     * Handles the request to retrieve all properties of the digital twin.
     *
     * @return A collection of properties of the digital twin.
     */
    Collection<DigitalTwinStateProperty<?>> onPropertiesGet();

    /**
     * Handles the request to retrieve all actions of the digital twin.
     *
     * @return A collection of actions of the digital twin.
     */
    Collection<DigitalTwinStateAction> onActionsGet();

    /**
     * Handles the request to retrieve all events of the digital twin.
     *
     * @return A collection of events of the digital twin.
     */
    Collection<DigitalTwinStateEvent> onEventsGet();

    /**
     * Handles the request to retrieve all relationships of the digital twin.
     *
     * @return A collection of relationships of the digital twin.
     */
    Collection<DigitalTwinStateRelationship<?>> onRelationshipsGet();

    /**
     * Handles the request to read the value of a specific property of the digital twin.
     *
     * @param propertyKey The key of the property to read.
     * @return An {@code Optional} containing the value of the property, or empty if not available.
     */
    Optional<String> onReadProperty(String propertyKey);

    /**
     * Handles the request to retrieve notifications for events of the digital twin.
     *
     * @return A list of event notifications for the digital twin.
     */
    List<DigitalTwinStateEventNotification<?>> onEventNotificationGet();

    /**
     * Handles the request to retrieve instances of a specific relationship of the digital twin.
     *
     * @param relationshipName The name of the relationship.
     * @return An {@code Optional} containing a list of relationship instances, or empty if not available.
     */
    Optional<List<DigitalTwinStateRelationshipInstance<?>>> onRelationshipInstancesGet(String relationshipName);

    /**
     * Handles the request to execute a specific action on the digital twin.
     *
     * @param actionKey    The key of the action to execute.
     * @param bodyRequest  The request body containing any required parameters.
     * @return The result of the action request, typically an integer status code.
     */
    Integer onActionRequest(String actionKey, String bodyRequest);

    /**
     * Handles the request to retrieve the overall digital twin instance.
     *
     * @return The digital twin instance.
     */
    DigitalTwin onInstanceRequest();

    /**
     * Handles the request to retrieve the Digital Twin Storage Information
     */
    StorageStats onStorageInfoRequest();

    /**
     * Handles the request to execute Storage Query.
     * @param queryRequest The query request to execute.
     * @return The result of the query request.
     */
    QueryResult<?> onQueryRequest(QueryRequest queryRequest);

}
