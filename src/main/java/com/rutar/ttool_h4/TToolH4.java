package com.rutar.ttool_h4;

import java.io.*;
import java.awt.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import javax.swing.*;
import java.nio.file.*;
import javax.imageio.*;
import java.util.jar.*;
import java.awt.event.*;
import java.awt.image.*;
import java.nio.charset.*;
import javax.swing.event.*;
import javax.swing.table.*;
import com.formdev.flatlaf.*;
import javax.swing.filechooser.*;
import com.formdev.flatlaf.themes.*;

import static java.lang.System.*;
import static javax.swing.JOptionPane.*;
import static javax.swing.JFileChooser.*;
import static java.awt.image.BufferedImage.*;

// ............................................................................
/// Головний клас програми
/// @author Rutar_Andriy
/// 14.01.2026

public class TToolH4 extends JFrame {

private File inputFile;                                         // вхідний файл
// private File outputFile;                                    // вихідний файл

private final JFileChooser fileOpen;           // відкривання/збереження файлів
// private final JFileChooser fntCompile;               // компілювання шрифтів
private final JFileChooser fntDecompile;              // декомпілювання шрифтів
// private final JFileChooser rawCompile;                 // компілювання даних
// private final JFileChooser rawDecompile;             // декомпілювання даних

private String appDescription;                                 // опис програми
private DefaultTableModel tableModel;              // стандартна модель таблиці

private boolean dataWasChanged;                // якщо true - дані були змінені

// ............................................................................

private byte[] allBytes;                                   // всі зчитані байти
private ByteBuffer buffer;                        // буфер для зчитування даних

// Домашня директорія користувача
private final File homeDir = FileSystemView.getFileSystemView()
                                           .getHomeDirectory();

// Фільтр для файлів із розширенням *.test
private final FileNameExtensionFilter extTest =
          new FileNameExtensionFilter("Особливий тип файлу", "test");

// Фільтр для файлів із розширенням *.fnt
private final FileNameExtensionFilter extFnt =
          new FileNameExtensionFilter("Особливі файли шрифтів", "fnt");

private SearchDialog searchDialog;         // діалогове вікно пошуку інформації

public static boolean debug = true;  // якщо true - увімк. режим налагоджування

// ============================================================================
/// Конструктор за замовчуванням

public TToolH4() {

initComponents();

fileOpen = new JFileChooser();
fileOpen.setFileSelectionMode(FILES_AND_DIRECTORIES);
//fileOpen.removeChoosableFileFilter(fileOpen.getChoosableFileFilters()[0]);
fileOpen.addChoosableFileFilter(extTest);
fileOpen.setCurrentDirectory(homeDir);
//fileOpen.setSelectedFile(new File("..."));

fntDecompile = new JFileChooser();
fntDecompile.setFileSelectionMode(FILES_ONLY);
//fileOpen.removeChoosableFileFilter(fileOpen.getChoosableFileFilters()[0]);
fntDecompile.addChoosableFileFilter(extFnt);
fntDecompile.setCurrentDirectory(homeDir);
//fntDecompile.setSelectedFile(new File("..."));

}

// ============================================================================
/// Головний метод програми
/// @param args масив переданих параметрів

public static void main (String args[]) {
    
    if (args.length > 0 &&
        args[0].equals("--debug")) { debug = true; }
    
    Map<String, String> defaults = new HashMap<>();
    defaults.put("@accentColor", "#93C763");
    FlatLaf.setGlobalExtraDefaults(defaults);

    UIManager.put("MenuItem.minimumIconSize", new Dimension(0, 0));
    UIManager.put("MenuItem.selectionType", "underline");
    UIManager.put("MenuBar.selectionType", "underline");
    UIManager.put("MenuItem.iconTextGap", 0);

    try { FlatMacDarkLaf.setup(); }
    catch (Exception e) {}
    
    EventQueue.invokeLater(() -> {
        new TToolH4().setVisible(true);
    });
}

// ============================================================================
/// Відкривання файлів

private void showOpenDialog() {

int result = fileOpen.showOpenDialog(this);
if (result != JFileChooser.APPROVE_OPTION) { return; }

openTestFile();

}

// ============================================================================
/// Відкривання *.test файлів

private void openTestFile() {

prepareNewTable();

// ............................................................................

try {

ArrayList<String> newRow = new ArrayList<>();

for (int z = 1; z <= 9; z++) {
    
    newRow.clear();
    newRow.add(String.valueOf(z));
    newRow.add("Key_"   + z);
    newRow.add("Value_" + z);
    tableModel.addRow(newRow.toArray(String[]::new));

}
}

catch (Exception ex) { showMessageDialog(this, "Помилка читання файлу: " +
                                                ex.getMessage()); }

// ............................................................................
    
finalizeNewTable();

}

// ============================================================================
/// Збереження файлів

private void showSaveDialog() {

fileOpen.setSelectedFile(inputFile);
int result = fileOpen.showSaveDialog(this);
if (result != JFileChooser.APPROVE_OPTION) { return; }

saveTestFile();

}

// ============================================================================
/// Збереження *.test файлів

private void saveTestFile() {

}

// ============================================================================
/// Відображення інформації про програму

private void showInfoDialog() {

// Отримуємо текст опису програми
if (appDescription == null) {

URL descriptionUrl = getClass().getResource("others/appDescription.txt");
URL channelUrl     = getClass().getResource("others/channelURL.txt");
URL manifestUrl    = getClass().getClassLoader()
                    .getResource("META-INF/MANIFEST.MF");

try (InputStream desc = descriptionUrl.openStream();
     InputStream link = channelUrl    .openStream();
     InputStream data = manifestUrl   .openStream()) {

Attributes attributes = new Manifest(data).getMainAttributes();
    
String channelURL = new String(link.readAllBytes(), StandardCharsets.UTF_8);
String appVersion = attributes.getValue("Version");
String buildDate  = attributes.getValue("Build-Date");

appVersion = (appVersion == null) ? "0.0.1" : appVersion;
buildDate  = (buildDate  == null) ? "25.04.1995" : buildDate.split(" ")[0];

appDescription = new String(desc.readAllBytes(), StandardCharsets.UTF_8)
                    .formatted(channelURL, appVersion, buildDate); }

catch (IOException _) {} }

// ............................................................................

JEditorPane pane = new JEditorPane("text/html", appDescription);
pane.setEditable(false);
pane.setFocusable(false);

pane.addHyperlinkListener((HyperlinkEvent e) -> {
    if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
        try { Desktop.getDesktop().browse(e.getURL().toURI()); }
        catch (IOException | URISyntaxException _) { }
    }
});

showMessageDialog(this, pane, "Про програму", INFORMATION_MESSAGE);

}

