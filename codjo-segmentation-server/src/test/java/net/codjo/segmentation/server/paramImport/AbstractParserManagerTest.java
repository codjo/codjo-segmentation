/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.paramImport;
import junit.framework.TestCase;
import net.codjo.segmentation.server.ParseException;
/**
 *
 */
public class AbstractParserManagerTest extends TestCase {
    private AbstractParserManager manager;


    public void test_createDataArray() throws Exception {
        String rawData =
              "AXE_ID\tLABEL_AXE\tFAMILY_AXE\n"
              + "2\tAxe event 2\tEVENT\n"
              + "3\tAxe event 3\tEVENT\n";

        manager = new ParserManagerMock(rawData);

        manager.parse();
        String[][] array = manager.getDataArray();

        assertEquals("AXE_ID", array[0][0]);
        assertEquals("FAMILY_AXE", array[0][1]);
        assertEquals("LABEL_AXE", array[0][2]);
        assertEquals("IS_QUARANTINE", array[0][3]);

        assertEquals("2", array[1][0]);
        assertEquals("EVENT", array[1][1]);
        assertEquals("Axe event 2", array[1][2]);
        assertEquals("false", array[1][3]);

        assertEquals("3", array[2][0]);
        assertEquals("EVENT", array[2][1]);
        assertEquals("Axe event 3", array[2][2]);
        assertEquals("false", array[2][3]);
    }


    public void test_badFileFormat_missingHeaders() throws Exception {
        String rawData = "AXE_ID\tFAMILY_AXE\tCOL1\tCOL2\tCOL3\tCOL4";
        try {
            manager = new ParserManagerMock(rawData);
            manager.parse();
            fail("Exception attendue");
        }
        catch (ParseException exception) {
            assertEquals(ParseException.BAD_FILE_FORMAT, exception.getMessage());
        }
    }


    public void test_badFileFormat_wrongHeaders() throws Exception {
        try {
            manager = new ParserManagerMock("AXE_ID\tBAD_HEADER\tFAMILY_AXE");
            manager.parse();
            fail("Exception attendue");
        }
        catch (ParseException exception) {
            assertEquals(ParseException.BAD_FILE_FORMAT, exception.getMessage());
        }
    }


    public void test_badFileFormat_wrongRows() throws Exception {
        try {
            String wrongLine = "AXE_ID\tLABEL_AXE\tFAMILY_AXE\n"
                               + "1\tLabel 1\tFamily 1\n"
                               + "2\tLabel 2";

            manager = new ParserManagerMock(wrongLine);
            manager.parse();
            fail("Exception attendue");
        }
        catch (ParseException exception) {
            assertEquals(ParseException.BAD_FILE_FORMAT, exception.getMessage());
        }
    }


    public void test_badFileFormat_noSeparators() throws Exception {
        try {
            String wrongLine = "AXE_ID;LABEL_AXE;FAMILY_AXE;\n"
                               + "1;Label 1;Family 1;\n"
                               + "2;Label 2;Family 2";

            manager = new ParserManagerMock(wrongLine);
            manager.parse();
            fail("Exception attendue");
        }
        catch (ParseException exception) {
            assertEquals(ParseException.BAD_FILE_FORMAT, exception.getMessage());
        }
    }


    public void test_blankValues() throws Exception {
        String line = "AXE_ID\tLABEL_AXE\tFAMILY_AXE\n"
                      + "1\tLabel 1\t\t\n"
                      + "2\tLabel 2\t\t\n";

        manager = new ParserManagerMock(line);
        manager.parse();
        String[][] array = manager.getDataArray();

        assertEquals("AXE_ID", array[0][0]);
        assertEquals("FAMILY_AXE", array[0][1]);
        assertEquals("LABEL_AXE", array[0][2]);
        assertEquals("IS_QUARANTINE", array[0][3]);

        assertEquals("1", array[1][0]);
        assertNull(array[1][1]);
        assertEquals("Label 1", array[1][2]);
        assertEquals("false", array[1][3]);

        assertEquals("2", array[2][0]);
        assertNull(array[2][1]);
        assertEquals("Label 2", array[2][2]);
        assertEquals("false", array[2][3]);
    }


    public void test_blankValues_middle() throws Exception {
        String line = "AXE_ID\tLABEL_AXE\tFAMILY_AXE\n"
                      + "1\t\tFamily 1\n"
                      + "2\t\tFamily 2\n";

        manager = new ParserManagerMock(line);
        manager.parse();
        String[][] array = manager.getDataArray();

        assertEquals("AXE_ID", array[0][0]);
        assertEquals("FAMILY_AXE", array[0][1]);
        assertEquals("LABEL_AXE", array[0][2]);
        assertEquals("IS_QUARANTINE", array[0][3]);

        assertEquals("1", array[1][0]);
        assertEquals("Family 1", array[1][1]);
        assertNull(array[1][2]);
        assertEquals("false", array[1][3]);

        assertEquals("2", array[2][0]);
        assertEquals("Family 2", array[2][1]);
        assertNull(array[2][2]);
        assertEquals("false", array[2][3]);
    }


    private static class ParserManagerMock extends AbstractParserManager {
        private ParserManagerMock(String rawData) {
            super(rawData);
        }


        @Override
        public String getTableName() {
            return "PM_DUMMY";
        }


        @Override
        public String[] getColumnNames() {
            return new String[]{"AXE_ID", "FAMILY_AXE", "LABEL_AXE"};
        }
    }
}
