package net.codjo.segmentation.releasetest;
import net.codjo.mad.client.plugin.MadConnectionPlugin;
import net.codjo.plugin.batch.BatchCore;
import net.codjo.plugin.common.CommandLineArguments;
import net.codjo.security.client.plugin.SecurityClientPlugin;
import net.codjo.segmentation.batch.plugin.SegmentationBatchPlugin;
/**
 *
 */
public class SegmentationBatchMock {
    private SegmentationBatchMock() {
    }


    public static void main(String[] args) throws Exception {
        CommandLineArguments arguments = new CommandLineArguments(args);
        BatchCore batch = new BatchCore(arguments);
        batch.addPlugin(SecurityClientPlugin.class);
        batch.addPlugin(MadConnectionPlugin.class);
        batch.addPlugin(SegmentationBatchPlugin.class);
        batch.start();
        batch.executeAndExit();
    }
}
