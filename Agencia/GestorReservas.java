package Agencia;

public class GestorReservas implements Runnable {

	private Monitor monitor;
	private int[] transiciones1= {2,5};
	private int[] transiciones2= {3,4};
	
	private int tipo;
	
	public GestorReservas(Monitor monitor, int tipo) {
		this.monitor = monitor;
		this.tipo = tipo;
	}
	
public void run() {

		if(tipo==1) {
			while(!Thread.currentThread().isInterrupted()) {
				for(int x: transiciones1) {
					while(!monitor.fireTransition(x)){}
				}
			}
		}
		else {
			while(!Thread.currentThread().isInterrupted()) {
				for(int x: transiciones2) {
					while(!monitor.fireTransition(x)){}
				}
			}
		}
	}
}
