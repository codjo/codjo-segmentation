package net.codjo.segmentation.server.paramImport.classificationStructure;
import net.codjo.tokio.TokioFixture;
import junit.framework.TestCase;
/**
 *
 */
public class ClassificationStructureControlManagerTest extends TestCase {
    private ClassificationStructureControlManager controlManager;
    private TokioFixture fixture = new TokioFixture(ClassificationStructureControlManagerTest.class);


    public void test_getMaxLengthForColumn() throws Exception {
        assertEquals(50, controlManager.getMaxLengthForColumn("SLEEVE_CODE"));
        assertEquals(50, controlManager.getMaxLengthForColumn("SLEEVE_NAME"));
    }


    public void test_getUniqueColumns() throws Exception {
        assertEquals("CLASSIFICATION_ID", controlManager.getPrimaryKeyColumns()[0]);
        assertEquals("SLEEVE_CODE", controlManager.getPrimaryKeyColumns()[1]);
    }


    public void test_getUnicitySQLQuery() throws Exception {
        assertNull(controlManager.getUnicitySQLQuery());
    }


    public void test_repeatedValues_multipleColumns() throws Exception {
        String[][] data =
              new String[][]{
                    {"CLASSIFICATION_ID", "SLEEVE_CODE", "SLEEVE_DUSTBIN", "IS_QUARANTINE"},
                    {"1", "01-1", "1", "false"},
                    {"2", "01-1", "1", "false"},
                    {"2", "01-1", "0", "false"},
                    {"3", "01-1", "1", "false"}
              };

        controlManager.setData(data);
        controlManager.controlRepeatedValues();

        String[][] quarantine = controlManager.getQuarantine();

        assertEquals(3, quarantine.length);

        assertEquals("2", quarantine[1][0]);
        assertEquals("01-1", quarantine[1][1]);
        assertEquals("Doublon de l'id Axe et du code Poche dans le fichier", quarantine[1][3]);

        assertEquals("2", quarantine[2][0]);
        assertEquals("01-1", quarantine[2][1]);
        assertEquals("Doublon de l'id Axe et du code Poche dans le fichier", quarantine[2][3]);
    }


    public void test_columnLength() throws Exception {
        String[][] data =
              new String[][]{
                    {"CLASSIFICATION_ID", "SLEEVE_CODE", "SLEEVE_NAME", "IS_QUARANTINE"},
                    {"1", "01-1", "blabla", "false"},
                    {"2", "C'est l'histoire de Toto qui part en vacances et qui se la coule douce",
                     "Un nom", "false"},
                    {"3", "01-1", "Cette fois c'est l'histoire de Titi qui va rejoindre Toto en vacances",
                     "false"}
              };

        controlManager.setData(data);
        controlManager.controlMaxLength();

        String[][] quarantine = controlManager.getQuarantine();

        assertEquals(3, quarantine.length);

        assertEquals("2", quarantine[1][0]);
        assertEquals("Le code poche est trop long", quarantine[1][3]);

        assertEquals("3", quarantine[2][0]);
        assertEquals("Le libellé de la poche est trop long", quarantine[2][3]);
    }


    public void test_classificationExistsInDb() throws Exception {
        fixture.insertInputInDb("classificationExistsInDb");

        String[][] data =
              new String[][]{
                    {"CLASSIFICATION_ID", "SLEEVE_CODE", "SLEEVE_NAME", "IS_QUARANTINE"},
                    {"1", "01-1", "Poche1", "false"},
                    {"2", "01-1", "Poche2", "false"},
                    {"3", "01-1", "Poche3", "false"}
              };

        controlManager.setData(data);
        controlManager.controlParentExistsInDb();

        String[][] quarantine = controlManager.getQuarantine();

        assertEquals(2, quarantine.length);

        assertEquals("2", quarantine[1][0]);
        assertEquals("Cet axe n'existe pas en base", quarantine[1][3]);
    }


