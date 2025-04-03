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
		if(tipo==1) {
			while(true) {
				for(int x: transicionesEntrada) {
					monitor.fireTransition(x);
				}
			}
		}
		else {
			while(true) {
				for(int x: transicionesSalida) {
					monitor.fireTransition(x);
				}
			}
		}
	}
}
