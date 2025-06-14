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
			while(true) {
				for(int x: transiciones1) {
					monitor.fireTransition(x);
				}
			}
		}else {
			while(true) {
				for(int x: transiciones2) {
					monitor.fireTransition(x);
				}
			}
		}
	}
}
