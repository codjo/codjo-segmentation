/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.paramImport;
import static net.codjo.segmentation.server.util.SegmentationUtil.getColumnIndexByName;
import net.codjo.segmentation.server.ParseException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public abstract class AbstractParserManager {

    protected String[][] dataArray;
    private String rawData;
    private String[] columnNames;
    private String[] fileHeader;
    private Connection connection;


    protected AbstractParserManager(String rawData) {
        this.rawData = rawData;
    }


    public boolean isValidColumn(String column) throws ParseException {
        return Arrays.binarySearch(getColumnNames(), column) > -1;
    }


    public void parse() throws ParseException {

        dataArray = new String[getRowCount()][getColumnNames().length + 1];
        String[] header = createHeader();

        System.arraycopy(header, 0, dataArray[0], 0, header.length);

        initialiseRows();
    }


    private String[] createHeader() throws ParseException {
        StringTokenizer tokenizer = new StringTokenizer(rawData, "\n");
        String headerLine = tokenizer.nextToken();

        StringTokenizer headerTokenizer = new StringTokenizer(headerLine, "\t");

        if (getColumnNames().length < headerTokenizer.countTokens()) {
            throw new ParseException(ParseException.BAD_FILE_FORMAT);
        }

        int index = 0;
        fileHeader = new String[headerTokenizer.countTokens()];

        while (headerTokenizer.hasMoreTokens()) {
            String column = headerTokenizer.nextToken();
            if (!isValidColumn(column)) {
                throw new ParseException(ParseException.BAD_FILE_FORMAT);
            }
            fileHeader[index] = column;
            index++;
        }

        String[] header = new String[getColumnNames().length + 1];
        System.arraycopy(getColumnNames(), 0, header, 0, getColumnNames().length);
        header[header.length - 1] = "IS_QUARANTINE";
        return header;
    }


    private void initialiseRows() throws ParseException {
        StringTokenizer lineTokenizer = new StringTokenizer(rawData, "\n");

        lineTokenizer.nextToken();

        int rowIndex = 1;
        while (lineTokenizer.hasMoreTokens()) {
            String currentLine = lineTokenizer.nextToken();

            String pattern = "\t";
            int occurrences = currentLine.length() - currentLine.replaceAll(pattern, "").length();

            if (fileHeader.length - 2 >= occurrences) {
                throw new ParseException(ParseException.BAD_FILE_FORMAT);
            }

            StringTokenizer currentLineTokenizer = new StringTokenizer(currentLine, "\t", true);
            int index = 0;
            boolean tabInPrevious = false;
            while (currentLineTokenizer.hasMoreTokens()) {

                String value = currentLineTokenizer.nextToken();
                String columnInFile = fileHeader[index];
                int indexInDataArray = getColumnIndexByName(dataArray[0], columnInFile);

                if (tabInPrevious && "\t".equals(value)) {
                    dataArray[rowIndex][indexInDataArray] = null;
                    index++;
                }
                else {
                    if (!"\t".equals(value)) {
                        dataArray[rowIndex][indexInDataArray] = value;
                        index++;
                    }
                }

                tabInPrevious = "\t".equals(value);
            }

            dataArray[rowIndex][getColumnIndexByName(dataArray[0], "IS_QUARANTINE")] = "false";
            rowIndex++;
        }
    }


    private int getRowCount() {
        return new StringTokenizer(rawData, "\n").countTokens();
    }


    public String[] getFileHeader() {
        return fileHeader;
    }


    public String[][] getDataArray() {
        return dataArray;
    }


    public abstract String getTableName();


    public String[] getColumnNames() throws ParseException {
        if (columnNames == null) {
            initColumnNames();
        }
        return columnNames;
    }


    private void initColumnNames() throws ParseException {
        try {
            Statement statement = connection.createStatement();
            String request = "select name from syscolumns where id=object_id('" + getTableName() + "')";
            ResultSet resultSet = statement.executeQuery(request);
            List<String> columnList = new ArrayList<String>();

            while (resultSet.next()) {
                columnList.add(resultSet.getString("name"));
            }

            columnNames = columnList.toArray(new String[columnList.size()]);
            Arrays.sort(columnNames);
        }
        catch (SQLException exception) {
            throw new ParseException(exception.getLocalizedMessage());
        }
    }


    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
