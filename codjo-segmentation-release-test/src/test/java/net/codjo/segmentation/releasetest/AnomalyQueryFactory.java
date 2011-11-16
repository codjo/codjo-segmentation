package net.codjo.segmentation.releasetest;
import net.codjo.mad.server.handler.HandlerException;
import net.codjo.mad.server.handler.sql.QueryBuilder;
import net.codjo.mad.server.handler.sql.SqlHandler;
import java.util.Map;
/**
 *
 */
public class AnomalyQueryFactory implements QueryBuilder {
    public String buildQuery(Map args, SqlHandler sqlHandler) throws HandlerException {
        String assetClassificationList = (String)args.get("assetClassificationList");

        return "SELECT MY_KEY, CLASSIFICATION_ID, SLEEVE_CODE, ANOMALY, ANOMALY_LOG FROM SEG_RESULT_EVENT"
               + " WHERE ANOMALY > 0 and CLASSIFICATION_ID in (" + assetClassificationList + ")"
               + " union"
               + " SELECT MY_KEY, CLASSIFICATION_ID, SLEEVE_CODE, ANOMALY, ANOMALY_LOG FROM SEG_RESULT_ACTION"
               + " WHERE ANOMALY > 0 and CLASSIFICATION_ID in (" + assetClassificationList + ")";
    }
}
