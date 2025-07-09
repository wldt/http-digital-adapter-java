package it.wldt.adapter.http.digital.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/** Serializable class that is used to cast info from a JSON configuration File */
public class HttpDigitalAdapterFileConfiguration implements Serializable {

    /** The unique identifier for the HTTP Digital Adapter configuration. */
    @JsonProperty("id")
    private String id;

    /** The host address for the HTTP Digital Adapter. */
    @JsonProperty("host")
    private String host;

    /** The port number for the HTTP Digital Adapter. */
    @JsonProperty("port")
    private Integer port;

    /** Empty class Constructor */
    public HttpDigitalAdapterFileConfiguration() {

    }

    /**
     * @return the unique identifier for the HTTP Digital Adapter configuration.
     */
    public String getId() {
        return id;
    }

    /**
     * @return the host address for the HTTP Digital Adapter.
     */
    public String getHost() {
        return host;
    }

    /**
     * @return the port number for the HTTP Digital Adapter.
     */
    public Integer getPort() {
        return port;
    }

    /**
     * Set the unique identifier.
     * @param id the unique identifier for the HTTP Digital Adapter configuration.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Set the host address.
     * @param host the host address for the HTTP Digital Adapter.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Set the port number.
     * @param port the port number for the HTTP Digital Adapter.
     */
    public void setPort(Integer port) {
        this.port = port;
    }
}
