package com.rutar.ttool_h4;

import java.io.*;
import java.awt.*;
import java.net.*;
import java.nio.*;
import java.util.*;
import javax.swing.*;
import java.nio.file.*;
import java.util.jar.*;
import javax.imageio.*;
import java.awt.image.*;
import java.awt.event.*;
import java.nio.charset.*;
import javax.swing.event.*;
import javax.swing.table.*;
import com.formdev.flatlaf.*;
import javax.swing.filechooser.*;
import com.rutar.ua_translator.*;
import com.formdev.flatlaf.themes.*;

import static java.io.File.*;
import static java.nio.ByteOrder.*;
import static javax.swing.JOptionPane.*;
import static javax.swing.JFileChooser.*;

// ............................................................................
/// Головний клас програми
/// @author Rutar_Andriy
/// 13.02.2026

public class TToolH4 extends JFrame {

private File inputFile;                                         // вхідний файл
private File outputFile;                                       // вихідний файл

private final JFileChooser fileOpen;           // відкривання/збереження файлів
private final JFileChooser fntCompile;                  // компілювання шрифтів
private final JFileChooser fntDecompile;              // декомпілювання шрифтів
private final JFileChooser rawCompile;                    // компілювання даних
private final JFileChooser rawDecompile;                // декомпілювання даних

private String appDescription;                                 // опис програми
private DefaultTableModel tableModel;              // стандартна модель таблиці

private File tmpFile;                                       // допоміжна змінна

private ByteBuffer buffer;                        // буфер для зчитування даних
private SearchDialog searchDialog;         // діалогове вікно пошуку інформації

private boolean dataWasChanged;                // якщо true - дані були змінені
private boolean originalCampagain;    // якщо true - файл є офіційною кампанією

private final ArrayList<DataBlock> blocks = new ArrayList<>();   // блоки даних
private final ArrayList<byte[]> preperedData = new ArrayList<>();  // обр. дані

// ............................................................................

public static byte[] allBytes;                             // всі зчитані байти
public static byte[] endBytes;                               // залишкові байти
public static int procPosition;                // поточна позиція обробки даних
public static String fileExt;

// Домашня директорія користувача
public static final File HOME_DIR = FileSystemView.getFileSystemView()
                                                  .getHomeDirectory();

public static boolean debug = false; // якщо true - увімк. режим налагоджування

// ============================================================================
/// Конструктор за замовчуванням

public TToolH4() {

initComponents();
initAppIcons();

fileOpen     = Utils.getFileChooser(FILES_ONLY, Map.of
                                   ("h4c", "H4 файли кампанії",
                                    "txt", "H4 файли локалізації"));
fntCompile   = Utils.getFileChooser(DIRECTORIES_ONLY,
                                    "fnt", "H4 файли шрифтів");
fntDecompile = Utils.getFileChooser(FILES_ONLY,
                                    "fnt", "H4 файли шрифтів");
rawCompile   = Utils.getFileChooser(FILES_ONLY,
                                    "bmp", "H4 розпаковані файли зображень");
rawDecompile = Utils.getFileChooser(FILES_ONLY,
                                    "raw", "H4 запаковані файли зображень");

}

// ============================================================================
/// Головний метод програми
/// @param args масив переданих параметрів

public static void main (String args[]) {
    
    if (args.length > 0 &&
        args[0].equals("--debug")) { debug = true; }
    
    // ........................................................................
    
    UATranslator.init();
    UIManager.put("FileChooser.readOnly", true);

    JFrame .setDefaultLookAndFeelDecorated(true);
    JDialog.setDefaultLookAndFeelDecorated(true);
    
    FlatLaf.registerCustomDefaultsSource("com.rutar.ttool_h4.themes");

    try { FlatMacDarkLaf.setup(); }
    catch (Exception _) {}
    
    // ........................................................................
    
    SwingUtilities.invokeLater(() ->
      { var window = new TToolH4();
        window.setVisible(true);
        SwingUtilities.invokeLater(() ->
          { window.setMinimumSize(window.getSize()); }); });
}

// ============================================================================
/// Відкривання файлів

private void showOpenDialog() {

// Дані змінилися - запитуємо чи відкривати новий файл
if (dataWasChanged) { 

String saveDataQuestion = """
  У відкритому файлі присутні зміни. При відкриванні
  нового файлу вони будуть втрачені. Бажаєте продовжити?
  """;

int answer = showConfirmDialog(this, saveDataQuestion,
                              "Повідомлення", YES_NO_OPTION);

if (answer != YES_OPTION) { return; }

}

// ............................................................................

int result = fileOpen.showOpenDialog(this);
if (result != JFileChooser.APPROVE_OPTION) { return; }

String[] split = fileOpen.getSelectedFile().getName().split("\\.");
fileExt = split[split.length - 1];

switch (fileExt) { case "txt" -> openTxtFile();
                   case "h4c" -> openH4cFile(); }

updateAppTitle();

}

// ============================================================================
/// Відкривання *.txt файлів

private void openTxtFile() {

String value;
byte[] bytes;
short valSize;

// ............................................................................

prepareNewTable(false);
DataBlock.BLOCK_ID = 0;
dataWasChanged = false;
ArrayList<String> newRow = new ArrayList<>();

try {

inputFile = fileOpen.getSelectedFile();
allBytes = Files.readAllBytes(inputFile.toPath());

buffer = ByteBuffer.wrap(allBytes);
buffer.order(ByteOrder.LITTLE_ENDIAN);

int rowCount = buffer.getInt();

// ............................................................................

for (int z = 0; z < rowCount; z++) {

  // Кількість клітинок у даному рядку
  int colCount = buffer.getShort();

  // Додавання нових стовбців, якщо потрібно
  while (colCount + 2 > tbl_main.getColumnCount())
      { int charCode = 65 - 3 + tbl_main.getColumnCount();
        tableModel.addColumn(new String(Character.toChars(charCode))); }

  newRow.clear();                          // очищення даних
  newRow.add(String.valueOf(z + 1));       // номер рядка
  newRow.add(String.valueOf(colCount));    // кількість клітинок в рядку
    
  for (int q = 0; q < colCount; q++) {     // дані для перекладу

    valSize = buffer.getShort();           // розмір рядка
    bytes = new byte[valSize];             // створення масиву байт
    buffer.get(bytes);                     // зчитування байтів у масив
    value = new String(bytes, "Cp1251");   // перетворення байтів на текст
    newRow.add(value);                     // додавання тексту в масив рядків
  }
    
    tableModel.addRow(newRow.toArray(String[]::new));
    
}

// Збереження решти байт - їх не потрібно обробляти
endBytes = new byte[buffer.remaining()];
buffer.get(endBytes);

finalizeNewTable(false);

}

// ............................................................................

catch (IOException _)
  { showMessageDialog(this, "При обробці файлу відбулася критична помилка",
                            "Помилка", ERROR_MESSAGE); }
}

// ============================================================================
/// Відкривання *.h4c файлів

private void openH4cFile() {

prepareNewTable(true);
inputFile = fileOpen.getSelectedFile();
ArrayList<String> newRow = new ArrayList<>();

try {

// Зчитування всіх байтів файлу
allBytes = Files.readAllBytes(inputFile.toPath());
originalCampagain = Utils.isOriginalCampagain();

// Очищуємо попередні дані
procPosition = 0;
DataBlock.BLOCK_ID = 0;
dataWasChanged = false;
preperedData.clear();
blocks.clear();

// ............................................................................
// Розкладання файлу на значущі частини

while (procPosition < allBytes.length)
  { if (Utils.isNextGzipArchive(procPosition))
      { blocks.add(new DataBlock(Utils.readUntilArchiveEnd())); }
    else
      { blocks.add(new DataBlock(Utils.readUntilArchiveStart())); } }

// ............................................................................
// Заповнення таблиці даними для перекладу

int currentBlock = 0, currentLine;

for (DataBlock block : blocks) {
    
  currentLine = 0;                                     // скидання ном. рядка

  newRow.clear();                                      // очищення даних
  newRow.add("");                                      // пустий рядок
  newRow.add("--- Блок №" + ++currentBlock + " ---");  // номер блоку
  tableModel.addRow(newRow.toArray(String[]::new));    // додавання рядка
    
  // Додаємо усі рядки з конкретного блоку
  for (String text : block.getStrings()) {
    newRow.clear();                                    // очищення даних
    newRow.add(String.valueOf(++currentLine));         // додавання номеру
    newRow.add(text);                                  // додавання тексту
    tableModel.addRow(newRow.toArray(String[]::new));  // додавання рядка
  } 
}

finalizeNewTable(true);

}

// ............................................................................

catch (IOException e)
  { showMessageDialog(this, "При відкриванні файлу кампанії сталася "
                          + "критична помилка", "Помилка", ERROR_MESSAGE); }
}

// ============================================================================
/// Збереження файлів

private void showSaveDialog() {

fileOpen.setSelectedFile(inputFile);
int result = fileOpen.showSaveDialog(this);
if (result != JFileChooser.APPROVE_OPTION) { return; }

switch (fileExt) { case "txt" -> saveTxtFile();
                   case "h4c" -> saveH4cFile(); }

}

// ============================================================================
/// Збереження *.txt файлів

private void saveTxtFile() {

try {

outputFile = fileOpen.getSelectedFile();

ResizableByteBuffer rBuffer = new ResizableByteBuffer(1024, LITTLE_ENDIAN);
rBuffer.putInt(tbl_main.getRowCount());

// Зчитування даних з таблиці
for (int z = 0; z < tbl_main.getRowCount(); z++) {

  // Кількість клітинок у даному рядку таблиці
  short count = Short.parseShort((String)tbl_main.getValueAt(z, 1));
  rBuffer.putShort(count);
    
  // Записування вмісту клітинок в буфер
  for (int q = 0; q < count; q++) {

    String value = (String) tbl_main.getValueAt(z, q + 2);
    value = Utils.replaceUnusedChars(value);
    rBuffer.putH4String(value, "cp1251");
  }
}

// Записування збережених необроблених байтів
rBuffer.putBytes(endBytes);

try (FileOutputStream fos = new FileOutputStream(outputFile, false))
  { fos.write(rBuffer.getByteArray()); }

dataWasChanged = false;
updateAppTitle();

showMessageDialog(this, "Файл " + outputFile.getName() + " успішно збережено",
                        "Повідомлення", INFORMATION_MESSAGE); }

// ............................................................................

catch (HeadlessException | IOException _)
  { showMessageDialog(this, "При збереженні файлу відбулася критична "
                          + "помилка", "Помилка", ERROR_MESSAGE); }
}

// ============================================================================
/// Збереження *.h4c файлів

private void saveH4cFile() {

try {

outputFile = fileOpen.getSelectedFile();
int currentLine = 0;

// ............................................................................
// Зчитування даних з таблиці та перетворення їх у бінарні рядки

for (DataBlock block : blocks)
  { currentLine++;
    String[] strings = block.getStrings();
    for (int q = 0; q < strings.length; q++)
      { strings[q] = (String) tbl_main.getValueAt(currentLine++, 1);
        strings[q] = Utils.replaceUnusedChars(strings[q]); }
    block.recalculateStrings(); }

// Перетворення блокових даних у бінарний вигляд
for (DataBlock block : blocks) { preperedData.add(block.getRawData()); }

// Запис метаданих заголовку для оригінальних кампаній
if (originalCampagain) { Utils.setHeaderMetadata(preperedData, blocks); }

// ............................................................................
// Запис бінарних даних у файл

try (FileOutputStream fos = new FileOutputStream(outputFile))
  { for (int blockNum = 0; blockNum < preperedData.size(); blockNum++)
      { fos.write(preperedData.get(blockNum)); } }

dataWasChanged = false;
updateAppTitle();

showMessageDialog(this, "Файл " + outputFile.getName() + " успішно збережено",
                        "Повідомлення", INFORMATION_MESSAGE);

}

// ............................................................................

catch (HeadlessException | IOException _)
  { showMessageDialog(this, "При збереженні файлу відбулася критична "
                          + "помилка", "Помилка", ERROR_MESSAGE); }

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

private void showSearchDialog()
  { searchDialog = new SearchDialog(this);   
    searchDialog.setVisible(true); }

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
String path = null;

try { allBytes = Files.readAllBytes(inputFile.toPath()); }
catch (IOException e)
  { showMessageDialog(this, "Помилка читання шрифту: ", "Помилка", 0);
                      return; }

// ............................................................................

int w, h, color;                // ширина та висота зображення, колір пікселя
BufferedImage image;            // об'єкт зображення
File dataFile = null;           // допоміжний файл-дескриптор
byte[] palette = new byte[8];   // палітра зображення

buffer = ByteBuffer.wrap(allBytes);
buffer.order(ByteOrder.LITTLE_ENDIAN);

// Створення папки для розшифрованих символів
try { path = inputFile.getParent() + separator;
      path += inputFile.getName().replace(".fnt", separator);
      File dir = new File(path);
      dataFile = new File(path + "font.dat");
      dir.mkdir(); }
catch (Exception e) { System.err.println("Creating dir error"); }

// Запис зображень кожного символа та їхніх даних у файл-дескриптор
try (FileOutputStream fos = new FileOutputStream(dataFile);
     BufferedOutputStream bos = new BufferedOutputStream(fos)) {

byte[] fontHeader = new byte[7];
buffer.get(fontHeader);                               // Запис заголовку шрифта

int charCount = Byte.toUnsignedInt(fontHeader[6]);        // кількість символів
bos.write(fontHeader);

for (int z = 0; z < charCount; z++) {
    
  w = buffer.getInt();   // ширина символу
  h = buffer.getInt();   // висота символу
  buffer.get(palette);   // палітра символу
  bos.write(palette);
    
  image = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);

  for (int r = 0; r < h; r++) {
  for (int c = 0; c < w; c++) {
    color = Byte.toUnsignedInt(buffer.get());
    color = (color << 16) | (color << 8) | color;
    image.setRGB(c, r, color);
  }
  }
    
  try { String num = String.format("%03d", z + 1);
        File output = new File(path + num + ".bmp");
        ImageIO.write(image, "bmp", output); }
    
  catch (IOException e)
    { showMessageDialog(this, "Помилка запису символа №" + (z + 1),
                              "Помилка", 0);
      return; }
}

if (debug) { System.out.println("Розпаковано " + charCount + " символів"); }
showMessageDialog(this, "Шрифт успішно розпаковано!");

}

// ............................................................................

catch (Exception _)
  { showMessageDialog(this, "Під час розпакування шрифта відбулася критична "
                          + "помилка", "Помилка", ERROR_MESSAGE); }
}

