package net.codjo.segmentation.gui.importParam;
import net.codjo.mad.gui.framework.AbstractAction;
import net.codjo.mad.gui.framework.GuiContext;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.UserId;
import javax.swing.JInternalFrame;
/**
 *
 */
public class ImportParametersAction extends AbstractAction {
    private final AgentContainer agentContainer;
    private final UserId userId;


    public ImportParametersAction(GuiContext ctxt, AgentContainer agentContainer, UserId userId) {
        super(ctxt, "", "");
        this.agentContainer = agentContainer;
        this.userId = userId;
    }


    @Override
    protected JInternalFrame buildFrame(GuiContext context) throws Exception {
        return new ImportParametersLogic(context, agentContainer, userId).getGui();
    }
}
