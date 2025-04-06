/**
 * @file Cliente.java
 * @brief Modela el comportamiento de un cliente en la agencia
 */
package Agencia;

public class Cliente implements Runnable{
	private Monitor monitor;
	private int[] transicionesEntrada= {0,1};	
	private int[] transicionesSalida= {11};	
	int tipo;
	
	public Cliente(Monitor monitor, int tipo) {
		this.monitor = monitor;
		this.tipo = tipo;
	}

	@Override
	public void run() {
		boolean estado1;
		boolean estado2;
		if(tipo==1) {
			while(true) {
				for(int x: transicionesEntrada) {
					do{
						estado1 = monitor.fireTransition(x);
					}while(!estado1);
				}
			}
		}else {
			while(true) {
				for(int x: transicionesSalida) {
					do{
						estado2 = monitor.fireTransition(x);
					}while(!estado2);
				}
			}
		}
	}
}
