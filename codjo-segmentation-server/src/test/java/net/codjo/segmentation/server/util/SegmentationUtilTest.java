package net.codjo.segmentation.server.util;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
/**
 *
 */
public class SegmentationUtilTest extends TestCase {

    public void test_checkParameters() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("segmentation.id", "");
        parameters.put("photo", "");
        SegmentationUtil.checkParameters(Arrays.asList("segmentation.id"), parameters);

        SegmentationUtil.checkParameters(Arrays.asList("segmentation.id", "photo"), parameters);
    }


    public void test_checkParameters_nok() throws Exception {

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("segmentation.id", "");

        try {
            SegmentationUtil.checkParameters(Arrays.asList("segmentation.id", "photo"), parameters);
            fail();
        }
        catch (IllegalArgumentException exception) {
            assertEquals("'photo' missing.", exception.getLocalizedMessage());
        }
    }
}
