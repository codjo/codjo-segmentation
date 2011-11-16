/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.settings;
import junit.framework.TestCase;
/**
 * Classe de test de {@link SleeveCodeComparator}.
 */
public class SleeveCodeComparatorTest extends TestCase {
    private SleeveCodeComparator comparator = new SleeveCodeComparator();

    public void test_compare() {
        assertTrue(comparator.compare("01-1", "01-1") == 0);
        assertTrue(comparator.compare("01-1", "01-2") < 0);
        assertTrue(comparator.compare("01-2", "01-1") > 0);

        assertTrue(comparator.compare("01-1", "02-1-2") < 0);
        assertTrue(comparator.compare("01-2", "02-1-2") < 0);

        assertTrue(comparator.compare("02-1.2", "02-1.3") < 0);
        assertTrue(comparator.compare("02-1.3", "02-1.2") > 0);
        assertTrue(comparator.compare("02-1.3", "02-1.10") < 0);
    }
}
