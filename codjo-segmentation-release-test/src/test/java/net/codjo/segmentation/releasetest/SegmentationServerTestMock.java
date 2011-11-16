package net.codjo.segmentation.releasetest;
import net.codjo.mad.server.plugin.MadServerPlugin;
import net.codjo.plugin.common.CommandLineArguments;
import net.codjo.plugin.server.AbstractServerPlugin;
import net.codjo.plugin.server.ServerCore;
import net.codjo.security.common.api.User;
import net.codjo.security.common.api.UserMock;
import net.codjo.security.server.plugin.SecurityServerPlugin;
import net.codjo.segmentation.server.plugin.SegmentationServerPlugin;
import net.codjo.sql.server.plugin.JdbcServerPlugin;
import net.codjo.workflow.server.plugin.WorkflowServerPlugin;
public class SegmentationServerTestMock {
    private ServerCore server;


    public SegmentationServerTestMock(String[] arguments) throws Exception {
        server = new ServerCore();

        UserMock userMock = new UserMock();
        server.addGlobalComponent(User.class, userMock.mockIsAllowedTo(true));

        server.addPlugin(JdbcServerPlugin.class);
        server.addPlugin(SecurityServerPlugin.class);
        server.addPlugin(MadServerPlugin.class);
        server.addPlugin(WorkflowServerPlugin.class);
        server.addPlugin(SegmentationServerPlugin.class);
        server.addPlugin(SegmentationServerTestMockPlugin.class);

        String configFile = server.getClass().getResource("/server-config.properties").getFile();
        arguments = new String[]{"-configuration", configFile};
        server.start(new CommandLineArguments(arguments));
    }


    public static class SegmentationServerTestMockPlugin extends AbstractServerPlugin {

        public SegmentationServerTestMockPlugin(SecurityServerPlugin security,
                                                User user,
                                                SegmentationServerPlugin segmentationServerPlugin) {
            security.getConfiguration().setUserFactory(new UserFactoryMock(user));
            segmentationServerPlugin.getConfiguration()
                  .setConfigurationFileUrl(getClass().getResource("/META-INF/segmentation-configs.xml"));
            segmentationServerPlugin.getConfiguration().setMaxAnalyzerAgents(2);
            segmentationServerPlugin.getConfiguration().setMaxDeleteAgents(3);
            segmentationServerPlugin.getConfiguration().setMaxPaginatorAgents(3);
            segmentationServerPlugin.getConfiguration().setMaxCalculatorAgents(12);
        }
    }


    public static void main(String[] arguments) throws Exception {
        new SegmentationServerTestMock(arguments);
    }


    public void stop() throws Exception {
        server.stop();
    }
}
