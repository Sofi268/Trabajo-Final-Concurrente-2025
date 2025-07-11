/**
 * @file GestorReservas.java
 * @brief Modela el comportamiento de un empleado de la agencia de viajes encargado de gestionar reservas
 */
package Agencia;

import java.util.concurrent.TimeUnit;

public class GestorReservas implements Runnable {

	private Monitor monitor;
	private int[] transiciones1= {2,5};
	private int[] transiciones2= {3,4};
	private int tipo;
	
	public GestorReservas(Monitor monitor, int tipo) {
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
					reservar();
				}
			}
		}else {
			while(!Thread.currentThread().isInterrupted() && !monitor.isFin()) {
				for(int x: transiciones2) {
					if(monitor.isFin() || Thread.currentThread().isInterrupted()) return; 
					while(!monitor.fireTransition(x) && !Thread.currentThread().isInterrupted() && !monitor.isFin()) {}
					reservar();
				}
			}
		}
	}

	/**
	 * @brief Simula la reserva de viaje con una breve espera
	 */
	private void reservar() {
		try {
			TimeUnit.MILLISECONDS.sleep(10);
		} catch (InterruptedException e) {
            Thread.currentThread().interrupt(); 
        }
	}

}
