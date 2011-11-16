package net.codjo.segmentation.server.plugin;
import net.codjo.agent.AclMessage;
import net.codjo.agent.AclMessage.Performative;
import net.codjo.agent.Agent;
import net.codjo.agent.AgentContainer;
import net.codjo.agent.UserId;
import net.codjo.aspect.Aspect;
import net.codjo.aspect.AspectContext;
import net.codjo.aspect.AspectException;
import net.codjo.aspect.AspectFilter;
import net.codjo.aspect.AspectHelper;
import net.codjo.aspect.AspectManager;
import net.codjo.aspect.JoinPoint;
import net.codjo.segmentation.server.participant.SegmentationParticipant;
import net.codjo.sql.server.JdbcServiceUtilMock;
import net.codjo.test.common.LogString;
import net.codjo.workflow.common.message.JobAudit;
import net.codjo.workflow.common.message.JobRequest;
import org.junit.Before;
import org.junit.Test;

public class SegmentationAspectParticipantTest {
    private static final LogString LOG = new LogString();
    private static final Agent FAKE_AGENT = new FakeAgent();
    private SegmentationAspectParticipant participant;


    @Before
    public void setUp() throws Exception {
        LOG.clear();
        participant = new SegmentationAspectParticipant(new JdbcServiceUtilMock(LOG),
                                                        new AspectManagerMock(LOG)) {
            @Override
            public void sendAudit(JobAudit audit) {
                LOG.call("sendAudit", audit.getType());
            }


            @Override
            public AclMessage getRequestMessage() {
                return new AclMessage(Performative.QUERY) {
                    @Override
                    public UserId decodeUserId() {
                        return UserId.createId("foo", "bar");
                    }
                };
            }


            @Override
            public Agent getAgent() {
                return FAKE_AGENT;
            }
        };
    }


    @Test
    public void test_handlePRE() throws Exception {
        participant.handlePRE(new JobRequest(SegmentationParticipant.SEGMENTATION_SERVICE));

        LOG.assertContent("getConnectionPool(FakeAgent, message:QUERY-REF)",
                          "setUp()",
                          "runBefore()",
                          "sendAudit(PRE)",
                          "runAfter()",
                          "cleanUp()");
    }


    @Test
    public void test_handlePOST() throws Exception {
        participant.handlePOST(new JobRequest(SegmentationParticipant.SEGMENTATION_SERVICE), null);

        LOG.assertContent("getConnectionPool(FakeAgent, message:QUERY-REF)",
                          "setUp()",
                          "runBefore()",
                          "sendAudit(POST)",
                          "runAfter()",
                          "cleanUp()");
    }


    @Test
    public void test_correctJoinPointOK() throws Exception {
        AspectManager aspectManager = new AspectManager();
        JoinPoint pointPre = new JoinPoint();
        pointPre.setCall(JoinPoint.CALL_AFTER);
        pointPre.setPoint("segmentation.pre");
        JoinPoint pointPost = new JoinPoint();
        pointPost.setCall(JoinPoint.CALL_BEFORE);
        pointPost.setPoint("segmentation.post");
        aspectManager.addAspect("test", new JoinPoint[]{pointPre, pointPost}, AspectMock.class);
        participant = new SegmentationAspectParticipant(new JdbcServiceUtilMock(LOG), aspectManager) {
            @Override
            public void sendAudit(JobAudit audit) {
            }


            @Override
            public AclMessage getRequestMessage() {
                return new AclMessage(Performative.QUERY) {
                    @Override
                    public UserId decodeUserId() {
                        return UserId.createId("foo", "bar");
                    }
                };
            }


            @Override
            public Agent getAgent() {
                return FAKE_AGENT;
            }
        };

        JobRequest jobRequest = new JobRequest(SegmentationParticipant.SEGMENTATION_SERVICE);
        participant.handlePRE(jobRequest);
        LOG.assertAndClear("getConnectionPool(FakeAgent, message:QUERY-REF)",
                           "AspectMock.setUp()",
                           "AspectMock.run()",
                           "AspectMock.cleanUp()");

        participant.handlePOST(jobRequest, null);
        LOG.assertContent("getConnectionPool(FakeAgent, message:QUERY-REF)",
                          "AspectMock.setUp()",
                          "AspectMock.run()",
                          "AspectMock.cleanUp()");
    }


    @Test
    public void test_wrongJoinPoint() throws Exception {
        AspectManager aspectManager = new AspectManager();
        JoinPoint joinPoint = new JoinPoint();
        joinPoint.setCall(JoinPoint.CALL_AFTER);
        joinPoint.setPoint("segmentation.wrong");
        aspectManager.addAspect("test", new JoinPoint[]{joinPoint}, AspectMock.class);
        participant = new SegmentationAspectParticipant(new JdbcServiceUtilMock(LOG), aspectManager) {
            @Override
            public void sendAudit(JobAudit audit) {
            }
        };

        participant.handlePRE(new JobRequest(SegmentationParticipant.SEGMENTATION_SERVICE));

        LOG.assertContent("getConnectionPool(null, message:null)");
    }


    private static class AspectManagerMock extends AspectManager {
        private LogString log;


        private AspectManagerMock(LogString log) {
            this.log = log;
        }


        @Override
        public AspectHelper createHelper(String point, String argument, AspectFilter aspectFilter)
              throws AspectException {
            return new AspectHelperMock(log);
        }
    }

    private static class AspectHelperMock implements AspectHelper {
        private LogString log;


        private AspectHelperMock(LogString log) {
            this.log = log;
        }


        public void setUp(AspectContext context) throws AspectException {
            log.call("setUp");
        }


        public void runBefore(AspectContext context) throws AspectException {
            log.call("runBefore");
        }


        public void runAfter(AspectContext context) throws AspectException {
            log.call("runAfter");
        }


        public void runError(AspectContext context) throws AspectException {
            log.call("runError");
        }


        public void cleanUp(AspectContext context) throws AspectException {
            log.call("cleanUp");
        }
    }

    public static class AspectMock implements Aspect {
        private final LogString log;


        public AspectMock() {
            this.log = new LogString(getClass().getSimpleName(), LOG);
        }


        public void setUp(AspectContext context, JoinPoint joinPoint) throws AspectException {
            log.call("setUp");
        }


        public void run(AspectContext context) throws AspectException {
            log.call("run");
        }


        public void cleanUp(AspectContext context) throws AspectException {
            log.call("cleanUp");
        }
    }

    private static class FakeAgent extends Agent {
        @Override
        public AgentContainer getAgentContainer() {
            return null;
        }
    }
}
