package it.wldt.adapter.http.digital;

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

public class HttpDigitalAdapterHandlersFactory {

    private final static String JSON_CONTENT_TYPE = "application/json";

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

    public static <T> HttpHandler createGetComponentsListHandler(Supplier<T> componentsSupplier){
        return exchange -> {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, JSON_CONTENT_TYPE);
            exchange.getResponseSender().send(getGson().toJson(componentsSupplier.get()));
        };
    }

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

    public static HttpHandler createGetDigitalTwinStateHandler(Supplier<Collection<DigitalTwinStateProperty<?>>> digitalTwinStatePropertiesProducer,
                                                               Supplier<Collection<DigitalTwinStateAction>> digitalTwinStateActionsProducer,
                                                               Supplier<Collection<DigitalTwinStateEvent>> digitalTwinStateEventsProducer,
                                                               Supplier<Collection<DigitalTwinStateRelationship<?>>> digitalTwinStateRelationshipsProducer){
        return exchange -> {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, JSON_CONTENT_TYPE);
            final Gson gson = new Gson();
            final JsonObject responseObj = new JsonObject();
            responseObj.add("properties", gson.toJsonTree(digitalTwinStatePropertiesProducer.get()));
            responseObj.add("actions", gson.toJsonTree(digitalTwinStateActionsProducer.get()));
            responseObj.add("events", gson.toJsonTree(digitalTwinStateEventsProducer.get()));
            responseObj.add("relationships", getGson().toJsonTree(digitalTwinStateRelationshipsProducer.get()));
            exchange.getResponseSender().send(gson.toJson(responseObj));
        };
    }

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

    private static Gson getGson(){
        return new GsonBuilder().registerTypeAdapter(Map.class, getRelationshipInstancesSerializer()).create();
    }

    private static JsonSerializer<Map<String, DigitalTwinStateRelationshipInstance<?>>> getRelationshipInstancesSerializer(){
        return (stringDigitalTwinStateRelationshipInstanceMap, type, jsonSerializationContext) ->
                new Gson().toJsonTree(new LinkedList<>(stringDigitalTwinStateRelationshipInstanceMap.values()), LinkedList.class);
    }
}
