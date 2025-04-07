/**
 * @file Agente.java
 * @brief Empleado encargado de aceptar o rechazar la reserva del viaje
 */
package Agencia;

import java.util.concurrent.TimeUnit;

public class GestorColaCondicional implements Runnable {
	private Monitor monitor = Monitor.getInstance();
	
	public GestorColaCondicional() {
	}
	
	@Override
	public void run() {
		while(true) {
			monitor.fireTransition(20);  //transicion de control.
		}
	}
}
