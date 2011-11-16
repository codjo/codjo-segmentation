/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.editor;
import java.awt.Color;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import net.codjo.expression.help.FunctionHelp;
/**
 *
 */
public class HelpStyle {
    private EditorMainPanelGui gui;
    private List<FunctionHelp> functionDataList;
    private List<OperatorHelper.OperatorHelp> operatorDataList;


    public HelpStyle(EditorMainPanelGui gui, List<FunctionHelp> functionDataList, List<OperatorHelper.OperatorHelp> operatorDataList) {
        this.gui = gui;
        this.functionDataList = functionDataList;
        this.operatorDataList = operatorDataList;
    }


    public void colorLog(String logContent) throws BadLocationException {
        StyledDocument styledDocument = buildStyles();
        initialiseDefaultStyle(styledDocument, logContent);
        colorDescription(styledDocument);
    }


    private void initialiseDefaultStyle(StyledDocument styledDocument, String logContent) {
        Style defaultStyle = gui.getLogStyle("Default");
        styledDocument.setCharacterAttributes(0, logContent.length(), defaultStyle, true);
    }


    private StyledDocument buildStyles() {
        StyledDocument styledDocument = gui.getLogStyledDocument();
        Style descritionStyle = gui.addLogStyle("Description", null);
        StyleConstants.setBold(descritionStyle, true);
        StyleConstants.setUnderline(descritionStyle, true);
        StyleConstants.setForeground(descritionStyle, Color.blue);

        Style functionStyle = gui.addLogStyle("Function", null);
        StyleConstants.setForeground(functionStyle, Color.red);

        Style defaultStyle = gui.addLogStyle("Default", null);
        StyleConstants.setForeground(defaultStyle, Color.black);
        return styledDocument;
    }


    private void colorDescription(StyledDocument styledDocument)
          throws BadLocationException {
        Style descriptionStyle = gui.getLogStyle("Description");
        colorWord(styledDocument, descriptionStyle, "Description ");
        colorWord(styledDocument, descriptionStyle, "Usage ");
        colorWord(styledDocument, descriptionStyle, "Exemple ");

        Style functionStyle = gui.getLogStyle("Function");
        for (FunctionHelp functionHelp : functionDataList) {
            colorWord(styledDocument, functionStyle, functionHelp.getFunctionName() + "(");
        }
        for (OperatorHelper.OperatorHelp operatorHelp : operatorDataList) {
            colorWord(styledDocument, functionStyle, operatorHelp.toString());
        }

        colorWord(styledDocument, functionStyle, "(");
        colorWord(styledDocument, functionStyle, ")");
    }


    private void colorWord(StyledDocument styledDocument, Style style, String pattern)
          throws BadLocationException {
        Document document = gui.getLogDocument();
        String text = document.getText(0, document.getLength());

        int pos = text.indexOf(pattern, 0);
        while (pos >= 0) {
            styledDocument.setCharacterAttributes(pos, pattern.length(), style, true);
            pos += pattern.length();
            pos = text.indexOf(pattern, pos);
        }
    }
}
