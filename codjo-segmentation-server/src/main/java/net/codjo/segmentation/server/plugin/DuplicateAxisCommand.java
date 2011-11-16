package net.codjo.segmentation.server.plugin;
import net.codjo.mad.server.handler.HandlerCommand;
import net.codjo.mad.server.handler.HandlerException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DuplicateAxisCommand extends HandlerCommand {
    private static final String SQL_REQUEST = "sp_SEG_Copy_AxisClassification @CLASSIFICATION_ID = ?";


    @Override
    public CommandResult executeQuery(CommandQuery query) throws HandlerException, SQLException {
        Connection connection = getContext().getConnection();
        PreparedStatement statement = connection.prepareStatement(SQL_REQUEST);
        statement.setInt(1, query.getArgumentInteger("classificationId"));
        statement.execute();
        return HandlerCommand.createEmptyResult();
    }
}
