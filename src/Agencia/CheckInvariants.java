package Agencia;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.*;

public class CheckInvariants {
    public CheckInvariants() {}

    public int invariantes() {
        try {
            String texto = new String(Files.readAllBytes(Paths.get("logTransiciones.txt")));

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
                    String reemplazo = matcher.group(1)  
                                     + matcher.group(3)  
                                     + matcher.group(5)  
                                     + (matcher.group(8) != null ? matcher.group(8) : "")  
                                     + (matcher.group(11) != null ? matcher.group(11) : "") 
                                     + matcher.group(13) 
                                     + (matcher.group(16) != null ? matcher.group(16) : "") 
                                     + (matcher.group(18) != null ? matcher.group(18) : "") 
                                     + (matcher.group(21) != null ? matcher.group(21) : "") 
                                     + matcher.group(23); 

                    texto = matcher.replaceFirst(Matcher.quoteReplacement(reemplazo));
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
