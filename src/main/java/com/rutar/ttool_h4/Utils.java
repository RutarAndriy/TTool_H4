package com.rutar.ttool_h4;

import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import org.apache.commons.compress.compressors.gzip.*;

import static com.rutar.ttool_h4.TToolH4.*;

// ............................................................................
/// Корисні допоміжні методи
/// @author Rutar_Andriy
/// 13.02.2026

public class Utils {

// ============================================================================
/// Перевірка, чи завантажений файл є оригінальною кампанією
/// @return якщо true - файл є оригінальною кампанією

public static boolean isOriginalCampagain() {

final byte[] mask = { 0x48, 0x34, 0x43, 0x41, 0x4D,
                      0x50, 0x41, 0x49, 0x47, 0x4E };

return Arrays.equals(mask, Arrays.copyOfRange(allBytes, 16, 26));

}

// ============================================================================
/// Перевірка, чи починається gzip-архів з визначеної позиції
/// @param position позиція для перевірки
/// @return якщо true - з визначеної позиції починається gzip-архів

public static boolean isNextGzipArchive (int position) {
    
return allBytes[position]   == (byte) 0x1F &&
       allBytes[position+1] == (byte) 0x8B &&
       allBytes[position+2] == (byte) 0x08;
}

// ============================================================================
/// Зчитування даних, поки не почнеться gzip-архів
/// @return масив прочитаних даних

public static byte[] readUntilArchiveStart() {

int sPosition = procPosition;
int ePosition = sPosition;

while (!isNextGzipArchive(ePosition)) { ePosition++; }

procPosition = ePosition;
return Arrays.copyOfRange(allBytes, sPosition, ePosition);

}

// ============================================================================
/// Зчитування даних, поки не закінчиться gzip-архів
/// @return масив прочитаних даних  

public static byte[] readUntilArchiveEnd() {
    
try (var bis = new ByteArrayInputStream(allBytes, procPosition,
                                        allBytes.length);
     var gcis = new GzipCompressorInputStream(bis)) {
    
    int sPosition = procPosition;
    
    int len;
    byte[] tmp = new byte[4096];
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    
    while ((len = gcis.read(tmp)) > 0) {
        bos.write(tmp, 0, len);
    }
    
    int ePosition = allBytes.length - bis.available();
    procPosition = ePosition;

    return Arrays.copyOfRange(allBytes, sPosition, ePosition);

}

catch (Exception e) { System.err.println("GZIP decompress error");
                      return null; }

}

// ============================================================================
/// Додавання метаданих до заголовку кампаній
/// @param preperedData масив даних, підготовлених для запису в файл
/// @param blocks масив блоків даних

public static void setHeaderMetadata (ArrayList<byte[]> preperedData,
                                      ArrayList<DataBlock> blocks) {

int totalSize = 0;
byte[] header = preperedData.getFirst();

for (int z = 0; z < preperedData.size(); z++) {

    // Оновлюємо загальний розмір даних
    totalSize += preperedData.get(z).length;
    
    // Якщо це не навчальна кампанія - записуємо у заголовок метадані
    if (z > 0 && blocks.getFirst().getStrings().length > 0) {
        
        // Розраховуємо позицію та дані для запису
        int pos = header.length - (preperedData.size() - z) * 4;
        byte[] data = Utils.getBytesByInteger(preperedData.get(z).length);
        
        // Записуємо метадані в заголовок
        setBytesFromPosition(header, data, pos); } }

// Оскільки розмір кампанії вказаний як int, починаючи з 12 позиції,
// необхідно від загального розміру файлу відняти (12 + 4) байт
totalSize -= 16;

// Задаємо загальний розмір даних у заголовку кампанії
setBytesFromPosition(header, Utils.getBytesByInteger(totalSize), 12);

}

// ============================================================================
/// Вставлення масиву байт у вихідний масив у заданій позиції

private static void setBytesFromPosition (byte[] destination,
                                          byte[] data, int position)
    { System.arraycopy(data, 0, destination, position, data.length); }

// ============================================================================
/// Виведення байтового масиву в консоль у вигляді hex-значень
/// @param array байтовий масив для виведення в консоль

public static void printAsHex (byte[] array) {

for (int q = 0; q < array.length; q++)
    { System.out.print(" " + String.format("%02X", array[q]));
      if ((q+1) % 8  == 0) { System.out.print(" ");  }
      if ((q+1) % 16 == 0) { System.out.println(""); } } }

// ============================================================================
/// Перетворення байтового масиву в тип short
/// @param bytes байтовий масив для перетворення
/// @return об'єкт типу short

public static short getShortByBytes (byte[] bytes) {
    return (short) ((bytes[1] & 0xFF) << 8 | bytes[0] & 0xFF);
}

// ============================================================================
/// Перетворення числа типу short в байтовий масив
/// @param value число типу short
/// @return байтовий масив

public static byte[] getBytesByShort (short value) {
    return new byte[] { (byte)(value), (byte)(value >> 8) };
}

// ============================================================================
/// Перетворення числа типу int в байтовий масив
/// @param value число типу int
/// @return байтовий масив

public static byte[] getBytesByInteger (int value) {
    return new byte[] { (byte)(value),       (byte)(value >> 8),
                        (byte)(value >> 16), (byte)(value >> 24) };
}

// ============================================================================
/// Отримання налаштованого JFileChooser'а
/// @param selectionMode тип виділення (папки, файли, папки+файли)
/// @param ext розширення файлів
/// @param desc опис розширення файлів
/// @return налаштований екземпляр JFileChooser'а

public static JFileChooser getFileChooser (int selectionMode,
                                           String ext, String desc)
    { return getFileChooser(selectionMode, Map.of(ext, desc)); }

// ============================================================================
/// Отримання налаштованого JFileChooser'а
/// @param selectionMode тип виділення (папки, файли, папки+файли)
/// @param filters масив розширень та описів файлів
/// @return налаштований екземпляр JFileChooser'а

public static JFileChooser getFileChooser (int selectionMode,
                                           Map<String, String> filters) {
    
    JFileChooser chooser = new JFileChooser();
    chooser.setFileSelectionMode(selectionMode);
    chooser.removeChoosableFileFilter(chooser
           .getChoosableFileFilters()[0]);
    chooser.setCurrentDirectory(HOME_DIR);
    
    filters.forEach((ext, desc) ->
        { FileNameExtensionFilter f = new FileNameExtensionFilter(desc, ext);
          chooser.addChoosableFileFilter(f); });
    
    return chooser;

}

// ============================================================================
/// Отримання папки, у якій міститься останній виділений файл/папка
/// @param chooser jFileChooser, який використовувався для вибору файлу
/// @return папка, у якій міститься останній виділений файл/папка

public static File getLastDir (JFileChooser chooser) {
    
    File file = chooser.getSelectedFile();
    
    // Якщо останього файлу немає - повертаємо null
    if (file == null)
        { return null; }
    // Якщо останній файл є папкою - повертаємо батьківську папку
    else if (file.isDirectory())
        { return new File(file.getParent()); }
    // Якщо останній файл є файлом - повертаємо шлях до його папки
    else
        { return new File(file.getPath().replace(file.getName(), "")); }

}

// ============================================================================
/// Заміна невикористовуваних символів у тексті
/// @param value текст із невикористовуваними символами
/// @return текст із заміненими символами

public static String replaceUnusedChars (String value) {
    
    return value.replace('‘', '\'')
                .replace('’', '\'')
                .replace('Ґ', 'Г')
                .replace('ґ', 'г');
}

// Кінець класу Utils =========================================================

}