// ============================================================================
/// Відображення вікна пошуку інформації

private void showSearchDialog() {
        
    if (searchDialog == null) { searchDialog = new SearchDialog(this); }    
    searchDialog.setVisible(true);

}

// ============================================================================
/// Відображення вікна підтвердження виходу

private void showExitDialog() {

// Якщо дані не змінювалися - просто виходимо
if (!dataWasChanged) { System.exit(0); }

String saveDataQuestion = """
    Ви бажаєте вийти з програми?
    Усі незбережені дані буде втрачено
    """;

int answer = showConfirmDialog(this, saveDataQuestion,
                              "Підтвердження виходу", YES_NO_OPTION);

if (answer == YES_OPTION) { System.exit(0); }

}

// ============================================================================
/// Вибір шрифту для розпакування

private void showDecompileFontDialog() {

int result = fntDecompile.showOpenDialog(this);
if (result != JFileChooser.APPROVE_OPTION) { return; }

inputFile = fntDecompile.getSelectedFile();

try { allBytes = Files.readAllBytes(inputFile.toPath()); }

catch (IOException e) { out.println("Помилка розпаковування шрифту: "
                                   + e.getMessage());
                        return; }

// ............................................................................

byte[] data;
buffer = ByteBuffer.wrap(allBytes);
buffer.order(ByteOrder.LITTLE_ENDIAN);

// ...
if (debug) { out.println("..."); }

int color;
int width = 25;
int height = 25;
File imgFile = new File(homeDir.getPath() + "/imgTest.bmp");
BufferedImage image = new BufferedImage(width, height, TYPE_3BYTE_BGR);

for (int r = 0; r < height; r++) {
for (int c = 0; c < width; c++) {
    color = 0x0000FF;
    image.setRGB(c, r, color);
}
}

try { ImageIO.write(image, "bmp", imgFile);
      if (debug) { out.println("File imgTest.bmp was written"); } }

catch (IOException e)
    { if (debug) { err.println("File imgTest.bmp error"); } }

}

