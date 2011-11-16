package net.codjo.segmentation.server.paramImport.classification;
import net.codjo.database.common.api.JdbcFixture;
import junit.framework.TestCase;
public class ClassificationParserManagerTest extends TestCase {
    private JdbcFixture fixture = JdbcFixture.newFixture();
    private ClassificationParserManager parser;


    public void test_getColumnNames() throws Exception {
        assertEquals("CLASSIFICATION_ID", parser.getColumnNames()[0]);
        assertEquals("CLASSIFICATION_NAME", parser.getColumnNames()[1]);
        assertEquals("CLASSIFICATION_TYPE", parser.getColumnNames()[2]);
        assertEquals("CUSTOM_FIELD", parser.getColumnNames()[3]);
    }


    public void test_isValidColumn() throws Exception {
        assertTrue(parser.isValidColumn("CLASSIFICATION_ID"));
        assertTrue(parser.isValidColumn("CLASSIFICATION_NAME"));
        assertTrue(parser.isValidColumn("CLASSIFICATION_TYPE"));
        assertFalse(parser.isValidColumn("AUTRE_COLONNE"));
    }


    @Override
    protected void setUp() throws Exception {
        fixture.doSetUp();
        parser = new ClassificationParserManager(null);
        parser.setConnection(fixture.getConnection());
    }


    @Override
    protected void tearDown() throws Exception {
        fixture.doTearDown();
    }
}
