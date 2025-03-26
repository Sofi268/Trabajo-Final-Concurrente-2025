/**
 *@file monitor.java
 *@brief encargada de centralizar y controlar la concurrencia del sistema
* */
package Agencia;

import java.util.concurrent.Semaphore;

public class Monitor implements MonitorInterface{
	
	private static final RedDePetri rdp = RedDePetri.getInstance();  // Traemos la red de petri a nuestro monitor.
	private static Monitor uniqueInstance;
	private static final Politicas politicas = Politicas.getInstance("Balanceada"); // Seteamos la politica elegida
	private static Semaphore mutex;
	private static Semaphore[] colasCondicion; // Variables de condicion
	private static boolean[] colasConHilos; // Colas de condicion con al menos un hilo esperando

	public Monitor() {}
	
	/**
	 * Aplicando el patron Singleton nos aseguramos que solo exista 1 monitor en nuestro programa.
	 * @return Monitor
	 */
	public static Monitor getInstance(){
		if(uniqueInstance == null){
			uniqueInstance = new Monitor();
			uniqueInstance.iniciarMonitor();
		}else{
			System.out.println("Ya existe una instancia de Monitor");
		}
		return uniqueInstance;
	}
	
	/* Inicializa los valores y condiciones inicales del monitor.
	* */
	public void iniciarMonitor(){
		mutex = new Semaphore(1);
		colasCondicion = new Semaphore[rdp.getTransiciones()];
		colasConHilos = new boolean[rdp.getTransiciones()];
		for (int i = 0; i < rdp.getTransiciones(); i++) {
            colasCondicion[i] =  new Semaphore(0);
        }
	}

	@Override
	public boolean fireTransition(int transition) {
		if (rdp.isSensible(transition)) {
			rdp.disparar(transition);
			return true;
		}
		return false;
	}
	
	//------------------------------------------------------------------------------------------------------------------

}