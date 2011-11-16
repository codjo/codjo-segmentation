package net.codjo.segmentation.server.paramImport.classificationStructure;
import net.codjo.database.common.api.JdbcFixture;
import junit.framework.TestCase;
public class ClassificationStructureParserManagerTest extends TestCase {
    private ClassificationStructureParserManager parser;
    private JdbcFixture fixture = JdbcFixture.newFixture();


    public void test_getColumnNames() throws Exception {
        assertEquals("CLASSIFICATION_ID", parser.getColumnNames()[0]);
        assertEquals("CUSTOM_FIELD", parser.getColumnNames()[1]);
        assertEquals("FORMULA", parser.getColumnNames()[2]);
        assertEquals("SLEEVE_CODE", parser.getColumnNames()[3]);
        assertEquals("SLEEVE_DUSTBIN", parser.getColumnNames()[4]);
        assertEquals("SLEEVE_ID", parser.getColumnNames()[5]);
        assertEquals("SLEEVE_NAME", parser.getColumnNames()[6]);
        assertEquals("SLEEVE_ROW_ID", parser.getColumnNames()[7]);
        assertEquals("TERMINAL_ELEMENT", parser.getColumnNames()[8]);
    }


    public void test_isValidColumn() throws Exception {
        assertTrue(parser.isValidColumn("CLASSIFICATION_ID"));
        assertTrue(parser.isValidColumn("SLEEVE_CODE"));
        assertTrue(parser.isValidColumn("SLEEVE_NAME"));
        assertTrue(parser.isValidColumn("SLEEVE_DUSTBIN"));
        assertTrue(parser.isValidColumn("TERMINAL_ELEMENT"));
        assertTrue(parser.isValidColumn("FORMULA"));
        assertFalse(parser.isValidColumn("AUTRE_COLONNE"));
    }


    public void test_getData() throws Exception {
        String importFile =
              "CLASSIFICATION_ID\tSLEEVE_CODE\tSLEEVE_NAME\tSLEEVE_DUSTBIN\tTERMINAL_ELEMENT\tFORMULA\n"
              + "1\t01-1\tPoche 1\t0\t1\tmanager == 1\n"
              + "1\t01-2\tFourre-tout\t0\t1\t \n"
              + "2\t01-1\tPoche 2\t0\t1\tmanager in (\"moner\")\n"
              + "2\t01-2\tPoche 3\t0\t1\tmanager not in (\"panicol\")\n";

        parser = new ClassificationStructureParserManager(importFile);
        parser.setConnection(fixture.getConnection());
        parser.parse();
        String[][] array = parser.getDataArray();

        assertEquals(5, array.length);

        assertEquals("CLASSIFICATION_ID", array[0][0]);
        assertEquals("CUSTOM_FIELD", array[0][1]);
        assertEquals("FORMULA", array[0][2]);
        assertEquals("SLEEVE_CODE", array[0][3]);
        assertEquals("SLEEVE_DUSTBIN", array[0][4]);
        assertEquals("SLEEVE_ID", array[0][5]);
        assertEquals("SLEEVE_NAME", array[0][6]);
        assertEquals("SLEEVE_ROW_ID", array[0][7]);
        assertEquals("TERMINAL_ELEMENT", array[0][8]);
        assertEquals("IS_QUARANTINE", array[0][9]);

        assertEquals("1", array[1][0]);
        assertNull(array[1][1]);
        assertEquals("manager == 1", array[1][2]);
        assertEquals("01-1", array[1][3]);
        assertEquals("0", array[1][4]);
        assertNotNull(array[1][5]);
        assertEquals("Poche 1", array[1][6]);
        assertNotNull(array[1][7]);
        assertEquals("1", array[1][8]);
        assertEquals("false", array[1][9]);
    }


    @Override
    protected void setUp() throws Exception {
        fixture.doSetUp();
        parser = new ClassificationStructureParserManager(null);
        parser.setConnection(fixture.getConnection());
    }


    @Override
    protected void tearDown() throws Exception {
        fixture.doTearDown();
    }
}
