/**
 * @file Cliente.java
 * @brief Modela el comportamiento de un cliente en la agencia
 */
package Agencia;

import java.util.concurrent.TimeUnit;

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
		//boolean estado1;
		//boolean estado2;
		if(tipo==1) {
			while(true) {
				for(int x: transicionesEntrada) {
					//do{
						monitor.fireTransition(x);
						
						getIn();
					//}while(!estado1);
				}
			}
		}else {
			while(true) {
				for(int x: transicionesSalida) {
					//do{
						monitor.fireTransition(x);
						getOut();
					//}while(!estado2);
				}
			}
		}
	}
	private void getIn() {
		try {
			System.out.printf("[Cliente entrante] Transiciones de entrada completadas. Durmiendo hilo:  %d \n", Thread.currentThread().getId());
			TimeUnit.MILLISECONDS.sleep(1);
			System.out.printf("[Cliente entrante] Despierta. Volviendo a intentar desde hilo: %d \n", Thread.currentThread().getId());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
	}
	private void getOut() {
		try {
			System.out.printf("[Cliente saliente] Transiciones de entrada completadas. Durmiendo hilo:  %d \n", Thread.currentThread().getId());
			TimeUnit.MILLISECONDS.sleep(2);
			System.out.printf("[Cliente saliente] Despierta. Volviendo a intentar desde hilo: %d \n", Thread.currentThread().getId());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
	}
}
