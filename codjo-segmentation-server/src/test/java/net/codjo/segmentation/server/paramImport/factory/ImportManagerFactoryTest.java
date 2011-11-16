package net.codjo.segmentation.server.paramImport.factory;
import net.codjo.segmentation.server.paramImport.AbstractControlManager;
import net.codjo.segmentation.server.paramImport.AbstractParserManager;
import net.codjo.segmentation.server.paramImport.AbstractDispatchManager;
import net.codjo.segmentation.server.paramImport.classification.ClassificationControlManager;
import net.codjo.segmentation.server.paramImport.classification.ClassificationDispatchManager;
import net.codjo.segmentation.server.paramImport.classification.ClassificationParserManager;
import net.codjo.segmentation.server.paramImport.classificationStructure.ClassificationStructureControlManager;
import net.codjo.segmentation.server.paramImport.classificationStructure.ClassificationStructureDispatchManager;
import net.codjo.segmentation.server.paramImport.classificationStructure.ClassificationStructureParserManager;
import static net.codjo.segmentation.server.paramImport.factory.ImportManagerFactory.CLASSIFICATION_STRUCTURE_TYPE;
import static net.codjo.segmentation.server.paramImport.factory.ImportManagerFactory.CLASSIFICATION_TYPE;
import junit.framework.TestCase;
/**
 *
 */
public class ImportManagerFactoryTest extends TestCase {

    private static final String IMPORT_CONTENTS
          = ";AXE_ID\tLABEL_AXE\tFAMILY_AXE\n2\tAxe event 2\tEVENT\n3\tAxe event 3\tEVENT";

    private String classificationRawdata = CLASSIFICATION_TYPE + IMPORT_CONTENTS;

    private String classSructureRawdata = CLASSIFICATION_STRUCTURE_TYPE + IMPORT_CONTENTS;


    public void test_createDataParserManager() throws Exception {
        AbstractParserManager manager = ImportManagerFactory.createDataParserManager(
              classificationRawdata);
        assertEquals(ClassificationParserManager.class, manager.getClass());

        manager = ImportManagerFactory.createDataParserManager(classSructureRawdata);
        assertEquals(ClassificationStructureParserManager.class, manager.getClass());
    }


    public void test_createControlManager() throws Exception {
        AbstractControlManager manager = ImportManagerFactory.createControlManager(classificationRawdata);
        assertEquals(ClassificationControlManager.class, manager.getClass());

        manager = ImportManagerFactory.createControlManager(classSructureRawdata);
        assertEquals(ClassificationStructureControlManager.class, manager.getClass());
    }


    public void test_createDispatchManager() throws Exception {
        AbstractDispatchManager manager = ImportManagerFactory.createDispatchManager(classificationRawdata);
        assertEquals(ClassificationDispatchManager.class, manager.getClass());

        manager = ImportManagerFactory.createDispatchManager(classSructureRawdata);
        assertEquals(ClassificationStructureDispatchManager.class, manager.getClass());
    }
}
