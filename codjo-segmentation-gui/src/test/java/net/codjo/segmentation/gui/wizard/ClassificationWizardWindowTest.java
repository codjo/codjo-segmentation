package net.codjo.segmentation.gui.wizard;
import net.codjo.mad.client.request.util.ServerWrapper;
import net.codjo.mad.client.request.util.ServerWrapperFactory;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.mad.gui.request.PreferenceFactory;
import java.io.IOException;
import java.io.InputStream;
import junit.framework.TestCase;
import org.mockito.Mockito;
import org.xml.sax.InputSource;
/**
 *
 */
public class ClassificationWizardWindowTest extends TestCase {
    public void testGetter() throws IOException {

        PreferenceFactory.initFactory();
        InputStream stream = ClassificationWizardWindowTest.class.getResourceAsStream("preference.xml");
        PreferenceFactory.addMapping(new InputSource(stream));

        GuiContext context = Mockito.mock(GuiContext.class);
        AnomalyLogWindowCustomizer windowCustomizer = Mockito.mock(AnomalyLogWindowCustomizer.class);

        ServerWrapper serverWrapper = Mockito.mock(ServerWrapper.class);
        ServerWrapperFactory.setPrototype(serverWrapper);
        Mockito.stub(serverWrapper.copy()).toReturn(serverWrapper);
        Mockito.stub(serverWrapper.sendWaitResponse(Mockito.anyString(), Mockito.anyLong())).toReturn("<?xml version=\"1.0\"?><results><result request_id=\"1\"  totalRowCount=\"0\" ><primarykey><field name=\"classificationId\"/></primarykey></result></results>");

        ClassificationWizardWindow window = new ClassificationWizardWindow(context,
                                                                           "",
                                                                           "MySpecialClassificationWindow",
                                                                           null,
                                                                           windowCustomizer,
                                                                           null);
        assertNotNull(window.getMainPanel());
        assertNotNull(window.getClassificationTable());
        assertEquals(window.getClassificationTable().getPreference().getId(), "MySpecialClassificationWindow");
    }
}
