/**
 * @file Agente.java
 * @brief Empleado encargado de aceptar o rechazar la reserva del viaje
 */
package Agencia;

import java.util.concurrent.TimeUnit;

public class Agente implements Runnable {
	private Monitor monitor;
	private int[] transiciones1 = {6,9,10};	
	private int[] transiciones2 = {7,8};
	private int tipo;
	
	public Agente(Monitor monitor, int tipo) {
		this.monitor = monitor;
		this.tipo = tipo;
	}
	
	@Override
	public void run() {
		if(tipo==1) {
			while(!Thread.currentThread().isInterrupted() && !monitor.isFin()) {
				for(int x: transiciones1) {
					if(monitor.isFin() || Thread.currentThread().isInterrupted()) return; 
					while(!monitor.fireTransition(x) && !Thread.currentThread().isInterrupted() && !monitor.isFin()) {}
					confirmarReserva();
				}
			}
		}	       
		else {
			while(!Thread.currentThread().isInterrupted() && !monitor.isFin()) {
				for(int x: transiciones2) {
					if(monitor.isFin() || Thread.currentThread().isInterrupted()) return; 
					while(!monitor.fireTransition(x) && !Thread.currentThread().isInterrupted() && !monitor.isFin()) {}
					rechazarReserva();
				}
			}
		}
	}

	/**
	 * @brief Simula la confirmación de una reserva con una breve espera
	 */
	private void confirmarReserva() {
		try {
			TimeUnit.MILLISECONDS.sleep(10);
		} catch (InterruptedException e) {
            Thread.currentThread().interrupt(); 
        }
	}

	/**
	 * @brief Simula el rechazo de una reserva con una breve espera
	 */
	private void rechazarReserva() {
		try {
			TimeUnit.MILLISECONDS.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); 
        }
	}
}
