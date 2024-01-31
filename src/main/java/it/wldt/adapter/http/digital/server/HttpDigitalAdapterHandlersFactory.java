package it.wldt.adapter.http.digital.server;

import com.google.gson.*;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.error.SimpleErrorPageHandler;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.state.*;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Factory class for creating HTTP handlers to handle various requests in an HTTP Digital Adapter.
 * This class provides methods to create default routing handlers and specific handlers for actions,
 * properties, events, relationships, and the digital twin instance.
 *
 * @author Marco Picone, Ph.D. - picone.m@gmail.com, Marta Spadoni University of Bologna
 */
public class HttpDigitalAdapterHandlersFactory {

    private final static String JSON_CONTENT_TYPE = "application/json";

    /**
     * Creates a default routing handler for the HTTP Digital Adapter based on the provided request listener.
     *
     * @param httpDigitalAdapterRequestListener The request listener implementing the HTTP Digital Adapter contract.
     * @return The default routing handler.
     */
    public static HttpHandler createDefaultRoutingHandler(HttpDigitalAdapterRequestListener httpDigitalAdapterRequestListener){
        return new RoutingHandler()
                .add(Methods.GET, "/instance", createGetDigitalTwinInstanceHandler(httpDigitalAdapterRequestListener::onInstanceRequest))
                .add(Methods.GET, "/state", createGetDigitalTwinStateHandler(httpDigitalAdapterRequestListener::onStateGet))
                .add(Methods.GET, "/state/previous", createGetDigitalTwinStateHandler(httpDigitalAdapterRequestListener::onPreviousStateGet))
                .add(Methods.GET, "/state/changes", createGetDigitalTwinStateChangeListHandler(httpDigitalAdapterRequestListener::onStateChangesListGet))
                .add(Methods.GET,"/state/properties", createGetComponentsListHandler(httpDigitalAdapterRequestListener::onPropertiesGet))
                .add(Methods.GET, "/state/properties/{key}", createGetComponentHandler(httpDigitalAdapterRequestListener::onPropertyGet))
                .add(Methods.GET, "/state/properties/{key}/value", createReadPropertyValueHandler(httpDigitalAdapterRequestListener::onReadProperty))
                .add(Methods.GET,"/state/actions", createGetComponentsListHandler(httpDigitalAdapterRequestListener::onActionsGet))
                .add(Methods.GET, "/state/actions/{key}", createGetComponentHandler(httpDigitalAdapterRequestListener::onActionGet))
                .add(Methods.POST, "/state/actions/{key}", createInvokeActionHandler(httpDigitalAdapterRequestListener::onActionRequest))
                .add(Methods.GET,"/state/events", createGetComponentsListHandler(httpDigitalAdapterRequestListener::onEventsGet))
                .add(Methods.GET, "/state/events/{key}", createGetComponentHandler(httpDigitalAdapterRequestListener::onEventGet))
                .add(Methods.GET,"/state/events/notifications", createGetComponentsListHandler(httpDigitalAdapterRequestListener::onEventNotificationGet))
                .add(Methods.GET, "/state/relationships", createGetComponentsListHandler(httpDigitalAdapterRequestListener::onRelationshipsGet))
                .add(Methods.GET, "/state/relationships/{key}", createGetComponentHandler(httpDigitalAdapterRequestListener::onRelationshipGet))
                .add(Methods.GET, "/state/relationships/{key}/instances", createGetComponentHandler(httpDigitalAdapterRequestListener::onRelationshipInstancesGet))
                .setFallbackHandler(new SimpleErrorPageHandler());
    }

