package com.rutar.ttool_h4;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

import static com.rutar.ttool_h4.TToolH4.*;

// ............................................................................
/// Реалізація зміненої промальовки клітинок таблиці
/// @author Rutar_Andriy
/// 13.02.2026

public class CellRender extends DefaultTableCellRenderer {

private Color searchColor;
private Color defaultColor;

private static int searchedCol = -1;
private static int searchedRow = -1;

// ============================================================================

@Override
public Component getTableCellRendererComponent(JTable table,
                                               Object value,
                                               boolean isSelected,
                                               boolean hasFocus,
                                               int row, int col) {

Component component = super.getTableCellRendererComponent(table, value,
                                                          isSelected,
                                                          hasFocus, row, col);

if (defaultColor == null)
    { defaultColor = component.getForeground();
      searchColor = UIManager.getColor("Component.accentColor"); }

// ............................................................................

if (searchedCol != -1 && searchedCol == col &&
    searchedRow != -1 && searchedRow == row)
     { component.setForeground(searchColor); }
else { setNormalColor(component, col, row);  }

return component;

}

// ============================================================================

private void setNormalColor (Component component, int c, int r) {
    
    if ((fileExt.equals("txt") && c < 3) ||
        (fileExt.equals("h4c") && c < 1))
         { component.setForeground(Color.GRAY);   }
    else { component.setForeground(defaultColor); }

}

// ============================================================================

public static void setSearchedCell (int searchedCol, int searchedRow)
    { CellRender.searchedCol = searchedCol;
      CellRender.searchedRow = searchedRow; }

// Кінець класу CellRender ====================================================

}
