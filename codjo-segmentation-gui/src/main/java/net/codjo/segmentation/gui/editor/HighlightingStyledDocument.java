/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.segmentation.gui.editor;
import net.codjo.expression.help.FunctionHelp;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.apache.log4j.Logger;
import org.apache.regexp.RE;
/**
 * Classe qui herite de DefaultStyledDocument et qui formate le style du text en fontion des élements données:
 * mot clés (ex : + , - = &lt; ( ...), colonnes (ex : PortfolioCode ...), fontions (ex : iif substring ...),
 * String (ex : "rrr"), error (tout ce qui est autre)
 *
 * @author Lajmi
 */
class HighlightingStyledDocument extends DefaultStyledDocument {
    private static final Logger LOG = Logger.getLogger(HighlightingStyledDocument.class);
    /* style des operations et les autres mot cles */

    //private SimpleAttributeSet operationStyle = new SimpleAttributeSet();

    /* style des colonnes  */

    //private SimpleAttributeSet columnStyle = new SimpleAttributeSet();
    private List<StringsStyle> stringsStyleList = new ArrayList<StringsStyle>();

    /* style des fontions  */
    private SimpleAttributeSet functionStyle = new SimpleAttributeSet();

    /* Style des String en erreur */
    private SimpleAttributeSet stringStyle = new SimpleAttributeSet();

    /* Style des digits */
    private SimpleAttributeSet digitStyle = new SimpleAttributeSet();

    /* Error Style */
    private SimpleAttributeSet errorStyle = new SimpleAttributeSet();
    private RE functionRegExp = new RE("");
    private Map<String, FunctionHelp> functionMap = new HashMap<String, FunctionHelp>();
    private UpdateHighlightRunnable postedUpdate = new UpdateHighlightRunnable();


    HighlightingStyledDocument() {
        initDefaultAttributeSet();
    }


    public SimpleAttributeSet getFunctionStyle() {
        return functionStyle;
    }


    public SimpleAttributeSet getStringStyle() {
        return stringStyle;
    }


    public SimpleAttributeSet getDigitStyle() {
        return digitStyle;
    }


    public SimpleAttributeSet getErrorStyle() {
        return errorStyle;
    }


    public void setFunctionStyle(SimpleAttributeSet functionStyle) {
        this.functionStyle = functionStyle;
    }


    public void setStringStyle(SimpleAttributeSet stringStyle) {
        this.stringStyle = stringStyle;
    }


    public void setDigitStyle(SimpleAttributeSet digitStyle) {
        this.digitStyle = digitStyle;
    }


    public void setErrorStyle(SimpleAttributeSet errorStyle) {
        this.errorStyle = errorStyle;
    }


    public void addStringsStyle(StringsStyle stringsStyle) {
        stringsStyleList.add(stringsStyle);
    }


    public void setColumns(Collection columns) {
        SimpleAttributeSet columnStyle = new SimpleAttributeSet();
        setDefaultStyle(columnStyle);
        StyleConstants.setForeground(columnStyle, Color.blue);
        StyleConstants.setBackground(columnStyle, new Color(204, 204, 255, 70));
        StringsStyle stringsStyle = new StringsStyle(columnStyle, columns);
        addStringsStyle(stringsStyle);
    }


    public void setOperations(Collection<OperatorHelper.OperatorHelp> operators) {
        List<String> operatorList = new ArrayList<String>();
        for (OperatorHelper.OperatorHelp operator : operators) {
            operatorList.add(operator.toString());
        }
        SimpleAttributeSet operationStyle = new SimpleAttributeSet();
        setDefaultStyle(operationStyle);
        StyleConstants.setForeground(operationStyle, Color.black);
        StyleConstants.setBackground(operationStyle, Color.white);
        StringsStyle stringsStyle = new StringsStyle(operationStyle, operatorList);
        addStringsStyle(stringsStyle);
    }


