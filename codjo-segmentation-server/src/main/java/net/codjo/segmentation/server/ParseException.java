package net.codjo.segmentation.server;
/**
 *
 */
public class ParseException extends Exception {

    public static final String BAD_FILE_FORMAT = "Le fichier n'est pas correctement formaté.";

    public ParseException(String message) {
        super(message);
    }
}
