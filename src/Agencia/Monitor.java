package Agencia;

import java.util.concurrent.Semaphore;

public class Monitor implements MonitorInterface {
    private static Monitor uniqueInstance;
    private static RedDePetri rdp = RedDePetri.getInstance();
    private static Politicas politica;
    private static Semaphore mutex = new Semaphore(1, true);
    private static Semaphore colaEntrada = new Semaphore(1, true);
    private static Semaphore[] colasCondicion;
    private static int[] colasConHilos;
    private static int ultimoOrden;

    public Monitor() {
    }

    /* Aplico patron Singleton ...
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
    	politica = Politicas.getInstance("Balanceada");
        colasCondicion = new Semaphore[rdp.getTransiciones()];
        colasConHilos = new int[rdp.getTransiciones()];
        ultimoOrden = -1;
        for (int i = 0; i < rdp.getTransiciones(); i++) {
            colasCondicion[i] = new Semaphore(0);
            colasConHilos[i] = 0; // Inicializa todas las colas como vacías
        }
        System.out.println("Monitor inicializado con " + rdp.getTransiciones() + " transiciones.");
    }
    // -----------------------------------------------------------------------------------------------------------------
    // logica de monitor.
    @Override
    public boolean fireTransition(int transition) {
        return fire(transition);
    }
    private static boolean fire(int transition) {
        try {
            colaEntrada.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        puertaMonitor(transition);
        return false;
    }
    private static void puertaMonitor(Integer transition){
        System.out.println("Intentando agarrar mutex: " + transition);
        agarrarMutex();
        System.out.println("Mutex adquirido para transición: " + transition);
        entrarMonitor(transition);
    }
    //-------------------------------------------------------------------------------------------------------------
    /**
     * Se intenta agarrar el mutex:
     */
    private static void agarrarMutex(){
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
        if (rdp.isSensible(t)){  // si esta sensibilizada la transicion y la politica lo permite
            System.out.println("Entrando al monitor con transición: " + t);
            if(t==2 || t==3 || t==6 || t==7) {
                if(!politica.sePuedeDisparar(t)) {
                    System.out.println("Transición " + t + " no se puede disparar todavia por la politica "+ politica.getNombre() + ". Derivando a cola.");
                    derivarACola(t);
                    return false;
                }
            }
            System.out.println("Entrando al monitor con transición: " + t);
            ejecutarDisparo(t);

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
        }else{
            System.out.println("Transición " + t + " no es sensible. Derivando a cola.");
            derivarACola(t);
            return false;
        }
    }
    private static void derivarACola(int t) {
        try {
            System.out.println("Colocando transición " + t + " en cola de condición.");
            colasConHilos[t] ++;
            liberarMutex();
            colaEntrada.release();
            colasCondicion[t].acquire();
            System.out.println("Hilo de transición " + t + " liberado de la cola.");
            entrarMonitor(t);
        } catch (InterruptedException e) {
            System.out.println("No se pudo derivar a la cola de condición");
            e.printStackTrace();
        }
    }
    private static void señalizarSig(int tSensible) {
        System.out.println("Liberando hilo en cola de transición " + tSensible);
        colasCondicion[tSensible].release();
        colasConHilos[tSensible]--; // se libera la cola de condicion.
    }
    /**
     * Dispara la transicion dentro de la red e incrementa la cantidad de invariantes en la clase Politicas, luego
     * chequea si hay Transiciones en la cola de concicion, si hay la dispara, incrementa la cantidad de invariantes en
     * la clase Politica y saca a la transicion de la cola de Condicion
     * @param t transición a disparar
     */
    private static void ejecutarDisparo(Integer t){
        boolean disparo = rdp.disparar(t);
        if (!disparo || tieneMarcadoNegativo()) {
            System.out.println("Error: Marcado negativo detectado tras disparar T" + t);
            System.out.println("No se pudo disparar transición " + t + ". Derivando a cola.");
            derivarACola(t);
            System.exit(1);
        }
        System.out.println("Transición " + t + " disparada exitosamente.");
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
        int n = 12;
        int inicio = (ultimoOrden + 1) % n;
        System.out.println("Buscando transiciones en cola de condición.");
        for (int i = inicio; i < n; i++) {
            if ((colasConHilos[i]>0) && rdp.isSensible(i)) {
                ultimoOrden = i;
                System.out.println("Encontrada transición en cola: " + i);
                return i;
            }
        }
        for (int i = 0; i < inicio; i++) {
            if ((colasConHilos[i]>0) && rdp.isSensible(i)) {
                ultimoOrden = i;
                System.out.println("Encontrada transición en cola: " + i);
                return i;
            }
        }
        System.out.println("No se encontraron transiciones en cola de condición.");
        return -1;
    }
}

