package com.rutar.ttool_h4;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;

// ............................................................................
/// Реалізація діалогового вікна пошуку інформації
/// @author Rutar_Andriy
/// 14.01.2026

public class SearchDialog extends JDialog {

private int findIndex;                                         // індекс пошуку
private TToolH4 editor;                                        // головна форма
private final ArrayList<int[]> findArray = new ArrayList<>();    // рез. пошуку

// ============================================================================
/// Головний конструктор
/// @param parent головне вікно програми

public SearchDialog (Frame parent) {
    
    super(parent, false);
    
    initComponents();
    initMoveTracking();
    setLocationRelativeTo(parent);
    editor = (TToolH4) parent;

}

// ============================================================================
/// Пошук заданого тексту в таблиці

private void findText() {

findIndex = -1;
findArray.clear();
String findText = fld_search.getText();

if (findText.isBlank()) { return; }

int col = editor.tbl_main.getColumnCount();
int row = editor.tbl_main.getRowCount();
String value;

for (int z = 0; z < row; z++) {
for (int q = 1; q < col; q++) {
    Object valueAt = editor.tbl_main.getValueAt(z, q);
    if (valueAt == null) { continue; }
    value = valueAt.toString();
    if (value == null) { continue; }
    if (value.toLowerCase().contains(findText
             .toLowerCase())) { findArray.add(new int[] { z, q }); } } }

lbl_info.setText("Знайдено: " + findArray.size() + " результатів");
if (!findArray.isEmpty()) { findIndex = 0;
                            selectResult(findIndex);
                            btn_next.setEnabled(true);
                            btn_prev.setEnabled(true); }

}

// ============================================================================
/// Виділення клітинки таблиці зі знайденою інформацією

private void selectResult (int index) {

if (index < 0) { return; }

int targetRow = findArray.get(index)[0];
int targetCol = findArray.get(index)[1];

editor.tbl_main.setRowSelectionInterval(targetRow, targetRow);
editor.tbl_main.setColumnSelectionInterval(targetCol, targetCol);

Rectangle rect = editor.tbl_main.getCellRect(targetRow, targetCol, true);
editor.tbl_main.scrollRectToVisible(rect);
editor.tbl_main.changeSelection(targetRow, targetCol, false, false);    

lbl_num.setText((index+1) + "/" + findArray.size());
editor.toFront();

}

// ============================================================================
/// Знаходження наступного результату пошуку

private void findNext() {
    if (findArray.isEmpty()) { return; }
    findIndex++;
    if (findIndex == findArray.size()) { findIndex = 0; }
    selectResult(findIndex);
}

// ============================================================================
/// Знаходження попереднього результату пошуку

private void findPrev() {
    if (findArray.isEmpty()) { return; }
    findIndex--;
    if (findIndex == -1) { findIndex = findArray.size() - 1; }
    selectResult(findIndex);  
}

// ============================================================================
/// Скидання всіх результатів пошуку

private void cleanAll() { btn_next.setEnabled(false);
                          btn_prev.setEnabled(false);
                          fld_search.requestFocus();
                          fld_search.setText(null);
                          lbl_info.setText("...");
                          lbl_num.setText("0/0");
                          findArray.clear();
                          findIndex = -1; }

// ============================================================================
/// Цей метод викликається з конструктора для ініціалізації форми.
/// УВАГА: НЕ змінюйте цей код. Вміст цього методу завжди 
/// перезапишеться редактором форм

@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fld_search = new JTextField();
        btn_clean = new JButton();
        btn_prev = new JButton();
        btn_next = new JButton();
        sep_one = new JSeparator();
        lbl_info = new JLabel();
        lbl_num = new JLabel();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Пошук тексту");
        setAlwaysOnTop(true);
        setResizable(false);
        addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent evt) {
                onWindowClose(evt);
            }
        });

        fld_search.setHorizontalAlignment(JTextField.CENTER);
        fld_search.setActionCommand("text");
        fld_search.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onComponentAction(evt);
            }
        });

        btn_clean.setText("✕");
        btn_clean.setActionCommand("clean");
        btn_clean.setMargin(new Insets(2, 2, 2, 2));
        btn_clean.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onComponentAction(evt);
            }
        });

        btn_prev.setText("<< знайти попереднє");
        btn_prev.setActionCommand("prev");
        btn_prev.setEnabled(false);
        btn_prev.setMargin(new Insets(2, 5, 2, 5));
        btn_prev.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onComponentAction(evt);
            }
        });

        btn_next.setText("знайти наступне >>");
        btn_next.setActionCommand("next");
        btn_next.setEnabled(false);
        btn_next.setMargin(new Insets(2, 5, 2, 5));
        btn_next.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onComponentAction(evt);
            }
        });

        lbl_info.setText("...");

        lbl_num.setHorizontalAlignment(SwingConstants.RIGHT);
        lbl_num.setText("0/0");

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(lbl_info, GroupLayout.PREFERRED_SIZE, 286, GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(lbl_num, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(sep_one)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btn_prev, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(btn_next, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fld_search)
                        .addGap(5, 5, 5)
                        .addComponent(btn_clean)))
                .addGap(5, 5, 5))
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(fld_search, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_clean))
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_prev)
                    .addComponent(btn_next))
                .addGap(5, 5, 5)
                .addComponent(sep_one, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lbl_info)
                    .addComponent(lbl_num))
                .addGap(5, 5, 5))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

// ============================================================================
/// Прослуховування елементів керування вікна

    private void onComponentAction(ActionEvent evt) {//GEN-FIRST:event_onComponentAction
        
        switch (evt.getActionCommand()) {
            case "text"  -> findText();
            case "prev"  -> findPrev();
            case "next"  -> findNext();
            case "clean" -> cleanAll();
        }
    }//GEN-LAST:event_onComponentAction

// ============================================================================
/// Прослуховування закривання вікна

    private void onWindowClose(WindowEvent evt) {//GEN-FIRST:event_onWindowClose
        cleanAll();
    }//GEN-LAST:event_onWindowClose

// ============================================================================
/// Список усіх об'явлених змінних

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton btn_clean;
    private JButton btn_next;
    private JButton btn_prev;
    private JTextField fld_search;
    private JLabel lbl_info;
    private JLabel lbl_num;
    private JSeparator sep_one;
    // End of variables declaration//GEN-END:variables

// ============================================================================
/// Відслідковування завершення переміщення вікна

private void initMoveTracking() {

final int DELEY_MS = 200;
final Timer timer = new Timer(DELEY_MS, windowMoveFinished);
timer.setRepeats(false);

addComponentListener(new ComponentAdapter() {
    @Override
    public void componentMoved(ComponentEvent e) { timer.restart(); }
});

}

// ============================================================================
/// Реагування на завершення переміщення вікна

private final ActionListener windowMoveFinished = (ActionEvent e) -> {
    if (!findArray.isEmpty()) { editor.toFront(); }
};

// Кінець класу SearchDialog ==================================================

}
