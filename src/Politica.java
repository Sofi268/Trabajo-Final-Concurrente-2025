// Clase Politica: implementa las políticas de balanceo y prioridad.
public class Politica {
    // Variable privada que indica si la política es balanceada.
    private boolean balanceada;

    // Constructor de la clase Politica que inicializa la variable balanceada.
    public Politica(boolean balanceada) {
        this.balanceada = balanceada;
    }

    // Método que aplica la política correspondiente según el valor de balanceada.
    public void aplicarPolitica() {
        try {
            if (balanceada) {         // Si balanceada es true, aplica la política balanceada.
                System.out.println("Aplicando política balanceada...");
            } else {
                // Si balanceada es false, aplica la política priorizada.
                System.out.println("Aplicando política priorizada...");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}