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
					monitor.fireTransition(x);
					try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); 
                  }
				}
			}
		}else {
			while(!Thread.currentThread().isInterrupted()) {
				for(int x: transiciones2) {
					monitor.fireTransition(x);
					try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); 
                 }
				}
			}
		}
		System.out.printf("[GestorReserva tipo %d] Hilo %d interrumpido y finalizado.\n",tipo, Thread.currentThread().getId());
	}
}
