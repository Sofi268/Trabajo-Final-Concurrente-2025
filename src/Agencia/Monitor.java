package Agencia;
import java.util.concurrent.Semaphore;

public class Monitor implements MonitorInterface{
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
    /* Aplico patron strategy para asegurar que solo haya una sola instancia del mismo.
     * */
    public static Monitor getInstance(){
        if(uniqueInstance == null){
            uniqueInstance = new Monitor();
            startMonitor();
        }
        return uniqueInstance;
    }
    /* startMonitor: Setea todos los valores inciales del monitor.
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
    // -----------------------------------------------------------------------------------------------------------------
    // logica de monitor.
    @Override
    public boolean fireTransition(int transition) {
            System.out.println("Intentando disparar transición: " + transition);
        agarrarMutex();
            System.out.println("Mutex adquirido para transición: " + transition);
        entrarMonitor(transition);
        return false;
    }
    //mutex --------------------------
    private void agarrarMutex(){
        try {
            mutex.acquire();
        }catch (InterruptedException e) {
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
    // Area monitor: ----------------

    /**
     * Una vez agarrado el mutex chequea que la transicion esté sensibilizada, si lo está se dispara
     * y si no lo está el hilo va a la cola de condicion de esa transición.
     * Si la transicion se dispara, antes de volver (y liberar el mutex) se fija si hay transiciones esperando sensibilizadas
     * Mientras existan, no va a liberar el mutex y se van a disparar todas
     * @param t transición a disparar.
     */
    private static void entrarMonitor(int t) {
        if (rdp.isSensible(t)){  // si esta sensibilizada la transicion.
            System.out.println("Entrando al monitor con transición: " + t);
            if (esTemporal(t)){  // si es temporal
                ejecutarDisparoTemporal(t);
            }else{  // no es temporal.
                ejecutarDisparo(t);
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
        }else{   // no esta sensiblilizada.
            System.out.println("Transición " + t + " no es sensible. Derivando a cola.");
            derivarACola(t);  // se coloca en la cola de condicion y se libera el mutex.
            entrarMonitor(t);  // ingresa nuevamente
        }
    }
    // Disparos:
    /**
     * Se realiza el disparo de la transición si está en la ventana.
     * En el caso de encontrarse antes, el hilo suelta el mútex y se duerme. Cuando sale del sleep, intenta tomar el mútex y disparar nuevamente
     * Si la transición se pasó de la ventana, sale del método y el hilo sale del monitor.
     */
    public static void ejecutarDisparoTemporal(Integer T){

    }

    /**
     * Dispara la transicion dentro de la red e incrementa la cantidad de invariantes en la clase Politicas, luego
     * chequea si hay Transiciones en la cola de concicion, si hay la dispara, incrementa la cantidad de invariantes en
     * la clase Politica y saca a la transicion de la cola de Condicion
     * @param t transición a disparar
     */
    private static boolean ejecutarDisparo(Integer t){
        boolean disparo = rdp.disparar(t);
        if (!disparo || tieneMarcadoNegativo()) {
            System.out.println("Error: Marcado negativo detectado tras disparar T" + t);
            System.out.println("No se pudo disparar transición " + t + ". Derivando a cola.");
            derivarACola(t);
            return false;
        }
        System.out.println("Transición " + t + " disparada exitosamente.");
        return true;
    }
    //-------------------------------------------------------------------------------------------------------
    private static void señalizarSig(int tSensible) {
        System.out.println("Liberando hilo en cola de transición " + tSensible);
        colasConHilos[tSensible] = false;
        colasCondicion[tSensible].release();
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
            liberarMutex();  // se libera el mutex.
            colasCondicion[t].acquire();
            System.out.println("Hilo de transición " + t + " liberado de la cola.");
        } catch (InterruptedException e) {
            System.out.println("No se pudo derivar a la cola de condición");
            e.printStackTrace();
        }
    }
    //-------------------------------------------------------------------------------------------------------
    private static Boolean esTemporal(Integer T){  // completar verificacion...
        return false;  // por defecto
    }
}
