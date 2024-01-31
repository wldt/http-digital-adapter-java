package it.wldt.adapter.http.digital;

import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.state.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface HttpDigitalAdapterRequestListener {

    Optional<DigitalTwinState> onStateGet();

    Optional<DigitalTwinState> onPreviousStateGet();

    Optional<Collection<DigitalTwinStateChange>> onStateChangesListGet();

    Optional<DigitalTwinStateProperty<?>> onPropertyGet(String propertyKey);

    Optional<DigitalTwinStateAction> onActionGet(String actionKey);

    Optional<DigitalTwinStateEvent> onEventGet(String eventKey);

    Optional<DigitalTwinStateRelationship<?>> onRelationshipGet(String relationshipName);

    Collection<DigitalTwinStateProperty<?>> onPropertiesGet();

    Collection<DigitalTwinStateAction> onActionsGet();

    Collection<DigitalTwinStateEvent> onEventsGet();

    Collection<DigitalTwinStateRelationship<?>> onRelationshipsGet();

    Optional<String> onReadProperty(String propertyKey);

    List<DigitalTwinStateEventNotification<?>> onEventNotificationGet();

    Optional<List<DigitalTwinStateRelationshipInstance<?>>> onRelationshipInstancesGet(String relationshipName);

    Integer onActionRequest(String actionKey, String bodyRequest);

    DigitalTwin onInstanceRequest();
}
