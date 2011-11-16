/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.paramImport;
import static net.codjo.segmentation.server.paramImport.AbstractControlManager.Anomaly.MAX_LENGTH;
import static net.codjo.segmentation.server.paramImport.AbstractControlManager.Anomaly.REPEATED_INDEX;
import static net.codjo.segmentation.server.paramImport.AbstractControlManager.Anomaly.UNICITY;
import static net.codjo.segmentation.server.util.SegmentationUtil.getColumnIndexByName;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 *
 */
public abstract class AbstractControlManager {

    public enum Anomaly {
        MAX_LENGTH,
        UNICITY,
        REPEATED_INDEX,
        PARENT_IN_DB,
        SLEEVES_IN_DB,
        BAD_SLEEVE_CODE,
        NO_DUSTBIN,
        TOO_MANY_DUSTBIN,
        INCORRECT_FORMULA,
        MISSING_FORMULA,
        NODE_CANNOT_HAVE_FORMULA,
        CLASSIFICATION_IN_ERROR,
        FORBIDDEN_FORMULA_FOR_DUSTBIN
    }

    private List<String[]> quarantine = new ArrayList<String[]>();

    private String[][] data;

    private Connection connection;

    private String[] fileHeader;


    public void control() throws SQLException {
        controlUnicity();
        controlMaxLength();
        controlRepeatedValues();
        performSpecificControls();
    }


    protected abstract void performSpecificControls() throws SQLException;


    public abstract int getMaxLengthForColumn(String column);


    public abstract String[] getPrimaryKeyColumns();


    public abstract String getUnicitySQLQuery();


    public abstract String getAnomalyLog(Anomaly anomaly, String... columnName);


    public final void controlUnicity() throws SQLException {
        if (getUnicitySQLQuery() == null) {
            return;
        }

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(getUnicitySQLQuery());

        String primaryKey = getPrimaryKeyColumns()[0];
        while (resultSet.next()) {
            for (String[] row : data) {
                if (resultSet.getString(primaryKey).equals(row[getColumnIndexByName(data[0], primaryKey)])) {
                    addRowToQuarantine(row, UNICITY, primaryKey);
                }
            }
        }
        closeStatement(statement);
    }


    public final void controlMaxLength() {
        for (int rowIndex = 1; rowIndex < data.length; rowIndex++) {
            String[] row = data[rowIndex];

            for (int columnIndex = 0; columnIndex < row.length; columnIndex++) {
                checkMaxLength(row, columnIndex);
            }
        }
    }


    private void checkMaxLength(String[] row, int column) {
        String columnName = data[0][column];

        if (getMaxLengthForColumn(columnName) > 0 && row[column].length() > getMaxLengthForColumn(
              columnName)) {
            addRowToQuarantine(row, MAX_LENGTH, columnName);
        }
    }


    public void controlRepeatedValues() {
        String[] columnNames = getPrimaryKeyColumns();
        Map<String, Integer> values = new HashMap<String, Integer>();

        for (int rowIndex = 0; rowIndex < data.length; rowIndex++) {

            StringBuffer compositeValue = new StringBuffer();

            for (String columnName : columnNames) {
                compositeValue.append(data[rowIndex][getColumnIndexByName(data[0], columnName)]);
            }

            if (!values.containsKey(compositeValue.toString())) {
                values.put(compositeValue.toString(), rowIndex);
            }
            else {
                addRowToQuarantine(data[values.get(compositeValue.toString())], REPEATED_INDEX, columnNames);
                addRowToQuarantine(data[rowIndex], REPEATED_INDEX, columnNames);
            }
        }
    }


    public String[][] getQuarantine() {
        if (quarantine.size() == 0) {
            return new String[0][0];
        }
        return quarantine.toArray(new String[quarantine.size()][data[0].length]);
    }


    protected final void addRowToQuarantine(String[] row, Anomaly anomalyType, String... columName) {
        if (quarantine.size() == 0) {
            addQuarantineColumnHeader();
        }

        if (!rowIsInQuarantine(row)) {
            row[row.length - 1] = "true";
            String[] quarantineRow = new String[row.length];
            System.arraycopy(row, 0, quarantineRow, 0, row.length - 1);
            quarantineRow[row.length - 1] = getAnomalyLog(anomalyType, columName);
            quarantine.add(quarantineRow);
        }
    }


    private void addQuarantineColumnHeader() {
        String[] quarantineRow = new String[data[0].length];
        System.arraycopy(data[0], 0, quarantineRow, 0, data[0].length - 1);
        quarantineRow[data[0].length - 1] = "ANOMALY_LOG";
        quarantine.add(0, quarantineRow);
    }


    public boolean rowIsInQuarantine(String[] row) {
        return "true".equals(row[getColumnIndexByName(data[0], "IS_QUARANTINE")]);
    }


    public String getQuarantineStream() {
        if (quarantine.size() == 0) {
            return "";
        }

        StringBuffer result = new StringBuffer();
        for (String[] row : quarantine) {
            int count = 1;
            for (int columnIndex = 0; columnIndex < row.length; columnIndex++) {
                String columnName = quarantine.get(0)[columnIndex];
                if ("ANOMALY_LOG".equals(columnName) || Arrays.binarySearch(fileHeader, columnName) > -1) {
                    String value = row[columnIndex];
                    result.append(value);
                    if (count < fileHeader.length + 1) {
                        result.append("\t");
                    }
                    count++;
                }
            }
            result.append("\n");
        }
        return result.toString();
    }


    public void setConnection(final Connection connection) {
        this.connection = connection;
    }


    public Connection getConnection() {
        return connection;
    }


    public void setQuarantine(List<String[]> quarantine) {
        this.quarantine = quarantine;
    }


    public final String[][] getData() {
        return data;
    }


    public final void setData(String[][] data) {
        this.data = data;
    }


    public void setFileHeader(String[] fileHeader) {
        this.fileHeader = fileHeader;
        Arrays.sort(this.fileHeader);
    }


    protected final void closeStatement(Statement statement) throws SQLException {
        if (statement != null) {
            statement.close();
        }
    }
}
