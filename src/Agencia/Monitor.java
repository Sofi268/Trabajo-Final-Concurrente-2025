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
    private static boolean[] colasConHilos; // Cantidad de hilos en cada cola de condicion
    private static int ultimoOrden; // Última transicion que se desperto.
    private static boolean finPrograma = false; // Indica si se ha solicitado finalizar el programa
    private static boolean colasLiberadas = false; // Indica si las colas han sido liberadas
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
        colasConHilos = new boolean[rdp.getTransiciones()];
        ultimoOrden = -1;
        for (int i = 0; i < rdp.getTransiciones(); i++) {
            colasCondicion[i] = new Semaphore(0);
            colasConHilos[i] = false; 
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
    private static boolean entrarMonitor(int t) {
        if(finPrograma){
            if(!colasLiberadas) {
                librerarColas(); 
            }
            System.out.println("Programa finalizado, no se puede entrar al monitor.");
            rdp.imprimirMarcado();
            rdp.comprobarInvariantes();
            liberarMutex();
            return false; 
        }

        int posibleDisparo = rdp.sePuedeDisparar(t);
        switch(posibleDisparo){
            case 0: //Se puede disparar
                if(ejecutarDisparo(t)) {
                    //Se  disparo la transicion
                    boolean sePudoSenializar = intentarSenializar();
                    if (!sePudoSenializar) { //No hay hilos en las colas de condicion
                        liberarMutex();
                        return true;
                    }
                    else{ //Se despertó un hilo en la cola de condicion
                        System.out.println("Se desperto una transicion en cola de condicion.");
                        return true; 
                    }
                }
                else{ //Por alguna razon no se pudo disparar
                    liberarMutex();
                    return false;
                }

            case -1: //No se puede disparar, no es temporal
                //Ver cola de condicion
                if (!colasConHilos[t]) { //Si no hay hilos esperando en la cola de condicion
                    try {
                        derivarAColaCondicion(t);
                        return entrarMonitor(t);

                    } catch (RuntimeException e) {
                        System.out.println("Error al esperar en la cola de condicion.");
                        Thread.currentThread().interrupt(); 
                    }
                } else { // Si hay un hilo esperando en la cola de condicion
                    liberarMutex(); 
                    return false;
                }

            case -2: // Es temporal pero no lo permite la politica
                liberarMutex();
                return false;
                
            default: 

                if(posibleDisparo>0){ //Es temporal pero todavia le falta tiempo para dispararse
                    liberarMutex();
                    dormirTemporal(posibleDisparo);
                    return false;
                }

                else{
                    System.out.println("ERROR: Transicion " + t + " no es valida o no se puede disparar.\n");
                    liberarMutex();
                    System.exit(1);
                }
        }
        return false;
    }

    //----------------------------------------------------------------------------------
    // metodos de utilidad para el monitor.

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
     * @brief Intenta despertar un hilo en alguna cola de condicion, si es sensible
     *
     * @return true si se senialo a algún hilo, false si no habia hilos esperando
     */
    private static boolean intentarSenializar() {
        System.out.println("Buscando transiciones en cola de condicion...");
        int tSiguiente = buscarEnColaCondicion();
        if (tSiguiente >= 0) {
            senializarSig(tSiguiente);
            return true;
        } else {
            System.out.println("No hay transiciones en cola de condicion.");
            return false;
        }
    }

    /**
     * @brief Busca una transicion sensible con hilos en espera.
     *
     * @return indice de la transicion encontrada o -1 si ninguna esta disponible
     */
    private static int buscarEnColaCondicion() {
        System.out.println("Buscando en colas de condicion...");
        System.out.print("Estado de colasConHilos: ");
        for (int i = 0; i < colasConHilos.length; i++) {
            System.out.print("T" + i + "=" + colasConHilos[i] + " ");
        }
        System.out.println(); 

        int n = Constantes.cantidadTransiciones;
        int inicio = (ultimoOrden + 1) % n;

        for (int i = inicio; i < n; i++) {
            if ((colasConHilos[i]) && rdp.isSensible(i)) {
                ultimoOrden = i;
                System.out.println("Encontrada transicion en cola: " + i);
                return i;
            }
        }

        for (int i = 0; i < inicio; i++) {
            if ((colasConHilos[i]) && rdp.isSensible(i)) {
                ultimoOrden = i;
                System.out.println("Encontrada transicion en cola: " + i);
                return i;
            }
        }

        System.out.println("No se encontraron transiciones en cola de condicion.");
        return -1;
    }

    /**
     * @brief Senializa y libera un hilo en espera para la transicion dada.
     *
     * @param tSensible transicion que sera senializada
     */
    private static void senializarSig(int tSensible) {
        System.out.println("Liberando hilo en cola de transicion " + tSensible);
        colasConHilos[tSensible] = false; 
        colasCondicion[tSensible].release();
    }

    /**
     * @brief Encola la transicion indicada y bloquea el hilo hasta ser despertado
     *
     * @param t transición que no pudo dispararse y debe esperar
     */
    private static void derivarAColaCondicion(int t) {
        System.out.println("Colocando transicion " + t + " en cola de condicion...");
        colasConHilos[t] = true;
        liberarMutex();

        if (finPrograma || Thread.currentThread().isInterrupted()) {
            colasConHilos[t] = false;
            System.out.println("Programa finalizado o hilo interrumpido, no se bloquea en cola de condicion.");
            return;
        }

        try {
            colasCondicion[t].acquire();
            System.out.println("Hilo de transicion " + t + " liberado de la cola.");
        } catch (InterruptedException e) {
            colasConHilos[t] = false;
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
        for (int i = 0; i < colasCondicion.length; i++) {
            if (colasConHilos[i]) {
                colasCondicion[i].release(); 
            }
        }
        System.out.println("Las colas de condicion fueron liberadas correctamente.");
        colasLiberadas = true; 
    }

    /**
     * @brief Solicita finalizar el programa
     */
    public void setFin(){
        finPrograma = true; 
        System.out.println("Se ha solicitado finalizar el programa. Liberando colas de condicion...");
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
        