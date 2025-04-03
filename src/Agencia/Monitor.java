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
            mutex.acquire();
            if (!rdp.isSensible(transition)) {
                liberarMutex();
                return false;
            }
            return entrarMonitor(transition);
        } catch (InterruptedException e) {
            System.out.println("Error en fireTransition: " + e.getMessage());
            return false;
        }
    }

    private boolean entrarMonitor(int t) {
        if (!rdp.isSensible(t)) {
            liberarMutex();
            return false;
        }

        boolean transitionFired = ejecutarDisparo(t);

        if (!transitionFired) {
            derivarACola(t);
            return false;
        }

        int tSensible = hayColaCondicion();
        if (tSensible >= 0 && rdp.isSensible(tSensible)) {
            señalizarSig(tSensible);
        } else {
            if (colaEntrada.hasQueuedThreads()) {
                colaEntrada.release();
            }
            liberarMutex();
        }
        return true;
    }
    
    private void liberarMutex() {
        if (mutex.availablePermits() == 0) {
            mutex.release();
        }
    }

    private void señalizarSig(int tSensible) {
        if (rdp.isSensible(tSensible)) {
            colasConHilos[tSensible] = false;
            colasCondicion[tSensible].release();
        }
    }

    private boolean ejecutarDisparo(int transition) {
        if (!rdp.isSensible(transition)) {
            return false;
        }
        
        boolean disparo = rdp.disparar(transition);
        
        if (!disparo || tieneMarcadoNegativo()) {
            System.out.println("Error: Marcado negativo detectado tras disparar T" + transition);
            return false;
        }
        return true;
    }
    
    private boolean tieneMarcadoNegativo() {
        for (Integer valor : rdp.getMarcadoActual()) {
            if (valor < 0) {
                return true;
            }
        }
        return false;
    }

    private int hayColaCondicion() {
        int n = colasConHilos.length;
        int inicio = (ultimoOrden + 1) % n;
        for (int i = inicio; i < n; i++) {
            if (colasConHilos[i] && rdp.isSensible(i)) {
                ultimoOrden = i;
                return i;
            }
        }
        for (int i = 0; i < inicio; i++) {
            if (colasConHilos[i] && rdp.isSensible(i)) {
                ultimoOrden = i;
                return i;
            }
        }
        return -1;
    }

    private void derivarACola(int t) {
        try {
            colasConHilos[t] = true;
            liberarMutex();
            colasCondicion[t].acquire();
        } catch (InterruptedException e) {
            System.out.println("No se pudo derivar a la cola de Condicion");
            e.printStackTrace();
        }
    }
}
