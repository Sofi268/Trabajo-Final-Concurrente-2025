// Transicion: Modela las Transiciones de la Red de Petri.
public class Transicion {
    private int id;    // Atributo que almacena el identificador único de la transición.
    private boolean activa; // Atributo que indica si la transición está activa o no.

    // Constructor de la clase Transicion que inicializa el identificador y establece la transición como inactiva.
    public Transicion(int id) {
        this.id = id;
        this.activa = false;
    }

    // Método que activa la transición, cambiando su estado a activo.
    public void activar() {
        activa = true;
    }

    // Método que desactiva la transición, cambiando su estado a inactivo.
    public void desactivar() {
        activa = false;
    }

    /* Método que verifica si la transición está activa.
     * Retorna true si la transición está activa, de lo contrario retorna false.
     */
    public boolean estaActiva() {
        return activa;
    }
    public int getId() {
        return id;
    }
}