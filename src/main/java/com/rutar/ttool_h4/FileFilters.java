package com.rutar.ttool_h4;

import java.io.File;
import javax.swing.filechooser.FileFilter;

// ............................................................................
/// Реалізація користувацьких файлових фільтрів
/// @author Rutar_Andriy
/// 14.01.2026

public final class FileFilters {

// ============================================================================
/// Користувацький фільтр для папок

public static final FileFilter dirFilter = new FileFilter() {
    
    @Override
    public boolean accept (File file) { return file.isDirectory(); }

    @Override
    public String getDescription() { return "Особлива папка"; }

};

// ============================================================================
/// Користувацький фільтр для файлів

public static final FileFilter fileFilter = new FileFilter() {
    
    @Override
    public boolean accept (File file) { return file.isFile(); }

    @Override
    public String getDescription() { return "Особливий файл"; }
    
};

// Кінець класу FileFilters ===================================================

}