// ============================================================================
/// Вибір розпакованого шрифту для пакування

private void showCompileFontDialog() {

tmpFile = Utils.getLastDir(fntDecompile);
if (tmpFile != null) { fntCompile.setCurrentDirectory(tmpFile); }

int result = fntCompile.showOpenDialog(this);
if (result != JFileChooser.APPROVE_OPTION) { return; }

inputFile = fntCompile.getSelectedFile();
String path = inputFile.getAbsolutePath();

outputFile = new File(path + ".fnt");

// Збирання шрифта із окремих символів
try (FileOutputStream fos = new FileOutputStream(outputFile);
     BufferedOutputStream bos = new BufferedOutputStream(fos)) {

  FileInputStream fis = new FileInputStream(path + "/font.dat");
  byte[] header = fis.readNBytes(7);
  int charCount = Byte.toUnsignedInt(header[6]);
  bos.write(header); // запис заголовку
     
  int w, h;
  BufferedImage image;
  ByteBuffer byteBuffer = ByteBuffer.allocate(4);
  byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
     
  // Проходження по кожному символу
  for (int z = 0; z < charCount; z++) {

    String num = String.format("%03d", z + 1);
    image = ImageIO.read(new File(path + separator + num + ".bmp"));

    w = image.getWidth();
    h = image.getHeight();

    // Записування ширини та вислти зображення
    bos.write(byteBuffer.clear().putInt(w).flip().array());
    bos.write(byteBuffer.clear().putInt(h).flip().array());

    // Записування палітри зображення
    bos.write(fis.readNBytes(8));

    // Отримання даних у вигляді масиву байт
    byte[] imageData = ((DataBufferByte)(image.getRaster()
                                              .getDataBuffer()))
                                              .getData();

    // Записування даних у файл
    byte[] writable = new byte[imageData.length / 3];
    for (int pixel = 0; pixel < writable.length; pixel++)
       { writable[pixel] = imageData[pixel * 3]; }
    bos.write(writable);
  }

if (debug) { System.out.println("Запаковано " + charCount + " символів"); }
showMessageDialog(this, "Шрифт успішно запаковано!");

}

// ............................................................................

catch (Exception _)
  { showMessageDialog(this, "Під час пакування шрифта відбулася критична "
                          + "помилка", "Помилка", ERROR_MESSAGE); }
}

