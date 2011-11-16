package net.codjo.segmentation.server.paramImport.classificationStructure;
import net.codjo.expression.SyntaxControl;
import net.codjo.segmentation.server.paramImport.AbstractControlManager;
import static net.codjo.segmentation.server.paramImport.AbstractControlManager.Anomaly.BAD_SLEEVE_CODE;
import static net.codjo.segmentation.server.paramImport.AbstractControlManager.Anomaly.FORBIDDEN_FORMULA_FOR_DUSTBIN;
import static net.codjo.segmentation.server.paramImport.AbstractControlManager.Anomaly.INCORRECT_FORMULA;
import static net.codjo.segmentation.server.paramImport.AbstractControlManager.Anomaly.MISSING_FORMULA;
import static net.codjo.segmentation.server.paramImport.AbstractControlManager.Anomaly.NODE_CANNOT_HAVE_FORMULA;
import static net.codjo.segmentation.server.paramImport.AbstractControlManager.Anomaly.NO_DUSTBIN;
import static net.codjo.segmentation.server.paramImport.AbstractControlManager.Anomaly.PARENT_IN_DB;
import static net.codjo.segmentation.server.paramImport.AbstractControlManager.Anomaly.SLEEVES_IN_DB;
import static net.codjo.segmentation.server.paramImport.AbstractControlManager.Anomaly.TOO_MANY_DUSTBIN;
import static net.codjo.segmentation.server.util.SegmentationUtil.getColumnIndexByName;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
/**
 *
 */
public class ClassificationStructureControlManager extends AbstractControlManager {
    @Override
    public int getMaxLengthForColumn(String column) {
        if ("SLEEVE_CODE".equals(column) || "SLEEVE_NAME".equals(column)) {
            return 50;
        }

        return -1;
    }


    @Override
    public String[] getPrimaryKeyColumns() {
        return new String[]{"CLASSIFICATION_ID", "SLEEVE_CODE"};
    }


    @Override
    public String getUnicitySQLQuery() {
        return null;
    }


    @Override
    public String getAnomalyLog(Anomaly anomaly, String... columnName) {
        switch (anomaly) {
            case REPEATED_INDEX:
                if (columnName.length == 2) {
                    if ("CLASSIFICATION_ID".equals(columnName[0]) && "SLEEVE_CODE".equals(columnName[1])) {
                        return "Doublon de l'id Axe et du code Poche dans le fichier";
                    }
                }
            case MAX_LENGTH:
                if ("SLEEVE_CODE".equals(columnName[0])) {
                    return "Le code poche est trop long";
                }
                else if ("SLEEVE_NAME".equals(columnName[0])) {
                    return "Le libellé de la poche est trop long";
                }
            case PARENT_IN_DB:
                return "Cet axe n'existe pas en base";
            case SLEEVES_IN_DB:
                return "Cet axe existe déjà en base avec des poches";
            case BAD_SLEEVE_CODE:
                return "Le code poche est incorrect";
            case NO_DUSTBIN:
                return "Cet axe ne comporte pas de poche fourre-tout";
            case TOO_MANY_DUSTBIN:
                return "Cet axe contient plus d'une poche fourre-tout";
            case INCORRECT_FORMULA:
                return "La formule de la poche est incorrecte";
            case MISSING_FORMULA:
                return "Cette poche ne comporte pas de formules";
            case NODE_CANNOT_HAVE_FORMULA:
                return "Un noeud ne peut pas avoir de formule";
            case FORBIDDEN_FORMULA_FOR_DUSTBIN:
                return "La poche fourre-tout ne peut pas contenir de formules";
            case CLASSIFICATION_IN_ERROR:
                return "Cet axe comporte une autre poche en erreur";
            default:
                return null;
        }
    }


    @Override
    protected void performSpecificControls() throws SQLException {
        controlParentExistsInDb();
        controlNoSleevesInDb();
        controlSleeveCodeFormat();
        controlOneDustbin();
        controlFormula();
        updateQuarantineFlagForAxeInError();
    }


    void controlNoSleevesInDb() throws SQLException {
        Statement statement = getConnection().createStatement();
        ResultSet resultSet = statement
              .executeQuery("select distinct CLASSIFICATION_ID from PM_CLASSIFICATION_STRUCTURE");

        try {
            while (resultSet.next()) {
                for (int rowIndex = 1; rowIndex < getData().length; rowIndex++) {
                    String[] row = getData()[rowIndex];

                    int classificationId = getColumnIndexByName(getData()[0], "CLASSIFICATION_ID");

                    if (row[classificationId].equals(resultSet.getString("CLASSIFICATION_ID"))) {
                        addRowToQuarantine(row, SLEEVES_IN_DB);
                    }
                }
            }
        }
        finally {
            closeStatement(statement);
        }
    }


    void controlParentExistsInDb() throws SQLException {
        Statement statement = getConnection().createStatement();
        ResultSet resultSet = statement.executeQuery("select CLASSIFICATION_ID from PM_CLASSIFICATION");
        try {
            List<Integer> foundRows = new ArrayList<Integer>();
            while (resultSet.next()) {
                for (int rowIndex = 1; rowIndex < getData().length; rowIndex++) {
                    String[] row = getData()[rowIndex];
                    String classificationId = row[getColumnIndexByName(getData()[0], "CLASSIFICATION_ID")];
                    if (classificationId.equals(resultSet.getString("CLASSIFICATION_ID"))) {
                        foundRows.add(rowIndex);
                    }
                }
            }

            for (int rowIndex = 1; rowIndex < getData().length; rowIndex++) {
                String[] row = getData()[rowIndex];

                if (!foundRows.contains(rowIndex)) {
                    addRowToQuarantine(row, PARENT_IN_DB);
                }
            }
        }
        finally {
            closeStatement(statement);
        }
    }


