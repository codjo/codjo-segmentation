/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.settings;
import java.util.Comparator;
import java.util.StringTokenizer;
/**
 * Classe utuilitaire permettant de trier les élément de l'arbre selon leur seelveCode.
 */
public class SleeveCodeComparator implements Comparator {
    public int compare(Object o1, Object o2) {
        String sleeveCode1 = (String)o1;
        String sleeveCode2 = (String)o2;
        int caret1 = sleeveCode1.indexOf("-");
        int caret2 = sleeveCode2.indexOf("-");
        int level1 = Integer.parseInt(sleeveCode1.substring(0, caret1));
        int level2 = Integer.parseInt(sleeveCode2.substring(0, caret2));
        if (level1 == level2) {
            String path1 = sleeveCode1.substring(caret1 + 1);
            String path2 = sleeveCode2.substring(caret2 + 1);
            StringTokenizer stknzr1 = new StringTokenizer(path1, ".");
            StringTokenizer stknzr2 = new StringTokenizer(path2, ".");
            while (stknzr1.hasMoreTokens()) {
                int pos1 = Integer.parseInt(stknzr1.nextToken());
                int pos2 = Integer.parseInt(stknzr2.nextToken());
                if (pos1 != pos2) {
                    return pos1 - pos2;
                }
            }
            return 0;
        }
        else {
            return level1 - level2;
        }
    }
}
