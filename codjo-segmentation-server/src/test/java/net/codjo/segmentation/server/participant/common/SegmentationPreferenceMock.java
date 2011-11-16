/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.participant.common;
import net.codjo.segmentation.server.preference.treatment.Expression;
import net.codjo.segmentation.server.preference.treatment.NullDustbinException;
import net.codjo.segmentation.server.preference.treatment.SegmentationPreference;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * {@link SegmentationPreference}
 */
public class SegmentationPreferenceMock extends SegmentationPreference {
    private List<Expression> expressions;


    public SegmentationPreferenceMock() throws SQLException, NullDustbinException {
        super(null, 1, new HashMap<String, String>(0));
    }


    @Override
    public String getFamily() {
        return null;
    }


    @Override
    public String getSegmentationName() {
        return null;
    }


    public void mockGetExpressions(List<Expression> expressionsMock) {
        this.expressions = expressionsMock;
    }


    @Override
    public List<Expression> getExpressions() {
        return expressions;
    }


    @Override
    protected void loadSegmentation(Connection connection) {
    }


    @Override
    protected void loadExpressions(Connection connection, Map<String, String> parameters) {
    }
}
