package it.wldt.adapter.http.digital.utils;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent;
import it.wldt.core.model.ShadowingFunction;
import it.wldt.core.state.DigitalTwinStateAction;
import it.wldt.core.state.DigitalTwinStateEvent;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.exception.EventBusException;
import it.wldt.exception.WldtDigitalTwinStateEventNotificationException;
import it.wldt.exception.WldtDigitalTwinStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.stream.Collectors;

public class  DefaultShadowingFunction extends ShadowingFunction {

    private static final Logger logger = LoggerFactory.getLogger(DefaultShadowingFunction.class);

    public DefaultShadowingFunction() {
        super("default-shadowing-function");
    }

    @Override
    protected void onCreate() {
        logger.debug("Shadowing - OnCreate");
    }

    @Override
    protected void onStart() {
        logger.debug("Shadowing - OnStart");
    }

    @Override
    protected void onStop() {
        logger.debug("Shadowing - OnStop");
    }

    @Override
    protected void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {
        logger.debug("Shadowing - onDtBound");
        try {

            startShadowing(adaptersPhysicalAssetDescriptionMap);

            // Observe all available properties
            this.observePhysicalAssetProperties(adaptersPhysicalAssetDescriptionMap.values()
                    .stream()
                    .flatMap(pad -> pad.getProperties().stream())
                    .collect(Collectors.toList()));

            //observes all the available events
            this.observePhysicalAssetEvents(adaptersPhysicalAssetDescriptionMap.values()
                    .stream()
                    .flatMap(pad -> pad.getEvents().stream())
                    .collect(Collectors.toList()));
            observeDigitalActionEvents();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startShadowing(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) throws WldtDigitalTwinStateException {

        // NEW in 0.3.0 -> Start DT State Change Transaction
        this.digitalTwinStateManager.startStateTransaction();

        adaptersPhysicalAssetDescriptionMap.forEach((id, pad) -> {
            pad.getProperties()
                    .forEach(p -> {
                        try {
                            this.digitalTwinStateManager.createProperty(new DigitalTwinStateProperty<>(p.getKey(), p.getInitialValue()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
            pad.getActions().forEach(a -> {
                try {
                    this.digitalTwinStateManager.enableAction(new DigitalTwinStateAction(a.getKey(), a.getType(), a.getContentType()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            pad.getEvents().forEach(e -> {
                try {
                    this.digitalTwinStateManager.registerEvent(new DigitalTwinStateEvent(e.getKey(), e.getType()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        });

        // NEW in 0.3.0 -> Commit DT State Change Transaction to apply the changes on the DT State and notify about the change
        this.digitalTwinStateManager.commitStateTransaction();

        notifyShadowingSync();
    }

    @Override
    protected void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap, String errorMessage) {
        logger.debug("Shadowing - onDTUnBound - error: {} ", errorMessage);
    }

    @Override
    protected void onPhysicalAdapterBidingUpdate(String adapterId, PhysicalAssetDescription adapterPhysicalAssetDescription) {
        logger.info("Shadowing - onPABindingUpdate - updated Adapter: {}, new PAD: {}", adapterId, adapterPhysicalAssetDescription);
    }

    @Override
    protected void onPhysicalAssetPropertyVariation(PhysicalAssetPropertyWldtEvent<?> physicalPropertyEventMessage) {
        logger.info("Shadowing - onPAPropertyVariation - property event: {} ", physicalPropertyEventMessage);
        //Update Digital Twin Status
        try {

            //Update Digital Twin State
            //NEW from 0.3.0 -> Start State Transaction
            this.digitalTwinStateManager.startStateTransaction();

            // Update Property Value
            this.digitalTwinStateManager.updatePropertyValue(
                    new DigitalTwinStateProperty<>(
                            physicalPropertyEventMessage.getPhysicalPropertyId(),
                            physicalPropertyEventMessage.getBody()));

            //NEW from 0.3.0 -> Commit State Transaction
            this.digitalTwinStateManager.commitStateTransaction();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPhysicalAssetEventNotification(PhysicalAssetEventWldtEvent<?> physicalAssetEventWldtEvent) {
        logger.info("Shadowing - onPAEventNotification - received Event:{}", physicalAssetEventWldtEvent);
        try {
            this.digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(
                    physicalAssetEventWldtEvent.getPhysicalEventKey(),
                    (String) physicalAssetEventWldtEvent.getBody(),
                    System.currentTimeMillis()));
        } catch (WldtDigitalTwinStateEventNotificationException | EventBusException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPhysicalAssetRelationshipEstablished(PhysicalAssetRelationshipInstanceCreatedWldtEvent<?> physicalAssetRelationshipWldtEvent) {

    }

    @Override
    protected void onPhysicalAssetRelationshipDeleted(PhysicalAssetRelationshipInstanceDeletedWldtEvent<?> physicalAssetRelationshipWldtEvent) {

    }

    @Override
    protected void onDigitalActionEvent(DigitalActionWldtEvent<?> digitalActionWldtEvent) {
        try {
            publishPhysicalAssetActionWldtEvent(digitalActionWldtEvent.getActionKey(), digitalActionWldtEvent.getBody());
        } catch (EventBusException e) {
            e.printStackTrace();
        }
    }
}
