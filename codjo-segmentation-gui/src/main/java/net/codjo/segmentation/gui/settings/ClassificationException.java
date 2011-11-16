/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.settings;
/**
 * Exception levée lors de la sauvegarde d'une axe de segmentation.
 */
public class ClassificationException extends RuntimeException {
    public ClassificationException(String message) {
        super(message);
    }
}
