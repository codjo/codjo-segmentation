/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.participant.common;
import net.codjo.expression.ExpressionException;
import net.codjo.segmentation.server.preference.family.Row;
import net.codjo.segmentation.server.preference.family.XmlFamilyPreference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
/**
 * Positionne le réultat en BD.
 */
public class SegmentationResult {
    private PreparedStatement preparedStatement;
    private boolean closed = false;
    private Connection connection;
    private XmlFamilyPreference familyPreference;


    public SegmentationResult(Connection connection, XmlFamilyPreference familyPreference) {
        this.connection = connection;
        this.familyPreference = familyPreference;
    }


    public void add(Row row) throws SQLException {
        createStatementIfNeeded();
        insertRow(row, 0, null);
    }


    public void addError(ComputeException error) throws SQLException {
        createStatementIfNeeded();
        insertRow(error.getResultRow(), error.getErrorCount(), buildErrorMessage(error));
    }


    private String buildErrorMessage(ComputeException error) {
        ExpressionException expressionException = error.getExpressionException();
        if (expressionException == null) {
            return error.getMessage();
        }
        else {
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < expressionException.getNbError(); i++) {
                if (buffer.length() > 0) {
                    buffer.append(",\n");
                }
                buffer.append(expressionException.getMessage(i));
            }
            return buffer.toString();
        }
    }


    public void close() throws SQLException {
        try {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        }
        finally {
            preparedStatement = null;
            closed = true;
        }
    }


    protected String buildInsertQuery() {
        StringBuffer buff = new StringBuffer();

        buff.append("insert into ");
        buff.append(familyPreference.getResultTableName());
        buff.append(" (");
        appendColumns(buff);
        buff.append(") values (");
        appendValues(buff);
        buff.append(")");
        return buff.toString();
    }


    private void appendColumns(StringBuffer buff) {
        String[] resultColumnNames = familyPreference.getResultTableColumnNames();

        for (int i = 0; i < resultColumnNames.length; i++) {
            String colName = resultColumnNames[i];

            if (i != 0) {
                buff.append(",");
            }

            buff.append(colName);
        }
        buff.append(",ANOMALY,ANOMALY_LOG");
    }


    private void appendValues(StringBuffer buff) {
        int length = familyPreference.getResultTableColumnNames().length;
        for (int i = 0; i < length; i++) {
            if (i != 0) {
                buff.append(",");
            }

            buff.append("?");
        }
        buff.append(",?,?");
    }


    private void assertRowIsCompatible(Row row) {
        if (!Arrays.equals(row.getColumnNames(), familyPreference.getResultTableColumnNames())) {
            throw new IllegalArgumentException("Row non compatible avec les colonnes résultat :"
                                               + Arrays.asList(familyPreference.getResultTableColumnNames()));
        }
    }


    private void insertRow(Row row, int anomalyNumber, String anomalyLog)
          throws SQLException {
        assertRowIsCompatible(row);

        int resultColumnCount = familyPreference.getResultTableColumnNames().length;
        for (int i = 0; i < resultColumnCount; i++) {
            Object columnValue = row.getColumnValue(i);
            if (columnValue == null) {
                preparedStatement.setNull(i + 1,
                                          familyPreference.getResultTableColumnType(
                                                familyPreference.getResultTableColumnNames()[i]));
            }
            else {
                preparedStatement.setObject(i + 1, columnValue);
            }
        }
        preparedStatement.setInt(resultColumnCount + 1, anomalyNumber);
        preparedStatement.setString(resultColumnCount + 2, anomalyLog);
        preparedStatement.executeUpdate();
        preparedStatement.clearParameters();
    }


    private void createStatementIfNeeded() throws SQLException {
        assertNotClosed();
        if (preparedStatement == null) {
            preparedStatement = connection.prepareStatement(buildInsertQuery());
        }
    }


    private void assertNotClosed() {
        if (closed) {
            throw new IllegalStateException("La méthode add est appelé après un close()");
        }
    }
}
