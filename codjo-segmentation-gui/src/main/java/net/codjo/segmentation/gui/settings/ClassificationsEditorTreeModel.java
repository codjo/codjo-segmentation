/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.settings;
import net.codjo.mad.client.request.FieldsList;
import net.codjo.mad.client.request.RequestException;
import net.codjo.mad.client.request.Result;
import net.codjo.mad.gui.request.DetailDataSource;
import net.codjo.mad.gui.request.ListDataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
/**
 * Modele du Tree des axes avec les poches qui ont des formules.
 *
 * @version $Revision: 1.1 $
 */
public class ClassificationsEditorTreeModel implements TreeModel {
    private ListDataSource sleeveDataSource = new ListDataSource();

    private List<Classification> classificationList = new ArrayList<Classification>();
    private Map<String, String> toBeReplaced = new HashMap<String, String>();
    private Collection<String> sleeveListForStyle = new ArrayList<String>();


    void init(String editingSleeveCode, ListDataSource sonDataSource,
              DetailDataSource fatherDataSource) throws RequestException {
        Map<String, Sleeve> axeSleeveMap = new HashMap<String, Sleeve>();

        String classificationType = fatherDataSource.getFieldValue("classificationType");        
        int axeId = Integer.valueOf(fatherDataSource.getFieldValue("classificationId"));

        sleeveDataSource.setLoadFactoryId("selectAllSleeve");
        Map<String, String> selectorMap = new HashMap<String, String>();
        selectorMap.put("classificationType", classificationType);
        selectorMap.put("classificationId", "" + axeId);
        FieldsList fieldsList = new FieldsList(selectorMap);
        sleeveDataSource.setSelector(fieldsList);
        sleeveDataSource.setColumns(new String[]{
              "classificationId", "sleeveRowId", "classificationName", "sleeveCode",
              "sleeveName", "terminalElement", "formula"
        });
        sleeveDataSource.load();

        Classification classification = new Classification();
        Result resultSet = sleeveDataSource.getLoadResult();
        for (int i = 0; i < resultSet.getRowCount(); i++) {
            int classificationId = Integer.valueOf(resultSet.getValue(i, "classificationId"));
            String formulaValue = resultSet.getValue(i, "formula");
            String sleeveCode = resultSet.getValue(i, "sleeveCode");
            String sleeveName = resultSet.getValue(i, "sleeveName");
            String sleeveRowId = resultSet.getValue(i, "sleeveRowId");
            String terminalElement = resultSet.getValue(i, "terminalElement");
            String classificationName = resultSet.getValue(i, "classificationName");

            if (classification.getId() != classificationId && classification.getId() != 0) {
                classificationList.add(classification);
                classification = new Classification();
                axeSleeveMap.clear();
            }

            classification.setId(classificationId);
            classification.setName(classificationName);

            Sleeve sleeve = createSleeve(sleeveCode, classificationId, sleeveRowId, sleeveName,
                                         classificationName, terminalElement, formulaValue);

            addSleeveToClassification(axeSleeveMap, sleeve, classification);
        }

        if (classification.getId() != 0) {
            classificationList.add(classification);
        }
    }


    public Collection getSleeveListForStyle() {
        return sleeveListForStyle;
    }


    public Map<String, String> getToBeReplaced() {
        return toBeReplaced;
    }


    public List getClassificationList() {
        return classificationList;
    }


    public void addTreeModelListener(TreeModelListener treeModelListener) {
    }


    public void removeTreeModelListener(TreeModelListener treeModelListener) {
    }


    public void valueForPathChanged(TreePath path, Object newValue) {
    }


    public Object getRoot() {
        return "Axes";
    }


    public Object getChild(Object parent, int index) {
        Object result;
        if (parent instanceof Classification) {
            result = ((Classification)parent).getSleeveList().get(index);
        }
        else if (parent instanceof Sleeve) {
            Sleeve sleeve = (Sleeve)parent;

            result = sleeve.get(index);
        }
        else {
            result = classificationList.get(index);
        }
        return result;
    }


    public int getChildCount(Object parent) {
        int result;
        if (parent instanceof Classification) {
            result = ((Classification)parent).getSleeveList().size();
        }
        else if (parent instanceof Sleeve) {
            Sleeve sleeve = (Sleeve)parent;

            result = sleeve.getSleeveList().size();
        }
        else {
            result = classificationList.size();
        }
        return result;
    }


    public boolean isLeaf(Object node) {
        if (node instanceof Classification) {
            Classification classification = (Classification)node;
            return classification.getSleeveList().size() == 0;
        }
        if (node instanceof Sleeve) {
            Sleeve sleeve = (Sleeve)node;
            return sleeve.isTerminalElement() || sleeve.getSleeveList().isEmpty();
        }
        return false;
    }


