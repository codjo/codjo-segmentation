package net.codjo.segmentation.server.blackboard.message;
import junit.framework.TestCase;
/**
 *
 */
public class LevelTest extends TestCase {
    public void test_equalsHashcode() throws Exception {
        Level level = new Level("foo");
        assertTrue(level.equals(new Level("foo")));
        assertEquals(level.hashCode(), new Level("foo").hashCode());

        assertFalse(level.equals(new Level("foot")));
        assertFalse(level.equals(new Level("xx")));
        assertFalse(level.equals(new Level(null)));

        //noinspection ObjectEqualsNull
        assertFalse(level.equals(null));
        assertFalse(level.equals(new Object()));
    }
}
