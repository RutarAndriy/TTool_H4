package com.rutar.ttool_h4;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import com.rutar.jhinttextfield.*;

import static javax.swing.JOptionPane.*;

// ............................................................................
/// Реалізація діалогового вікна пошуку інформації
/// @author Rutar_Andriy
/// 24.04.2026

public class SearchDialog extends JDialog {

private int findIndex;                                         // індекс пошуку
private TToolH4 editor;                                        // головна форма

private final JTable table;                     // посилання на головну таблицю
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
    table = editor.tbl_main;
    cb_ignoreCase.requestFocus();
}

// ============================================================================
/// Пошук заданого тексту в таблиці

private void findText() {

    findIndex = -1;
    findArray.clear();
    String findText = hfld_findText.getText();

    if (findText.isBlank()) { return; }

    int col = table.getColumnCount();
    int row = table.getRowCount();
    String value;

    for (int z = 0; z < row; z++) {
    for (int q = 1; q < col; q++) {
      // Отримуємо об'єкт у заданій клітинці
      Object valueAt = table.getValueAt(z, q);
      if (valueAt == null) { continue; }
      // Отримуємо текст у заданій клітинці
      value = valueAt.toString();
      if (value == null) { continue; }
      // Перевіряємо чи є клітинка змінюваною
      if (cb_editableOnly.isSelected() &&
         !table.isCellEditable(z, q)) { continue; }
      // Пошук тексту з врахуванням/без врахування регістру
      if (cb_ignoreCase.isSelected() &&
          value.toLowerCase().contains(findText.toLowerCase()))
        { findArray.add(new int[] { z, q }); }
      else if (value.contains(findText))
        { findArray.add(new int[] { z, q }); } } }

    lbl_info.setText("Знайдено: " + findArray.size() + " результатів");

    if (!findArray.isEmpty()) { findIndex = 0;
                                selectResult(findIndex); }
    else                      { lbl_num.setText("0/0");  }

    updateAllComponentsState();
}

// ============================================================================
/// Виділення клітинки таблиці зі знайденою інформацією

private void selectResult (int index) {

    if (index < 0) { table.clearSelection();
                     return; }

    int targetRow = findArray.get(index)[0];
    int targetCol = findArray.get(index)[1];

    CellRender.setSearchedCell(targetCol, targetRow);
    Utils.selectCell(table, targetCol, targetRow);

    lbl_num.setText((index+1) + "/" + findArray.size());
    updateAllComponentsState();
    editor.toFront();
}

// ============================================================================
/// Знаходження наступного результату пошуку

private void findNext()
  { if (findArray.isEmpty()) { return; }
    findIndex++;
    if (findIndex == findArray.size()) { findIndex = 0; }
    selectResult(findIndex); }

// ============================================================================
/// Знаходження попереднього результату пошуку

private void findPrev()
  { if (findArray.isEmpty()) { return; }
    findIndex--;
    if (findIndex == -1) { findIndex = findArray.size() - 1; }
    selectResult(findIndex); }

// ============================================================================
/// Заміна вибраного результату пошуку

private void replaceThis() {
    
    int targetRow = findArray.get(findIndex)[0];
    int targetCol = findArray.get(findIndex)[1];
    int count = 0, index = 0, searchIndex = 0;

    String text = (String) table.getValueAt(targetRow, targetCol);
    String searchText  = hfld_findText.getText();
    String replaceText = hfld_replaceText.getText();
    
    cencelCellEditing();

    while (index != -1)
      { index = text.indexOf(searchText, searchIndex);
        if (index != -1) { searchIndex = index + searchText.length();
                           count++; } }
    
    if (count == 1)
      { text = text.replace(searchText, replaceText); }
    else if (count > 1) {
      switch (showReplaceTypeDialog()) {
        case 0  -> text = replaceFirst(text, searchText, replaceText);
        case 1  -> text = text.replace(searchText, replaceText);
        case 2  -> text = replaceLast(text, searchText, replaceText); } }
    
    table.setValueAt(text, targetRow, targetCol);
}

// ============================================================================
/// Заміна всіх результатів пошуку

private void replaceAll() {

    String messageText = "<html>Ви справді хочете виконати заміну<br>"
                       + "всіх співпадінь у змінюваних клітинках?</html>";

    if (showConfirmDialog(this, messageText, "Повідомлення", 0) != 0)
      { return; }

    String text;
    String searchText  = hfld_findText.getText();
    String replaceText = hfld_replaceText.getText();

    cencelCellEditing();

    for (int[] cell : findArray)
      { if (!table.isCellEditable(cell[0], cell[1])) { continue; }
        text = (String) table.getValueAt(cell[0], cell[1]);
        text = text.replace(searchText, replaceText);
        table.setValueAt(text, cell[0], cell[1]); }
}

