package net.codjo.segmentation.server.blackboard.message;
import net.codjo.agent.AclMessage;
import net.codjo.test.common.LogString;
import net.codjo.test.common.XmlUtil;
import junit.framework.TestCase;
/**
 *
 */
public class MessageCodecTest extends TestCase {
    private static final String ONE_WRITE = "<write>"
                                            + "  <todo id='-1'>"
                                            + "     <content class='string'>1</content>"
                                            + "  </todo>"
                                            + "  <level name='level-a'/>"
                                            + "</write>";
    private static final String WRITE_AND_WRITE = "<actions>"
                                                  + "  <write>"
                                                  + "    <todo id='-1'>"
                                                  + "      <content class='string'>1</content>"
                                                  + "    </todo>"
                                                  + "    <level name='level-a'/>"
                                                  + "  </write>"
                                                  + "  <write>"
                                                  + "    <todo id='-1'>"
                                                  + "      <content class='string'>2</content>"
                                                  + "    </todo>"
                                                  + "    <level class='nextLevel'>"
                                                  + "        <current name='level-a'/>"
                                                  + "    </level>"
                                                  + "  </write>"
                                                  + "</actions>";
    private LogString log = new LogString();


    public void test_toXml_write() throws Exception {
        XmlUtil.assertEquals(ONE_WRITE,
                             new MessageCodec().toXml(createOneWrite()));
    }


    public void test_toXml_writeThenWrite() throws Exception {
        BlackboardActionBuilder builder = new BlackboardActionBuilder(true);
        Write last = builder
              .write(new Todo<String>("1"), new Level("level-a"))
              .then()
              .write(new Todo<String>("2"), builder.nextLevel(new Level("level-a")));

        XmlUtil.assertEquals(WRITE_AND_WRITE, new MessageCodec().toXml(last));
    }


    public void test_fromXml() throws Exception {
        BlackboardAction action = new MessageCodec().fromXml(ONE_WRITE);
        action.acceptVisitor(new BlackboardActionVisitorMock(log));
        log.assertContent("visit(write(todo:1))");
    }


    public void test_fromXml_writeThenWrite() throws Exception {
        BlackboardAction action = new MessageCodec().fromXml(WRITE_AND_WRITE);
        action.acceptVisitor(new BlackboardActionVisitorMock(log));
        log.assertContent("visit(write(todo:1)), visit(write(todo:2))");
    }


    public void test_encodeDecode() throws Exception {
        MessageCodec messageCodec = new MessageCodec();
        AclMessage aclMessage = new AclMessage(AclMessage.Performative.REQUEST);

        messageCodec.encode(aclMessage, createOneWrite());
        assertEquals("xml", aclMessage.getLanguage());
        BlackboardAction actual = messageCodec.decode(aclMessage);

        actual.acceptVisitor(new BlackboardActionVisitorMock(log));
        log.assertContent("visit(write(todo:1))");
    }


    public void test_encodeDecode_read() throws Exception {
        MessageCodec messageCodec = new MessageCodec();

        AclMessage inform = new AclMessage(AclMessage.Performative.INFORM);

        messageCodec.encodeRead(inform, new Read(new Level("a"), new Todo(1)));
        assertEquals("xml", inform.getLanguage());
        Read actual = messageCodec.decodeRead(inform);

        assertEquals("a", actual.getLevel().getName());
        assertEquals(1, actual.getTodo().getId());
    }


    private Write createOneWrite() {
        return new Write(new Todo<String>("1"), new Level("level-a"));
    }
}
