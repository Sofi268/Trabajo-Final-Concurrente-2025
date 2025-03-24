/*Monitor: en la clase encargada de centralizar ycontrolar la concurrencia del sistema
* */
package Agencia;

import java.util.concurrent.Semaphore;

public class Monitor implements MonitorInterface{
	private static Monitor uniqueInstance;
	private static RedDePetri redPetri;
	private static Semaphore mutex;
	private static Semaphore[] colasCondicion;
	private static boolean[] colasConHilos;
	private boolean k;
	boolean m = false;
	private static Politica politica;
	
	public Monitor() {}
	
	public Monitor(RedDePetri red) {		
		 redPetri = red;
		 
		 mutex = new Semaphore(1);
		 colasCondicion = new Semaphore[redPetri.getTransiciones()];
		 
		 for (int i = 0; i < redPetri.getTransiciones(); i++) {
             colasCondicion[i] =  new Semaphore(0);
         }
		 
		 colasConHilos = new boolean[red.getTransiciones()];
		 k = false; 
		 
		setPolitica("Balanceada");
	}
	
	/**
	 * Aplicando el patron Singleton nos aseguramos que solo exista 1 monitor en nuestro programa.
	 * @return Monitor
	 */
	public static Monitor getInstance(RedDePetri red){
		if(uniqueInstance == null){
			uniqueInstance = new Monitor();
		}else{
			System.out.println("Ya existe una instancia de Monitor");
		}
		return uniqueInstance;
	}
	
	@Override
	public boolean fireTransition(int transition) {
		int disparos = 0;
		entrarMonitor();
		k = true;
		
		while(k){
			k = dentroDelMonitor(transition);
			if(k) disparos++;
		}
		
		quienSigue();
		salirMonitor();
		return disparos > 0 ? true : false;
	}
	
	//El hilo intenta agarrar el mutex, si esta ocupado se duerme en la cola de entrada del monitor
	private void entrarMonitor() {
		try {
            mutex.acquire();
        }
        catch (InterruptedException e) {
            System.exit(1);
        }
	}
	
	private void salirMonitor() {
		mutex.release();
	}
	
	private boolean dentroDelMonitor(int transition) {
		if (redPetri.isSensible(transition)) {
			System.out.println("La transicion se puede disparar");
			redPetri.disparar(transition);
			m = quienSigue();
			if(!m) k = false;
			
			System.out.println("La transicion fue disparada");
			return true;
		}
		System.out.println("No es posible disparar la transicion " + transition);
		salirMonitor();
		try {
			colasConHilos[transition] = true;
			colasCondicion[transition].acquire();
		} catch (InterruptedException e) {
            System.exit(1);
        }
		return false;		
	}
	
	//Determina el siguiente hilo en entrar al monitor
	private boolean quienSigue() {
		
	    for (int i = 0; i < colasConHilos.length; i++) {
	        if (colasConHilos[i] && redPetri.isSensible(i)) {
	            colasCondicion[i].release(); // Despierta un hilo si su transición está sensibilizada
	            m = true;
	        }
	    }
	    return m;
	}

//
	private void setPolitica(String politicaElegida) {
		if(politicaElegida.equals("Prioridad")){
			 politica = new PoliticaPrioridad();
		}
		else politica = new PoliticaBalanceada();
	}
}

/* 
 * Primero hago el acquire del mutex
 * Despues k = true para ingresar al loop
 * De ahi intento disparar la transicion, si se puede disparo y k devuelve true
 * Me fijo despues cuales transiciones estan sensibilizadas porque cambie el estado de la red
 * Me fijo quienes estan, que hilos estaban esperando a que las transiciones se sensibilicen
 * Me puede dar que haya una o mas de una T o que no haya ninguna (m en diagrama de secuencias), si es 0 pongo k = false y salgo
 * Ahi entra en juego la politica para los conflictos
 * Hago un release de la cola, de la var de condicion que elija
 * Salgo del monitor y el proximo hilo se activa dentro del monitor
 * Si no se puede disparar en un principio, suelto el mutex y hago acquire de la cola de condicion
 * 
 */
