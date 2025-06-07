package Agencia;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Monitor {
    private static Monitor uniqueInstance;
    private static RedDePetri rdp = RedDePetri.getInstance();
    // -------------------------------------------------------------------------------------------------
    private static Politicas politica;  // a prriori se setea que tio de politica se usara.
    private static Semaphore mutex = new Semaphore(1, true);
    private static Semaphore[] colasCondicion; // Colas de condición para cada transición
    private static boolean[] colasConHilos; // Cantidad de hilos en cada cola de condición
    private static int ultimoOrden; // Última transición que se desperto.
    // -------------------------------------------------------------------------------------------------
    public Monitor() {
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
        politica = Politicas.getInstance("Balanceada"); // "Balanceada" o "Prioridad".
        colasCondicion = new Semaphore[rdp.getTransiciones()];
        colasConHilos = new boolean[rdp.getTransiciones()];
        ultimoOrden = -1;
        for (int i = 0; i < rdp.getTransiciones(); i++) {
            colasCondicion[i] = new Semaphore(0);
            colasConHilos[i] = false; // Inicializa todas las colas como vacías.
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
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void liberarMutex() {
        if (mutex.availablePermits() == 0) {
            System.out.println("Liberando mutex.");
            mutex.release();
        }
    }

    private static boolean entrarMonitor(int t) {
        System.out.println("Entrando al monitor con transición: " + t);        
    
        if (rdp.isSensible(t)) {
            System.out.println("Transición " + t + " se puede disparar.");
            if( ejecutarDisparo(t)) {
                liberarMutex();
                return true;
            }
            else{
                liberarMutex();
                return false;
            }
        }else{
            System.out.println("Transición " + t + " no es sensible.");
            liberarMutex();
            return false;
        }
    }
    //----------------------------------------------------------------------------------
    // metodos de utilidad para el monitor.
    private static boolean ejecutarDisparo(int t) {
        System.out.println("Intentando disparar transición " + t + "...");
        boolean disparo = rdp.disparar(t);
        if (!disparo || tieneMarcadoNegativo()) {
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


}