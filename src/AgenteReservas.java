// AgenteReservas: Maneja las reservas de los clientes.
public class AgenteReservas implements Runnable {
    private int id; // Identificador único del agente de reservas
    private Monitor monitor; // Referencia al monitor que gestiona las transiciones

    // Constructor de la clase AgenteReservas
    // @param id Identificador único del agente
    // @param monitor Referencia al monitor asociado
    public AgenteReservas(int id, Monitor monitor) {
        this.id = id; // Asigna el identificador del agente
        this.monitor = monitor; // Asigna el monitor asociado
    }

    // Método que se ejecuta cuando el hilo es iniciado
    @Override
    public void run() {
        try {
            System.out.println("Agente " + id + " procesando reserva."); // Imprime un mensaje indicando que el agente está procesando una reserva
            monitor.fireTransition(5); // Llama al método fireTransition del monitor para simular la gestión de una reserva
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}