// ============================================================================
/// Вибір зашифрованого зображення для розшифрування

private void showDecompileRawDialog() {

int result = rawDecompile.showOpenDialog(this);
if (result != JFileChooser.APPROVE_OPTION) { return; }

inputFile = rawDecompile.getSelectedFile();
String path = null;

try { allBytes = Files.readAllBytes(inputFile.toPath()); }
catch (IOException e)
  { showMessageDialog(this, "Помилка читання *.raw файлу", "Помилка", 0);
    return; }

buffer = ByteBuffer.wrap(allBytes);
buffer.order(ByteOrder.LITTLE_ENDIAN);

int R, G, B, RGB;      // допоміжні змінні
BufferedImage image;   // об'єкт зображення

try { path = inputFile.getParent() + "/";
      path += inputFile.getName().replace(".raw", ".bmp"); }
catch (Exception _) { }

int imagesCount = buffer.getInt();         // кількість зображень у *.raw файлі
int h = buffer.getInt();                                   // висота зображення
int w = buffer.getInt();                                   // ширина зображення
int dataCount = buffer.getInt();                    // загальна кількість даних

image = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);

// Створення зображення на основі "сирих" даних
for (int r = 0; r < h; r++) {
for (int c = 0; c < w; c++) {
  B = Byte.toUnsignedInt(buffer.get());
  G = Byte.toUnsignedInt(buffer.get());
  R = Byte.toUnsignedInt(buffer.get());
  RGB = (R << 16) | (G << 8) | B;
  image.setRGB(c, r, RGB);
}
}

