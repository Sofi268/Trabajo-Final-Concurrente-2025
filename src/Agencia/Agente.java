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
		//boolean estado1;
		//boolean estado2;
		if(tipo==1) {
			while(true) {
				for(int x: transiciones1) {
					//do{
						monitor.fireTransition(x);
						
					//}while(!estado1);
				}
			}
		}else {
			while(true) {
				for(int x: transiciones2) {
					//do{
						monitor.fireTransition(x);
						
					//}while(!estado2);
				}
			}
		}
	}
}