    public void test_noOtherSleeveInDb() throws Exception {
        fixture.insertInputInDb("noOtherSleeveInDb");

        String[][] data =
              new String[][]{
                    {"CLASSIFICATION_ID", "SLEEVE_CODE", "IS_QUARANTINE"},
                    {"1", "01-1", "false"},
                    {"1", "01-2", "false"},
                    {"2", "01-1", "false"},
                    {"3", "01-1", "false"}
              };

        controlManager.setData(data);
        controlManager.controlNoSleevesInDb();

        String[][] quarantine = controlManager.getQuarantine();

        assertEquals(3, quarantine.length);

        assertEquals("1", quarantine[1][0]);
        assertEquals("01-1", quarantine[1][1]);
        assertEquals("Cet axe existe déjà en base avec des poches", quarantine[1][2]);

        assertEquals("1", quarantine[2][0]);
        assertEquals("01-2", quarantine[2][1]);
        assertEquals("Cet axe existe déjà en base avec des poches", quarantine[2][2]);
    }


    public void test_sleeveCodeFormat() throws Exception {
        String[][] data =
              new String[][]{
                    {"CLASSIFICATION_ID", "SLEEVE_CODE", "SLEEVE_NAME", "IS_QUARANTINE"},
                    {"1", "01-1", "Poche 1", "false"},
                    {"1", "01-2", "Poche 2", "false"},
                    {"1", "01-3", "Poche 3", "false"},
                    {"1", "01-3-0", "Poche 4 KO", "false"},
                    {"1", "01-4", "Fourre-tout", "false"},
                    {"1", "02-3.1", "Poche 5", "false"},
                    {"1", "02-3.2", "Poche 6", "false"},
                    {"1", "02-3.2.2", "Poche 7 KO", "false"},
                    {"1", "909-3.2.2", "Poche 8 KO", "false"},
                    {"1", "9093.2.2", "Poche 10 KO", "false"},
                    {"1", "9", "Poche 11 KO", "false"},
                    {"1", "04-3.1.2.1", "Poche 12", "false"},
                    {"1", "04-3.1.2.1.6", "Poche 13 KO", "false"},
                    {"1", "03-3.2.1", "Poche 14", "false"}};

        controlManager.setData(data);
        controlManager.controlSleeveCodeFormat();

        String[][] quarantine = controlManager.getQuarantine();

        assertEquals(7, quarantine.length);

        assertEquals("Poche 4 KO", quarantine[1][2]);
        assertEquals("Le code poche est incorrect", quarantine[1][3]);

        assertEquals("Poche 7 KO", quarantine[2][2]);
        assertEquals("Le code poche est incorrect", quarantine[2][3]);

        assertEquals("Poche 8 KO", quarantine[3][2]);
        assertEquals("Le code poche est incorrect", quarantine[3][3]);

        assertEquals("Poche 10 KO", quarantine[4][2]);
        assertEquals("Le code poche est incorrect", quarantine[4][3]);

        assertEquals("Poche 11 KO", quarantine[5][2]);
        assertEquals("Le code poche est incorrect", quarantine[5][3]);

        assertEquals("Poche 13 KO", quarantine[6][2]);
        assertEquals("Le code poche est incorrect", quarantine[6][3]);
    }


