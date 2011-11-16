package net.codjo.segmentation.server.preference.treatment;

public class NullDustbinException extends Exception {
    public NullDustbinException(int segmentationId, String segmentationName) {
        super("L'axe " + segmentationName + "(id = " + segmentationId
              + ") ne contient pas de poche Fourre-tout");
    }
}