    public void setFunctions(Collection functions) {
        List<String> functionNameList = new ArrayList<String>();
        for (Object function1 : functions) {
            FunctionHelp function = (FunctionHelp)function1;

            functionNameList.add(function.getFunctionName());
            functionMap.put(function.getFunctionName(), function);
        }
        functionRegExp = new RE(getRegularExpression(functionNameList));
    }


    /**
     * getRegularExpression(List values) Pour une liste de valeur possible returne l'expression régulière pour
     * rechercher l'un de ces mots. the method replace the meta-character <b>\ | ( ) [ { ^ $ + ? . &lt;
     * &gt;</b> \meta-carachter<p>Example: pour la liste : value1, value2 le retour est :
     * "value1|value2".</p>
     *
     * @param values la liste des valeurs possible <b>String</b>s
     *
     * @return L'expression régulière
     */
    String getRegularExpression(Collection values) {
        StringBuffer exp = new StringBuffer("");
        int idx = 0;
        for (Object value1 : values) {
            String value = (String)value1;
            value = replaceMetaCaracters(value);
            if (idx == 0) {
                exp.append(value.trim());
            }
            exp.append("|").append(value.trim());
            idx++;
        }
        exp.append("");
        return exp.toString();
    }


    /**
     * the method replace the meta-character <b>\ | ( ) [ { ^ $  + ? . &lt; &gt;</b> \meta-carachter
     *
     * @param value
     *
     * @return metaCatarters replaced
     */
    String replaceMetaCaracters(String value) {
        RE re = new RE("\\(|\\)|\\[|\\{|\\^|\\$|\\*|\\+|\\?|\\.|\\<|\\>|\\|");
        re.match(value);

        StringBuffer sbf = new StringBuffer(value);

        re.match(sbf.toString());
        int beginColSearch = re.getParenEnd(0);
        while (beginColSearch != -1) {
//            String theChar = re.getParen(0);
            sbf.insert(beginColSearch - 1, "\\");
            re.match(sbf.toString(), beginColSearch + 1);
            beginColSearch = re.getParenEnd(0) != beginColSearch ? re.getParenEnd(0) : -1;
        }
        return sbf.toString();
    }


    /**
     * insertString Overrides the default method from DefaultStyledDocument.  Calls appropriate syntax
     * highlighting code and then class super.
     *
     * @param offs the starting offset >= 0
     * @param str  the string to insert; does nothing with null/empty strings
     * @param att  the attributes for the inserted content
     *
     * @throws BadLocationException the given insert position is not a valid  position within the document
     */
    @Override
    public void insertString(int offs, String str, AttributeSet att)
          throws BadLocationException {
        super.insertString(offs, str, att);

        postUpdateHighlight();
    }


    private void postUpdateHighlight() {
        postedUpdate.cancel();
        postedUpdate = new UpdateHighlightRunnable();
        SwingUtilities.invokeLater(postedUpdate);
    }


    /**
     * fireRemoveUpdate Overrides the default method from DefaultStyledDocument.  Calls appropriate syntax
     * highlighting code and then class super.
     *
     * @param event the DocumentEvent
     */
    @Override
    protected void fireRemoveUpdate(DocumentEvent event) {
        super.fireRemoveUpdate(event);
        postUpdateHighlight();
    }


    /**
     * Method: updateHighlightingInRange
     */
    public void updateHighlightingInRange() {
        try {
            int start = getStartPosition().getOffset();
            int end = getEndPosition().getOffset();

            String text = getText(start, end - start);
            if (text.length() == 0) {
                return;
            }
            setCharacterAttributes(start, end - start, errorStyle, true);

            //Highlighting functions
            // et Vérifie le nombre de parametre
            functionRegExp.match(text);
            int beginColSearch = functionRegExp.getParenEnd(0);
            while (beginColSearch != -1) {
                String functionNameFound = functionRegExp.getParen(0);
                FunctionHelp functionFound = functionMap.get(functionNameFound);

                if (functionFound.getParameterNumber() == -1
                    || doesParameterMatch(functionFound, text, beginColSearch)) {
                    int parenStart = functionRegExp.getParenStart(0);
                    setCharacterAttributes(parenStart, beginColSearch - parenStart, functionStyle, true);
                }
                functionRegExp.match(text, beginColSearch);
                beginColSearch = functionRegExp.getParenEnd(0);
            }

            for (StringsStyle stringsStyle : stringsStyleList) {
                highlightStandard(text, stringsStyle);
            }

            //Highlighting Strings
            RE stringMatcher = new RE("\"[^\n\"]*\"");
            stringMatcher.match(text);
            beginColSearch = stringMatcher.getParenEnd(0);
            while (beginColSearch != -1) {
                final int parenStart = stringMatcher.getParenStart(0);

                setCharacterAttributes(parenStart, beginColSearch - parenStart, stringStyle, true);

                stringMatcher.match(text, beginColSearch);
                int parenEnd = stringMatcher.getParenEnd(0);
                beginColSearch = (parenEnd != beginColSearch) ? parenEnd : -1;
            }
        }
        catch (Exception error) {
            LOG.error(error);
        }
    }


