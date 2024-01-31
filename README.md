# HTTP Digital Adapter Java

The `HttpDigitalAdapter` is a powerful component designed to facilitate the integration of Digital Twins into HTTP-based systems. 
It serves as a bridge between a Digital Twin and HTTP-based applications, allowing developers to easily expose and interact with 
Digital Twin data and functionalities over HTTP.

Key Features:

- **HTTP Integration:** Seamlessly integrates Digital Twins into HTTP environments, enabling communication with web applications and services.
- **Dynamic Configuration:** Offers a flexible configuration mechanism through the HttpDigitalAdapterConfiguration, allowing developers to customize the adapter's behavior based on specific requirements.
- **State Monitoring:** Monitors changes in the Digital Twin state and provides HTTP endpoints to query the state of the Digital Twin (properties, events, actions and relationships).
- **Event Notifications:** Allows developers to retrieve event notifications triggered by changes in the Digital Twin state.

<div align="center">
  <img class="center" src="images/adapter_schema.jpg" width="100%">
</div>

A complete example is provided in the `test` folder with a complete DT Creation in the `TestMain` class together with a demo DT with and emulated Physical Adapter and the HTTP Digital Adapter.

## WLDT-Core Version Compatibility

The correct mapping and compatibility between versions is reported in the following table

| **http-digital-adapter** |   wldt-core 0.2.1  |   wldt-core 0.3.0  |
|:------------------------:|:------------------:|:------------------:|
|          0.1.1           |         :x:        | :white_check_mark: |

## Installation

To use HttpDigitalAdapter in your Java project, you can include it as a dependency using Maven or Gradle.

### Maven

```xml
<dependency>
    <groupId>io.github.wldt</groupId>
    <artifactId>http-digital-adapter</artifactId>
    <version>0.1.1</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.github.wldt:http-digital-adapter:0.1.1'
```

## Class Structure & Functionalities

### HttpDigitalAdapterConfiguration

The `HttpDigitalAdapterConfiguration` is a crucial part of the HttpDigitalAdapter, providing the necessary settings to 
tailor the adapter's behavior to meet specific needs.

Represents the configuration for an HTTP Digital Adapter, specifying the host, port,
and filters for properties, actions, events, and relationships.

The filters are used to selectively include or exclude specific properties, actions,
events, and relationships when interacting with the HTTP Digital Adapter.
Filters are meant to be white list filters, if they are empty, it means that ALL fields are considered

This class provides methods to add filters for each type and getters to retrieve the configured values.

Key functionalities and exposed capabilities: 

- **Basic Configuration**
  - **Adapter ID:** A unique identifier for the HttpDigitalAdapter instance.
  - **Host:** The hostname or IP address on which the adapter will listen for incoming HTTP requests.
  - **Port:** The port number on which the adapter will listen for incoming HTTP requests.
- **Filters (Optional)**
  - `addPropertyFilter(String propertyKey)`: Adds a single property key to the property filter.
  - `addPropertiesFilter(Collection<String> propertiesKey)`: Adds a collection of property keys to the property filter.
  - `addActionFilter(String actionKey)`: Adds a single action key to the action filter.
  - `addActionsFilter(Collection<String> actionsKey)`: Adds a collection of action keys to the action filter.
  - `addEventFilter(String eventKey)`: Adds a single event key to the event filter.
  - `addEventsFilter(Collection<String> eventsKey)`: Adds a collection of event keys to the event filter.
  - `addRelationshipFilter(String relationshipName)`: Adds a single relationship name to the relationship filter.
  - `addRelationshipsFilter(Collection<String> relationshipNames)`: Adds a collection of relationship names to the relationship filter.
  - Configured Filter can be accessed using: 
    - `getPropertyFilter()`
    - `getActionFilter()`
    - `getEventFilter()`
    - `getRelationshipFilter()`

A basic example without any filter that accesses and uses the entire DT State is: 

```java
HttpDigitalAdapterConfiguration config = new HttpDigitalAdapterConfiguration("my-http-adapter", "localhost", 8080);
```

An example of using filter to select specific field of interest can be structured ad follows:

