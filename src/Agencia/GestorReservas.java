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
					while(!monitor.fireTransition(x)&&!Thread.currentThread().isInterrupted()) {}
					reservar();
				}
			}
		}	       
		else {
			while(!Thread.currentThread().isInterrupted()) {
				for(int x: transiciones2) {
					while(!monitor.fireTransition(x)&&!Thread.currentThread().isInterrupted()) {}
					reservar();
				}
			}
		}
	}

	private void reservar() {
		try {
			TimeUnit.MILLISECONDS.sleep(10);
		} catch (InterruptedException e) {
            Thread.currentThread().interrupt(); 
        }
	}
}