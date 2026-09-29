import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CalculadoraTest {

    private Calculadora calc;

    @BeforeEach
    void setUp() {
        calc = new Calculadora();
    }

    @Test
    @DisplayName("Prueba para Suma: 5 + 3 = 8")
    void debeSumarCorrectamente() {
        assertEquals(8, calc.sumar(5, 3));
    }

    @Test
    @DisplayName("Prueba para Resta: 10 - 4 = 6")
    void debeRestarCorrectamente() {
        assertEquals(6, calc.restar(10, 4));
    }

    @Test
    @DisplayName("Prueba para Multiplicación: 4 * 3 = 12")
    void debeMultiplicarCorrectamente() {
        assertEquals(12, calc.multiplicar(4, 3));
    }

    @Test
    @DisplayName("Prueba para División: 15 / 3 = 5")
    void debeDividirCorrectamente() {
        assertEquals(5, calc.dividir(15, 3));
    }

    @Test
    @DisplayName("Prueba para División por Cero: debe lanzar ArithmeticException")
    void debeLanzarExcepcionAlDividirPorCero() {
        assertThrows(ArithmeticException.class, () -> calc.dividir(10, 0));
    }
}
