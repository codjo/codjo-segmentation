package net.codjo.segmentation.gui.plugin;
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
import net.codjo.workflow.gui.plugin.WorkflowGuiPlugin;
import net.codjo.segmentation.gui.SegmentationGuiContext;
import java.rmi.RemoteException;
import junit.framework.TestCase;
import org.mockito.Mockito;
import org.picocontainer.MutablePicoContainer;
/**
 *
 */
public class SegmentationGuiPluginTest extends TestCase {
    private MadGuiPlugin madGuiPlugin;
    private GuiConfiguration guiConfiguration;
    private SegmentationGuiContext guiContext;
    private SegmentationGuiPlugin segmentationGuiPlugin;


    @Override
    public void setUp() throws Exception {
        RequestIdManager.getInstance().reset();
        PreferenceFactory.initFactory();
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