// ============================================================================
/// Вибір розпакованого шрифту для пакування

private void showCompileFontDialog() {}

// ============================================================================
/// Вибір даних для розпакування

private void showDecompileRawDialog() {}

// ============================================================================
/// Вибір розпакованих даних для пакування

private void showCompileRawDialog() {}

// ============================================================================
/// Попередня ініціалізація нової таблиці

private void prepareNewTable() {

dataWasChanged = false;
inputFile = fileOpen.getSelectedFile();
sp_table.getVerticalScrollBar().setValue(0);

tableModel = new DefaultTableModel() {
    @Override
    public boolean isCellEditable (int row, int column) { return column >= 2; }
};

tbl_main.setModel(tableModel);

tableModel.addColumn("№");
tableModel.addColumn("Ключ");
tableModel.addColumn("Значення");

}

// ============================================================================
/// Завершальна ініціалізація нової таблиці

private void finalizeNewTable() {

TableColumn tColumn;

CellRender cellRenderer = new CellRender();
cellRenderer.setHorizontalAlignment(SwingConstants.CENTER);

tColumn = tbl_main.getColumnModel().getColumn(0);
tColumn.setCellRenderer(cellRenderer);
tColumn.setPreferredWidth(45);
tColumn.setResizable(false);

for (int z = 1; z < tbl_main.getColumnCount(); z++) {
    tbl_main.getColumnModel().getColumn(z).setCellRenderer(new CellRender());
    tbl_main.getColumnModel().getColumn(z).setPreferredWidth(175);    
}

// ............................................................................

setTableInfo();

tableModel.addTableModelListener((TableModelEvent e) -> {
    dataWasChanged = true;
});

}

// ============================================================================
/// Оновлення інформації про таблицю

private void setTableInfo() {

String tmp;
    
tmp = lbl_rowCount.getText();
tmp = tmp.substring(0, tmp.indexOf(":") + 1) + " "
                  + tableModel.getRowCount();
lbl_rowCount.setText(tmp);

tmp = lbl_colCount.getText();
tmp = tmp.substring(0, tmp.indexOf(":") + 1) + " "
                  + tableModel.getColumnCount();
lbl_colCount.setText(tmp);
    
}

