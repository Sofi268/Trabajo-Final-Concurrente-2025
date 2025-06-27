/**
 * @file Cliente.java
 * @brief Modela el comportamiento de un cliente en la agencia tanto al ingresar como al salir
 */
package Agencia;

import java.util.concurrent.TimeUnit;
public class Cliente implements Runnable{
	private Monitor monitor;
	private int[] transicionesEntrada= {0,1};	
	private int[] transicionesSalida= {11};	
	int tipo;

	public Cliente(Monitor monitor, int tipo) {
		this.monitor = monitor;
		this.tipo = tipo;
	}

	@Override
	public void run() {
	    if (tipo == 1) {
	        while(!Thread.currentThread().isInterrupted() && !monitor.isFin()) {
				for(int x: transicionesEntrada) {
					if(monitor.isFin() || Thread.currentThread().isInterrupted()) return; 
					while(!monitor.fireTransition(x) && !Thread.currentThread().isInterrupted() && !monitor.isFin()) {}
					entrar();
				}
			}
	    } else {
	        while(!Thread.currentThread().isInterrupted() && !monitor.isFin()) {
				for(int x: transicionesSalida) {
					if(monitor.isFin() || Thread.currentThread().isInterrupted()) return; 
					while(!monitor.fireTransition(x) && !Thread.currentThread().isInterrupted() && !monitor.isFin()) {}
					salir();
				}
			}
	    }
	}

	/**
	 * @brief Simula el ingreso del cliente con una breve espera
	 */
	private void entrar() {
		try {
			TimeUnit.MILLISECONDS.sleep(10);
		} catch (InterruptedException e) {
            Thread.currentThread().interrupt(); 
        }
	}
	
	/**
	 * @brief Simula la salida del cliente con una breve espera
	 */
	private void salir() {
		try {
			TimeUnit.MILLISECONDS.sleep(10);
		} catch (InterruptedException e) {
            Thread.currentThread().interrupt(); 
        }
	}
	
}