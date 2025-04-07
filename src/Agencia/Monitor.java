package Agencia;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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

    public static void startMonitor() {
        System.out.println("Iniciando Monitor...");
        politica = Politicas.getInstance("Balanceada"); // "Balanceada" o "Prioridad".
        colasCondicion = new Semaphore[rdp.getTransiciones()];
        colasConHilos = new int[rdp.getTransiciones()];
        ultimoOrden = -1;
        for (int i = 0; i < rdp.getTransiciones(); i++) {
            colasCondicion[i] = new Semaphore(0);
            colasConHilos[i] = 0; // Inicializa todas las colas como vacías
        }
        System.out.println("Monitor inicializado con " + rdp.getTransiciones() + " transiciones.");
    }

    @Override
    public boolean fireTransition(int transition) {
        System.out.println("Intentando disparar transición: " + transition);
        return puertaSalaEspera(transition);
    }

    private static boolean puertaSalaEspera(int transition) {
        System.out.println("Transición " + transition + " intentando entrar a la cola de entrada...");
        try {
            colaEntrada.acquire();
            System.out.println("Transición " + transition + " adquirió cola de entrada.");
        } catch (InterruptedException e) {
            System.out.println("Error al adquirir cola de entrada para transición " + transition);
            e.printStackTrace();
        }
        return puertaMonitor(transition);
    }

    private static boolean puertaMonitor(int transition) {
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

    private static void liberarColaEntrada() {
       if (colaEntrada.hasQueuedThreads()) {
            System.out.println("Liberando cola de entrada.");
            colaEntrada.release();
        } else {
            System.out.println("No hay hilos en cola de entrada.");
        } 
    }

    private static boolean entrarMonitor(int t) {
        System.out.println("Entrando al monitor con transición: " + t);
        if (checkGeneral(t)) {
            System.out.println("Transición " + t + " pasó las verificaciones generales.");
            ejecutarDisparo(t);
            boolean sePudoSenializar = intentarSenializar();
            liberarMutex();
            if (!sePudoSenializar) {
                liberarColaEntrada();
            }
        } else {
            System.out.println("Derivando a cola de condición.");
            derivarAColaCondicion(t);
            puertaMonitor(t);
        }
        return true;
    }

    private static boolean checkGeneral(int t) {
        System.out.println("Verificando transición " + t + "...");
        if (rdp.isSensible(t)){
            if(checkPolicy(t)){
                if(checkTemporal(t)){
                    System.out.println("Transición " + t + " es sensible, cumple política y está en ventana temporal.");
                    return true;
                }else System.out.println("Transición " + t + " no está en ventana temporal.");
            }else System.out.println("Transición " + t + " no cumple política.");
        
        }else System.out.println("Transición " + t + " no es sensible.");
        
        //System.out.println("Transición " + t + " no pasó las verificaciones.");
        return false;
    }

    private static boolean checkPolicy(int t) {
        System.out.println("Verificando política para transición " + t + "...");
        boolean check = true;
        for (int conflicto : Constantes.conflictos) {
            if (conflicto == t) {
                if (!politica.sePuedeDisparar(t)) {
                    System.out.println("Transición " + t + " no cumple política.");
                    return false;
                }
            }
        }
        return check;
    }

    private static boolean checkTemporal(int t) {
        System.out.println("Verificando si transición " + t + " es temporal...");
        if (esTemporal(t)) {
            return checkTemporaryShot(t);
        }
        return true;
    }

    private static boolean checkTemporaryShot(int t) {
        System.out.println("Verificando disparo temporal para transición " + t + "...");
        Long tiempoRestanteTransicion = rdp.tiempoSensibilizado(t);
        System.out.println("Tiempo restante para transición " + t + ": " + tiempoRestanteTransicion);
        if (tiempoRestanteTransicion != null) {
            if (tiempoRestanteTransicion == 0) {
                System.out.println("Transición " + t + " está en la ventana.");
                return true;
            } else {
                System.out.println("Transición " + t + " está " + tiempoRestanteTransicion + " ms antes de la ventana.");
                return false;
            }
        } else {
            System.out.println("Transición " + t + " se pasó de la ventana.");
            System.exit(-1);
        }
        return false;
    }

    private static Boolean esTemporal(int t) {
        System.out.println("Verificando si transición " + t + " es temporal...");
        List<Integer> lista = Arrays.asList(Arrays.stream(Constantes.tTemporales).toArray(Integer[]::new));
        return lista.contains(t);
    }

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
            if ((colasConHilos[i] > 0) && checkGeneral(i)) {
                ultimoOrden = i;
                System.out.println("Encontrada transición en cola: " + i);
                return i;
            }
        }
        for (int i = 0; i < inicio; i++) {
            if ((colasConHilos[i] > 0) && checkGeneral(i)) {
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
        colasCondicion[tSensible].release();
        colasConHilos[tSensible]--;
    }

    private static void derivarAColaCondicion(int t) {
        System.out.println("Colocando transición " + t + " en cola de condición...");
        try {
            colasConHilos[t]++;
            liberarColaEntrada();
            liberarMutex();
            colasCondicion[t].acquire();
            System.out.println("Hilo de transición " + t + " liberado de la cola.");
        } catch (InterruptedException e) {
            System.out.println("Error al derivar a cola de condición para transición " + t);
            e.printStackTrace();
        }
    }
}
