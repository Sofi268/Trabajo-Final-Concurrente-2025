package Agencia;

public class Main {

	public static RedDePetri red = new RedDePetri(); 
    public static final Monitor monitor = new Monitor(red);

    public static void main(String[] args){
        Thread[] hilos = new Thread[9];  

        CrearPuestos(hilos);
        IniciarPuestos(hilos);
        IniciarEstadisticas();

        try {
            for (Thread hilo : hilos) {
                if (hilo != null) {
                    hilo.join();  
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Error al esperar los hilos.");
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // INICIO METODOS DE UTILIDAD:

    /**
     * @brief Creación de puestos de trabajo y asignación de hilos.
     */
    private static void CrearPuestos(Thread[] hilos){
        Cliente clienteEntrada = new Cliente(monitor, 1);
        hilos[0] = new Thread(clienteEntrada);  
        Cliente clienteSalida = new Cliente(monitor, 2);
        hilos[1] = new Thread(clienteSalida);  

        GestorReservas gestorTipo1 = new GestorReservas(monitor, 1);
        hilos[2] = new Thread(gestorTipo1);  
        GestorReservas gestorTipo2 = new GestorReservas(monitor, 2);
        hilos[3] = new Thread(gestorTipo2);  

        Agente agenteTipo1 = new Agente(monitor, 1);
        hilos[4] = new Thread(agenteTipo1);
        Agente agenteTipo2 = new Agente(monitor, 2);
        hilos[5] = new Thread(agenteTipo2);  
        Agente agenteTipo3 = new Agente(monitor, 2);
        hilos[6] = new Thread(agenteTipo3);

    }

    /**
     * @brief Inicia los hilos que han sido creados.
     */
    private static void IniciarPuestos(Thread[] hilos){
        for (Thread hilo : hilos) {
            if (hilo != null) {
                hilo.start(); 
            }
        }
    }
	
	/**
	 * @brief Inicia la escritura en el log de las estadisticas del programa.
	 */
	private static void IniciarEstadisticas(){
		Estadisticas estadisticas = new Estadisticas(red);
		Thread stats = new Thread(estadisticas);
		stats.start();
	}
}
