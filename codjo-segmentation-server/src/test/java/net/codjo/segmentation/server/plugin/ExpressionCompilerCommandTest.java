package net.codjo.segmentation.server.plugin;
import net.codjo.mad.server.handler.HandlerCommand;
import net.codjo.mad.server.handler.HandlerCommandTestCase;
import net.codjo.mad.server.handler.HandlerException;
import net.codjo.segmentation.server.participant.context.ContextManager;
import net.codjo.segmentation.server.preference.family.XmlPreferenceLoader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.xml.sax.InputSource;
/**
 *
 */
public class ExpressionCompilerCommandTest extends HandlerCommandTestCase {
    private ContextManager contextManager;


    @Override
    protected void setUp() throws Exception {
        super.setUp();

        XmlPreferenceLoader preferenceLoader = new XmlPreferenceLoader();
        preferenceLoader.load(new InputSource(getClass().getResourceAsStream(
              "/META-INF/segmentation-configs.xml")));
        contextManager = new ContextManager(preferenceLoader);
    }


    public void test_compile() throws Exception {
        Map<String, String> args = new HashMap<String, String>();
        args.put("expression", "isNull(SRC_SEG_INPUT_EVENT$AMF_APPROVAL_DATE)");
        args.put("familyId", "EVENT");

        assertCompilationOK(args);
    }


    public void test_compile_variables() throws SQLException, HandlerException {
        Map<String, String> args = new HashMap<String, String>();
        args.put("expression", "isNull(SRC_SEG_INPUT_EVENT$AMF_APPROVAL_DATE) && VAR_ANY_TABLE_NAME$COLUMN_NAME == \"555\"");
        args.put("familyId", "EVENT");

        assertCompilationOK(args);
    }


    public void test_compile_nestedExpression() throws Exception {
        Map<String, String> args = new HashMap<String, String>();
        args.put("expression", "isNull(INC_$$1$1111)");
        args.put("familyId", "EVENT");

        assertCompilationOK(args);
    }


    public void test_compile_failure() throws Exception {
        Map<String, String> args = new HashMap<String, String>();
        args.put("expression", "n_importe quoi");
        args.put("familyId", "EVENT");

        assertCompilationFailure(args, "L'expression : \n'" + "n_importe quoi" + "'\n est invalide.");
    }


    public void test_compile_failure_badExpression() throws Exception {
        Map<String, String> args = new HashMap<String, String>();
        args.put("expression", "isNull(SRC_SEG_INPUT_EVENT$AMF_APPROVAL_DATE || INC_$$1$1111)");
        args.put("familyId", "EVENT");

        assertCompilationFailure(args,
                                 "Impossible de compiler : isNull(SRC_SEG_INPUT_EVENT$AMF_APPROVAL_DATE || tototo). Une ou plusieurs de ses expressions comportent des erreurs.");
    }


    public void test_compile_failure_unknownVariable() throws SQLException, HandlerException {
        Map<String, String> args = new HashMap<String, String>();
        args.put("expression", "isNull(INC_$$1$1235)");
        args.put("familyId", "EVENT");

        assertCompilationFailure(args, "La variable \"INC_$$1$1235\" est inconnue ");
    }


    public void test_compile_failure_cycliqueVariable() throws Exception {
        Map<String, String> args = new HashMap<String, String>();
        args.put("expression", "isNull(INC_$$1$3333)");
        args.put("familyId", "EVENT");

        assertCompilationFailure(args, "La variable 'INC_$$1$3333' est une référence cyclique.");
    }


    @Override
    protected HandlerCommand createHandlerCommand() {
        return new ExpressionCompilerCommand(contextManager);
    }


    @Override
    protected String getHandlerId() {
        return "expressionCompiler";
    }


    private void assertCompilationOK(Map<String, String> args) throws SQLException, HandlerException {
        assertEquals(HandlerCommand.createEmptyResult(), assertExecuteQuery("compile", args));
    }


    private void assertCompilationFailure(Map<String, String> args, String expectedMessage)
          throws SQLException, HandlerException {
        try {
            assertExecuteQuery("compile", args);
            fail();
        }
        catch (HandlerException e) {
            assertEquals(expectedMessage, e.getMessage());
        }
    }
}
