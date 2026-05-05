package com.rutar.ttool_h4;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

// ............................................................................
/// Базове тестування програми
/// @author Rutar_Andriy
/// 13.02.2026

@DisplayName("TToolH4Test class")
public class TToolH4Test {

// ============================================================================

@Test
@DisplayName("Should pass")
void shouldAnswerWithTrue()
  { assertTrue(true); }

// ============================================================================

@Test
@DisplayName("File .empty exist")
void fileEmptyExist()
  { assertNotNull(getClass().getResource(".empty")); }

// ============================================================================
    
// @Test
// @Disabled("skipped")
// @DisplayName("Should skip")
// void shouldSkip()
//   { fail("This error will be skipped"); }

// ============================================================================

// @Test
// @DisplayName("Should fail")
// void shouldFail()
//   { fail("Some error ..."); }

// Кінець класу TToolH4Test ===================================================

}
