/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.editor;
import java.util.Collection;
import javax.swing.text.SimpleAttributeSet;
/**
 * classe qui représente un style d'une collection de strings.
 *
 * @version $Revision: 1.5 $
 */
public class StringsStyle {
    private SimpleAttributeSet attributeStyle;
    private Collection stringStyle;


    public StringsStyle(SimpleAttributeSet attributeStyle, Collection stringStyle) {
        this.attributeStyle = attributeStyle;
        this.stringStyle = stringStyle;
    }


    public SimpleAttributeSet getAttributeStyle() {
        return attributeStyle;
    }


    public void setAttributeStyle(SimpleAttributeSet attributeStyle) {
        this.attributeStyle = attributeStyle;
    }


    public Collection getStringStyle() {
        return stringStyle;
    }


    public void setStringStyle(Collection stringStyle) {
        this.stringStyle = stringStyle;
    }
}
