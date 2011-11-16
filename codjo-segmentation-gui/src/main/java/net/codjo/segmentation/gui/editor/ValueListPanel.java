package net.codjo.segmentation.gui.editor;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ValueListPanel extends JPanel {

    JList list = new JList();


    public JList getList() {
        return list;
    }


    public ValueListPanel() {
        list.setName("editor.valueList");
        this.setLayout(new BorderLayout());
        this.add(new JLabel("List Of Values"), BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(list);
        this.add(scroll, BorderLayout.CENTER);
    }
}