// ............................................................................

try { File output = new File(path);
      ImageIO.write(image, "bmp", output); }
catch (IOException e)
  { showMessageDialog(this, "Помилка запису розпакованого зображення",
                            "Помилка", 0);
    return; }

// ............................................................................

showMessageDialog(this, "Raw-зображення успішно розшифровано!");

}

// ============================================================================
/// Вибір розшифрованого зображення для шифрування

private void showCompileRawDialog() {

tmpFile = Utils.getLastDir(rawDecompile);
if (tmpFile != null) { rawCompile.setCurrentDirectory(tmpFile); }

int result = rawCompile.showOpenDialog(this);
if (result != JFileChooser.APPROVE_OPTION) { return; }

inputFile = rawCompile.getSelectedFile();
String path = null;

BufferedImage image;   // об'єкт зображення

try { path = inputFile.getAbsolutePath().replace(".bmp", ".raw"); }
catch (Exception _) { }

// ............................................................................

try (FileOutputStream fos = new FileOutputStream(path);
     BufferedOutputStream bos = new BufferedOutputStream(fos)) {
    
  image = ImageIO.read(inputFile);

  int RGB;                     // допоміжна змінна
  int imagesCount = 1;         // кількість зображень
  int w = image.getWidth();    // ширина зображення
  int h = image.getHeight();   // висота зображення
  int dataCount = w * h * 3;   // загальна кількість даних

  ByteBuffer byteBuffer = ByteBuffer.allocate(4);
  byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

  // Запис даних у буфер
  bos.write(byteBuffer.clear().putInt(imagesCount).flip().array());
  bos.write(byteBuffer.clear().putInt(h).flip().array());
  bos.write(byteBuffer.clear().putInt(w).flip().array());
  bos.write(byteBuffer.clear().putInt(dataCount).flip().array());
      
  for (int r = 0; r < h; r++) {
  for (int c = 0; c < w; c++) {
    RGB = image.getRGB(c, r);    // отримання кольору пікселя
    bos.write(RGB & 0xFF);       // запис значення синього каналу
    bos.write(RGB >> 8 & 0xFF);  // запис значення зеленого каналу
    bos.write(RGB >> 16 & 0xFF); // запис значення червоного каналу
  }
  }
}

// ............................................................................

catch (Exception e)
  { showMessageDialog(this, "Помилка запису запакованого зображення",
                            "Помилка", 0);
    return; }

// ............................................................................

showMessageDialog(this, "Raw-зображення успішно зашифровано!");

}