    public void test_oneDustBin() throws Exception {
        String[][] data =
              new String[][]{
                    {"CLASSIFICATION_ID", "SLEEVE_CODE", "SLEEVE_NAME", "SLEEVE_DUSTBIN", "IS_QUARANTINE"},
                    {"1", "01-1", "Poche 1", "0", "false"},
                    {"1", "01-2", "Poche 2", "0", "false"},
                    {"1", "01-3", "Poche 3", "0", "false"},
                    {"1", "01-4", "Fourre-tout", "1", "false"},
                    {"2", "02-3.1", "Poche 5", "0", "false"},
                    {"2", "02-3.2", "Fourre-tout2", "1", "false"},
                    {"2", "04-3.1.2.1", "Fourre-tout3", "1", "false"},
                    {"3", "03-3.2.1", "Poche 14", "0", "false"},
                    {"3", "03-3.2.2", "Poche 14", "0", "false"},
                    {"3", "03-3.2.3", "Poche 14", "0", "false"}};

        controlManager.setData(data);
        controlManager.controlOneDustbin();

        String[][] quarantine = controlManager.getQuarantine();

        assertEquals(7, quarantine.length);

        assertEquals("2", quarantine[1][0]);
        assertEquals("02-3.1", quarantine[1][1]);
        assertEquals("Cet axe contient plus d'une poche fourre-tout", quarantine[1][4]);

        assertEquals("2", quarantine[2][0]);
        assertEquals("02-3.2", quarantine[2][1]);
        assertEquals("Cet axe contient plus d'une poche fourre-tout", quarantine[2][4]);

        assertEquals("2", quarantine[3][0]);
        assertEquals("04-3.1.2.1", quarantine[3][1]);
        assertEquals("Cet axe contient plus d'une poche fourre-tout", quarantine[3][4]);

        assertEquals("3", quarantine[4][0]);
        assertEquals("03-3.2.1", quarantine[4][1]);
        assertEquals("Cet axe ne comporte pas de poche fourre-tout", quarantine[4][4]);

        assertEquals("3", quarantine[5][0]);
        assertEquals("03-3.2.2", quarantine[5][1]);
        assertEquals("Cet axe ne comporte pas de poche fourre-tout", quarantine[5][4]);

        assertEquals("3", quarantine[6][0]);
        assertEquals("03-3.2.3", quarantine[6][1]);
        assertEquals("Cet axe ne comporte pas de poche fourre-tout", quarantine[6][4]);
    }


    public void test_formula_ok() throws Exception {
        String[][] data = new String[][]{
              {"CLASSIFICATION_ID", "SLEEVE_CODE", "SLEEVE_NAME", "SLEEVE_DUSTBIN", "TERMINAL_ELEMENT",
               "FORMULA", "IS_QUARANTINE"},
              {"1", "01-1", "Poche 1", "0", "1", "in('a', 'b', 'c')", "false"},
              {"1", "01-2", "Poche 2", "0", "1", "mon num == 2 ", "false"},
              {"1", "01-3", "Poche 3", "0", "1", "Date d'agrément > '30/09/2007'", "false"},
              {"1", "01-4", "Fourre-tout", "1", "1", "", "false"},
              {"2", "02-3.1", "Formule Incorrecte", "0", "1", "(ma clé < 8", "false"},
              {"2", "02-3.2", "Fourre-tout2", "1", "1", "", "false"}};

        controlManager.setData(data);
        controlManager.controlFormula();

        String[][] quarantine = controlManager.getQuarantine();

        assertEquals(2, quarantine.length);

        assertEquals("2", quarantine[1][0]);
        assertEquals("02-3.1", quarantine[1][1]);
        assertEquals("La formule de la poche est incorrecte", quarantine[1][6]);
    }


    public void test_formula_forbiddenWhenDustbin() throws Exception {
        String[][] data = new String[][]{
              {"CLASSIFICATION_ID", "SLEEVE_CODE", "SLEEVE_NAME", "SLEEVE_DUSTBIN", "TERMINAL_ELEMENT",
               "FORMULA", "IS_QUARANTINE"},
              {"2", "01-1", "Poche ok", "0", "1", "ma clé < 8", "false"},
              {"2", "01-2", "Fourre-tout2", "1", "1", "ma clé > 8", "false"}};

        controlManager.setData(data);
        controlManager.controlFormula();

        String[][] quarantine = controlManager.getQuarantine();

        assertEquals(2, quarantine.length);

        assertEquals("2", quarantine[1][0]);
        assertEquals("01-2", quarantine[1][1]);
        assertEquals("La poche fourre-tout ne peut pas contenir de formules", quarantine[1][6]);
    }


