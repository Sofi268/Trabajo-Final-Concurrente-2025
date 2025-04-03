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
    	//Primero un hilo entra e intenta agarrar el mutex para ingresar al monitor
    	if(mutex.availablePermits()>0) {
    		entrarMonitor(transition);
    	}
    	//Si no puede, se duerme en la cola de espera
    	else {
    		try {
				colaEntrada.acquire();
				entrarMonitor(transition); // Entra al monitor una vez que lo sacan de la cola de entrada
			} catch (InterruptedException e) {
				System.out.println("No se pudo ir a la cola de entrada a dormir");
				e.printStackTrace();
			}
    	}
        return false;
    }

    private void entrarMonitor(int t) {
    	int tSensible;
    	tomarMutex();
    	if(ejecutarDisparo(t)) {
    		tSensible = hayColaCondicion();
    		// Caso en que hay hilos en Cola de Condicion
    		if(tSensible>=0) {
        		señalizarSig(tSensible);
        		liberarMutex();
        	}
    		//Caso en el que no hay Hilos en Colas de Condicion 
        	else {
        		if(colaEntrada.getQueueLength()>0) {
        			colaEntrada.release();
        		}
        		liberarMutex();
        	}
    	}
    	else {
    		System.out.println("Estoy por el else");
    		derivarACola(t);
    		entrarMonitor(t);
    	}    	
    }
    
    private void tomarMutex() {
    	try {
			mutex.acquire();
		} catch (InterruptedException e) {
			System.out.println("No se pudo tomar el mutex");
			e.printStackTrace();
		}
    }

    private void liberarMutex() {
        mutex.release();
    }
    
    private void señalizarSig(int tSensible) {
    	colasCondicion[tSensible].release();
    }
    
    private boolean ejecutarDisparo(int transition) {
    	boolean disparo = rdp.disparar(transition);
		return disparo;    	
    }
    
    private int hayColaCondicion() {
        int n = colasConHilos.length; 
        int inicio = (ultimoOrden + 1) % n; 
        // Primera pasada: desde la posicion de la ultima cola despertada hasta el final 
        for (int i = inicio; i < n; i++) {
            if (colasConHilos[i] && rdp.isSensible(i)) {
                ultimoOrden = i;
                return i;
            }
        }
        // Segunda pasada: desde 0 hasta ultimoOrden
        for (int i = 0; i < inicio; i++) {
            if (colasConHilos[i] && rdp.isSensible(i)) {
                ultimoOrden = i;
                return i;
            }
        }
        return -1;
    }
        
    private void derivarACola(int t) {
    	System.out.println("Estoy en derivarACola");
    	try {
    		if(!colasConHilos[t]) colasConHilos[t] = true;
    		System.out.println("Ya setee el valor de colasConHilos en: " + colasConHilos[t]);
    		System.out.println("Liberando el mutex");
    		liberarMutex();
    		System.out.printf("Estado de semaforo de cola de condicion "+ t + ": " +colasCondicion[t].getQueueLength());
    		colasCondicion[t].acquire();
    	}
    	catch(InterruptedException e){
    		System.out.println("No se pudo derivar a la cola de Condicion");
			e.printStackTrace();
    	}
    	
    }
}

