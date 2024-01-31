package it.wldt.adapter.http.digital.exception;

/**
 * Exception thrown to indicate an error in the configuration of an HTTP Digital Adapter.
 * This exception is typically raised when there are issues with the provided configuration parameters.
 * It extends the standard {@code Exception} class and includes a message to provide more
 * information about the specific configuration error.
 *
 * @author Marco Picone, Ph.D. - picone.m@gmail.com, Marta Spadoni University of Bologna
 */
public class HttpDigitalAdapterConfigurationException extends Exception {
    public HttpDigitalAdapterConfigurationException(String message) {
        super(message);
    }
}