// ============================================================================
/// Цей метод викликається з конструктора для ініціалізації форми.
/// УВАГА: НЕ змінюйте цей код. Вміст цього методу завжди 
/// перезапишеться редактором форм

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sp_table = new JScrollPane();
        tbl_main = new JTable();
        pnl_footer = new JPanel();
        lbl_colCount = new JLabel();
        lbl_rowCount = new JLabel();
        mnb_main = new JMenuBar();
        mn_file = new JMenu();
        mni_open = new JMenuItem();
        mni_save = new JMenuItem();
        sep_one = new JPopupMenu.Separator();
        mni_find = new JMenuItem();
        sep_two = new JPopupMenu.Separator();
        mni_exit = new JMenuItem();
        mn_edit = new JMenu();
        mni_fntDecompile = new JMenuItem();
        mni_fntCompile = new JMenuItem();
        sep_three = new JPopupMenu.Separator();
        mni_rawDecompile = new JMenuItem();
        mni_rawCompile = new JMenuItem();
        mn_info = new JMenu();
        mni_about = new JMenuItem();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("TTool_H4");
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                onWindowClose(evt);
            }
        });

        tbl_main.setModel(new DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        tbl_main.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbl_main.setAutoscrolls(false);
        tbl_main.setIntercellSpacing(new Dimension(2, 2));
        tbl_main.setRowSelectionAllowed(false);
        tbl_main.setShowGrid(true);
        tbl_main.getTableHeader().setReorderingAllowed(false);
        sp_table.setViewportView(tbl_main);

        pnl_footer.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 5));

        lbl_colCount.setText("Кількість стовбців: 0");
        pnl_footer.add(lbl_colCount);

        lbl_rowCount.setText("Кількість рядків: 0");
        pnl_footer.add(lbl_rowCount);

        mn_file.setText("Файл");

        mni_open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        mni_open.setText("Відкрити");
        mni_open.setActionCommand("open");
        mni_open.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onMenuClick(evt);
            }
        });
        mn_file.add(mni_open);

        mni_save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        mni_save.setText("Зберегти");
        mni_save.setActionCommand("save");
        mni_save.setEnabled(false);
        mni_save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onMenuClick(evt);
            }
        });
        mn_file.add(mni_save);
        mn_file.add(sep_one);

        mni_find.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
        mni_find.setText("Пошук");
        mni_find.setActionCommand("find");
        mni_find.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onMenuClick(evt);
            }
        });
        mn_file.add(mni_find);
        mn_file.add(sep_two);

        mni_exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
        mni_exit.setText("Вихід");
        mni_exit.setActionCommand("exit");
        mni_exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onMenuClick(evt);
            }
        });
        mn_file.add(mni_exit);

        mnb_main.add(mn_file);

        mn_edit.setText("Правка");

        mni_fntDecompile.setText("Розпакувати шрифт");
        mni_fntDecompile.setActionCommand("decompileFont");
        mni_fntDecompile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onMenuClick(evt);
            }
        });
        mn_edit.add(mni_fntDecompile);

        mni_fntCompile.setText("Запакувати шрифт");
        mni_fntCompile.setActionCommand("compileFont");
        mni_fntCompile.setEnabled(false);
        mni_fntCompile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onMenuClick(evt);
            }
        });
        mn_edit.add(mni_fntCompile);
        mn_edit.add(sep_three);

        mni_rawDecompile.setText("Розпакувати дані");
        mni_rawDecompile.setActionCommand("decompileRaw");
        mni_rawDecompile.setEnabled(false);
        mni_rawDecompile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onMenuClick(evt);
            }
        });
        mn_edit.add(mni_rawDecompile);

        mni_rawCompile.setText("Запакувати дані");
        mni_rawCompile.setActionCommand("compileRaw");
        mni_rawCompile.setEnabled(false);
        mni_rawCompile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onMenuClick(evt);
            }
        });
        mn_edit.add(mni_rawCompile);

        mnb_main.add(mn_edit);

        mn_info.setText("Інфо");

        mni_about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
        mni_about.setText("Про програму");
        mni_about.setActionCommand("info");
        mni_about.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onMenuClick(evt);
            }
        });
        mn_info.add(mni_about);

        mnb_main.add(mn_info);

        setJMenuBar(mnb_main);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(sp_table, GroupLayout.DEFAULT_SIZE, 588, Short.MAX_VALUE)
                    .addComponent(pnl_footer, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sp_table, GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnl_footer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

// ============================================================================
/// Прослуховування пунктів меню програми

    private void onMenuClick(ActionEvent evt) {//GEN-FIRST:event_onMenuClick

    switch (evt.getActionCommand()) {

        case "open" -> showOpenDialog();
        case "save" -> showSaveDialog();
        case "find" -> showSearchDialog();
        case "exit" -> showExitDialog();
        case "info" -> showInfoDialog();

        case "decompileFont" -> showDecompileFontDialog();
        case "compileFont"   -> showCompileFontDialog();
        case "decompileRaw"  -> showDecompileRawDialog();
        case "compileRaw"    -> showCompileRawDialog();

    }   
    }//GEN-LAST:event_onMenuClick

// ============================================================================
/// Прослуховування закривання вікна

    private void onWindowClose(WindowEvent evt) {//GEN-FIRST:event_onWindowClose
        showExitDialog();
    }//GEN-LAST:event_onWindowClose

// ============================================================================
/// Список усіх об'явлених змінних

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel lbl_colCount;
    private JLabel lbl_rowCount;
    private JMenu mn_edit;
    private JMenu mn_file;
    private JMenu mn_info;
    private JMenuBar mnb_main;
    private JMenuItem mni_about;
    private JMenuItem mni_exit;
    private JMenuItem mni_find;
    private JMenuItem mni_fntCompile;
    private JMenuItem mni_fntDecompile;
    private JMenuItem mni_open;
    private JMenuItem mni_rawCompile;
    private JMenuItem mni_rawDecompile;
    private JMenuItem mni_save;
    private JPanel pnl_footer;
    private JPopupMenu.Separator sep_one;
    private JPopupMenu.Separator sep_three;
    private JPopupMenu.Separator sep_two;
    private JScrollPane sp_table;
    public JTable tbl_main;
    // End of variables declaration//GEN-END:variables

// Кінець класу TToolH4 =======================================================

}
