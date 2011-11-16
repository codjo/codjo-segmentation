/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.settings;
import net.codjo.mad.client.request.Row;
import java.util.Map;
import javax.swing.tree.DefaultMutableTreeNode;
/**
 * Une poche. Les poches sont ajoutées à l'arbre en appelant les méthodes <code>addSleeve</code> de la classe
 * <code>AxisTreeModel</code>.
 */
public class Sleeve extends DefaultMutableTreeNode {
    private Row row;


    protected Sleeve() {
    }


    Sleeve(Row row) {
        this.row = row;
    }


    public Sleeve(Map<String, String> fields) {
        row = new Row(fields);
    }


    protected void setRow(Row row) {
        this.row = row;
    }


    public String getPathCode() {
        final String sleeveCode = row.getFieldValue("sleeveCode");
        int caretIndex = sleeveCode.indexOf("-");

        return sleeveCode.substring(caretIndex + 1);
    }


    public Row getRow() {
        return row;
    }


    public String getSleeveId() {
        return row.getFieldValue("sleeveId");
    }


    public String getSleeveCode() {
        return row.getFieldValue("sleeveCode");
    }


    public boolean isTerminal() {
        return Boolean.valueOf(row.getFieldValue("terminalElement"));
    }


    public boolean isDustbin() {
        return Boolean.valueOf(row.getFieldValue("sleeveDustbin"));
    }


    public String getFormula() {
        return row.getFieldValue("formula");
    }


    /**
     * Le nom de l'élément affiché dans l'arbre.
     *
     * @return Description of the Return Value
     */
    @Override
    public String toString() {
        return getSleeveName();
    }


    public String getSleeveName() {
        return row.getFieldValue("sleeveName");
    }
}
