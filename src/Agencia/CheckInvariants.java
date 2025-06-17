package Agencia;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.*;

public class CheckInvariants {
    public CheckInvariants() {
    }
    public int invariantes() {
        try {
            String texto = new String(Files.readAllBytes(Paths.get("logTransiciones.txt")));

            String regex = "(.*?)(T0)(.*?)(T1)(.*?)((T2)(.*?)(T5)|(T3)(.*?)(T4))(.*?)((T6)(.*?)(T9)(.*?)(T10)|(T7)(.*?)(T8))(.*?)(T11)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher;

            int cantidadInvariantes = 0;
            boolean huboMatch;

            do {
                matcher = pattern.matcher(texto);
                huboMatch = matcher.find();

                if (huboMatch) {
                    texto = matcher.replaceFirst("");
                    cantidadInvariantes++;
                }
            } while (huboMatch);

            System.out.println("Cantidad de invariantes que se cumplieron: " + cantidadInvariantes);

            if (texto.trim().isEmpty()) {
                System.out.println(" Todos los invariantes fueron reconocidos sin residuos.");
            } else {
                System.out.println("Transiciones fuera de secuencia:\n" + texto);
            }

            System.out.println("INVARIANTES=" + cantidadInvariantes);
            return cantidadInvariantes;

        } catch (IOException e) {
            System.err.println("Error leyendo el archivo: " + e.getMessage());
        }
        return 0;
    }
}