// ============================================================================
/// Попередня ініціалізація нової таблиці

private void prepareNewTable (boolean isCampagain) {

dataWasChanged = false;
inputFile = fileOpen.getSelectedFile();
sp_table.getVerticalScrollBar().setValue(0);

tableModel = new DefaultTableModel() {
    @Override
    public boolean isCellEditable (int row, int column)
        { return (fileExt.equals("txt") && column >= 3) ||
                 (fileExt.equals("h4c") && column >= 1); } };

tbl_main.setModel(tableModel);

if (isCampagain)
  { tableModel.addColumn("№");
    tableModel.addColumn("Текст для перекладу"); }
else
  { tableModel.addColumn("№");
    tableModel.addColumn("#");
    tableModel.addColumn("Ключ"); }

}

// ============================================================================
/// Завершальна ініціалізація нової таблиці

private void finalizeNewTable (boolean isCampagain) {

TableColumn tColumn;

CellRender cellRenderer = new CellRender();
cellRenderer.setHorizontalAlignment(SwingConstants.CENTER);

if (isCampagain) {
    
  tColumn = tbl_main.getColumnModel().getColumn(0);
  tColumn.setCellRenderer(cellRenderer);
  tColumn.setPreferredWidth(35);
  tColumn.setResizable(false);

  tbl_main.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
}

else {

  tColumn = tbl_main.getColumnModel().getColumn(0);
  tColumn.setCellRenderer(cellRenderer);
  tColumn.setPreferredWidth(45);
  tColumn.setResizable(false);

  tColumn = tbl_main.getColumnModel().getColumn(1);
  tColumn.setCellRenderer(cellRenderer);
  tColumn.setPreferredWidth(25);
  tColumn.setResizable(false);

  for (int z = 2; z < tbl_main.getColumnCount(); z++) {
    tColumn = tbl_main.getColumnModel().getColumn(z);
    tColumn.setCellRenderer(new CellRender());
    tColumn.setPreferredWidth(175);    
  }
}
// ............................................................................

updateTableInfo();

mni_find.setEnabled(true);
tableModel.addTableModelListener((TableModelEvent e) ->
  { mni_save.setEnabled(true);
    dataWasChanged = true;
    updateAppTitle(); });

}

