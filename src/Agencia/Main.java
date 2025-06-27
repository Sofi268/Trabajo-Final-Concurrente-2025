/**
 * @file Main.java 
 * @brief Inicia los hilos y gestionar la ejecución del programa.
 * 
 * Este archivo es el punto de entrada de la aplicación, donde se crean los hilos necesarios
 * para simular el comportamiento de una agencia de viajes, incluyendo clientes entrantes y salientes,
 * gestores de reservas y un gestor de colas condicionales.
 */
package Agencia;

public class Main {
    public static RedDePetri rdp = RedDePetri.getInstance();
    public static Monitor monitor = Monitor.getInstance();
    public static Estadisticas estadisticas = Estadisticas.getInstance(); 

    public static void main(String[] args){
        long inicio = System.nanoTime(); 

        Thread[] hilos = new Thread[6];
        crearPuestos(hilos);
        iniciarPuestos(hilos);
        Thread stats = new Thread(estadisticas);
        stats.start();

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

        long fin = System.nanoTime(); 
        double segundos = (fin - inicio) / 1_000_000_000.0;

        System.out.println("\nPrograma finalizado correctamente.\n");
        rdp.imprimirMarcado();
        rdp.comprobarInvariantes();
        System.out.printf("Tiempo total de ejecucion: %.3f segundos%n", segundos);
    }

// -----------------------------------------------------------------------------------------------------------------
    
    /**
     * @brief Crea e inicializa los hilos del sistema: clientes, gestores y agentes.
     * 
     * Asigna cada uno al arreglo de hilos recibido.
     */
    private static void crearPuestos(Thread[] hilos){
        //Hilos Cliente Entrante
        Cliente clienteEntrada1 = new Cliente(monitor, 1);
        hilos[0] = new Thread(clienteEntrada1);  

        //Hilos Cliente Saliente
        Cliente clienteSalida2 = new Cliente(monitor, 2);
        hilos[1] = new Thread(clienteSalida2); 

        //Hilos Gestor 1
        GestorReservas gestor1 = new GestorReservas(monitor, 1);
        hilos[2] = new Thread(gestor1);

        //Hilos Gestor 2
        GestorReservas gestor2 = new GestorReservas(monitor, 2);
        hilos[3] = new Thread(gestor2);  
        
        //Hilos Agente aprobacion de reserva
        Agente agente1 = new Agente(monitor, 1);
        hilos[4] = new Thread(agente1);
        
        //Hilos Agente rechazo de reserva
        Agente agente2 = new Agente(monitor, 2);
        hilos[5] = new Thread(agente2);  

    }
    
    /**
     * @brief Inicia todos los hilos previamente creados y almacenados en el arreglo.
     */
    private static void iniciarPuestos(Thread[] hilos){
        for (Thread hilo : hilos) {
            if (hilo != null) {
                hilo.start(); 
            }
        }
    }   
    
    /** 
     * @brief Interrumpe los hilos en ejecución y espera a que terminen.
     */
    private static void finalizacionHilos(Thread[] hilos) {
    	System.out.println("FINALIZANDO HILOS");
        for (Thread hilo : hilos) {
            if (hilo != null) {
                hilo.interrupt(); 
                System.out.println("Intentando detener el hilo: " + hilo.getName() + "\n");
            }
        }
    }
}