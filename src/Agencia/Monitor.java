package Agencia;

import java.util.concurrent.Semaphore;

public class Monitor implements MonitorInterface {
    private static RedDePetri rdp;
    private static final Politicas politicas = Politicas.getInstance("Balanceada");
    private static Semaphore mutex = new Semaphore(1, true);
    private static Semaphore colaEntrada = new Semaphore(0, true);
    private static Semaphore[] colasCondicion;
    private static boolean[] colasConHilos;
    private int ultimoOrden;

    public Monitor(RedDePetri redPetri) {
        rdp = redPetri;
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
        try {
            System.out.println("Intentando disparar transición: " + transition);
            mutex.acquire();
            System.out.println("Mutex adquirido para transición: " + transition);
            if (!rdp.isSensible(transition)) {
                System.out.println("Transición " + transition + " no es sensible. Derivando a cola.");
                derivarACola(transition);
                return false;
            }
            return entrarMonitor(transition);
        } catch (InterruptedException e) {
            System.out.println("Error en fireTransition: " + e.getMessage());
            return false;
        }
    }

    private boolean entrarMonitor(int t) {
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
    
    private void liberarMutex() {
        if (mutex.availablePermits() == 0) {
            System.out.println("Liberando mutex.");
            mutex.release();
        }
    }

    private void señalizarSig(int tSensible) {
        System.out.println("Liberando hilo en cola de transición " + tSensible);
        colasConHilos[tSensible] = false;
        colasCondicion[tSensible].release();
    }

    private boolean ejecutarDisparo(int transition) {
       
        boolean disparo = rdp.disparar(transition);
        
        if (!disparo || tieneMarcadoNegativo()) {
            System.out.println("Error: Marcado negativo detectado tras disparar T" + transition);
            return false;
        }
        System.out.println("Transición " + transition + " disparada exitosamente.");
        return true;
    }
    
    private boolean tieneMarcadoNegativo() {
        for (Integer valor : rdp.getMarcadoActual()) {
            if (valor < 0) {
                System.out.println("Marcado negativo detectado.");
                return true;
            }
        }
        return false;
    }

    private int hayColaCondicion() {
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

    private void derivarACola(int t) {
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
