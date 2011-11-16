/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.editor;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
/**
 * DOCUMENT ME!
 *
 * @version $Revision: 1.4 $
 */
public class HighlightingStyledDocumentTest extends TestCase {
    HighlightingStyledDocument document;


    public void testGetRegularExpression() throws Exception {
        List values = new ArrayList();
        values.add("val1");
        values.add("val2");
        values.add("val3");
        assertEquals("val1|val1|val2|val3", document.getRegularExpression(values));
    }


    protected void tearDown() throws Exception {
    }


    protected void setUp() throws Exception {
        document = new HighlightingStyledDocument();
    }


    /**
     * the method replace the meta-character <b>\ | ( ) [ { ^ $  + ? . &lt; &gt;</b> \meta-carachter
     *
     * @throws Exception exception
     */
    public void testReplaceMetaCaracters() throws Exception {
        String replaced =
              document.replaceMetaCaracters("(ad.f)rt<>er*==+fg$dgfsdg^t{qdsfr?[");

        assertEquals("\\(ad\\.f\\)rt\\<\\>er\\*==\\+fg\\$dgfsdg\\^t\\{qdsfr\\?\\[",
                     replaced);
    }


    public void testGetParamFounds() throws Exception {
        assertEquals(3, document.getParamFounds(" func (a, 5, rb)", 5));
        assertEquals(0, document.getParamFounds("func( )", 4));
        assertEquals(1, document.getParamFounds("func (4)", 5));
        assertEquals(2, document.getParamFounds("func (d, g )", 4));

        assertEquals(-2, document.getParamFounds("func d, g )", 4));
        assertEquals(-3, document.getParamFounds("func (d, g ", 4));
        assertEquals(-2, document.getParamFounds("func func2 (d, g) ", 4));
    }
}
