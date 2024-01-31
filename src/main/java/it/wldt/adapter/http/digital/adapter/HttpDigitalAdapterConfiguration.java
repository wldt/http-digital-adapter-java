package it.wldt.adapter.http.digital.adapter;

import it.wldt.adapter.http.digital.exception.HttpDigitalAdapterConfigurationException;

import java.util.*;

/**
 * Represents the configuration for an HTTP Digital Adapter, specifying the host, port,
 * and filters for properties, actions, events, and relationships.
 * The filters are used to selectively include or exclude specific properties, actions,
 * events, and relationships when interacting with the HTTP Digital Adapter.
 * This class provides methods to add filters for each type and getters to retrieve the
 * configured values.
 *
 * @author Marco Picone, Ph.D. - picone.m@gmail.com, Marta Spadoni University of Bologna
 */
public class HttpDigitalAdapterConfiguration {

    /**
     * Adapter Id
     */
    private final String id;

    /**
     * Adapter HTTP Host
     */
    private final String host;

    /**
     * Adapter HTTP listening port
     */
    private final Integer port;

    /**
     * Filter for target Properties of interest to map into the adapter
     * It is a white list filter, but if it is empty, it means that ALL are considered
     */
    private final List<String> propertyFilter;

    /**
     * Filter for target Actions of interest to map into the adapter
     * It is a white list filter, but if it is empty, it means that ALL are considered
     */
    private final List<String> actionFilter;

    /**
     * Filter for target Events of interest to map into the adapter
     * It is a white list filter, but if it is empty, it means that ALL are considered
     */
    private final List<String> eventFilter;

    /**
     * Filter for target Relationships of interest to map into the adapter
     * It is a white list filter, but if it is empty, it means that ALL are considered
     */
    private final List<String> relationshipFilter;

    /**
     * Constructs a new {@code HttpDigitalAdapterConfiguration} with the specified
     * identifier, host, and port.
     *
     * @param id   The unique identifier for the HTTP Digital Adapter configuration.
     * @param host The host address for the HTTP Digital Adapter.
     * @param port The port number for the HTTP Digital Adapter.
     */
    public HttpDigitalAdapterConfiguration(String id, String host, Integer port) {
        this.id = id;
        this.host = host;
        this.port = port;
        propertyFilter = new LinkedList<>();
        actionFilter = new LinkedList<>();
        eventFilter = new LinkedList<>();
        relationshipFilter = new LinkedList<>();
    }

    /**
     * Adds a single property key to the property filter.
     *
     * @param propertyKey The property key to be added to the filter.
     * @throws HttpDigitalAdapterConfigurationException If the property key is null or empty.
     */
    public void addPropertyFilter(String propertyKey) throws HttpDigitalAdapterConfigurationException {
        if(propertyKey == null || propertyKey.isEmpty()) throw new HttpDigitalAdapterConfigurationException("Cannot use null or empty property key as filter");
        addFilter(propertyFilter, Collections.singleton(propertyKey));
    }

    /**
     * Adds a collection of property keys to the property filter.
     *
     * @param propertiesKey The collection of property keys to be added to the filter.
     * @throws HttpDigitalAdapterConfigurationException If the collection is null or empty.
     */
    public void addPropertiesFilter(Collection<String> propertiesKey) throws HttpDigitalAdapterConfigurationException {
        if(propertiesKey == null || propertiesKey.isEmpty()) throw new HttpDigitalAdapterConfigurationException("Cannot use null or empty list of property key as filter");
        addFilter(propertyFilter, propertiesKey);
    }

    /**
     * Adds a single action key to the action filter.
     *
     * @param actionKey The action key to be added to the filter.
     * @throws HttpDigitalAdapterConfigurationException If the action key is null or empty.
     */
    public void addActionFilter(String actionKey) throws HttpDigitalAdapterConfigurationException {
        if(actionKey == null || actionKey.isEmpty()) throw new HttpDigitalAdapterConfigurationException("Cannot use null or empty action key as filter");
        addFilter(actionFilter, Collections.singleton(actionKey));
    }