    public void test_formula_notNullIfTerminalElement() throws Exception {
        String[][] data = new String[][]{
              {"CLASSIFICATION_ID", "SLEEVE_CODE", "SLEEVE_NAME", "SLEEVE_DUSTBIN", "TERMINAL_ELEMENT",
               "FORMULA", "IS_QUARANTINE"},
              {"1", "01-1", "Poche 1", "0", "1", "mon num == 2", "false"},
              {"1", "01-2", "Poche 2", "0", "1", null, "false"}};

        controlManager.setData(data);
        controlManager.controlFormula();

        String[][] quarantine = controlManager.getQuarantine();

        assertEquals(2, quarantine.length);

        assertEquals("1", quarantine[1][0]);
        assertEquals("01-2", quarantine[1][1]);
        assertEquals("Cette poche ne comporte pas de formules", quarantine[1][6]);
    }


    public void test_formula_noFormulaIfNotIfTerminalElement() throws Exception {
        String[][] data = new String[][]{
              {"CLASSIFICATION_ID", "SLEEVE_CODE", "SLEEVE_NAME", "SLEEVE_DUSTBIN", "TERMINAL_ELEMENT",
               "FORMULA", "IS_QUARANTINE"},
              {"1", "01-1", "Poche 1", "0", "1", "mon num == 2", "false"},
              {"1", "01-2", "Poche 2", "0", "0", "mon num == 3", "false"}};

        controlManager.setData(data);
        controlManager.controlFormula();

        String[][] quarantine = controlManager.getQuarantine();

        assertEquals(2, quarantine.length);

        assertEquals("1", quarantine[1][0]);
        assertEquals("01-2", quarantine[1][1]);
        assertEquals("Un noeud ne peut pas avoir de formule", quarantine[1][6]);
    }


    public void test_flagOtherSleevesWhenAxeInError() throws Exception {
        fixture.insertInputInDb("flagOtherSleevesWhenAxeInError");
        String[][] data = new String[][]{
              {"CLASSIFICATION_ID", "SLEEVE_CODE", "SLEEVE_NAME", "SLEEVE_DUSTBIN", "TERMINAL_ELEMENT",
               "FORMULA", "IS_QUARANTINE"},
              {"1", "01-1", "Un libelle trop longue qui va faire passer en quarantine les autres poches", "0",
               "0", "", "false"},
              {"1", "01-2", "Poche 2", "0", "0", null, "false"},
              {"1", "01-3", "Poche 3", "0", "0", null, "false"},
              {"1", "01-4", "Fourre-tout", "1", "1", "", "false"},
              {"2", "02-3.1", "Poche 5", "0", "1", "ma clé < 8", "false"},
              {"2", "02-3.2", "Fourre-tout", "1", "1", "", "false"}};

        controlManager.setData(data);
        controlManager.control();

        String[][] quarantine = controlManager.getQuarantine();

        assertEquals(5, quarantine.length);

        assertEquals("1", quarantine[1][0]);
        assertEquals("01-1", quarantine[1][1]);
        assertEquals("Le libellé de la poche est trop long", quarantine[1][6]);

        assertEquals("1", quarantine[2][0]);
        assertEquals("01-2", quarantine[2][1]);
        assertEquals("Cet axe comporte une autre poche en erreur", quarantine[2][6]);

        assertEquals("1", quarantine[3][0]);
        assertEquals("01-3", quarantine[3][1]);
        assertEquals("Cet axe comporte une autre poche en erreur", quarantine[3][6]);

        assertEquals("1", quarantine[4][0]);
        assertEquals("01-4", quarantine[4][1]);
        assertEquals("Cet axe comporte une autre poche en erreur", quarantine[4][6]);
    }


    @Override
    protected void setUp() throws Exception {
        fixture.doSetUp();
        controlManager = new ClassificationStructureControlManager();
        controlManager.setConnection(fixture.getConnection());
    }


    @Override
    protected void tearDown() throws Exception {
        fixture.doTearDown();
    }
}
