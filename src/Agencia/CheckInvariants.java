/**
 * @file CheckInvariants.java
 * @brief Contador de invariantes en el log de transiciones
 */
package Agencia;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.*;

public class CheckInvariants {
    public CheckInvariants() {}

    /**
     * @brief Cuenta cuántas veces se cumple un patrón de invariante en el log de transiciones.
     * Usa expresiones regulares para reconocer secuencias validas, las elimina del texto,
     * y devuelve la cantidad total de coincidencias encontradas.
     *
     * @return número de invariantes detectados en el archivo logTransiciones.txt
     */
    public static int invariantes() {
        try {
            String texto = new String(Files.readAllBytes(Paths.get("logTransiciones.txt")));
            
            // Reemplaza las transiciones temporales por letras para que no interfieran con el regex al confundirse con los grupos
            texto = texto.replace("T10", "A").replace("T11", "B");

            String regex = "(.*?)(T0)(.*?)(T1)(.*?)((T2)(.*?)(T5)|(T3)(.*?)(T4))(.*?)((T6)(.*?)(T9)(.*?)(A)|(T7)(.*?)(T8))(.*?)(B)";
            Pattern pattern = Pattern.compile(regex);

            int cantidadInvariantes = 0;
            Matcher matcher;
            boolean huboMatch;

            do {
                matcher = pattern.matcher(texto);
                huboMatch = matcher.find();

                if (huboMatch) {
                    String reemplazo = matcher.group(1)  // antes de T0
                                     + matcher.group(3)  // entre T0 y T1
                                     + matcher.group(5)  // entre T1 y T2/T3
                                     + (matcher.group(8) != null ? matcher.group(8) : "")  // entre T2 y T5
                                     + (matcher.group(11) != null ? matcher.group(11) : "") // entre T3 y T4
                                     + matcher.group(13) // entre bloque 1 y bloque 2
                                     + (matcher.group(16) != null ? matcher.group(16) : "") // entre T6 y T9
                                     + (matcher.group(18) != null ? matcher.group(18) : "") // entre T9 y A
                                     + (matcher.group(21) != null ? matcher.group(21) : "") // entre T7 y T8
                                     + matcher.group(23); // entre bloque 2 y B

                    texto = matcher.replaceFirst(Matcher.quoteReplacement(reemplazo));
                    cantidadInvariantes++;
                }
            } while (huboMatch);

            System.out.println("Cantidad de invariantes que se cumplieron: " + cantidadInvariantes);
            if (texto.trim().isEmpty()) {
                System.out.println(" Todos los invariantes fueron reconocidos sin residuos.");
            } else {
                System.out.println("Transiciones sobrantes:\n" + texto);
            }
            System.out.println("INVARIANTES=" + cantidadInvariantes);
            return cantidadInvariantes;

        } catch (IOException e) {
            System.err.println("Error leyendo el archivo: " + e.getMessage());
        }

        return 0;
    }
}
