/*
* */
package Agencia;
public class Main {
	public static RedDePetri red = RedDePetri.getInstance();
    public static final Monitor monitor = Monitor.getInstance();

    public static void main(String[] args){
        Thread[] hilos = new Thread[19];
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
        Cliente clienteEntrada1 = new Cliente(monitor, 1);
        hilos[0] = new Thread(clienteEntrada1);  
        Cliente clienteEntrada2 = new Cliente(monitor, 1);
        hilos[1] = new Thread(clienteEntrada2); 
        Cliente clienteEntrada3 = new Cliente(monitor, 1);
        hilos[2] = new Thread(clienteEntrada3); 
        Cliente clienteEntrada4 = new Cliente(monitor, 1);
        hilos[3] = new Thread(clienteEntrada4); 
        Cliente clienteSalida5 = new Cliente(monitor, 2);
        hilos[4] = new Thread(clienteSalida5); 
        Cliente clienteSalida6 = new Cliente(monitor, 2);
        hilos[5] = new Thread(clienteSalida6);  
        Cliente clienteSalida7 = new Cliente(monitor, 2);
        hilos[6] = new Thread(clienteSalida7); 

        GestorReservas gestor1 = new GestorReservas(monitor, 1);
        hilos[7] = new Thread(gestor1);
        GestorReservas gestor2 = new GestorReservas(monitor, 1);
        hilos[8] = new Thread(gestor2); 
        GestorReservas gestor3 = new GestorReservas(monitor, 1);
        hilos[9] = new Thread(gestor3); 
        GestorReservas gestor4 = new GestorReservas(monitor, 2);
        hilos[10] = new Thread(gestor4);  
        GestorReservas gestor5 = new GestorReservas(monitor, 2);
        hilos[11] = new Thread(gestor5); 
        GestorReservas gestor6 = new GestorReservas(monitor, 2);
        hilos[12] = new Thread(gestor6); 
        

        Agente agente1 = new Agente(monitor, 1);
        hilos[13] = new Thread(agente1);
        Agente agente2 = new Agente(monitor, 1);
        hilos[14] = new Thread(agente2);
        Agente agente3 = new Agente(monitor, 1);
        hilos[15] = new Thread(agente3);
        Agente agente4 = new Agente(monitor, 2);
        hilos[16] = new Thread(agente4);  
        Agente agente5 = new Agente(monitor, 2);
        hilos[17] = new Thread(agente5);
        Agente agente6 = new Agente(monitor, 1);
        hilos[18] = new Thread(agente6);
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
