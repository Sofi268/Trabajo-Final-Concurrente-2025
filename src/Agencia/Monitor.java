package Agencia;

import java.util.concurrent.Semaphore;

public class Monitor implements MonitorInterface {
    private static Monitor uniqueInstance;
    private static RedDePetri rdp = RedDePetri.getInstance();
    private static final Politicas politicas = Politicas.getInstance("Balanceada");
    private static Semaphore mutex = new Semaphore(1, true);
    private static Semaphore colaEntrada = new Semaphore(0, true);
    private static Semaphore[] colasCondicion;
    private static boolean[] colasConHilos;
    private static int ultimoOrden;

    public Monitor() {
    }

    /* Aplico patron strategy ...
    * */
    public static Monitor getInstance(){
        if(uniqueInstance == null){
            uniqueInstance = new Monitor();
            startMonitor();
        }
        return uniqueInstance;
    }

    /* startMonitor:
    * */
    public static void startMonitor(){
        colasCondicion = new Semaphore[rdp.getTransiciones()];
        colasConHilos = new boolean[rdp.getTransiciones()];
        ultimoOrden = -1;
        for (int i = 0; i < rdp.getTransiciones(); i++) {
            colasCondicion[i] = new Semaphore(0);
        }
        System.out.println("Monitor inicializado con " + rdp.getTransiciones() + " transiciones.");
    }

    @Override
    public boolean fireTransition(int transition) {
            System.out.println("Intentando disparar transición: " + transition);
        agarrarMutex();
            System.out.println("Mutex adquirido para transición: " + transition);
        entrarMonitor(transition);
        return false;
    }
    //-------------------------------------------------------------------------------------------------------------
    /**
     * Se intenta agarrar el mutex:
     *      si -> el hilo ingresa en la sección crítica (monitor)
     *      no ->
     */
    private void agarrarMutex(){
        try {
            mutex.acquire();
        }
        catch (InterruptedException e) {
            System.out.println("Error en fireTransition: " + e.getMessage());
            //System.exit(1);
        }
    }
    private static void liberarMutex() {
        if (mutex.availablePermits() == 0) {
            System.out.println("Liberando mutex.");
            mutex.release();
        }
    }

    //-------------------------------------------------------------------------------------------------------------
    private static boolean entrarMonitor(int t) {
        //espacio para optmizar sies temporal o no y si esta sensibilizado o no

        if (!rdp.isSensible(t)) {
            System.out.println("Transición " + t + " no es sensible. Derivando a cola.");
            derivarACola(t);
            return false;
        }

        System.out.println("Entrando al monitor con transición: " + t);
        boolean transitionFired = ejecutarDisparo(t);

        if (!transitionFired) {
            System.out.println("No se pudo disparar transición " + t + ". Derivando a cola.");
            derivarACola(t);
            return false;
        }

        int tSensible = hayColaCondicion();
        if (tSensible >= 0) {
            System.out.println("Señalizando siguiente transición en cola: " + tSensible);
            señalizarSig(tSensible);
        } else {
            System.out.println("No hay transiciones en cola. Liberando mutex.");
            if (colaEntrada.hasQueuedThreads()) {
                colaEntrada.release();
            }
            liberarMutex();
        }
        return true;
    }

    private static void señalizarSig(int tSensible) {
        System.out.println("Liberando hilo en cola de transición " + tSensible);
        colasConHilos[tSensible] = false;
        colasCondicion[tSensible].release();
    }

    private static boolean ejecutarDisparo(int transition) {
       
        boolean disparo = rdp.disparar(transition);
        
        if (!disparo || tieneMarcadoNegativo()) {
            System.out.println("Error: Marcado negativo detectado tras disparar T" + transition);
            return false;
        }
        System.out.println("Transición " + transition + " disparada exitosamente.");
        return true;
    }
    
    private static boolean tieneMarcadoNegativo() {
        for (Integer valor : rdp.getMarcadoActual()) {
            if (valor < 0) {
                System.out.println("Marcado negativo detectado.");
                return true;
            }
        }
        return false;
    }

    private static int hayColaCondicion() {
        int n = colasConHilos.length;
        int inicio = (ultimoOrden + 1) % n;
        System.out.println("Buscando transiciones en cola de condición.");
        for (int i = inicio; i < n; i++) {
            if (colasConHilos[i] && rdp.isSensible(i)) {
                ultimoOrden = i;
                System.out.println("Encontrada transición en cola: " + i);
                return i;
            }
        }
        for (int i = 0; i < inicio; i++) {
            if (colasConHilos[i] && rdp.isSensible(i)) {
                ultimoOrden = i;
                System.out.println("Encontrada transición en cola: " + i);
                return i;
            }
        }
        System.out.println("No se encontraron transiciones en cola de condición.");
        return -1;
    }

    private static void derivarACola(int t) {
        try {
            System.out.println("Colocando transición " + t + " en cola de condición.");
            colasConHilos[t] = true;
            liberarMutex();
            colasCondicion[t].acquire();
            System.out.println("Hilo de transición " + t + " liberado de la cola.");
            entrarMonitor(t);
        } catch (InterruptedException e) {
            System.out.println("No se pudo derivar a la cola de condición");
            e.printStackTrace();
        }
    }
}