    @SuppressWarnings({"SuspiciousMethodCalls"})
    public int getIndexOfChild(Object parent, Object child) {
        int result;
        if (parent instanceof Classification) {
            result = ((Classification)parent).getSleeveList().indexOf(child);
        }
        else if (parent instanceof Sleeve) {
            Sleeve sleeve = (Sleeve)parent;
            result = sleeve.getSleeveList().indexOf(child);
        }
        else {
            result = classificationList.indexOf(child);
        }
        return result;
    }


    private static String getLogicalName(Sleeve memorySleeve) {
        return memorySleeve.getClassificationName()
               + "$"
               + memorySleeve.getSleeveName();
    }


    private static String getPhysicalAlias(Sleeve memorySleeve) {
        return "INC_$$"
               + memorySleeve.getClassificationId()
               + "$"
               + memorySleeve.getSleeveRowId();
    }


    private static Sleeve createSleeve(String sleeveCode,
                                       int classificationId,
                                       String sleeveRowId,
                                       String sleeveName,
                                       String classificationName,
                                       String terminalElement, String formulaValue) {
        Sleeve sleeve = new Sleeve();
        sleeve.setSleeveCode(sleeveCode);
        sleeve.setClassificationId(classificationId);
        sleeve.setSleeveRowId(sleeveRowId);
        sleeve.setSleeveName(sleeveName);
        sleeve.setClassificationName(classificationName);
        sleeve.setTerminalElement(Boolean.valueOf(terminalElement));
        sleeve.setFormula(formulaValue);
        return sleeve;
    }


    private void addSleeveToClassification(Map<String, Sleeve> axeSleeveMap, Sleeve sleeve,
                                           Classification classification) {
        axeSleeveMap.put(sleeve.getIndex(), sleeve);

        if (sleeve.getLevel() == 1) {
            classification.addSleeve(sleeve);
        }
        else {
            Sleeve parent = axeSleeveMap.get(sleeve.getParentIndex());

            parent.addSleeve(sleeve);
        }
        String sleeveLogicName = getLogicalName(sleeve);
        toBeReplaced.put(getPhysicalAlias(sleeve), sleeveLogicName);
        sleeveListForStyle.add(sleeveLogicName);
    }


    public static class Sleeve {
        private int classificationId;
        private String sleeveRowId;
        private String classificationName;
        private String sleeveCode;
        private String sleeveName;
        private boolean terminalElement;
        private List<Sleeve> sleeveList = new ArrayList<Sleeve>();
        private String sleeveFormula;


        public void addSleeve(Sleeve sleeve) {
            sleeveList.add(sleeve);
        }


        public List getSleeveList() {
            return sleeveList;
        }


        public Sleeve get(int index) {
            return sleeveList.get(index);
        }


        public String getClassificationName() {
            return classificationName;
        }


        public void setClassificationName(String classificationName) {
            this.classificationName = classificationName;
        }


        public int getClassificationId() {
            return classificationId;
        }


        public void setClassificationId(int classificationId) {
            this.classificationId = classificationId;
        }


        public String getSleeveCode() {
            return sleeveCode;
        }


        public void setSleeveCode(String sleeveCode) {
            this.sleeveCode = sleeveCode;
        }


        public String getSleeveName() {
            return sleeveName;
        }


        public void setSleeveName(String sleeveName) {
            this.sleeveName = sleeveName;
        }


        public boolean isTerminalElement() {
            return terminalElement;
        }


        public void setTerminalElement(boolean terminalElement) {
            this.terminalElement = terminalElement;
        }


        public String getIndex() {
            int dashIndex = sleeveCode.indexOf('-');

            return sleeveCode.substring(dashIndex + 1);
        }


        public String getSleeveRowId() {
            return sleeveRowId;
        }


        public void setSleeveRowId(String sleeveRowId) {
            this.sleeveRowId = sleeveRowId;
        }


        public String getParentIndex() {
            String index = getIndex();

            int lastDotIndex = index.lastIndexOf('.');

            if (lastDotIndex != -1) {
                return index.substring(0, lastDotIndex);
            }
            else {
                return "";
            }
        }


        public int getLevel() {
            int dashIndex = sleeveCode.indexOf('-');

            return Integer.parseInt(sleeveCode.substring(0, dashIndex));
        }


        @Override
        public String toString() {
            return sleeveName;
        }


        public String getFormula() {
            return sleeveFormula;
        }


        public void setFormula(String newFormula) {
            sleeveFormula = newFormula;
        }
    }

    public static class Classification {
        private int id;
        private String name;
        private List<Sleeve> sleeveList = new ArrayList<Sleeve>();


        public void addSleeve(Sleeve sleeve) {
            sleeveList.add(sleeve);
        }


        public List getSleeveList() {
            return sleeveList;
        }


        public int getId() {
            return id;
        }


        public void setId(int id) {
            this.id = id;
        }


        public String getName() {
            return name;
        }


        public void setName(String name) {
            this.name = name;
        }


        @Override
        public String toString() {
            return name;
        }
    }
}
