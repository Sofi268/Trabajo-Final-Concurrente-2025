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
					while(!monitor.fireTransition(x)&&!Thread.currentThread().isInterrupted()) {}
					getIn();
	            }
	        }
	    } else {
	        while (!Thread.currentThread().isInterrupted()) {
	            for (int x : transicionesSalida) {
					while(!monitor.fireTransition(x)&&!Thread.currentThread().isInterrupted()) {}
	                getOut();
	            }
	        }
	    }
	}

	private void getIn() {
		try {
			TimeUnit.MILLISECONDS.sleep(10);
		} catch (InterruptedException e) {
            Thread.currentThread().interrupt(); 
        }
	}
	private void getOut() {
		try {
			TimeUnit.MILLISECONDS.sleep(10);
		} catch (InterruptedException e) {
            Thread.currentThread().interrupt(); 
        }
	}
	
}
