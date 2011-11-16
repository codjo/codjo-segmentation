package net.codjo.segmentation.server.blackboard.message;
import net.codjo.agent.AclMessage;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
/**
 *
 */
public class MessageCodec {
    XStream xstream = new XStream(new DomDriver());


    public MessageCodec() {
        xstream.alias("read", Read.class);
        xstream.alias("erase", Erase.class);
        xstream.alias("getTodo", GetTodo.class);
        xstream.alias("write", Write.class);
        xstream.alias("nextLevel", NextLevel.class);
        xstream.alias("actions", BlackboardActionBuilder.class);
        xstream.addImplicitCollection(BlackboardActionBuilder.class, "actions");
        xstream.useAttributeFor("id", long.class);
        xstream.useAttributeFor("name", String.class);
    }


    String toXml(BlackboardAction action) {
        if (action.hasBlackBoardActionBuilder()) {
            return xstream.toXML(action.then());
        }
        return xstream.toXML(action);
    }


    BlackboardAction fromXml(String xml) {
        Object result = xstream.fromXML(xml);
        if (result instanceof BlackboardActionBuilder) {
            return wrapp((BlackboardActionBuilder)result);
        }
        return (BlackboardAction)result;
    }


    private BlackboardAction wrapp(final BlackboardActionBuilder builder) {
        return new BlackboardAction() {
            @Override
            public void acceptVisitor(BlackboardActionVisitor visitor) {
                builder.visit(visitor);
            }
        };
    }


    public void encode(AclMessage aclMessage, BlackboardAction action) {
        aclMessage.setLanguage("xml");
        aclMessage.setContent(toXml(action));
    }


    public BlackboardAction decode(AclMessage aclMessage) {
        return fromXml(aclMessage.getContent());
    }


    public void encodeRead(AclMessage inform, Read read) {
        inform.setLanguage("xml");
        inform.setContent(xstream.toXML(read));
    }


    public Read decodeRead(AclMessage inform) {
        return (Read)xstream.fromXML(inform.getContent());
    }
}
