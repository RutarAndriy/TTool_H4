package com.rutar.ttool_h4;

import java.io.*;
import javax.swing.filechooser.FileFilter;

// ............................................................................
/// Реалізація користувацьких файлових фільтрів
/// @author Rutar_Andriy
/// 13.02.2026

public final class FileFilters {

// ============================================================================
/// Користувацький фільтр для папок

public static final FileFilter dirFilter = new FileFilter() {
    
    @Override
    public boolean accept (File file) { return file.isDirectory(); }

    @Override
    public String getDescription() { return "H4 папки розпакованих шрифтів"; }

};

// Кінець класу FileFilters ===================================================

}
