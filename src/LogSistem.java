import java.io.FileWriter; // Importa la clase FileWriter para escribir en archivos.
import java.io.IOException; // Importa la clase IOException para manejar excepciones de entrada/salida.
import java.io.PrintWriter; // Importa la clase PrintWriter para imprimir representaciones formateadas de objetos en un flujo de salida.

public class LogSistem implements Runnable {
    private static final String FILE_NAME = "log.txt"; // Define el nombre del archivo de log como una constante.
    private static LogSistem instancia; // Variable estática para la instancia única de la clase.

    // Constructor privado para evitar la instanciación directa.
    private LogSistem() {}

    // Método estático para obtener la instancia única de la clase.
    public static synchronized LogSistem getInstance() {
        if (instancia == null) {
            instancia = new LogSistem();
        }
        return instancia;
    }

    // Método para registrar un evento en el archivo de log.
    public void registrarEvento(String evento) {
        // Intenta abrir el archivo en modo de adición y escribir en él.
        try (FileWriter fileWriter = new FileWriter(FILE_NAME, true);
            PrintWriter printWriter = new PrintWriter(fileWriter)) {
            printWriter.println(evento); // Escribe el evento en una nueva línea del archivo de log.
        } catch (IOException e) { // Captura cualquier excepción de entrada/salida que ocurra.
            e.printStackTrace(); // Imprime el stack trace de la excepción para depuración.
        }
    }

    public void run() {
        // Implementación del método run de la interfaz Runnable.
        // Este método se ejecuta cuando se inicia el hilo del log.
        System.out.println("Iniciando log del sistema..."); // Imprime un mensaje indicando que el log se ha iniciado.
        registrarEvento("Inicio del log del sistema."); // Registra un evento en el archivo de log.
    }
}