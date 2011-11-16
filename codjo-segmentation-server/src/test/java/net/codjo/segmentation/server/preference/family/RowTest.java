/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.preference.family;
import junit.framework.TestCase;
/**
 * Classe de test de {@link net.codjo.segmentation.server.preference.family.Row}.
 */
public class RowTest extends TestCase {
    public void test_constructor_noInitialValue()
          throws Exception {
        String[] columnNames = new String[]{"col_a"};

        Row row = new Row(columnNames);

        assertEquals(null, row.getColumnValue(0));
        assertSame(columnNames, row.getColumnNames());
    }


    public void test_constructor_withInitialValue()
          throws Exception {
        Row row = new Row(new String[]{"col_a"}, new Object[]{"string"});
        assertEquals("string", row.getColumnValue(0));
    }


    public void test_constructor_error() throws Exception {
        try {
            new Row(new String[]{"col_a"}, new Object[]{"string", "bad"});
            fail();
        }
        catch (IllegalArgumentException ex) {
            assertEquals("Nombre de colonnes et de valeurs différentes", ex.getMessage());
        }
    }


    public void test_setColumnValue() throws Exception {
        Row row = new Row(new String[]{"col_a"});

        row.setColumnValue(0, "Bobo");

        assertEquals("Bobo", row.getColumnValue(0));
    }


    public void test_setColumnValue_error() throws Exception {
        Row row = new Row(new String[]{"col_a", "col_b"});

        row.setColumnValue(1, "2");
        assertEquals("2", row.getColumnValue(1));

        try {
            row.getColumnValue(2);
            fail();
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            ;
        }
    }


    public void test_getColumnValue() throws Exception {
        Row row = new Row(new String[]{"AP_ROOT$COL_STR"});

        row.setColumnValue(0, "Bobo");

        assertEquals("Bobo", row.getColumnValue("AP_ROOT", "COL_STR"));
    }


    public void test_toString() throws Exception {
        Row row = new Row(new String[]{"col_a", "col_b"});

        row.setColumnValue(0, "1");
        row.setColumnValue(1, "2");

        assertEquals("row{col_a=1, col_b=2}", row.toString());
    }
}
