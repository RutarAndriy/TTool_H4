package com.rutar.ttool_h4;

import java.io.*;
import java.net.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

// ............................................................................
/// Базове тестування програми
/// @author Rutar_Andriy
/// 14.01.2026

@DisplayName("Main test class")
public class TToolH4Test {

// ============================================================================

@Test
@DisplayName("Should pass")
void should_Answer_With_True()
    { assertTrue(true); }

// ============================================================================

@Test
@DisplayName("File .empty exist")
void file_Empty_Exist()
    { URL resource = getClass().getResource(".empty");
      assertTrue(resource != null &&
                 new File(resource.getFile()).exists()); }

// ============================================================================
    
// @Test
// @Disabled("skipped")
// @DisplayName("Should skip")
// void should_Skip() {
//     fail("This error will be skipped");
// }

// ============================================================================

// @Test
// @DisplayName("Should fail")
// void should_Fail() {
//     fail("Some error ...");
// }

// Кінець класу TToolH4Test ===================================================

}