    private boolean doesParameterMatch(FunctionHelp functionFound, String text, int beginColSearch) {
        return functionFound.getParameterNumber() == getParamFounds(text, beginColSearch);
    }


    private void highlightStandard(String text, StringsStyle stringsStyle) {
        if (!stringsStyle.getStringStyle().isEmpty()) {
            RE regExpStyle = new RE(getRegularExpression(stringsStyle.getStringStyle()));

            int beginColSearch;
            regExpStyle.match(text);
            beginColSearch = regExpStyle.getParenEnd(0);
            while (beginColSearch != -1) {
                setCharacterAttributes(regExpStyle.getParenStart(0),
                                       beginColSearch - regExpStyle.getParenStart(0),
                                       stringsStyle.getAttributeStyle(), true);
                regExpStyle.match(text, beginColSearch);
                beginColSearch = regExpStyle.getParenEnd(0);
            }
        }
    }


    /**
     * returne le nombre de parametres utilisé par la méthode
     *
     * @param text           le text
     * @param beginColSearch l'index de fin de la methode
     *
     * @return int nombre de param dans les parenthèses
     */
    int getParamFounds(String text, int beginColSearch) {
        String textAfter = text.substring(beginColSearch, text.length()).trim();
        if (!textAfter.startsWith("(")) {
            return -2;
        }
        RE params = new RE("\\(.*\\)");
        params.match(textAfter);
        // si c'est pas les parenthèses de ma méthode en question
        if (0 != params.getParenStart(0)) {
            return -3;
        }
        StringTokenizer tokenizer =
              new StringTokenizer(params.getParen(0).substring(1, params.getParen(0).length() - 1), ",");
        int count = 0;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (!(token.trim().length() == 0)) {
                count++;
            }
        }
        return count;
    }


    protected void initDefaultAttributeSet() {
        setDefaultStyle(errorStyle);
        StyleConstants.setItalic(errorStyle, true);
        StyleConstants.setForeground(errorStyle, Color.red);
        StyleConstants.setBackground(errorStyle, Color.white);

        setDefaultStyle(functionStyle);
        StyleConstants.setBold(functionStyle, true);
        StyleConstants.setForeground(functionStyle, new Color(153, 0, 51));
        StyleConstants.setBackground(functionStyle, Color.white);

        setDefaultStyle(stringStyle);
        StyleConstants.setBold(stringStyle, true);
        StyleConstants.setForeground(stringStyle, new Color(51, 153, 0));
        StyleConstants.setBackground(stringStyle, Color.white);
    }


    private void setDefaultStyle(SimpleAttributeSet attributeSet) {
        StyleConstants.setFontFamily(attributeSet, "Monospaced");
        StyleConstants.setFontSize(attributeSet, 12);
        StyleConstants.setItalic(attributeSet, false);
        StyleConstants.setBold(attributeSet, false);
    }


    private class UpdateHighlightRunnable implements Runnable {
        private boolean canceled = false;


        public void cancel() {
            canceled = true;
        }


        public void run() {
            if (canceled) {
                return;
            }
            updateHighlightingInRange();
        }
    }
}
