package Agencia;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Monitor implements MonitorInterface{
    private static Monitor uniqueInstance;
    private static RedDePetri rdp = RedDePetri.getInstance();
    // -------------------------------------------------------------------------------------------------
    private static Semaphore mutex = new Semaphore(1, true);
    private static Semaphore[] colasCondicion; // Colas de condición para cada transición
    private static boolean[] colasConHilos; // Cantidad de hilos en cada cola de condición
    private static int ultimoOrden; // Última transición que se desperto.
    private static boolean finPrograma = false; // Indica si se ha solicitado finalizar el programa
    private static boolean colasLiberadas = false; // Indica si las colas han sido liberadas
    // -------------------------------------------------------------------------------------------------
    public Monitor(){
        System.out.println("Monitor creado.");
    }
    public static Monitor getInstance() {
        if (uniqueInstance == null) {
            System.out.println("Instanciando Monitor...");
            uniqueInstance = new Monitor();
            startMonitor();
        }
        return uniqueInstance;
    }
// -------------------------------------------------------------------------------------------------
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

    /* encargado de administrar el ingreso al monitor y la obtencion del mutex.
    */
    public boolean fireTransition(int transition) {
        System.out.println("Transición " + transition + " intentando adquirir mutex...");
        agarrarMutex();
        System.out.println("Transición " + transition + " adquirió mutex.");
        return entrarMonitor(transition);
    }
    
    private static void agarrarMutex() {
        try {
            mutex.acquire();
            System.out.println("Mutex adquirido.");
        } catch (InterruptedException e) {
            System.out.println("Error al adquirir mutex.");
            Thread.currentThread().interrupt(); 
        }
    }

    private static void liberarMutex() {
        if (mutex.availablePermits() == 0) {
            System.out.println("Liberando mutex.");
            mutex.release();
        }
    }

    private static boolean entrarMonitor(int t) {
        if(finPrograma){
            if(!colasLiberadas) {
                librerarColas(); 
            }
            System.out.println("Programa finalizado, no se puede entrar al monitor.");
            liberarMutex();
            return false; 
        }

        System.out.println("Entrando al monitor con transición: " + t);        

        if (rdp.isSensible(t)) { //Se puede disparar
            System.out.println("Transición " + t + " se puede disparar.");
            if(ejecutarDisparo(t)) {
                //Se  disparo la transición
                boolean sePudoSenializar = intentarSenializar();
                if (!sePudoSenializar) {
                    liberarMutex();
                    return true;
                }
                else{
                    System.out.println("Se despertó una transición en cola de condición.");
                    return true; // Se despertó una transición en cola de condición
                }
            }
            else{
                liberarMutex();
                return false;
            }
        }
        else{ //No se puede disparar
            if(!colasConHilos[t]) { // Si hay un hilo esperando en la cola de condición
                System.out.println("Transición " + t + " no es sensible, derivando a cola de condición.");
                derivarAColaCondicion(t);
                return entrarMonitor(t); // Volver a intentar entrar al monitor
            } 
            else { // Si no hay hilos esperando en la cola de condición
                System.out.println("Transición " + t + " no es sensible, pero ya hay un hilo en la cola de condicion.");
                liberarMutex();
                return false; // No se pudo disparar y no hay hilos esperando
            }
        }
    }

    //----------------------------------------------------------------------------------
    // metodos de utilidad para el monitor.
    private static boolean ejecutarDisparo(int t) {
        System.out.println("Intentando disparar transición " + t + "...");
        boolean disparo = rdp.disparar(t);
        if (tieneMarcadoNegativo()) {
            System.out.println("Error: Marcado negativo detectado tras disparar T" + t);
            System.exit(1);
        }
        System.out.println("[OK] Transición " + t + " disparada exitosamente.");
        return disparo;
    }

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
    
    private static boolean intentarSenializar() {
        System.out.println("Buscando transiciones en cola de condición...");
        int tSiguiente = buscarEnColaCondicion();
        if (tSiguiente >= 0) {
            señalizarSig(tSiguiente);
            return true;
        } else {
            System.out.println("No hay transiciones en cola de condición.");
            return false;
        }
    }

    private static int buscarEnColaCondicion() {
        System.out.println("Buscando en colas de condición...");
        int n = Constantes.cantidadTransiciones;
        int inicio = (ultimoOrden + 1) % n;
        for (int i = inicio; i < n; i++) {
            if ((colasConHilos[i]) && rdp.isSensible(i)) {
                ultimoOrden = i;
                System.out.println("Encontrada transición en cola: " + i);
                return i;
            }
        }

        for (int i = 0; i < inicio; i++) {
            if ((colasConHilos[i]) && rdp.isSensible(i)) {
                ultimoOrden = i;
                System.out.println("Encontrada transición en cola: " + i);
                return i;
            }
        }
        System.out.println("No se encontraron transiciones en cola de condición.");
        return -1;
    }

    private static void señalizarSig(int tSensible) {
        System.out.println("Liberando hilo en cola de transición " + tSensible);
        colasConHilos[tSensible] = false; // Marcar que ya no hay hilos esperando
        colasCondicion[tSensible].release();
    }

    private static void derivarAColaCondicion(int t) {
        System.out.println("Colocando transición " + t + " en cola de condición...");
        try {
            colasConHilos[t]= true; // Marcar que hay un hilo esperando
            liberarMutex();
            colasCondicion[t].acquire();
            System.out.println("Hilo de transición " + t + " liberado de la cola.");
        } catch (InterruptedException e) {
            System.out.println("Error al derivar a cola de condición para transición " + t);
            e.printStackTrace();
        }
    }

    private static void librerarColas() {
        System.out.println("Finalizando Monitor...");
        for (int i = 0; i < colasCondicion.length; i++) {
            if (colasConHilos[i]) {
                colasCondicion[i].release(); 
            }
        }
        System.out.println("Las colas de condicion fueron liberadas correctamente.");
        colasLiberadas = true; 
    }

    public void setFin(){
        finPrograma = true; // Indica que se ha solicitado finalizar el programa
        System.out.println("Se ha solicitado finalizar el programa. Liberando colas de condición...");
    }

}