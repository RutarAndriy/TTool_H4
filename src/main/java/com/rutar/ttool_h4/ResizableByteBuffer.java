package com.rutar.ttool_h4;

import java.io.*;
import java.nio.*;

// ............................................................................
/// Реалізація ByteBuffer'а з динамічним розміром
/// @author Rutar_Andriy
/// 13.02.2026

public class ResizableByteBuffer {

private ByteBuffer buffer;

// ============================================================================
/// Конструктор за замовчуванням
/// @param defaultSize початковий розмір буфера
/// @param defaultByteOrder стандартний порядок байт буфера

public ResizableByteBuffer (int defaultSize, ByteOrder defaultByteOrder)
    { buffer = ByteBuffer.allocate(defaultSize).order(defaultByteOrder); }

// ============================================================================
/// Перевірка розміру буферу перед довананням даних

private void ensureCapacity (int needed) {
    
    // Якщо розміру не достатньо - збільшуємо буфер
    if (buffer.remaining() < needed) {  
        
        // Збільшення вдвічі
        int newCapacity = buffer.capacity() * 2;
        // Перевірка, чи достатньо нової ємності
        while (newCapacity < buffer.position() + needed)
            { newCapacity *= 2; }
        // Створення буфера з новим розміром
        ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity)
                                         .order(buffer.order());
        // Переведення буфера в режим читання
        buffer.flip();
        // Копіювання вмісту старого буфера в новий
        newBuffer.put(buffer);
        // Заміна старого буфера на новий
        buffer = newBuffer;
    }
}

// ============================================================================ 
/// Запис байту в динамічний буфер
/// @param b байт для запису
/// @return екземпляр динамічного буферу

public ResizableByteBuffer putByte (byte b)
    { ensureCapacity(1);
      buffer.put(b);
      return this; }

// ============================================================================
/// Запис масиву байт в динамічний буфер
/// @param src масив байт для запису
/// @return екземпляр динамічного буферу

public ResizableByteBuffer putBytes (byte[] src)
    { ensureCapacity(src.length);
      buffer.put(src);
      return this; }

// ============================================================================
/// Запис короткого цілого числа в динамічний буфер
/// @param value коротке ціле число для запису
/// @return екземпляр динамічного буферу

public ResizableByteBuffer putShort (short value)
    { ensureCapacity(2);
      buffer.putShort(value);
      return this; }

// ============================================================================
/// Запис цілого числа в динамічний буфер
/// @param value ціле число для запису
/// @return екземпляр динамічного буферу

public ResizableByteBuffer putInt (int value)
    { ensureCapacity(4);
      buffer.putInt(value);
      return this; }

// ============================================================================
/// Запис тексту в динамічний буфер
/// @param string текст для запису
/// @param charset кодування тексту
/// @return екземпляр динамічного буферу
/// @throws UnsupportedEncodingException якщо кодування не підтримується

public ResizableByteBuffer putH4String (String string, String charset)
                    throws UnsupportedEncodingException
    { byte[] bytes = string.getBytes(charset);
      putShort((short) bytes.length); // Записуємо довжину рядка
      putBytes(bytes);                // Записуємо байти рядка
      return this; }

// ============================================================================
/// Отримання внутрішнього ByteBuffer'а
/// @return внутрішній екземпляр класу ByteBuffer

public ByteBuffer getByteBuffer()
    { buffer.flip();
      return buffer; }

// ============================================================================
/// Отримання даних буферу у вигляді масиву байт
/// @return дані буферу у вигляді масиву байт
    
public byte[] getByteArray() {
    
    buffer.flip();                   // Переведення буфера в режим читання
    int count = buffer.limit();      // Отримання розміру буферу
    byte[] result = new byte[count]; // Створення масиву байт
    buffer.get(result, 0, count);    // Копіювання даних з буферу
    
    return result;                   // Повернення результату

}

// Кінець класу ResizableByteBuffer ===========================================

}