/* Main: encargado de iniciar los hilos y gestionar la ejecución del programa.
 * 
 * Este archivo es el punto de entrada de la aplicación, donde se crean los hilos necesarios
 * para simular el comportamiento de una agencia de viajes, incluyendo clientes entrantes y salientes,
 * gestores de reservas y un gestor de colas condicionales.
 * 
 * La clase `Main` inicializa los hilos, los arranca y espera a que todos terminen su ejecución.
* */
package Agencia;

import java.util.concurrent.TimeUnit;

public class Main {
    public static RedDePetri Rdp = RedDePetri.getInstance();
    public static Monitor monitor = Monitor.getInstance();
    public static Estadisticas estadisticas = Estadisticas.getInstance(); 
    public static void main(String[] args){
        Thread[] hilos = new Thread[18];
        CrearPuestos(hilos);
        IniciarPuestos(hilos);
        Thread stats = new Thread(estadisticas);
        stats.start();
        IniciarEstadisticas();  

        try {
            stats.join();  
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Error al esperar los hilos.");
        }       
        
        finalizacionHilos(hilos);

        for (Thread hilo : hilos) {
            try {
				hilo.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }

        System.out.println("\nPrograma finalizado correctamente.\n");

    }
// -----------------------------------------------------------------------------------------------------------------
    
    /* */
    private static void CrearPuestos(Thread[] hilos){
        //Hilos Cliente Entrante
        Cliente clienteEntrada1 = new Cliente(monitor, 1);
        hilos[0] = new Thread(clienteEntrada1);  
        hilos[1] = new Thread(clienteEntrada1); 
        hilos[2] = new Thread(clienteEntrada1);  

        //Hilos Cliente Saliente
        Cliente clienteSalida2 = new Cliente(monitor, 2);
        hilos[3] = new Thread(clienteSalida2); 
        hilos[4] = new Thread(clienteSalida2);  
        hilos[5] = new Thread(clienteSalida2); 

        //Hilos Gestor 1
        GestorReservas gestor1 = new GestorReservas(monitor, 1);
        hilos[6] = new Thread(gestor1);
        hilos[7] = new Thread(gestor1); 
        hilos[8] = new Thread(gestor1); 
        
        //Hilos Gestor 2
        GestorReservas gestor2 = new GestorReservas(monitor, 2);
        hilos[9] = new Thread(gestor2);  
        hilos[10] = new Thread(gestor2); 
        hilos[11] = new Thread(gestor2); 
        
        //Hilos Agente aprobacion de reserva
        Agente agente1 = new Agente(monitor, 1);
        hilos[12] = new Thread(agente1);
        hilos[13] = new Thread(agente1);
        hilos[14] = new Thread(agente1);
        
        //Hilos Agente rechazo de reserva
        Agente agente2 = new Agente(monitor, 2);
        hilos[15] = new Thread(agente2);  
        hilos[16] = new Thread(agente2);
        hilos[17] = new Thread(agente2);
    }
    
    /* */
    private static void IniciarPuestos(Thread[] hilos){
        for (Thread hilo : hilos) {
            if (hilo != null) {
                hilo.start(); 
            }
        }
    }   
    
    /* Se encarga de iniciar el hilo que controla los estadisticos. */
    private static void IniciarEstadisticas(){
        // Thread stats = new Thread(estadisticas);
        // stats.start();
	}

    /* 
     * Interrumpe los hilos en ejecución y espera a que terminen.
    */
    public static void finalizacionHilos(Thread[] hilos) {
    	System.out.println("FINALIZANDO HILOS");
        for (Thread hilo : hilos) {
            if (hilo != null) {
                hilo.interrupt(); 
                System.out.println("Intentando detener el hilo: " + hilo.getId() + "\n");
            }
        }
    }
}