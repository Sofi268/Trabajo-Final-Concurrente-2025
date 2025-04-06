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
	
	@Override
	public void run() {
		boolean estado1;
		boolean estado2;
		if(tipo==1) {
			while(true) {
				for(int x: transiciones1) {
					do{
						estado1 = monitor.fireTransition(x);
					}while(!estado1);
				}
			}
		}else {
			while(true) {
				for(int x: transiciones2) {
					do{
						estado2 = monitor.fireTransition(x);
					}while(!estado2);
				}
			}
		}
	}
}
