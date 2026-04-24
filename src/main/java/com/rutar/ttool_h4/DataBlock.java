package com.rutar.ttool_h4;

import java.io.*;
import java.util.*;
import org.apache.commons.compress.compressors.gzip.*;

import static com.rutar.ttool_h4.TToolH4.*;

// ............................................................................
/// Представлення структури ігрових даних
/// @author Rutar_Andriy
/// 13.02.2026

public class DataBlock {

private byte[] rawData;                              // масив необроблених байт
private String[] strings;                                       // масив рядків
private GzipParameters parameters;                  // параметри стиснення zlib

private final boolean compressed;                  // якщо true - дані стиснені
private final ArrayList<byte[]> data = new ArrayList<>(); // масив оброб. даних

public static int BLOCK_ID = 0;                    // ідентифікатор блоку даних

// ============================================================================
/// Конструктор за замовчуванням
/// @param rawBytes байтовий масив для обробки

public DataBlock (byte[] rawBytes) {

    rawData = rawBytes;
    compressed = isCompressed();
    
    processRawData();
    processStrings();
}

// ============================================================================
/// Обробка "сирого" масиву байт

private void processRawData() {

if (debug) {                 // запис у файл стиснених даних для налагоджування
  try (FileOutputStream fos = new FileOutputStream("Compressed_" + 
                                ++BLOCK_ID + ".bin")) { fos.write(rawData); }
  catch (Exception _) {} }

/// Якщо дані стиснені - розпаковуємо їх
if (compressed) { rawData = decompressData(rawData); }

if (debug) {              // запис у файл розпакованих даних для налагоджування
  try (FileOutputStream fos = new FileOutputStream("Decompressed_" +
                                  BLOCK_ID + ".bin")) { fos.write(rawData); }
  catch (Exception _) {} }

// ............................................................................
// Обробка масиву байт в циклі

int repeatableCharCount, index = 0;
boolean validChar, validLetter, validString, hasLetter;

for (int z = 0; z < rawData.length - 1; z++) {
    
  hasLetter = false;                        // якщо true - рядок містить літери
  validString = true;                            // якщо true - рядок коректний
  repeatableCharCount = 0;                   // кількість повторюваних символів
  short sLenght = Utils.getShortByBytes(new byte[] { rawData[z],
                                                     rawData[z+1] });
    
  // Перевірка довщини рядка та виходу за межі масиву
  if (sLenght <= 0 || z+2+sLenght > rawData.length) { continue; }

  // Обробка потенційного текстового рядка
  for (int q = z+2; q < z+2+sLenght; q++) {

    validChar   = isValidChar  (rawData[q]);      // перев. коректності символу
    validLetter = isValidLetter(rawData[q]);       // перев. коректності літери

    // Обробка псевдорядків, типу "яяяяя" або "їяяя" ...
    if (isRepeatableChar(rawData[q])) { repeatableCharCount++; }

    if (validLetter) { hasLetter = true; }
    if (!validChar && !validLetter) { validString = false; break; }
  }
    
  // Перевірка виконання всіх вимог до текстового рядка
  if (validString && sLenght > 1 &&
    hasLetter && repeatableCharCount != sLenght) {

    // Копіювання даних, які не обробляються
    data.add(Arrays.copyOfRange(rawData, index, z));
    // Копіювання даних, що відповідають текстовому рядку
    data.add(Arrays.copyOfRange(rawData, z, z+sLenght+2));

    index = z + sLenght + 2;
    z += sLenght + 1;        // якщо рядок знайдено, то пропускаємо зайві байти
  }
}

// Копіювання залишкових даних
data.add(Arrays.copyOfRange(rawData, index, rawData.length));

}

// ============================================================================
/// Повернення масиву "сирих" байт
/// @return масив "сирих" байт

public byte[] getRawData() {

ByteArrayOutputStream baos = new ByteArrayOutputStream();

try { for (byte[] dataPart : data) { baos.write(dataPart); }
      return compressed ? compressData(baos.toByteArray()) :
                                       baos.toByteArray(); }

catch (IOException e) { return null; }

}
// ============================================================================
/// Перетворення байтового масиву в текстові рядки

private void processStrings() {

ArrayList<String> array = new ArrayList<>();

try { for (int z = 1; z < data.size(); z+=2) {
        byte[] binaryString = data.get(z);
        replaceByte(binaryString, (byte) 0x0A, (byte) 0xAC, 2);
        short sLenght = Utils.getShortByBytes(binaryString);
        String string = new String(binaryString, 2, sLenght, "cp1251");
        array.add(string); }

      strings = array.toArray(String[]::new); }

catch (UnsupportedEncodingException e)
  { System.err.println("Unsupported encoding error"); }

}

// ============================================================================
/// Розпакування даних, стиснених за допомогою алгоритму zlib

private byte[] decompressData (byte[] data) {

try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
     GzipCompressorInputStream gcis = new GzipCompressorInputStream(bis)) {
    
  parameters = gcis.getMetaData();

  int len;
  byte[] tmp = new byte[4096];
  ByteArrayOutputStream bos = new ByteArrayOutputStream();

  while ((len = gcis.read(tmp)) > 0) { bos.write(tmp, 0, len); }

  return bos.toByteArray();
}

catch (Exception e) { System.err.println("GZIP decompress error");
                      return null; }
           
}

