package com.rutar.ttool_h4;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

// ............................................................................
/// Реалізація зміненої промальовки клітинок таблиці
/// @author Rutar_Andriy
/// 14.01.2026

public class CellRender extends DefaultTableCellRenderer {

// ============================================================================

@Override
public Component getTableCellRendererComponent(JTable table,
                                               Object value,
                                               boolean isSelected,
                                               boolean hasFocus,
                                               int row, int column) {

Component component = super.getTableCellRendererComponent(table, value,
                                                          isSelected, hasFocus,
                                                          row, column);

if (column != 2) { component.setForeground(Color.GRAY); }

return component;

}

// Кінець класу CellRender ====================================================

}