    /**
     * Adds a collection of action keys to the action filter.
     *
     * @param actionsKey The collection of action keys to be added to the filter.
     * @throws HttpDigitalAdapterConfigurationException If the collection is null or empty.
     */
    public void addActionsFilter(Collection<String> actionsKey) throws HttpDigitalAdapterConfigurationException {
        if(actionsKey == null || actionsKey.isEmpty()) throw new HttpDigitalAdapterConfigurationException("Cannot use null or empty list of action key as filter");
        addFilter(actionFilter, actionsKey);
    }

    /**
     * Adds a single event key to the event filter.
     *
     * @param eventKey The event key to be added to the filter.
     * @throws HttpDigitalAdapterConfigurationException If the event key is null or empty.
     */
    public void addEventFilter(String eventKey) throws HttpDigitalAdapterConfigurationException {
        if(eventKey == null || eventKey.isEmpty()) throw new HttpDigitalAdapterConfigurationException("Cannot use null or empty event key as filter");
        addFilter(eventFilter, Collections.singleton(eventKey));
    }

    /**
     * Adds a collection of event keys to the event filter.
     *
     * @param eventsKey The collection of event keys to be added to the filter.
     * @throws HttpDigitalAdapterConfigurationException If the collection is null or empty.
     */
    public void addEventsFilter(Collection<String> eventsKey) throws HttpDigitalAdapterConfigurationException {
        if(eventsKey == null || eventsKey.isEmpty()) throw new HttpDigitalAdapterConfigurationException("Cannot use null or empty list of event key as filter");
        addFilter(eventFilter, eventsKey);
    }

    /**
     * Adds a single relationship name to the relationship filter.
     *
     * @param relationshipName The relationship name to be added to the filter.
     * @throws HttpDigitalAdapterConfigurationException If the relationship name is null or empty.
     */
    public void addRelationshipFilter(String relationshipName) throws HttpDigitalAdapterConfigurationException {
        if(relationshipName == null || relationshipName.isEmpty()) throw new HttpDigitalAdapterConfigurationException("Cannot use null or empty relationship name as filter");
        addFilter(relationshipFilter, Collections.singleton(relationshipName));
    }

    /**
     * Adds a collection of relationship names to the relationship filter.
     *
     * @param relationshipNames The collection of relationship names to be added to the filter.
     * @throws HttpDigitalAdapterConfigurationException If the collection is null or empty.
     */
    public void addRelationshipsFilter(Collection<String> relationshipNames) throws HttpDigitalAdapterConfigurationException {
        if(relationshipNames == null || relationshipNames.isEmpty()) throw new HttpDigitalAdapterConfigurationException("Cannot use null or empty list of relationship name as filter");
        addFilter(relationshipFilter, relationshipNames);
    }

    /**
     * Retrieves the list of property keys used as a filter.
     *
     * @return The list of property keys.
     */
    public List<String> getPropertyFilter() {
        return propertyFilter;
    }

    /**
     * Retrieves the list of action keys used as a filter.
     *
     * @return The list of action keys.
     */
    public List<String> getActionFilter() {
        return actionFilter;
    }

    /**
     * Retrieves the list of event keys used as a filter.
     *
     * @return The list of event keys.
     */
    public List<String> getEventFilter() {
        return eventFilter;
    }

    /**
     * Retrieves the list of relationship names used as a filter.
     *
     * @return The list of relationship names.
     */
    public List<String> getRelationshipFilter() {
        return relationshipFilter;
    }

    /**
     * Retrieves the host address for the HTTP Digital Adapter.
     *
     * @return The host address.
     */
    public String getHost() {
        return host;
    }

    /**
     * Retrieves the port number for the HTTP Digital Adapter.
     *
     * @return The port number.
     */
    public Integer getPort() {
        return port;
    }

    /**
     * Retrieves the unique identifier for the HTTP Digital Adapter configuration.
     *
     * @return The unique identifier.
     */
    public String getId() {
        return id;
    }

    /**
     * Adds a collection of filter keys to the specified filter list.
     *
     * @param actualFilter The target filter list.
     * @param filterKeys   The collection of keys to be added to the filter.
     */
    private void addFilter(List<String> actualFilter, Collection<String> filterKeys){
        if(actualFilter == null){
            actualFilter = new LinkedList<>();
        }
        actualFilter.addAll(filterKeys);
    }
}
