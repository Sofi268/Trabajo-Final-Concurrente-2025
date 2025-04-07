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
	boolean first = true;
	boolean fin = false;
	
    /**
     * @brief Constructor del cliente
     * @param monitor Monitor que maneja la red de Petri
     * @param tipo Tipo de cliente: 1 (entrante), otro valor (saliente)
     */
	public Cliente(Monitor monitor, int tipo) {
		this.monitor = monitor;
		this.tipo = tipo;
	}

    /**
     * @brief Lógica de ejecución del hilo del cliente
     *
     * Si el cliente es de tipo 1 (entrante), dispara las transiciones de entrada (T0 y T1)
     * Si no, dispara las transiciones de salida (T11). Reintenta hasta que le avisen que termino el programa
     */
	@Override
	public void run() {
		if(tipo==1) {
			while(!fin) {
				if(!first ) System.out.printf("Reintentando disparar transiciones de hilo: %d%n \n", Thread.currentThread().getId());
				for(int x: transicionesEntrada) {
					if(!first)System.out.printf("Disparando transicion %d desde hilo numero: %d \n", x, Thread.currentThread().getId());
					monitor.fireTransition(x);
				}
				
				//getIn();
				first = false;
			}
		}
		else {
			while(!fin) {
				if(!first ) System.out.printf("Reintentando disparar transiciones de hilo: %d%n \n", Thread.currentThread().getId());
				for(int x: transicionesSalida) {
					if(!first)System.out.printf("Disparando transicion %d desde hilo numero: %d \n", x, Thread.currentThread().getId());
					monitor.fireTransition(x);
				}
				//getOut();	
				first = false;
			}
		}
	}
	
	/**
     * @brief Simula una espera para un cliente entrante luego de disparar sus transiciones
     */
	private void getIn() {
		try {
			System.out.printf("[Cliente entrante] Transiciones de entrada completadas. Durmiendo hilo:  %d \n", Thread.currentThread().getId());
			TimeUnit.MILLISECONDS.sleep(2);
			System.out.printf("[Cliente entrante] Despierta. Volviendo a intentar desde hilo: %d \n", Thread.currentThread().getId());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
	}
	
	/**
     * @brief Simula una espera para un cliente saliente luego de disparar sus transiciones
     */
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