    /**
     * Creates an HTTP handler for invoking a specific action on the digital twin.
     *
     * @param actionFunction The function to handle the action invocation.
     * @return The action invocation handler.
     */
    private static HttpHandler createInvokeActionHandler(BiFunction<String, String, Integer> actionFunction) {
        return exchange -> {
            String pathKey = exchange.getQueryParameters().get("key").getFirst();
            exchange.getRequestReceiver().receiveFullBytes((e, requestBody) -> {
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, JSON_CONTENT_TYPE);
                exchange.setStatusCode(actionFunction.apply(pathKey, new String(requestBody)));
//                exchange.endExchange();
            });
        };
    }

    /**
     * Creates an HTTP handler for retrieving a list of components (properties, actions, events, relationships).
     *
     * @param componentsSupplier The supplier for obtaining the list of components.
     * @param <T> The type of the components.
     * @return The handler for retrieving a list of components.
     */
    public static <T> HttpHandler createGetComponentsListHandler(Supplier<T> componentsSupplier){
        return exchange -> {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, JSON_CONTENT_TYPE);
            exchange.getResponseSender().send(getGson().toJson(componentsSupplier.get()));
        };
    }

    /**
     * Creates an HTTP handler for retrieving a specific component (property, action, event, relationship).
     *
     * @param digitalTwinStateComponentProducer The function to produce the specified component.
     * @param <T> The type of the component.
     * @return The handler for retrieving a specific component.
     */
    public static <T> HttpHandler createGetComponentHandler(Function<String, Optional<T>> digitalTwinStateComponentProducer){
        return exchange -> {
            String pathKey = exchange.getQueryParameters().get("key").getFirst();
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, JSON_CONTENT_TYPE);
            Optional<T> component = digitalTwinStateComponentProducer.apply(pathKey);
            String response = component.isPresent() ?
                    getGson().toJson(component.get())
                    : "{error: \"not found\"}";
            exchange.getResponseSender().send(response);
        };
    }

    /**
     * Creates an HTTP handler for retrieving the digital twin instance.
     *
     * @param instanceSupplier The supplier for obtaining the digital twin instance.
     * @return The handler for retrieving the digital twin instance.
     */
    public static HttpHandler createGetDigitalTwinInstanceHandler(Supplier<DigitalTwin> instanceSupplier){
        return exchange -> {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, JSON_CONTENT_TYPE);
            final DigitalTwin instance = instanceSupplier.get();
            final Gson gson = new Gson();
            final JsonObject responseObj = new JsonObject();
            responseObj.addProperty("id", instance.getDigitalTwinId());
            responseObj.add("digitalizedPhysicalAssets", gson.toJsonTree(instance.getDigitalizedPhysicalAssets()));
            responseObj.add("physicalAdapters", gson.toJsonTree(instance.getPhysicalAdapterIds()));
            responseObj.add("digitalAdapters", gson.toJsonTree(instance.getDigitalAdapterIds()));
            exchange.getResponseSender().send(gson.toJson(responseObj));
        };
    }

    /**
     * Creates an HTTP handler for retrieving the digital twin state.
     *
     * @param dtStateSupplier The supplier for obtaining the digital twin state.
     * @return The handler for retrieving the digital twin state.
     */
    public static HttpHandler createGetDigitalTwinStateHandler(Supplier<Optional<DigitalTwinState>> dtStateSupplier){
        return exchange -> {

            // Check if the supplier and the DT State are available otherwise sends back a 500 Internal Server Error
            if(dtStateSupplier == null || !dtStateSupplier.get().isPresent()){
                exchange.setStatusCode(500);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                exchange.getResponseSender().send("DigitalTwinState Supplier = Null ! Internal Server Error");
            }
            else {

                DigitalTwinState digitalTwinState = dtStateSupplier.get().get();

                Collection<DigitalTwinStateProperty<?>> digitalTwinStatePropertiesList = (digitalTwinState.getPropertyList().isPresent()) ? digitalTwinState.getPropertyList().get() : new ArrayList<>();
                Collection<DigitalTwinStateAction> digitalTwinStateActionsList = (digitalTwinState.getActionList().isPresent()) ? digitalTwinState.getActionList().get() : new ArrayList<>();
                Collection<DigitalTwinStateEvent> digitalTwinStateEventsList = (digitalTwinState.getEventList().isPresent()) ? digitalTwinState.getEventList().get() : new ArrayList<>();
                Collection<DigitalTwinStateRelationship<?>> digitalTwinStateRelationships = (digitalTwinState.getRelationshipList().isPresent()) ? digitalTwinState.getRelationshipList().get() : new ArrayList<>();

                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, JSON_CONTENT_TYPE);

                final Gson gson = new Gson();
                final JsonObject responseObj = new JsonObject();

                // Instant Date String
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
                String formattedDateTime = formatter.format(digitalTwinState.getEvaluationInstant());

                responseObj.addProperty("evaluation_instant_epoch_ms", digitalTwinState.getEvaluationInstant().toEpochMilli());
                responseObj.addProperty("evaluation_instant_date", formattedDateTime);
                responseObj.add("properties", gson.toJsonTree(digitalTwinStatePropertiesList));
                responseObj.add("actions", gson.toJsonTree(digitalTwinStateActionsList));
                responseObj.add("events", gson.toJsonTree(digitalTwinStateEventsList));
                responseObj.add("relationships", getGson().toJsonTree(digitalTwinStateRelationships));

                exchange.getResponseSender().send(gson.toJson(responseObj));
            }
        };
    }

    /**
     * Creates an HTTP handler for retrieving the digital twin state change list.
     *
     * @param dtStateChangeListSupplier The supplier for obtaining the digital twin state change list.
     * @return The handler for retrieving the digital twin state change list.
     */
    private static HttpHandler createGetDigitalTwinStateChangeListHandler(Supplier<Optional<Collection<DigitalTwinStateChange>>> dtStateChangeListSupplier) {

        return exchange -> {

            try {

                // Check if the supplier and the DT State are available otherwise sends back a 500 Internal Server Error
                if(dtStateChangeListSupplier == null || !dtStateChangeListSupplier.get().isPresent()){
                    exchange.setStatusCode(500);
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("DigitalTwinState Supplier = Null ! Internal Server Error");
                }
                else {

                    Collection<DigitalTwinStateChange> dtStateChangeList = dtStateChangeListSupplier.get().get();

                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, JSON_CONTENT_TYPE);

                    final Gson gson = new Gson();
                    exchange.getResponseSender().send(gson.toJson(dtStateChangeList));
                }

            }catch (Exception e){
                exchange.setStatusCode(500);
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                exchange.getResponseSender().send("Internal Server Error Building DT State Change List");
            }
        };

    }

    /**
     * Creates an HTTP handler for reading the value of a specific property.
     *
     * @param propertyValueProducer The function to produce the value of the specified property.
     * @return The handler for reading the value of a property.
     */
    public static HttpHandler createReadPropertyValueHandler(Function<String, Optional<String>> propertyValueProducer){
        return exchange -> {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            String pathKey = exchange.getQueryParameters().get("key").getFirst();
            Optional<String> propertyValue = propertyValueProducer.apply(pathKey);
            if(propertyValue.isPresent())
              exchange.getResponseSender().send(propertyValue.get());
            else
              exchange.getResponseSender().send("Property not is readable");
        };
    }

    /**
     * Returns a Gson instance configured with a custom serializer for handling relationship instances.
     *
     * @return A Gson instance.
     */
    private static Gson getGson(){
        return new GsonBuilder().registerTypeAdapter(Map.class, getRelationshipInstancesSerializer()).create();
    }

    /**
     * Returns a custom serializer for handling relationship instances in Gson.
     *
     * @return A custom Gson serializer for relationship instances.
     */
    private static JsonSerializer<Map<String, DigitalTwinStateRelationshipInstance<?>>> getRelationshipInstancesSerializer(){
        return (stringDigitalTwinStateRelationshipInstanceMap, type, jsonSerializationContext) ->
                new Gson().toJsonTree(new LinkedList<>(stringDigitalTwinStateRelationshipInstanceMap.values()), LinkedList.class);
    }
}
