/**
 * @file Cliente.java
 * @brief Modela el comportamiento de un cliente en la agencia
 */
package Agencia;

import java.util.concurrent.TimeUnit;

public class Cliente implements Runnable{
	private Monitor monitor;
	private int[] transicionesEntrada= {0,1};	
	private int[] transicionesSalida= {11};	
	int tipo;
	boolean terminar;
	public Cliente(Monitor monitor, int tipo) {
		this.monitor = monitor;
		this.tipo = tipo;
		terminar = false;
	}

	@Override
	public void run() {
	    if (tipo == 1) {
	        while (!Thread.currentThread().isInterrupted()) {
	            for (int x : transicionesEntrada) {
	                monitor.fireTransition(x);
	                getIn();
	            }
	        }
	    } else {
	        while (!Thread.currentThread().isInterrupted()) {
	            for (int x : transicionesSalida) {
	                monitor.fireTransition(x);
	                getOut();
	            }
	        }
	    }
	    System.out.printf("[Cliente tipo %d] Hilo %d interrumpido y finalizado.\n", tipo, Thread.currentThread().getId());
	}


	private void getIn() {
		try {
			System.out.printf("[Cliente entrante] Transiciones de entrada completadas. Durmiendo hilo:  %d \n", Thread.currentThread().getId());
			TimeUnit.MILLISECONDS.sleep(10);
			System.out.printf("[Cliente entrante] Despierta. Volviendo a intentar desde hilo: %d \n", Thread.currentThread().getId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); 
        }
	}
	private void getOut() {
		try {
			System.out.printf("[Cliente saliente] Transiciones de entrada completadas. Durmiendo hilo:  %d \n", Thread.currentThread().getId());
			TimeUnit.MILLISECONDS.sleep(10);
			System.out.printf("[Cliente saliente] Despierta. Volviendo a intentar desde hilo: %d \n", Thread.currentThread().getId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); 
        }
	}
	
}