    void controlSleeveCodeFormat() {
        String data[][] = getData();
        for (int rowIndex = 1; rowIndex < data.length; rowIndex++) {
            String[] row = data[rowIndex];

            int sleeveCode = getColumnIndexByName(getData()[0], "SLEEVE_CODE");

            StringTokenizer prefix = new StringTokenizer(row[sleeveCode], "-");

            if (prefix.countTokens() != 2) {
                addRowToQuarantine(row, BAD_SLEEVE_CODE);
                continue;
            }

            String levelPart = prefix.nextToken();
            int level;

            try {
                level = Integer.parseInt(levelPart);

                String paragraphPart = prefix.nextToken();
                StringTokenizer suffix = new StringTokenizer(paragraphPart, ".");

                if ((suffix.countTokens() != level)) {
                    addRowToQuarantine(row, BAD_SLEEVE_CODE);
                    continue;
                }

                while (suffix.hasMoreTokens()) {
                    Integer.parseInt(suffix.nextToken());
                }
            }
            catch (NumberFormatException exception) {
                addRowToQuarantine(row, BAD_SLEEVE_CODE);
            }
        }
    }


    void controlOneDustbin() {
        String data[][] = getData();
        Map<String, Integer> classificationDustbin = new HashMap<String, Integer>(data.length);

        int classificationId = getColumnIndexByName(getData()[0], "CLASSIFICATION_ID");
        int dustbin = getColumnIndexByName(getData()[0], "SLEEVE_DUSTBIN");

        for (int rowIndex = 1; rowIndex < data.length; rowIndex++) {

            String currentClassification = data[rowIndex][classificationId];

            if (!classificationDustbin.containsKey(currentClassification)) {
                classificationDustbin.put(currentClassification, 0);
            }

            String currentDustbin = data[rowIndex][dustbin];

            if ("1".equals(currentDustbin)) {
                int counter = classificationDustbin.get(currentClassification);
                classificationDustbin.put(currentClassification, counter + 1);
            }
        }

        for (int rowIndex = 1; rowIndex < data.length; rowIndex++) {
            String[] row = data[rowIndex];

            int dustbinCount = classificationDustbin.get(row[classificationId]);

            if (dustbinCount == 0) {
                addRowToQuarantine(row, NO_DUSTBIN);
            }
            else if (dustbinCount > 1) {
                addRowToQuarantine(row, TOO_MANY_DUSTBIN);
            }
        }
    }


    void controlFormula() {
        String data[][] = getData();

        for (int rowIndex = 1; rowIndex < data.length; rowIndex++) {

            String formula = data[rowIndex][getColumnIndexByName(getData()[0], "FORMULA")];
            String terminalElement = data[rowIndex][getColumnIndexByName(getData()[0], "TERMINAL_ELEMENT")];
            String dustbin = data[rowIndex][getColumnIndexByName(getData()[0], "SLEEVE_DUSTBIN")];

            if ("0".equals(terminalElement)) {
                if ("0".equals(dustbin) && formula != null) {
                    addRowToQuarantine(data[rowIndex], NODE_CANNOT_HAVE_FORMULA);
                }
            }
            else {
                if ("0".equals(dustbin)) {
                    if (formula == null || formula.trim().length() == 0) {
                        addRowToQuarantine(data[rowIndex], MISSING_FORMULA);
                    }
                    else if (!SyntaxControl.isCorrectFormula(formula)) {
                        addRowToQuarantine(data[rowIndex], INCORRECT_FORMULA);
                    }
                }
                else {
                    if (formula != null && formula.trim().length() > 0) {
                        addRowToQuarantine(data[rowIndex], FORBIDDEN_FORMULA_FOR_DUSTBIN);
                    }
                }
            }
        }
    }


    private void updateQuarantineFlagForAxeInError() {
        String data[][] = getData();

        Set<String> quarantineList = new HashSet<String>();

        for (int rowIndex = 1; rowIndex < data.length; rowIndex++) {

            String classification = data[rowIndex][getColumnIndexByName(getData()[0], "CLASSIFICATION_ID")];
            String quarantine = data[rowIndex][getColumnIndexByName(getData()[0], "IS_QUARANTINE")];

            if ("true".equals(quarantine)) {
                quarantineList.add(classification);
            }
        }

        for (int rowIndex = 1; rowIndex < data.length; rowIndex++) {
            String classification = data[rowIndex][getColumnIndexByName(getData()[0], "CLASSIFICATION_ID")];
            String quarantine = data[rowIndex][getColumnIndexByName(getData()[0], "IS_QUARANTINE")];
            if (quarantineList.contains(classification) && "false".equals(quarantine)) {
                addRowToQuarantine(data[rowIndex], Anomaly.CLASSIFICATION_IN_ERROR);
            }
        }
    }
}