// ============================================================================
/// Стиснення даних за допомогою алгоритму zlib

private byte[] compressData (byte[] data) {

ByteArrayOutputStream baos = new ByteArrayOutputStream();

try (GzipCompressorOutputStream gcos = new
     GzipCompressorOutputStream(baos, parameters)) { gcos.write(data); }

catch (Exception e) { System.err.println("GZIP decompress error");
                      return null; }

return baos.toByteArray();

}

// ============================================================================
/// Повернення масиву текстових рядків
/// @return масив текстових рядків

public String[] getStrings() { return strings; }

// ============================================================================
/// Перетворення тексту в бінарний формат, який використовується у грі

public void recalculateStrings() {

ByteArrayOutputStream baos = new ByteArrayOutputStream();

try {

for (int z = 0; z < strings.length; z++)
  { // Отримання тексту з масиву
    String str = strings[z];
    // Визначення довжини тексту
    short len = (short) str.length();
    // Очищення буферу
    baos.reset();
    // Запис у буфер довжини тексту
    baos.write(Utils.getBytesByShort(len));
    // Перетворення тексту в кодуванні cp1251 у масив байт
    byte[] binaryString = str.getBytes("cp1251");
    // Заміна символу перенесення рядка
    replaceByte(binaryString, (byte) 0xAC, (byte) 0x0A, 0);
    // Запис у буфер одержаного масиву
    baos.write(binaryString);
    // Перевизначення початкових даних
    data.set(z*2+1, baos.toByteArray()); } }

catch (IOException e) { System.err.println(e.getMessage()); }
    
}

// ============================================================================
/// Перевірка, чи вказаний байт відповідає допустимим симв. в кодуванні cp1251

private boolean isValidChar (byte b) {

    return  b == 0x0A ||                 // символ переходу на новий рядок
           (b >= 0x20 && b <= 0x40) ||   // пробіл, цифри, знаки пунктуації
           (b >= 0x5B && b <= 0x60) ||   // квадратні дужки, апостроф
           (b >= 0x7b && b <= 0x7f);     // фігурні дужки, тильда
}

// ============================================================================
/// Перевірка, чи вказаний байт відповідає повторюваному символу

private boolean isRepeatableChar (byte b) {
    
    return b == (byte) 0xFF ||   // я
           b == (byte) 0xBF ||   // ї
           b == (byte) 0xFD;     // э
}

// ============================================================================
/// Перевірка, чи вказаний байт відповідає літері в кодуванні cp1251

private boolean isValidLetter (byte b) {
    
    return (b >= (byte) 0xC0 && b <= (byte) 0xFF) ||   // кирилиця
            b == (byte) 0xA5 ||                        // Ґ
            b == (byte) 0xAA ||                        // Є
            b == (byte) 0xAF ||                        // Ї
            b == (byte) 0xB2 ||                        // І
            b == (byte) 0xB4 ||                        // ґ
            b == (byte) 0xBA ||                        // є
            b == (byte) 0xBF ||                        // ї
            b == (byte) 0xB3;                          // і
}

// ============================================================================
/// Знаходження та заміна конкретних значень у байтовому масиві

private void replaceByte (byte[] array,
                          byte oldValue, byte newValue, int skip) {

    for (int z = 0; z < array.length; z++)
        { if (z >= skip && array[z] == oldValue)
              { array[z] = newValue; } }
}

// ============================================================================
/// Перевірка, чи дані є стисненими за допомогою алгоритму zlib

private boolean isCompressed() { return rawData[0] ==        0x1F &&
                                        rawData[1] == (byte) 0x8B &&
                                        rawData[2] ==        0x08; }

// Кінець класу DataBlock =====================================================

}