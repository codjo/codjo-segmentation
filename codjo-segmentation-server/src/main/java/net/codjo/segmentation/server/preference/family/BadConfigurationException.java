/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.preference.family;
/**
 * 
 */
public class BadConfigurationException extends Exception {
    public BadConfigurationException(String family, Throwable cause) {
        super("Configuration de la famille '" + family + "' incorrecte", cause);
    }
}
