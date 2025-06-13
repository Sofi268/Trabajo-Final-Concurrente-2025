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
				for(int x: transiciones1) {
					int i = 1;
	            	while(!monitor.fireTransition(x)&&!Thread.currentThread().isInterrupted()) {
	                	System.out.printf("[Agente Confirmacion] Intento numero %d de disparo transicion: %d \n",i,x);
						i++;
	                }
					if(i==0) System.out.printf("[Agente Confirmacion] Se disparo la transicion: %d en el primer intento",x);	
					else System.out.printf("[Agente Confirmacion] Se disparo transicion: %d en el intento %d\n", x,i);
					confirmarReserva();
				}
			}
		}	       
		else {
			while(!Thread.currentThread().isInterrupted()) {
				for(int x: transiciones2) {
					int j = 1;
	            	while(!monitor.fireTransition(x)&&!Thread.currentThread().isInterrupted()) {
	                	System.out.printf("[Agente Rechaza] Intento numero %d de disparo transicion: %d \n",j,x);
						j++;
	                }
					if(j==0) System.out.printf("[Agente Rechaza] Se disparo la transicion: %d en el primer intento",x);	
					else System.out.printf("[Agente Rechaza] Se disparo transicion: %d en el intento %d\n", x,j);
					rechazarReserva();
				}
			}
		}

		System.out.printf("[Agente tipo %d] Hilo %d interrumpido y finalizado.\n",tipo, Thread.currentThread().getId());
	}

	private void confirmarReserva() {
		try {
			System.out.printf("[Agente Confirmacion] Transicion de confirmacion de reserva completada. Durmiendo hilo:  %d \n", tipo,Thread.currentThread().getId());
			TimeUnit.MILLISECONDS.sleep(10);
			System.out.printf("[Agente Confirmacion] Despierta. Volviendo a intentar desde hilo: %d \n",tipo, Thread.currentThread().getId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); 
        }
	}

	private void rechazarReserva() {
		try {
			System.out.printf("[Agente Rechaza] Transicion de rechazo de reserva completada. Durmiendo hilo:  %d \n", tipo,Thread.currentThread().getId());
			TimeUnit.MILLISECONDS.sleep(10);
			System.out.printf("[Agente Rechaza] Despierta. Volviendo a intentar desde hilo: %d \n",tipo, Thread.currentThread().getId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); 
        }
	}
}