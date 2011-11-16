package net.codjo.segmentation.server.participant.context;
import net.codjo.expression.InvalidExpressionException;
import net.codjo.segmentation.server.participant.common.ExpressionsEvaluator;
import net.codjo.segmentation.server.participant.common.ExpressionsEvaluatorFactory;
import net.codjo.segmentation.server.participant.common.Page;
import net.codjo.segmentation.server.participant.common.PageStructure;
import net.codjo.segmentation.server.participant.common.SegmentationResult;
import net.codjo.segmentation.server.preference.family.XmlFamilyPreference;
import net.codjo.segmentation.server.preference.treatment.SegmentationPreference;
import java.sql.Connection;
import java.util.Map;
/**
 *
 */
public class SegmentationContext extends AbstractContext<Integer, Page> {
    private XmlFamilyPreference familyPreference;
    private SegmentationPreference segmentationPreference;
    private PageStructure pageStructure;
    private Map<String, String> parameters;
    private int segmentationId;


    public SegmentationContext(int segmentationId,
                                 XmlFamilyPreference familyPreference,
                                 SegmentationPreference segmentationPreference,
                                 Map<String, String> parameters) {
        this.segmentationId = segmentationId;
        this.familyPreference = familyPreference;
        this.segmentationPreference = segmentationPreference;
        this.parameters = parameters;
    }


    public void setPageStructure(PageStructure pageStructure) {
        synchronized (lock) {
            this.pageStructure = pageStructure;
        }
    }


    public PageStructure getPageStructure() {
        synchronized (lock) {
            return pageStructure;
        }
    }


    public Map<String, String> getParameters() {
        return parameters;
    }


    public XmlFamilyPreference getFamilyPreference() {
        return familyPreference;
    }


    public SegmentationPreference getSegmentationPreference() {
        return segmentationPreference;
    }


    public ExpressionsEvaluator createExpressionsEvaluator() throws InvalidExpressionException {
        synchronized (lock) {
            if (pageStructure == null) {
                throw new NullPointerException("La structure des pages n'est pas renseignee.");
            }
            return ExpressionsEvaluatorFactory.create(familyPreference, segmentationPreference, pageStructure);
        }
    }


    public SegmentationResult createSegmentationResult(Connection connection) {
        synchronized (lock) {
            return new SegmentationResult(connection, familyPreference);
        }
    }


    public int getSegmentationId() {
        return segmentationId;
    }


    public Page removePage(Integer id) {
        return super.remove(id);
    }


    public void putPage(Integer id, Page context) {
        super.put(id, context);
    }
}
