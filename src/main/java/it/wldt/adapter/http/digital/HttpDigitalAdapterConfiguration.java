package it.wldt.adapter.http.digital;

import java.util.*;

public class HttpDigitalAdapterConfiguration {

    private final String id;
    private final String host;
    private final Integer port;
    //It is a white list filter, but if it is empty, it means that ALL are considered
    private final List<String> propertyFilter;
    private final List<String> actionFilter;
    private final List<String> eventFilter;
    private final List<String> relationshipFilter;

    public HttpDigitalAdapterConfiguration(String id, String host, Integer port) {
        this.id = id;
        this.host = host;
        this.port = port;
        propertyFilter = new LinkedList<>();
        actionFilter = new LinkedList<>();
        eventFilter = new LinkedList<>();
        relationshipFilter = new LinkedList<>();
    }

    public void addPropertyFilter(String propertyKey) throws HttpDigitalAdapterConfigurationException {
        if(propertyKey == null || propertyKey.isEmpty()) throw new HttpDigitalAdapterConfigurationException("Cannot use null or empty property key as filter");
        addFilter(propertyFilter, Collections.singleton(propertyKey));
    }

    public void addPropertiesFilter(Collection<String> propertiesKey) throws HttpDigitalAdapterConfigurationException {
        if(propertiesKey == null || propertiesKey.isEmpty()) throw new HttpDigitalAdapterConfigurationException("Cannot use null or empty list of property key as filter");
        addFilter(propertyFilter, propertiesKey);
    }

    public void addActionFilter(String actionKey) throws HttpDigitalAdapterConfigurationException {
        if(actionKey == null || actionKey.isEmpty()) throw new HttpDigitalAdapterConfigurationException("Cannot use null or empty action key as filter");
        addFilter(actionFilter, Collections.singleton(actionKey));
    }

    public void addActionsFilter(Collection<String> actionsKey) throws HttpDigitalAdapterConfigurationException {
        if(actionsKey == null || actionsKey.isEmpty()) throw new HttpDigitalAdapterConfigurationException("Cannot use null or empty list of action key as filter");
        addFilter(actionFilter, actionsKey);
    }

    public void addEventFilter(String eventKey) throws HttpDigitalAdapterConfigurationException {
        if(eventKey == null || eventKey.isEmpty()) throw new HttpDigitalAdapterConfigurationException("Cannot use null or empty event key as filter");
        addFilter(eventFilter, Collections.singleton(eventKey));
    }

    public void addEventsFilter(Collection<String> eventsKey) throws HttpDigitalAdapterConfigurationException {
        if(eventsKey == null || eventsKey.isEmpty()) throw new HttpDigitalAdapterConfigurationException("Cannot use null or empty list of event key as filter");
        addFilter(eventFilter, eventsKey);
    }

    public void addRelationshipFilter(String relationshipName) throws HttpDigitalAdapterConfigurationException {
        if(relationshipName == null || relationshipName.isEmpty()) throw new HttpDigitalAdapterConfigurationException("Cannot use null or empty relationship name as filter");
        addFilter(relationshipFilter, Collections.singleton(relationshipName));
    }

    public void addRelationshipsFilter(Collection<String> relationshipName) throws HttpDigitalAdapterConfigurationException {
        if(relationshipName == null || relationshipName.isEmpty()) throw new HttpDigitalAdapterConfigurationException("Cannot use null or empty list of relationship name as filter");
        addFilter(relationshipFilter, relationshipName);
    }

    public List<String> getPropertyFilter() {
        return propertyFilter;
    }

    public List<String> getActionFilter() {
        return actionFilter;
    }

    public List<String> getEventFilter() {
        return eventFilter;
    }

    public List<String> getRelationshipFilter() {
        return relationshipFilter;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getId() {
        return id;
    }

    private void addFilter(List<String> actualFilter, Collection<String> filterKeys){
        if(actualFilter == null){
            actualFilter = new LinkedList<>();
        }
        actualFilter.addAll(filterKeys);
    }
}
