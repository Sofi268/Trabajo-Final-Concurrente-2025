// Cliente: Representa a los clientes que ingresan a la agencia.
public class Cliente implements Runnable {
    private int id; // Identificador único del cliente
    private Monitor monitor; // Referencia al monitor que controla las transiciones

    // Constructor de la clase Cliente
    public Cliente(int id, Monitor monitor) {
        this.id = id; // Asigna el identificador del cliente
        this.monitor = monitor; // Asigna el monitor
    }

    // Método que se ejecuta cuando el hilo del cliente comienza
    @Override
    public void run() {
        try {
            System.out.println("Cliente " + id + " ingresando a la agencia."); // Imprime un mensaje indicando que el cliente está ingresando
            monitor.fireTransition(2); // Simula la entrada del cliente a la agencia mediante una transición en el monitor
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}