/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.preference;
import net.codjo.mad.common.structure.StructureReader;
import net.codjo.mad.common.structure.DefaultStructureReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
/**
 * Classe de test de {@link net.codjo.segmentation.gui.preference.DBStructureMad}.
 */
public class DBStructureMadTest extends TestCase {
    private DBStructureMad structure;


    @Override
    protected void setUp() throws Exception {
        structure = new DBStructureMad();

        String xml =
              "<structure>"
              + "    <table type='quarantine' label='Q_AP_TOTO' name='Toto' sql='Q_AP_TOTO'>"
              + "        <field label='Code portefeuille' name='portfolio' sql='PORTFOLIO'/>"
              + "        <field label='coupon net' name='netDividend' sql='NET_DIVIDEND'/>"
              + "    </table>"
              + "    <table type='data' label='la table a BOBO' name='Bobo' sql='BOBO'>"
              + "        <field label='Code portefeuille' name='portfolio' sql='PORTFOLIO'/>"
              + "        <field label='coupon net' name='netDividend' sql='NET_DIVIDEND' referential='Referential' sql-type='NUMERIC'/>"
              + "    </table>"
              + "</structure>";
        StructureReader structureReader = new DefaultStructureReader(new StringReader(xml));

        structure.setMad(structureReader);
    }


    public void test_getColumnsFor() throws Exception {
        List<String> colList = new ArrayList<String>();
        colList.add("PORTFOLIO");
        colList.add("NET_DIVIDEND");

        assertEquals(colList, structure.getColumnsFor("Q_AP_TOTO"));
    }


    public void test_getColumnLabelFor() throws Exception {
        assertEquals("Code portefeuille", structure.getColumnLabelFor("BOBO", "PORTFOLIO"));
    }
}