// ============================================================================
/// Оновлення інформації про таблицю

private void updateTableInfo() {

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
/// Оновлення заголовку головного вікна

private void updateAppTitle() {
    
    String newTitle = !dataWasChanged ? inputFile.getName() :
                                 "* " + inputFile.getName() + " *";
    
    if (!getTitle().equals(newTitle)) { setTitle(newTitle); }
}

// ============================================================================
/// Встановлення іконок для головного вікна

private void initAppIcons() {

    BufferedImage icon;
    ArrayList<Image> appIcons = new ArrayList<>();

    try {
        
    for (String resource : new String[] { "icon_16.png",
                                          "icon_32.png" }) {
        resource = "icons/" + resource;
        icon = ImageIO.read(getClass().getResourceAsStream(resource));
        appIcons.add(icon); }
    
    setIconImages(appIcons); }
    
    catch (IOException _) { }
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
    mni_find.setEnabled(false);
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
    mni_fntCompile.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        onMenuClick(evt);
      }
    });
    mn_edit.add(mni_fntCompile);
    mn_edit.add(sep_three);

    mni_rawDecompile.setText("Розшифрувати *.raw файл");
    mni_rawDecompile.setActionCommand("decompileRaw");
    mni_rawDecompile.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        onMenuClick(evt);
      }
    });
    mn_edit.add(mni_rawDecompile);

    mni_rawCompile.setText("Зашифрувати *.raw файл");
    mni_rawCompile.setActionCommand("compileRaw");
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
