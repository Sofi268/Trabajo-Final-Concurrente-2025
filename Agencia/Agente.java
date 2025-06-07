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
	int tipo;
	
	public Agente(Monitor monitor, int tipo) {
		this.monitor = monitor;
		this.tipo = tipo;
	}
	
	@Override
	public void run() {

		if(tipo==1) {
			while(!Thread.currentThread().isInterrupted()) {
				try {
					for(int x: transiciones1) {
						while(!monitor.fireTransition(x)){}
					}
				}
				catch (InterruptedException e) {
					throw 
					Thread.currentThread().interrupt();
					break;
				}
			}
		}
		else {
			while(!Thread.currentThread().isInterrupted()) {
				try {
					for(int x: transiciones2) {
						while(!monitor.fireTransition(x)){}
					}
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt(); 
					break;
				}
			}
		}
	}
}
