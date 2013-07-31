package net.codjo.segmentation.gui.plugin;
import java.io.StringReader;
import java.rmi.RemoteException;
import junit.framework.TestCase;
import net.codjo.mad.client.plugin.MadConnectionPluginMock;
import net.codjo.mad.client.request.RequestIdManager;
import net.codjo.mad.client.request.util.ServerWrapper;
import net.codjo.mad.client.request.util.ServerWrapperFactory;
import net.codjo.mad.gui.base.DefaultGuiConfiguration;
import net.codjo.mad.gui.base.GuiConfiguration;
import net.codjo.mad.gui.framework.LocalGuiContext;
import net.codjo.mad.gui.plugin.MadGuiPlugin;
import net.codjo.mad.gui.request.Preference;
import net.codjo.mad.gui.request.PreferenceFactory;
import net.codjo.plugin.common.ApplicationCore;
import net.codjo.security.common.api.User;
import net.codjo.segmentation.gui.SegmentationGuiContext;
import net.codjo.workflow.gui.plugin.WorkflowGuiPlugin;
import org.mockito.Mockito;
import org.picocontainer.MutablePicoContainer;
import org.xml.sax.InputSource;

import static net.codjo.mad.gui.request.PreferenceFactory.getPreference;
/**
 *
 */
public class SegmentationGuiPluginTest extends TestCase {
    static final String PREFERENCE = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
                                     + "<preferenceList xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                                     + "                xsi:noNamespaceSchemaLocation=\"http://preference.xsd\">\n"
                                     + "    <preference id=\"ClassificationWindow\"\n"
                                     + "                detailWindowClassName=\"net.codjo.segmentation.gui.settings.ClassificationStructureLogic\">\n"
                                     + "        <selectAll>selectAllClassification</selectAll>\n"
                                     + "        <column fieldName=\"id\" label=\"Secret Id\" preferredSize=\"20\"/>\n"
                                     + "        <hidden>\n"
                                     + "            <column fieldName=\"hiddenColumn\" label=\"Hidden Column\" preferredSize=\"2000\"/>\n"
                                     + "        </hidden>\n"
                                     + "    </preference>\n"
                                     + "    <preference id=\"ClassificationWizardWindow\"\n"
                                     + "                detailWindowClassName=\"net.codjo.segmentation.gui.settings.ClassificationStructureLogic\">\n"
                                     + "        <selectAll>selectAllClassification</selectAll>\n"
                                     + "        <column fieldName=\"id\" label=\"Secret Id\" preferredSize=\"20\"/>\n"
                                     + "        <hidden>\n"
                                     + "            <column fieldName=\"hiddenColumn\" label=\"Hidden Column\" preferredSize=\"2000\"/>\n"
                                     + "        </hidden>\n"
                                     + "    </preference>\n"
                                     + "    <preference id=\"ClassificationStructureWindow\"\n"
                                     + "                detailWindowClassName=\"net.codjo.segmentation.gui.settings.ClassificationStructureLogic\">\n"
                                     + "        <selectAll>selectAllClassification</selectAll>\n"
                                     + "        <column fieldName=\"id\" label=\"Secret Id\" preferredSize=\"20\"/>\n"
                                     + "        <hidden>\n"
                                     + "            <column fieldName=\"hiddenColumn\" label=\"Hidden Column\" preferredSize=\"2000\"/>\n"
                                     + "        </hidden>\n"
                                     + "    </preference>\n"
                                     + "</preferenceList>";
    private MadGuiPlugin madGuiPlugin;
    private GuiConfiguration guiConfiguration;
    private SegmentationGuiContext guiContext;
    private SegmentationGuiPlugin segmentationGuiPlugin;


    @Override
    public void setUp() throws Exception {
        RequestIdManager.getInstance().reset();
        PreferenceFactory.initFactory();
        PreferenceFactory.loadMapping(new InputSource(new StringReader(PREFERENCE)));

        initServerWrapperMock();
        initGuiContextMock();
        initGuiConfigurationMock();
        initMadGuiPluginMock();
        segmentationGuiPlugin = new SegmentationGuiPlugin(Mockito.mock(ApplicationCore.class),
                                                          madGuiPlugin,
                                                          new WorkflowGuiPlugin());
    }


    @Override
    public void tearDown() {
        RequestIdManager.getInstance().reset();
        PreferenceFactory.clearPreferences();
    }


    public void testPreferenceCustomization() throws Exception {

        segmentationGuiPlugin.getConfiguration()
              .setClassificationPreferenceId("MySpecialClassificationWindow");
        segmentationGuiPlugin.getConfiguration()
              .setClassificationAnomalyPreferenceId("MyAnomalyPreferenceId");
        segmentationGuiPlugin.initGui(guiConfiguration);

        Preference preference = PreferenceFactory.getPreference("ClassificationWindow");

        assertEquals(
              "The lib preference must be extended with the column coming from the application preference",
              2,
              preference.getColumns().size());
        assertEquals(
              "The lib preference must be extended with the hidden column coming from the application preference",
              2,
              preference.getHiddenColumns().size());

        assertEquals("id", preference.getColumns().get(0).getFieldName());
        assertEquals("myId", preference.getColumns().get(1).getFieldName());
        assertEquals("hiddenColumn", preference.getHiddenColumns().get(0).getFieldName());
        assertEquals("myHiddenColumn", preference.getHiddenColumns().get(1).getFieldName());
    }


    private void initGuiContextMock() {
        User user = Mockito.mock(User.class);
        Mockito.when(user.isAllowedTo(Mockito.anyString())).thenReturn(false);
        guiContext = new SegmentationGuiContext();
        guiContext.setUser(user);
    }


    private void initGuiConfigurationMock() throws Exception {
        guiConfiguration = new DefaultGuiConfiguration(
              Mockito.mock(MutablePicoContainer.class),
              guiContext,
              Mockito.mock(LocalGuiContext.class));
    }


    private void initMadGuiPluginMock() throws Exception {
        madGuiPlugin = new MadGuiPlugin(new MadConnectionPluginMock());
        madGuiPlugin.initGui(guiConfiguration);
    }


    private void initServerWrapperMock() throws RemoteException {
        ServerWrapper serverWrapper = Mockito.mock(ServerWrapper.class);
        ServerWrapperFactory.setPrototype(serverWrapper);
        Mockito.when(serverWrapper.copy()).thenReturn(serverWrapper);
        Mockito.when(serverWrapper.sendWaitResponse(Mockito.anyString(), Mockito.anyLong()))
              .thenReturn(
                    "<?xml version=\"1.0\"?><results><result request_id='1'><row><field name=\"result\"><![CDATA[<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"
                    + "<structure></structure>]]></field></row></result></results>");
    }
}
