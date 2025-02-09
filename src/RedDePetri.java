// RedDePetri: Coordina las plazas y las transiciones.
import java.util.ArrayList; // Importa la clase ArrayList para manejar listas dinámicas.
import java.util.List; // Importa la interfaz List para definir listas.

public class RedDePetri {
    private List<Plaza> plazas; // Declara una lista de objetos Plaza.
    private List<Transicion> transiciones; // Declara una lista de objetos Transicion.

    // Variable estática para mantener la única instancia de la clase.
    private static RedDePetri instance;

    // Método estático para obtener la única instancia de la clase.
    public static synchronized RedDePetri getInstancia() {
        if (instance == null) {
            instance = new RedDePetri();
        }
        return instance;
    }

    // Constructor privado para evitar la creación de instancias desde fuera de la clase.
    private RedDePetri() {
        plazas = new ArrayList<>(); // Inicializa la lista de plazas como un ArrayList.
        transiciones = new ArrayList<>(); // Inicializa la lista de transiciones como un ArrayList.
    }

    public void agregarPlaza(Plaza plaza) {
        try {
            plazas.add(plaza);   // Agrega una plaza a la lista de plazas.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void agregarTransicion(Transicion transicion) {
        transiciones.add(transicion); // Agrega una transición a la lista de transiciones.
        try {
            transiciones.add(transicion);  // Agrega una transición a la lista de transiciones.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}