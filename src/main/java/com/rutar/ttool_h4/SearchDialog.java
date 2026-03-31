package com.rutar.ttool_h4;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;

import static javax.swing.JOptionPane.*;

// ............................................................................
/// Реалізація діалогового вікна пошуку інформації
/// @author Rutar_Andriy
/// 13.02.2026

public class SearchDialog extends JDialog {

private int findIndex;                                         // індекс пошуку
private String tipsText;                                      // текст підказки
private TToolH4 editor;                                        // головна форма
private JTextField tmpField;                                // допоміжна змінна

private final JTable table;                     // посилання на головну таблицю
private final Color defaultTextColor;               // колір нормального тексту
private final Color disableTextColor;               // колір неактивного тексту
private final String textFind = "Текст для пошуку";      // підказка для пошуку
private final String textRepl = "Текст для заміни";      // підказка для заміни
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
    
    defaultTextColor = fld_find_text.getForeground();
    disableTextColor = fld_repl_text.getDisabledTextColor();

    fld_find_text.setText(textFind);
    fld_find_text.setForeground(disableTextColor);
    fld_find_text.requestFocus();
    
    fld_repl_text.setText(textRepl);
    fld_repl_text.setForeground(disableTextColor);
    
}

// ============================================================================
/// Пошук заданого тексту в таблиці

