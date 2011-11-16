package net.codjo.segmentation.server.plugin;
import net.codjo.mad.server.handler.HandlerCommand;
import net.codjo.mad.server.handler.HandlerException;
import java.sql.SQLException;
/**
 *
 */
public class GetSegmentationConfigCommand extends HandlerCommand {
    @Override
    public CommandResult executeQuery(CommandQuery query) throws HandlerException, SQLException {
        try {
            return createResult(new PreferenceGuiHome().buildResponse());
        }
        catch (Exception e) {
            throw new HandlerException("Impossible de charger la configuration de segmentation", e);
        }
    }
}
