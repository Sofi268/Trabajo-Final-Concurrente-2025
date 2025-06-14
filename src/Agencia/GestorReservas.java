package Agencia;

import java.util.concurrent.TimeUnit;

public class GestorReservas implements Runnable {

	private Monitor monitor;
	private int[] transiciones1= {2,5};
	private int[] transiciones2= {3,4};
	
	int tipo;
	
	public GestorReservas(Monitor monitor, int tipo) {
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
	                	System.out.printf("[Gestor 1] Intento numero %d de disparo transicion: %d \n",i,x);
						i++;
	                }
					if(i==0) System.out.printf("[Gestor 1] Se disparo la transicion: %d en el primer intento",x);	
					else System.out.printf("[Gestor 1] Se disparo transicion: %d en el intento %d\n", x,i);
					reservar();
				}
			}
		}	       
		else {
			while(!Thread.currentThread().isInterrupted()) {
				for(int x: transiciones2) {
					int i = 1;
	            	while(!monitor.fireTransition(x)&&!Thread.currentThread().isInterrupted()) {
	                	System.out.printf("[Gestor 2] Intento numero %d de disparo transicion: %d \n",i,x);
						i++;
	                }
					if(i==0) System.out.printf("[Gestor 2] Se disparo la transicion: %d en el primer intento",x);	
					else System.out.printf("[Gestor 2] Se disparo transicion: %d en el intento %d\n", x,i);
					reservar();
				}
			}
		}

		System.out.printf("[GestorReserva %d] Hilo %d interrumpido y finalizado.\n",tipo, Thread.currentThread().getId());
	}

	private void reservar() {
		try {
			System.out.printf("[GestorReserva %d] Transiciones de gestion de reserva completadas. Durmiendo hilo:  %d \n", tipo,Thread.currentThread().getId());
			TimeUnit.MILLISECONDS.sleep(10);
			System.out.printf("[GestorReserva %d] Despierta. Volviendo a intentar desde hilo: %d \n",tipo, Thread.currentThread().getId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); 
        }
	}
}