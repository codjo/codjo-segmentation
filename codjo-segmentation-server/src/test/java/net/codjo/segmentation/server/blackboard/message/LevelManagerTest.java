package net.codjo.segmentation.server.blackboard.message;
import junit.framework.TestCase;
/**
 *
 */
public class LevelManagerTest extends TestCase {
    public void test_findLevelIndex() throws Exception {
        LevelManager manager = new LevelManager(new Level("a"), new Level("b"), new Level("c"));

        assertEquals(0, manager.indexOf(new Level("a")));
        assertEquals(-1, manager.indexOf(new Level("unnkow")));
    }


    public void test_findNextLevelIndex() throws Exception {
        LevelManager manager = new LevelManager(new Level("a"), new Level("b"), new Level("c"));

        assertEquals(1, manager.indexOf(new NextLevel(new Level("a"))));
        assertEquals(-1, manager.indexOf(new NextLevel(new Level("c"))));

        assertEquals(2, manager.indexOf(new NextLevel(new NextLevel(new Level("a")))));
        assertEquals(-1, manager.indexOf(new NextLevel(new NextLevel(new Level("b")))));
    }


    public void test_getLevel() throws Exception {
        LevelManager manager = new LevelManager(new Level("a"), new Level("b"), new Level("c"));

        assertEquals(new Level("a"), manager.getLevel(0));
        assertEquals(new Level("c"), manager.getLevel(2));
        assertNull(manager.getLevel(3));
        assertNull(manager.getLevel(-1));
    }
}
