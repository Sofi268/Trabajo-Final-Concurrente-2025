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

    private static void CrearPuestos(Thread[] hilos){
        //gestor de colas de condicion
        GestorColaCondicional gestorColaCondicional1 = new GestorColaCondicional();
        hilos[0] = new Thread(gestorColaCondicional1); 
        //hilos[0].setDaemon(true); // Configurar como daemon para que tenga el minimo de prioridad sin afectar al resto.

    	//Hilos Cliente Entrante
        Cliente clienteEntrada1 = new Cliente(monitor, 1);
        hilos[1] = new Thread(clienteEntrada1);  
        hilos[2] = new Thread(clienteEntrada1); 
        hilos[3] = new Thread(clienteEntrada1); 
        // hilos[4] = new Thread(clienteEntrada1); 
        // hilos[5] = new Thread(clienteEntrada1);  
        //Hilos Cliente Saliente
        Cliente clienteSalida2 = new Cliente(monitor, 2);
        hilos[4] = new Thread(clienteSalida2); 
        hilos[5] = new Thread(clienteSalida2);  
        hilos[6] = new Thread(clienteSalida2); 
        // hilos[9] = new Thread(clienteSalida2); 
        // hilos[10] = new Thread(clienteSalida2); 

        //Hilos Gestor 1
        GestorReservas gestor1 = new GestorReservas(monitor, 1);
        hilos[7] = new Thread(gestor1);
        hilos[8] = new Thread(gestor1); 
        hilos[9] = new Thread(gestor1); 
        // hilos[14] = new Thread(gestor1); 
        // hilos[15] = new Thread(gestor1); 
        
        //Hilos Gestor 2
        GestorReservas gestor2 = new GestorReservas(monitor, 2);
        hilos[10] = new Thread(gestor2);  
        hilos[11] = new Thread(gestor2); 
        hilos[12] = new Thread(gestor2); 
        // hilos[19] = new Thread(gestor2); 
        // hilos[20] = new Thread(gestor2); 
        
        //Hilos Agente aprobacion de reserva
        Agente agente1 = new Agente(monitor, 1);
        hilos[13] = new Thread(agente1);
        hilos[14] = new Thread(agente1);
        hilos[15] = new Thread(agente1);
        // hilos[24] = new Thread(agente1);
        // hilos[25] = new Thread(agente1);
        
        //Hilos Agente rechazo de reserva
        Agente agente2 = new Agente(monitor, 2);
        hilos[16] = new Thread(agente2);  
        hilos[17] = new Thread(agente2);
        hilos[18] = new Thread(agente2);
        // hilos[29] = new Thread(agente2);
        // hilos[30] = new Thread(agente2);
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