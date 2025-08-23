/**
 * @file Monitor.java
 * @brief Monitor sincronizado que controla el acceso concurrente a la Red de Petri con una politica Signal and Continue
 * Coordina hilos que disparan transiciones, manejando condiciones y prioridades 
 */
package Agencia;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Monitor implements MonitorInterface{
    private static Monitor uniqueInstance;
    private static RedDePetri rdp = RedDePetri.getInstance();
    // -------------------------------------------------------------------------------------------------
    private static Semaphore mutex = new Semaphore(1, true);
    private static Semaphore[] colasCondicion; // Colas de condicion para cada transicion
    private static String[] flagSleep; // Indica si hay hilos temporales durmiendo (nombre del hilo que "toma" la temporal)
    private static boolean finPrograma = false; // Indica si se ha solicitado finalizar el programa
    private static boolean colasLiberadas = false; // Indica si las colas han sido liberadas
    private static Politicas politica = Politicas.getInstance("Balanceada"); // "Balanceada" o "Prioridad".
    // -------------------------------------------------------------------------------------------------
    public Monitor(){
        System.out.println("Monitor creado.");
    }

    /**
     * @brief Retorna la instancia única del Monitor, inicializándolo si es necesario.
     */
    public static Monitor getInstance() {
        if (uniqueInstance == null) {
            System.out.println("Instanciando Monitor...");
            uniqueInstance = new Monitor();
            startMonitor();
        }
        return uniqueInstance;
    }
    // -------------------------------------------------------------------------------------------------
    /**
     * @brief Inicializa estructuras internas del Monitor según la cantidad de transiciones.
     */
    public static void startMonitor() {
        System.out.println("Iniciando Monitor...");
        colasCondicion = new Semaphore[rdp.getTransiciones()];
        flagSleep = new String[rdp.getTransiciones()]; // Inicializa el flag de sleep para cada transicion temporal
        for (int i = 0; i < rdp.getTransiciones(); i++) {
            colasCondicion[i] = new Semaphore(0);
            flagSleep[i] = null;    // Inicio las flags con null. 
        }
        System.out.println("Monitor inicializado con " + rdp.getTransiciones() + " transiciones.");
    }

    /**
     * @brief Intenta disparar una transición. Administra mutex y lógica de entrada al monitor.
     *
     * @param transition número de transición a disparar
     * @return true si se disparó o se encoló exitosamente, false si no fue posible
     */
    public boolean fireTransition(int transition) {
        agarrarMutex();
        System.out.println("Transicion " + transition + " adquirio mutex.");
        return entrarMonitor(transition);
    }

    /**
     * Intenta adquirir el mutex para entrar a la sección crítica
     */
    private static void agarrarMutex() {
        try {
            mutex.acquire();
            System.out.println("Mutex adquirido.");
        } catch (InterruptedException e) {
            System.out.println("Error al adquirir mutex.");
            Thread.currentThread().interrupt(); 
        }
    }

    /**
     * @brief Libera el mutex 
     */
    private static void liberarMutex() {
        if (mutex.availablePermits() == 0) {
            System.out.println("Liberando mutex.");
            mutex.release();
        }
    }

    /**
     * @brief Logica principal de entrada al monitor. Evalua si se puede disparar y actua en consecuencia
     *
     * @param t transicion que se quiere disparar
     * @return true si se se disparo, false en caso contrario
     */
    private boolean entrarMonitor(int t) {
        if (rdp.isFinalizado() || finPrograma) {
            setFin(); 
            liberarMutex();
            return false;
        }
        
        // FIX flagSleep: null-check + uso correcto de currentThread().getName()
        String currentName = Thread.currentThread().getName();
        if (flagSleep[t] != null && flagSleep[t].equals(currentName)) {
            flagSleep[t] = null; // Reset flag
        }
        
        int posibleDisparo = rdp.sePuedeDisparar(t);
        switch(posibleDisparo){
            case 0: //Se puede disparar
                if(ejecutarDisparo(t)) {    //Se  disparo la transicion
                    politica.actualizarPolitica(t); // Actualizar la politica de disparo
                    int hiloADespertar = hayHiloParaDespertar();                    
                    if (hiloADespertar>=0) { //SI Hay hilos en las colas de condicion para despertar 
                        colasCondicion[hiloADespertar].release(); // Despertar el hilo
                        System.out.println("Se desperto una transicion en cola de condicion.");
                    }else{                  //No hay hilos para despertar
                        liberarMutex();
                    }
                    return true;
                }else{          //Por alguna razon no se pudo disparar
                    liberarMutex();
                    return false;
                }
            case -1: //No se puede disparar, va a la cola de condicion.[temporales y no temporales]
                try {
                    derivarAColaCondicion(t);
                    if (finPrograma || Thread.currentThread().isInterrupted()) { //TERMINA EL PROGRAMA.
                        System.out.println("Programa finalizado o hilo interrumpido");
                        liberarMutex();
                        return false;
                    }
                    else return entrarMonitor(t);
                } catch (RuntimeException e) {
                    System.out.println("Error al esperar en la cola de condicion.");
                    Thread.currentThread().interrupt();
                    liberarMutex();
                    return false; 
                }                
            default: // Es temporal pero aun le falta tiempo.
                if(posibleDisparo>0){ // No esta en la ventana de disparo
                    if (flagSleep[t] == null) { // sos el primero? ver la flag. si es asi, dormis.
                        flagSleep[t] = Thread.currentThread().getName(); //levanto el flag.
                        liberarMutex();
                        dormirTemporal(posibleDisparo);
                        return false;
                    } else { // no sos el primero, cola de condicion.
                        try {
                            derivarAColaCondicion(t);
                            if (finPrograma || Thread.currentThread().isInterrupted()) {
                                System.out.println("Programa finalizado o hilo interrumpido");
                                return false;
                            } else return entrarMonitor(t);
                        } catch (RuntimeException e) {
                            System.out.println("Error al esperar en la cola de condicion.");
                            Thread.currentThread().interrupt();
                            return false;
                        }
                    }
                } else {
                    System.out.println("ERROR: Transicion " + t + " no es valida o no se puede disparar.\n");
                    liberarMutex();
                    System.exit(1);
                }
        }
        return false;
    }

    //----------------------------------------------------------------------------------
    // metodos de utilidad para el monitor.

    private int hayHiloParaDespertar() {
        Integer[] tSensibles = rdp.getTSensibles();
        for (int i = 0; i < tSensibles.length; i++) {
            if (colasCondicion[i].getQueueLength() > 0 && tSensibles[i] == 1) {
                if (politica.sePuedeDisparar(i)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * @brief Ejecuta el disparo de la transicion en la Red de Petri y verifica el marcado
     */
    private static boolean ejecutarDisparo(int t) {
        System.out.println("Intentando disparar transicion " + t + "...");
        boolean disparo = rdp.disparar(t);
        if (tieneMarcadoNegativo()) {
            System.out.println("Error: Marcado negativo detectado tras disparar T" + t);
            System.exit(1);
        }
        System.out.println("[OK] Transicion " + t + " disparada exitosamente.");
        return disparo;
    }

    /**
     * @brief Verifica si el marcado actual tiene valores negativos (estado invalido).
     */
    private static boolean tieneMarcadoNegativo() {
        System.out.println("Verificando marcado actual...");
        for (Integer valor : rdp.getMarcadoActual()) {
            if (valor < 0) {
                System.out.println("Marcado negativo detectado.");
                return true;
            }
        }
        return false;
    }
    
    /**
     * @brief Encola la transicion indicada y bloquea el hilo hasta ser despertado
     *
     * @param t transición que no pudo dispararse y debe esperar
     */
    private static void derivarAColaCondicion(int t) {
        System.out.println("Colocando transicion " + t + " en cola de condicion...");
        liberarMutex();

        if (finPrograma || Thread.currentThread().isInterrupted()) {
            System.out.println("Programa finalizado o hilo interrumpido, no se bloquea en cola de condicion.");
            return;
        }

        try {
            colasCondicion[t].acquire();
            System.out.println("Hilo de transicion " + t + " liberado de la cola.");
        } catch (InterruptedException e) {
            System.out.println("Hilo interrumpido al esperar en cola (T" + t + ").");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * @brief Libera todos los hilos en espera. Se ejecuta una sola vez al finalizar el sistema
     */
    private static void librerarColas() {
        if (colasLiberadas) return;
        System.out.println("Finalizando Monitor...");
        for (Semaphore colasCondicion1 : colasCondicion) {
            if (colasCondicion1.getQueueLength() > 0) { // Si hay hilos en la cola
                colasCondicion1.release();
            } 
        }
        System.out.println("Las colas de condicion fueron liberadas correctamente.");
        colasLiberadas = true; 
    }

    /**
     * @brief Solicita finalizar el programa
     */
    private void setFin(){
        finPrograma = true;
        if (!colasLiberadas) librerarColas();
    }

    /**
     * @brief Indica si se ha solicitado el fin del programa
     * 
     * @return true si se ha solicitado finalizar, false en caso contrario
     */
    public boolean isFin() {
        return finPrograma;
    }

    /**
     * @brief Duerme al hilo actual durante el tiempo especificado
     *
     * @param sensible tiempo de espera para que este en la ventana de disparo
     */
    private static void dormirTemporal(int sensible) {
        if (finPrograma || Thread.currentThread().isInterrupted()) {
            System.out.println("Programa finalizado o hilo interrumpido, no se duerme temporalmente.");
            return;
        }
        try {
            System.out.println("Durmiendo temporalmente por " + sensible + " ms...\n");
            TimeUnit.MILLISECONDS.sleep(sensible);
        } catch (InterruptedException e) {
            System.out.println("Hilo interrumpido durante el sleep temporal.");
            Thread.currentThread().interrupt();
        }
    }

} 