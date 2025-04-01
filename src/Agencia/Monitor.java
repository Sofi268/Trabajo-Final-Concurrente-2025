/**
 *@file monitor.java
 *@brief encargada de centralizar y controlar la concurrencia del sistema
* */
package Agencia;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Monitor implements MonitorInterface{
	
	private static RedDePetri rdp;
	private static final Politicas politicas = Politicas.getInstance("Balanceada"); // Seteamos la politica elegida
	private static Semaphore mutex; // Cola de entrada del monitor
	private static Semaphore prioridad; // Cola de prioridad de entrada de hilos senialados
	private static Semaphore[] colasCondicion; // Variables de condicion
	private static boolean[] colasConHilos; // Colas de condicion con al menos un hilo esperando
	
	public Monitor(RedDePetri redPetri) {
		rdp = redPetri;
		mutex = new Semaphore(1,true);
		prioridad = new Semaphore (1, true);
		colasCondicion = new Semaphore[rdp.getTransiciones()];
		colasConHilos = new boolean[rdp.getTransiciones()];
		
		for (int i = 0; i < rdp.getTransiciones(); i++) {
            colasCondicion[i] =  new Semaphore(0);
        }
	}

	//------------------------------------------------------------------------------------------------------------------
	
	@Override
	public boolean fireTransition(int transition) {
		try {
			// Primero intentamos adquirir un permiso, verificando si hay hilos esperando en la cola de prioridad.
			adquirirPermiso();
			
			// Verificamos si la transición es sensible, si lo es, la ejecutamos.
			if (rdp.isSensible(transition)) {
				ejecutarTransicion(transition); // Ejecuta la transición en la Red de Petri.
				liberarMutex(); // Liberamos el mutex para permitir que otros hilos entren.
				return true; // La transición se ejecutó correctamente.
			} else {
				// Si la transición no es sensible, esperamos en la cola de condición asociada.
				esperarTransicion(transition);
				return fireTransition(transition); // Reintentamos la operación.
			}
		} catch (InterruptedException e) {
			// En caso de interrupción, restauramos el estado de interrupción del hilo y devolvemos falso.
			Thread.currentThread().interrupt();
			return false;
		}
	}

	/**
	 * Método para adquirir el permiso adecuado dependiendo de si hay hilos en la cola de prioridad o no.
	 * Si hay hilos en la cola de prioridad, entramos con prioridad, de lo contrario, accedemos con el mutex.
	 * @throws InterruptedException
	 */
	private void adquirirPermiso() throws InterruptedException {
		// Si hay hilos en la cola de prioridad, intentamos acceder con prioridad.
		if (prioridad.availablePermits() > 0) {
			prioridad.acquire(); // Adquiere permiso en la cola de prioridad.
		} else {
			mutex.acquire(); // Si no hay hilos en prioridad, adquirimos el mutex para acceso exclusivo.
		}
	}

	/**
	 * Método que ejecuta la transición solicitada en la Red de Petri.
	 * @param transition La transición que se intenta ejecutar.
	 */
	private void ejecutarTransicion(int transition) {
		rdp.disparar(transition); // Ejecuta la transición en la Red de Petri.
	}

	/**
	 * Método para liberar el mutex después de que un hilo haya terminado de realizar su operación.
	 */
	private void liberarMutex() {
		mutex.release(); // Libera el mutex para permitir que otros hilos accedan al monitor.
	}

	/**
	 * Método para manejar los hilos que no pueden ejecutar la transición porque no es sensible.
	 * El hilo se duerme en la cola de condición asociada a la transición y se marca como prioritario al despertar.
	 * @param transition La transición en la que el hilo está esperando.
	 * @throws InterruptedException
	 */
	private void esperarTransicion(int transition) throws InterruptedException {
		colasConHilos[transition] = true; // Marcamos que hay al menos un hilo esperando en esta transición.
		liberarMutex(); // Liberamos el mutex antes de dormir el hilo.
		colasCondicion[transition].acquire(); // El hilo espera en su cola de condición asociada.
		prioridad.release(); // Al despertar, liberamos un permiso en la cola de prioridad para garantizar que este hilo tiene prioridad al reingresar.
	}
	
	
}