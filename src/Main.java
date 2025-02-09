//TP final - Redes de petri - febrero de 2025
//By: Sofia Castro y Efrain Veliz

//Main: Punto de entrada para ejecutar la simulacion.
public class Main {
    public static void main(String[] args) {
        // Crear una instancia del monitor que sincroniza las acciones de los clientes y agentes
        Monitor monitor = Monitor.getInstance();
        
        // Crear una instancia de la red de Petri que modela el sistema. la cual inicializa las plazas y transiciones
        RedDePetri.getInstancia();

        // LOG 
        LogSistem log = LogSistem.getInstance();  // Crear una instancia del log y asociarlo al monitor
        Thread logThread = new Thread(log); // Crear un hilo para el log
        logThread.setDaemon(true); // Configurar el hilo como un hilo demonio
        logThread.start(); // Iniciar el hilo del log

        // Crear dos clientes con identificadores únicos y asociarlos al monitor
        Cliente cliente1 = new Cliente(1, monitor);
        Cliente cliente2 = new Cliente(2, monitor);

        // Crear dos agentes de reservas con identificadores únicos y asociarlos al monitor
        AgenteReservas agente1 = new AgenteReservas(1, monitor);
        AgenteReservas agente2 = new AgenteReservas(2, monitor);

        // Crear hilos para los clientes y agentes
        Thread hilo1 = new Thread(cliente1);
        Thread hilo2 = new Thread(cliente2);
        Thread hilo3 = new Thread(agente1);
        Thread hilo4 = new Thread(agente2);

        // Iniciar los hilos para comenzar la simulación concurrente
        hilo1.start();
        hilo2.start();
        hilo3.start();
        hilo4.start();
    }
}