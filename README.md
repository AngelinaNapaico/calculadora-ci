# Calculadora CI - Integración Continua con Jenkins y Slack

Proyecto Java ligero gestionado con **Maven** y pruebas unitarias automáticas con **JUnit 5**, diseñado como base para la integración continua (CI/CD) con **Jenkins** y notificaciones en tiempo real vía **Slack**.

---

## 👥 Equipo de Trabajo (Equipo 5)

| Rol | Integrante | Responsabilidad Principal |
| :--- | :--- | :--- |
| **QA Lead** | Caycho Casas, Carlos Alejandro | Coordinación y organización de evidencias / documentación |
| **QA Automation** | Napaico Valencia, Angelina | Configuración de automatización, Maven, Jenkins y Pipeline |
| **QA Tester** | Huayhua Jaquehua, Paul Antony | Diseño y ejecución de pruebas unitarias locales |
| **QA Tester** | Silva Laura, Jhon Brayan | Apoyo en ejecución y validación de integraciones |

---

## 📁 Estructura del Proyecto

```text
calculadora-ci/
├── pom.xml
├── README.md
├── .gitignore
└── src/
    ├── main/
    │   └── java/
    │       └── Calculadora.java
    └── test/
        └── java/
            └── CalculadoraTest.java
```

---

## ⚙️ Requisitos Previos

* **Java JDK 17** o superior.
* **Apache Maven 3.8+**.
* **Git**.

---

## 🚀 Compilación y Ejecución de Pruebas

Para compilar el proyecto y ejecutar las pruebas unitarias localmente con Maven:

```bash
# Limpiar y ejecutar suite de pruebas
mvn clean test
```

### Salida esperada:
```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running CalculadoraTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## 🧪 Pruebas Unitarias Implementadas

* `debeSumarCorrectamente`: Valida la operación de adición ($5 + 3 = 8$).
* `debeRestarCorrectamente`: Valida la operación de sustracción ($10 - 4 = 6$).
* `debeMultiplicarCorrectamente`: Valida la operación de multiplicación ($4 \times 3 = 12$).
* `debeDividirCorrectamente`: Valida la operación de división ($15 / 3 = 5$).
* `debeLanzarExcepcionAlDividirPorCero`: Valida el control de excepciones ante división por cero.

---

## 🔄 Integración Continua (CI/CD)

Este repositorio se conecta con un Pipeline de Jenkins para:
1. Clonar el repositorio automáticamente al recibir cambios.
2. Ejecutar `mvn clean test`.
3. Notificar inmediatamente al canal de Slack el resultado del build (`SUCCESS` o `FAILURE`).