// ============================================================================
/// Скидання всіх результатів пошуку

private void cleanSearch()
  { hfld_findText.requestFocus();
    hfld_findText.setText(null);
    lbl_info.setText("...");
    lbl_num.setText("0/0");
    findArray.clear();
    findIndex = -1;
    updateAllComponentsState(); }

// ============================================================================
/// Скидання всіх результатів заміни

private void cleanReplace()
  { hfld_replaceText.requestFocus();
    hfld_replaceText.setText(null);
    updateAllComponentsState(); }

// ============================================================================
/// Оновлення стану всіх елементів вікна

private void updateAllComponentsState() {

boolean hasEditableCell = false;
boolean canReplace = !cb_ignoreCase.isSelected();

for (int[] cell : findArray) {
  if (table.isCellEditable(cell[0], cell[1]))
    { hasEditableCell = true;
      break; } }

// Оновлення стану компонентів
btn_next    .setEnabled(!findArray.isEmpty());
btn_previous.setEnabled(!findArray.isEmpty());
cb_replace.setEnabled(canReplace);

btn_replaceAll .setEnabled(canReplace && !findArray.isEmpty() &&
                           cb_replace.isSelected() && hasEditableCell);
btn_replaceThis.setEnabled(canReplace &&
                          !findArray.isEmpty() && cb_replace.isSelected() &&
                           table.isCellEditable(findArray.get(findIndex)[0],
                                                findArray.get(findIndex)[1]));

hfld_replaceText .setEnabled(canReplace && cb_replace.isSelected() &&
                                          !findArray.isEmpty());
btn_cleanReplace.setEnabled(canReplace && cb_replace.isSelected() &&
                                         !findArray.isEmpty());
}

// ============================================================================
/// Цей метод викликається з конструктора для ініціалізації форми.
/// УВАГА: НЕ змінюйте цей код. Вміст цього методу завжди 
/// перезапишеться редактором форм

@SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    cb_ignoreCase = new JCheckBox();
    cb_editableOnly = new JCheckBox();
    pnl_findText = new JPanel();
    hfld_findText = new JHintTextField();
    btn_cleanFind = new JButton();
    pnl_goTo = new JPanel();
    btn_previous = new JButton();
    btn_next = new JButton();
    sep_goTo = new JSeparator();
    cb_replace = new JCheckBox();
    pnl_replace = new JPanel();
    hfld_replaceText = new JHintTextField();
    btn_cleanReplace = new JButton();
    pnl_replaceType = new JPanel();
    btn_replaceThis = new JButton();
    btn_replaceAll = new JButton();
    sep_replaceType = new JSeparator();
    lbl_info = new JLabel();
    lbl_num = new JLabel();

    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setTitle("Пошук та заміна");
    setAlwaysOnTop(true);
    setResizable(false);
    addWindowListener(new WindowAdapter() {
      public void windowClosed(WindowEvent evt) {
        onWindowClose(evt);
      }
    });

    cb_ignoreCase.setText("Пошук без врахуванням регістру");
    cb_ignoreCase.setActionCommand("cbIgnoreCase");
    cb_ignoreCase.addActionListener(this::onComponentAction);

    cb_editableOnly.setSelected(true);
    cb_editableOnly.setText("Пошук лише в змінюваних клітинках");
    cb_editableOnly.setActionCommand("cbEditableOnly");
    cb_editableOnly.addActionListener(this::onComponentAction);

    hfld_findText.setHintText("Текст для пошуку");
    hfld_findText.setHorizontalAlignment(JTextField.CENTER);
    hfld_findText.setActionCommand("search");
    hfld_findText.addActionListener(this::onComponentAction);

    btn_cleanFind.setText("✕");
    btn_cleanFind.setActionCommand("cleanSearch");
    btn_cleanFind.setMargin(new Insets(2, 2, 2, 2));
    btn_cleanFind.addActionListener(this::onComponentAction);

    GroupLayout pnl_findTextLayout = new GroupLayout(pnl_findText);
    pnl_findText.setLayout(pnl_findTextLayout);
    pnl_findTextLayout.setHorizontalGroup(pnl_findTextLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addGroup(pnl_findTextLayout.createSequentialGroup()
        .addComponent(hfld_findText, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGap(3, 3, 3)
        .addComponent(btn_cleanFind))
    );
    pnl_findTextLayout.setVerticalGroup(pnl_findTextLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addComponent(hfld_findText, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      .addComponent(btn_cleanFind, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );

    pnl_goTo.setLayout(new GridLayout(1, 0));

    btn_previous.setText("<< знайти попереднє");
    btn_previous.setActionCommand("previus");
    btn_previous.setEnabled(false);
    btn_previous.setMargin(new Insets(2, 5, 2, 5));
    btn_previous.addActionListener(this::onComponentAction);
    pnl_goTo.add(btn_previous);

    btn_next.setText("знайти наступне >>");
    btn_next.setActionCommand("next");
    btn_next.setEnabled(false);
    btn_next.setMargin(new Insets(2, 5, 2, 5));
    btn_next.addActionListener(this::onComponentAction);
    pnl_goTo.add(btn_next);

    cb_replace.setText("Замінити текст");
    cb_replace.setActionCommand("cbReplace");
    cb_replace.addActionListener(this::onComponentAction);

    hfld_replaceText.setHintText("Текст для заміни");
    hfld_replaceText.setHorizontalAlignment(JTextField.CENTER);
    hfld_replaceText.setActionCommand("replace");
    hfld_replaceText.setEnabled(false);

    btn_cleanReplace.setText("✕");
    btn_cleanReplace.setActionCommand("cleanReplace");
    btn_cleanReplace.setEnabled(false);
    btn_cleanReplace.setMargin(new Insets(2, 2, 2, 2));
    btn_cleanReplace.addActionListener(this::onComponentAction);

    GroupLayout pnl_replaceLayout = new GroupLayout(pnl_replace);
    pnl_replace.setLayout(pnl_replaceLayout);
    pnl_replaceLayout.setHorizontalGroup(pnl_replaceLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addGroup(pnl_replaceLayout.createSequentialGroup()
        .addComponent(hfld_replaceText, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGap(3, 3, 3)
        .addComponent(btn_cleanReplace))
    );
    pnl_replaceLayout.setVerticalGroup(pnl_replaceLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addComponent(hfld_replaceText, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      .addComponent(btn_cleanReplace, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );

    pnl_replaceType.setLayout(new GridLayout(1, 0));

    btn_replaceThis.setText("Замінити знайдене");
    btn_replaceThis.setActionCommand("replaceThis");
    btn_replaceThis.setEnabled(false);
    btn_replaceThis.setMargin(new Insets(2, 5, 2, 5));
    btn_replaceThis.addActionListener(this::onComponentAction);
    pnl_replaceType.add(btn_replaceThis);

    btn_replaceAll.setText("Замінити все");
    btn_replaceAll.setActionCommand("replaceAll");
    btn_replaceAll.setEnabled(false);
    btn_replaceAll.setMargin(new Insets(2, 5, 2, 5));
    btn_replaceAll.addActionListener(this::onComponentAction);
    pnl_replaceType.add(btn_replaceAll);

    lbl_info.setBackground(new Color(0, 102, 102));
    lbl_info.setText("...");

    lbl_num.setBackground(new Color(204, 0, 51));
    lbl_num.setHorizontalAlignment(SwingConstants.RIGHT);
    lbl_num.setText("0/0");

    GroupLayout layout = new GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGap(5, 5, 5)
        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
          .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addComponent(lbl_info, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(5, 5, 5)
            .addComponent(lbl_num))
          .addComponent(pnl_goTo, GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
          .addComponent(pnl_replace, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(cb_replace, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(sep_goTo)
          .addComponent(pnl_findText, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(cb_editableOnly, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(cb_ignoreCase, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(pnl_replaceType, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(sep_replaceType))
        .addGap(5, 5, 5))
    );
    layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGap(5, 5, 5)
        .addComponent(cb_ignoreCase)
        .addGap(5, 5, 5)
        .addComponent(cb_editableOnly)
        .addGap(5, 5, 5)
        .addComponent(pnl_findText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        .addGap(5, 5, 5)
        .addComponent(pnl_goTo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        .addGap(5, 5, 5)
        .addComponent(sep_goTo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        .addGap(5, 5, 5)
        .addComponent(cb_replace)
        .addGap(5, 5, 5)
        .addComponent(pnl_replace, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        .addGap(5, 5, 5)
        .addComponent(pnl_replaceType, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        .addGap(5, 5, 5)
        .addComponent(sep_replaceType, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        .addGap(5, 5, 5)
        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
          .addComponent(lbl_num, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(lbl_info, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addGap(5, 5, 5))
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

// ============================================================================
/// Прослуховування елементів керування вікна

  private void onComponentAction(ActionEvent evt) {//GEN-FIRST:event_onComponentAction

    var text = hfld_findText.getText();

    switch (evt.getActionCommand()) {
      case "search"         -> findText();
      case "prev"           -> findPrev();
      case "next"           -> findNext();
      case "cleanSearch"    -> cleanSearch();
      case "cleanReplace"   -> cleanReplace();
      case "replaceAll"     -> replaceAll();
      case "replaceThis"    -> replaceThis();
      case "cbReplace"      -> updateAllComponentsState();
      case "cbIgnoreCase",
           "cbEditableOnly" ->
        { if (!text.isEmpty()) { findText(); }
          else                 { updateAllComponentsState(); } }
    }
  }//GEN-LAST:event_onComponentAction

// ============================================================================
/// Прослуховування закривання вікна

  private void onWindowClose(WindowEvent evt) {//GEN-FIRST:event_onWindowClose
    cleanSearch();
    cleanReplace();
    CellRender.setSearchedCell(-1, -1);
  }//GEN-LAST:event_onWindowClose

// ============================================================================
/// Список усіх об'явлених змінних

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JButton btn_cleanFind;
  private JButton btn_cleanReplace;
  private JButton btn_next;
  private JButton btn_previous;
  private JButton btn_replaceAll;
  private JButton btn_replaceThis;
  private JCheckBox cb_editableOnly;
  private JCheckBox cb_ignoreCase;
  private JCheckBox cb_replace;
  private JHintTextField hfld_findText;
  private JHintTextField hfld_replaceText;
  private JLabel lbl_info;
  private JLabel lbl_num;
  private JPanel pnl_findText;
  private JPanel pnl_goTo;
  private JPanel pnl_replace;
  private JPanel pnl_replaceType;
  private JSeparator sep_goTo;
  private JSeparator sep_replaceType;
  // End of variables declaration//GEN-END:variables

// ============================================================================
/// Заміна першого співпадіння у тексті

private String replaceFirst (String text, String oldText, String newText) {

    int index = text.indexOf(oldText);

    if (index != -1)
        { return text.substring(0, index) + newText +
                 text.substring(index + oldText.length()); }

    return text;
}

// ============================================================================
/// Заміна останього співпадіння у тексті

private String replaceLast (String text, String oldText, String newText) {

    int index = text.lastIndexOf(oldText);

    if (index != -1)
        { return text.substring(0, index) + newText +
                 text.substring(index + oldText.length()); }

    return text;
}

// ============================================================================
/// Діалог вибору типу заміни тексту

private int showReplaceTypeDialog() {
        
    String[] options = { "Замінити перше співпадіння",
                         "Замінити всі співпадіння",
                         "Замінити останнє співпадіння" };

    var messageText = "<html>У даному тексті знайдено декілька<br>"
                    + "співпадінь. Що необхідно замінити?<br>&nbsp;</html>";

    var panel = new JPanel();
    var group = new ButtonGroup();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.add(new JLabel(messageText));

    for (int z = 0; z < options.length; z++)
      { var rb = new JRadioButton(options[z]);
        group.add(rb);
        panel.add(rb);
        if (z == 0) { rb.setSelected(true); } }

    var result = showConfirmDialog(this, panel, "Тип заміни", 2);
    if (result != OK_OPTION) { return -1; }

    for (var c : panel.getComponents()) {
      if (c instanceof JRadioButton rb && rb.isSelected())
        { return Arrays.asList(options).indexOf(rb.getText()); } }

    return -1;
}

// ============================================================================
/// Відслідковування завершення переміщення вікна

private void initMoveTracking() {

    final var DELEY_MS = 200;
    final var timer = new javax.swing.Timer(DELEY_MS, windowMoveFinished);

    timer.setRepeats(false);
    addComponentListener(new ComponentAdapter() {
        @Override
        public void componentMoved(ComponentEvent e) { timer.restart(); }
    });
}

// ============================================================================
/// Скасування активного редагування клітинки

private void cencelCellEditing()
  { if (table.isEditing()) { table.getCellEditor().cancelCellEditing(); } }

// ============================================================================
/// Реагування на завершення переміщення вікна

private final ActionListener windowMoveFinished = (ActionEvent e) ->
  { if (!findArray.isEmpty()) { editor.toFront(); } };

// Кінець класу SearchDialog ==================================================

}
