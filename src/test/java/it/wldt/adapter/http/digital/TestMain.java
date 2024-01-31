package it.wldt.adapter.http.digital;

import it.wldt.adapter.http.digital.adapter.HttpDigitalAdapter;
import it.wldt.adapter.http.digital.adapter.HttpDigitalAdapterConfiguration;
import it.wldt.adapter.http.digital.utils.DefaultShadowingFunction;
import it.wldt.adapter.http.digital.utils.DummyPhysicalAdapter;
import it.wldt.adapter.http.digital.utils.DummyPhysicalAdapterConfiguration;
import it.wldt.adapter.physical.PhysicalAdapter;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetProperty;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.engine.DigitalTwinEngine;
import it.wldt.exception.EventBusException;
import it.wldt.exception.PhysicalAdapterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TestMain class serves as the main entry point for testing Digital Twin functionality.
 * It creates a Digital Twin instance, assigns physical adapters, and sets up an HTTP Digital Adapter.
 * The Digital Twin Engine is used to manage and execute the created Digital Twin instance.
 *
 * @author Marco Picone, Ph.D. - picone.m@gmail.com, Marta Spadoni University of Bologna
 */
public class TestMain {

    /**
     * The main method where the execution begins.
     *
     * @param args Command-line arguments (not used in this context).
     */
    public static void main(String[] args)  {

        try {

            // Create a Digital Twin instance named "http-digital-twin" with a DefaultShadowingFunction
            DigitalTwin digitalTwin = new DigitalTwin("http-digital-twin", new DefaultShadowingFunction());

            // Create and assign a Demo Physical Adapter generating random physical variation to test the MQTT Digital Adapter
            digitalTwin.addPhysicalAdapter(
                    new DummyPhysicalAdapter("test-pa",
                            new DummyPhysicalAdapterConfiguration(),
                            true));

            // Add additional "empty" Physical Adapter to test the Instance Description of the DT with multiple adapters
            digitalTwin.addPhysicalAdapter(createPhysicalAdapter("test-pa-2", Arrays.asList("temperature", "volume")));
            digitalTwin.addPhysicalAdapter(createPhysicalAdapter("test-pa-3", Arrays.asList("intensity", "color")));

            // Create Http Digital Adapter Configuration
            HttpDigitalAdapterConfiguration config = new HttpDigitalAdapterConfiguration("test-http-da", "localhost", 3000);

            // Create the Digital Adapter Http with its configuration and the reference of the DT instance to describe its structure
            HttpDigitalAdapter httpDigitalAdapter = new HttpDigitalAdapter(config, digitalTwin);

            // Add the HTTP Digital Adapter to the DT
            digitalTwin.addDigitalAdapter(httpDigitalAdapter);

            // Create the Digital Twin Engine to execute the created DT instance
            DigitalTwinEngine digitalTwinEngine = new DigitalTwinEngine();

            // Add the Digital Twin to the Engine
            digitalTwinEngine.addDigitalTwin(digitalTwin);

            // Start all the DTs registered on the engine
            digitalTwinEngine.startAll();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Creates a simple Physical Adapter with the provided ID and property keys.
     *
     * @param id             The ID of the Physical Adapter.
     * @param propertiesKeys The list of property keys for the Physical Adapter.
     * @return A new Physical Adapter instance.
     */
    private static PhysicalAdapter createPhysicalAdapter(String id, List<String> propertiesKeys){

        return new PhysicalAdapter(id) {
            @Override
            public void onIncomingPhysicalAction(PhysicalAssetActionWldtEvent<?> physicalActionEvent) {

            }

            @Override
            public void onAdapterStart() {
                try {
                    List<PhysicalAssetProperty<?>> properties = propertiesKeys.stream()
                            .map(key -> new PhysicalAssetProperty<>(key, 0))
                            .collect(Collectors.toList());
                    notifyPhysicalAdapterBound(new PhysicalAssetDescription(new ArrayList<>(), properties, new ArrayList<>()));
                } catch (PhysicalAdapterException | EventBusException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAdapterStop() {

            }
        };
    }
}
