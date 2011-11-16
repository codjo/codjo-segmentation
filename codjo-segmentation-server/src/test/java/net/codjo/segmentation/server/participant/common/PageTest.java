/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.participant.common;
import net.codjo.segmentation.server.preference.family.Row;
import junit.framework.TestCase;
/**
 * Classe de test de {@link Page}.
 */
public class PageTest extends TestCase {
    private Page page;


    public void test_getMaxRow() throws Exception {
        assertEquals(1000, page.getMaxRow());
    }


    public void test_addRow() throws Exception {
        Row row = new Row(new String[0]);
        page.addRow(row);

        assertSame(row, page.getRow(0));
    }


    public void test_addRow_error() throws Exception {
        fillPage(page.getMaxRow());

        try {
            page.addRow(new Row(new String[0]));
            fail();
        }
        catch (IllegalStateException ex) {
            assertEquals("La page est pleine", ex.getMessage());
        }
    }


    public void test_isFull() throws Exception {
        assertFalse(page.isFull());
        assertEquals(0, page.getRowCount());

        fillPage(page.getMaxRow());

        assertEquals(page.getMaxRow(), page.getRowCount());
        assertTrue(page.isFull());
    }


    @Override
    protected void setUp() throws Exception {
        page = new Page();
    }


    private void fillPage(int maxRow) {
        for (int i = 1; i <= maxRow; i++) {
            page.addRow(new Row(new String[0]));
        }
    }
}
