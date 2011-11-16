package net.codjo.segmentation.server.participant.context;
import net.codjo.segmentation.server.participant.common.ExpressionsEvaluator;
import net.codjo.segmentation.server.participant.common.SegmentationResult;
import net.codjo.segmentation.server.preference.family.XmlFamilyPreference;
import net.codjo.segmentation.server.preference.treatment.SegmentationPreference;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
/**
 *
 */
public class SegmentationContextMock extends SegmentationContext {
    private ExpressionsEvaluator evaluator;
    private SegmentationResult result;
    private Map<String, String> params = new HashMap<String, String>();
    private XmlFamilyPreference xmlFamilyPreference = new XmlFamilyPreference("id", "ap_root", "AP_DEST");
    private SegmentationPreference segmentationPreference;


    public SegmentationContextMock(ExpressionsEvaluator evaluator, SegmentationResult result) {
        this(1, evaluator, result);
    }


    public SegmentationContextMock(int segmentationId,
                                   ExpressionsEvaluator evaluator,
                                   SegmentationResult result) {
        super(segmentationId, null, null, null);
        this.evaluator = evaluator;
        this.result = result;
    }


    @Override
    public ExpressionsEvaluator createExpressionsEvaluator() {
        return evaluator;
    }


    @Override
    public SegmentationResult createSegmentationResult(Connection connection) {
        return result;
    }


    @Override
    public Map<String, String> getParameters() {
        return params;
    }


    @Override
    public XmlFamilyPreference getFamilyPreference() {
        return xmlFamilyPreference;
    }


    @Override
    public SegmentationPreference getSegmentationPreference() {
        return segmentationPreference;
    }


    public void mockGetParams(Map<String, String> map) {
        this.params = map;
    }


    public void mockGetXmlFamilyPreference(XmlFamilyPreference preference) {
        this.xmlFamilyPreference = preference;
    }


    public void mockGetSegmentationPreference(SegmentationPreference preference) {
        this.segmentationPreference = preference;
    }
}