```java
HttpDigitalAdapterConfiguration config = new HttpDigitalAdapterConfiguration("my-http-adapter", "localhost", 8080);

// Add property filter
config.addPropertyFilter("temperature");
config.addPropertiesFilter(Arrays.asList("humidity", "pressure"));

// Add action filter
config.addActionFilter("start");
config.addActionsFilter(Arrays.asList("stop", "reset"));

// Add event filter
config.addEventFilter("temperatureChange");
config.addEventsFilter(Collections.singletonList("pressureChange"));

// Add relationship filter
config.addRelationshipFilter("connectedTo");
config.addRelationshipsFilter(Arrays.asList("parentOf", "siblingOf"));

// Retrieve and display filters
List<String> propertyFilter = config.getPropertyFilter();
List<String> actionFilter = config.getActionFilter();
List<String> eventFilter = config.getEventFilter();
List<String> relationshipFilter = config.getRelationshipFilter();

System.out.println("Property Filter: " + propertyFilter);
System.out.println("Action Filter: " + actionFilter);
System.out.println("Event Filter: " + eventFilter);
System.out.println("Relationship Filter: " + relationshipFilter);
```

### HttpDigitalAdapter

The `HttpDigitalAdapter` itself is the core component responsible for handling HTTP requests and 
interacting with the underlying Digital Twin. It extends the capabilities of the base DigitalAdapter 
to specifically cater to HTTP-based scenarios.

Key Functionalities:

- RESTful Endpoints: Provides RESTful endpoints for reading properties, invoking actions, querying events, and managing relationships.
- State Updates: Automatically reflects changes in the Digital Twin state to the HTTP endpoints, ensuring real-time information.
- Event Handling: Listens for Digital Twin events and provides events notifications to HTTP clients.

Here's a basic example illustrating how to use MqttDigitalAdapter:

#### Getting Started:

Create HttpDigitalAdapterConfiguration:

```java
HttpDigitalAdapterConfiguration config = new HttpDigitalAdapterConfiguration("my-http-adapter", "localhost", 8080);
```
Instantiate HttpDigitalAdapter:

```java
DigitalTwin digitalTwin = new DigitalTwin("my-digital-twin", new DefaultShadowingFunction());
HttpDigitalAdapter httpDigitalAdapter = new HttpDigitalAdapter(config, digitalTwin);

// Add a Physical Adapter to the DT
[...]
```

Add HttpDigitalAdapter to DigitalTwin:

```java
digitalTwin.addDigitalAdapter(httpDigitalAdapter);
```

Start the Digital Twin Engine:

```java
DigitalTwinEngine digitalTwinEngine = new DigitalTwinEngine();
digitalTwinEngine.addDigitalTwin(digitalTwin);
digitalTwinEngine.startAll();
```

## HTTP RESTful API 

This section of the documentation provides detailed information about the RESTful API exposed by the WLDT - HTTP Digital Adapter. 
The API allows you to interact with the Digital Twin (DT) instance, retrieve its state, read properties, actions, event and relationships description, 
and trigger actions.

Available endpoints with the associated methods are:

- `GET` `/instance`: Retrieves information about the Digital Twin instance.
- `GET` `/state`: Retrieves the current state of the Digital Twin.
- `GET` `/state/changes`: Retrieves the list of state changes in the Digital Twin.
- `GET` `/state/previous`: Retrieves the previous state of the Digital Twin.
- `GET` `/state/properties`: Retrieves the list of properties in the Digital Twin state.
- `GET` `/properties/{propertyKey}`: Retrieves the value of a specific property (e.g., /properties/color) from the Digital Twin state.
- `GET` `/state/events`: Retrieves the list of events in the Digital Twin state.
- `GET` `/state/actions`: Retrieves the list of actions in the Digital Twin state.
- `POST` `/state/actions/{actionKey}`: Triggers the specified action (e.g., /state/actions/switch_on) in the Digital Twin state. The raw body contains the action request payload.
- `GET` `/state/relationships`: Retrieves the list of relationships in the Digital Twin state.
- `GET` `/state/relationships/{relationshipName}/instances`: Retrieves the instances of the specified relationship (e.g., /state/relationships/insideIn/instances) in the Digital Twin state.

Note: Replace {propertyKey}, {actionKey}, and {relationshipName} with the actual values you want to retrieve or trigger.
Make sure to use the appropriate HTTP method (GET, POST) and include any required parameters or payload as described in each endpoint's description. For more detailed information, refer to the Postman Collection for this API available in the folder `api`: [http_adapter_api_postman.json](api%2Fhttp_adapter_api_postman.json)`






