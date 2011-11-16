/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.server.preference.family;
import junit.framework.TestCase;
/**
 * Classe de test de {@link net.codjo.segmentation.server.preference.family.ColumnMetaData}.
 */
public class ColumnMetaDataTest extends TestCase {
    public void test_constructor() throws Exception {
        ColumnMetaData metadata = new ColumnMetaData("col", java.sql.Types.VARCHAR);

        assertEquals(java.sql.Types.VARCHAR, metadata.getColumnType());
        assertEquals("col", metadata.getColumnName());
    }
}