private void findText() {

findIndex = -1;
findArray.clear();
String findText = fld_find_text.getText();

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
    if (cb_editable_only.isSelected() &&
       !table.isCellEditable(z, q)) { continue; }
    // Пошук тексту з врахуванням/без врахування регістру
    if (cb_ignore_case.isSelected() &&
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
    String searchText  = getFindText();
    String replaceText = getReplText();
    
    cencelCellEditing();

    while (index != -1)
        { index = text.indexOf(searchText, searchIndex);
          if (index != -1) { searchIndex = index + searchText.length();
                             count++; } }
    
    if (count == 1)
        { text = text.replace(searchText, replaceText); }
    else if (count > 1)
        { switch (showReplaceTypeDialog())
            { case 0  -> text = replaceFirst(text, searchText, replaceText);
              case 1  -> text = text.replace(searchText, replaceText);
              case 2  -> text = replaceLast(text, searchText, replaceText); } }
    
    table.setValueAt(text, targetRow, targetCol);
}

// ============================================================================
/// Заміна всіх результатів пошуку

private void replaceAll() {

String messageText = "<html>Ви справді хочете виконати заміну<br>"
                   + "всіх співпадінь у змінюваних клітинках?</html>";

if (showConfirmDialog(this, messageText, "Повідомлення", 0) != 0) { return; }

String text;
String searchText  = getFindText();
String replaceText = getReplText();

cencelCellEditing();

for (int[] cell : findArray)
    { if (!table.isCellEditable(cell[0], cell[1])) { continue; }
      text = (String) table.getValueAt(cell[0], cell[1]);
      text = text.replace(searchText, replaceText);
      table.setValueAt(text, cell[0], cell[1]); } }

// ============================================================================
/// Скидання всіх результатів пошуку

private void cleanSearch()
    { fld_find_text.requestFocus();
      fld_find_text.setText(null);
      lbl_info.setText("...");
      lbl_num.setText("0/0");
      findArray.clear();
      findIndex = -1;
      updateAllComponentsState(); }

// ============================================================================
/// Скидання всіх результатів заміни

private void cleanReplace()
    { fld_repl_text.requestFocus();
      fld_repl_text.setText(null);
      updateAllComponentsState(); }

// ============================================================================
/// Оновлення стану всіх елементів вікна

private void updateAllComponentsState() {

boolean hasEditableCell = false;
boolean canReplace = !cb_ignore_case.isSelected();

for (int[] cell : findArray) {
    if (table.isCellEditable(cell[0], cell[1]))
        { hasEditableCell = true;
          break; } }

// Оновлення стану компонентів
btn_next  .setEnabled(!findArray.isEmpty());
btn_prev  .setEnabled(!findArray.isEmpty());
cb_replace.setEnabled(canReplace);

btn_repl_all .setEnabled(canReplace && !findArray.isEmpty() &&
                         cb_replace.isSelected() && hasEditableCell);
btn_repl_this.setEnabled(canReplace &&
                        !findArray.isEmpty() && cb_replace.isSelected() &&
                         table.isCellEditable(findArray.get(findIndex)[0],
                                              findArray.get(findIndex)[1]));

fld_repl_text .setEnabled(canReplace && cb_replace.isSelected() &&
                                       !findArray.isEmpty());
btn_clean_repl.setEnabled(canReplace && cb_replace.isSelected() &&
                                       !findArray.isEmpty());
}

// ============================================================================
/// Цей метод викликається з конструктора для ініціалізації форми.
/// УВАГА: НЕ змінюйте цей код. Вміст цього методу завжди 
/// перезапишеться редактором форм

@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cb_ignore_case = new JCheckBox();
        cb_editable_only = new JCheckBox();
        fld_find_text = new JTextField();
        btn_clean_find = new JButton();
        btn_prev = new JButton();
        btn_next = new JButton();
        sep_one = new JSeparator();
        cb_replace = new JCheckBox();
        fld_repl_text = new JTextField();
        btn_clean_repl = new JButton();
        btn_repl_this = new JButton();
        btn_repl_all = new JButton();
        sep_two = new JSeparator();
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

        cb_ignore_case.setText("Пошук без врахуванням регістру");
        cb_ignore_case.setActionCommand("cbIgnoreCase");
        cb_ignore_case.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onComponentAction(evt);
            }
        });

        cb_editable_only.setSelected(true);
        cb_editable_only.setText("Пошук лише в змінюваних клітинках");
        cb_editable_only.setActionCommand("cbEditableOnly");
        cb_editable_only.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onComponentAction(evt);
            }
        });

        fld_find_text.setHorizontalAlignment(JTextField.CENTER);
        fld_find_text.setActionCommand("search");
        fld_find_text.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                onFocusGained(evt);
            }
            public void focusLost(FocusEvent evt) {
                onFocusLost(evt);
            }
        });
        fld_find_text.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onComponentAction(evt);
            }
        });

        btn_clean_find.setText("✕");
        btn_clean_find.setActionCommand("cleanSearch");
        btn_clean_find.setMargin(new Insets(2, 2, 2, 2));
        btn_clean_find.addActionListener(new ActionListener() {
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

        cb_replace.setText("Замінити текст");
        cb_replace.setActionCommand("cbReplace");
        cb_replace.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onComponentAction(evt);
            }
        });

        fld_repl_text.setHorizontalAlignment(JTextField.CENTER);
        fld_repl_text.setActionCommand("replace");
        fld_repl_text.setEnabled(false);
        fld_repl_text.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent evt) {
                onFocusGained(evt);
            }
            public void focusLost(FocusEvent evt) {
                onFocusLost(evt);
            }
        });

        btn_clean_repl.setText("✕");
        btn_clean_repl.setActionCommand("cleanReplace");
        btn_clean_repl.setEnabled(false);
        btn_clean_repl.setMargin(new Insets(2, 2, 2, 2));
        btn_clean_repl.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onComponentAction(evt);
            }
        });

        btn_repl_this.setText("Замінити знайдене");
        btn_repl_this.setActionCommand("replaceThis");
        btn_repl_this.setEnabled(false);
        btn_repl_this.setMargin(new Insets(2, 5, 2, 5));
        btn_repl_this.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onComponentAction(evt);
            }
        });

        btn_repl_all.setText("Замінити все");
        btn_repl_all.setActionCommand("replaceAll");
        btn_repl_all.setEnabled(false);
        btn_repl_all.setMargin(new Insets(2, 5, 2, 5));
        btn_repl_all.addActionListener(new ActionListener() {
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
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(sep_one)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(fld_find_text)
                                .addGap(5, 5, 5)
                                .addComponent(btn_clean_find))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(lbl_info, GroupLayout.PREFERRED_SIZE, 255, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(lbl_num, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(cb_editable_only, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(5, 5, 5)))
                        .addGap(5, 5, 5))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fld_repl_text)
                        .addGap(5, 5, 5)
                        .addComponent(btn_clean_repl)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sep_two)
                        .addGap(5, 5, 5))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cb_replace, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(5, 5, 5))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btn_repl_this, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)
                                .addGap(5, 5, 5)
                                .addComponent(btn_repl_all, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btn_prev, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)
                                .addGap(5, 5, 5)
                                .addComponent(btn_next, GroupLayout.PREFERRED_SIZE, 170, GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cb_ignore_case, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(5, 5, 5))))
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(cb_ignore_case)
                .addGap(5, 5, 5)
                .addComponent(cb_editable_only)
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(btn_clean_find, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fld_find_text))
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_prev)
                    .addComponent(btn_next))
                .addGap(5, 5, 5)
                .addComponent(sep_one, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(cb_replace)
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(btn_clean_repl, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(fld_repl_text))
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_repl_this)
                    .addComponent(btn_repl_all))
                .addGap(5, 5, 5)
                .addComponent(sep_two, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
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
        
        String text = fld_find_text.getText();
        
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
                { if (!text.equals(textFind)) { findText(); }
                  else                        { updateAllComponentsState(); } }
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
/// Прослуховування отримання фокусу

    private void onFocusGained(FocusEvent evt) {//GEN-FIRST:event_onFocusGained
        
        tmpField = (JTextField) evt.getComponent();
        
        if (tmpField == fld_find_text) { tipsText = textFind; }
        else                           { tipsText = textRepl; }
        
        tmpField.setForeground(defaultTextColor);
        
        if (tmpField.getText().equals(tipsText))
            { tmpField.setText(""); }
    }//GEN-LAST:event_onFocusGained

// ============================================================================
/// Прослуховування втрати фокусу

    private void onFocusLost(FocusEvent evt) {//GEN-FIRST:event_onFocusLost
        
        tmpField = (JTextField) evt.getComponent();
        
        if (tmpField == fld_find_text) { tipsText = textFind; }
        else                           { tipsText = textRepl; }
        
        if (tmpField.getText().isEmpty())
             { tmpField.setText(tipsText);
               tmpField.setForeground(disableTextColor); }
        else { tmpField.setForeground(defaultTextColor); }
    }//GEN-LAST:event_onFocusLost

// ============================================================================
/// Список усіх об'явлених змінних

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton btn_clean_find;
    private JButton btn_clean_repl;
    private JButton btn_next;
    private JButton btn_prev;
    private JButton btn_repl_all;
    private JButton btn_repl_this;
    private JCheckBox cb_editable_only;
    private JCheckBox cb_ignore_case;
    private JCheckBox cb_replace;
    private JTextField fld_find_text;
    private JTextField fld_repl_text;
    private JLabel lbl_info;
    private JLabel lbl_num;
    private JSeparator sep_one;
    private JSeparator sep_two;
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

String messageText = "<html>У даному тексті знайдено декілька<br>"
                   + "співпадінь. Що необхідно замінити?<br>&nbsp;</html>";

JPanel panel = new JPanel();
ButtonGroup group = new ButtonGroup();
panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
panel.add(new JLabel(messageText));

for (int z = 0; z < options.length; z++)
    { JRadioButton rb = new JRadioButton(options[z]);
      group.add(rb);
      panel.add(rb);
      if (z == 0) { rb.setSelected(true); } }

int result = showConfirmDialog(this, panel, "Тип заміни", 2);
if (result != OK_OPTION) { return -1; }

for (Component c : panel.getComponents()) {
    if (c instanceof JRadioButton rb && rb.isSelected())
        { return Arrays.asList(options).indexOf(rb.getText()); } }

return -1;

}

// ============================================================================
/// Відслідковування завершення переміщення вікна

private void initMoveTracking() {

final int DELEY_MS = 200;
final javax.swing.Timer timer =
  new javax.swing.Timer(DELEY_MS, windowMoveFinished);

timer.setRepeats(false);

addComponentListener(new ComponentAdapter() {
    @Override
    public void componentMoved(ComponentEvent e) { timer.restart(); }
});

}

// ============================================================================
/// Отримання даних з текстового поля пошуку

private String getFindText()
    { String text = fld_find_text.getText();
      return text.equals(textFind) ? "" : text; }

// ============================================================================
/// Отримання даних з текстового поля заміни

private String getReplText()
    { String text = fld_repl_text.getText();
      return text.equals(textRepl) ? "" : text; }

// ============================================================================
/// Скасування активного редагування клітинки

private void cencelCellEditing()
    { if (table.isEditing()) { table.getCellEditor().cancelCellEditing(); } }

// ============================================================================
/// Реагування на завершення переміщення вікна

private final ActionListener windowMoveFinished = (ActionEvent e) -> {
    if (!findArray.isEmpty()) { editor.toFront(); }
};

// Кінець класу SearchDialog ==================================================

}
