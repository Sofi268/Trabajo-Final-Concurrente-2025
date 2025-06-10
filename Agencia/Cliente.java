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
	boolean terminar;
	public Cliente(Monitor monitor, int tipo) {
		this.monitor = monitor;
		this.tipo = tipo;
		terminar = false;
	}

	@Override
	public void run() {
	    if (tipo == 1) {  //Si es un cliente entrante.
	        while (!Thread.currentThread().isInterrupted()) {
	            for (int x : transicionesEntrada) {
					int i = 1;
	                while(!monitor.fireTransition(x)&&!Thread.currentThread().isInterrupted()) {
	                	System.out.printf("[Cliente entrante] Intento numero %d de disparo transicion: %d \n",i,x);
						i++;
	                }
					if(i==0) System.out.printf("Cliente entrante] Se disparo la transicion: %d en el primer intento",x);	
					else System.out.printf("[Cliente entrante] Se disparo transicion: %d en el intento %d\n", x,i);
	                getIn();
	            }
	        }
	    } else {  // Si es un clinte saliente.
	        while (!Thread.currentThread().isInterrupted()) {
	            for (int x : transicionesSalida) {
					int i = 1;
	            	while(!monitor.fireTransition(x)&&!Thread.currentThread().isInterrupted()) {
	                	System.out.printf("[Cliente saliente] Intento numero %d de disparo transicion: %d \n",i,x);
	                }
	                if(i==0) System.out.printf("Cliente saliente] Se disparo la transicion: %d en el primer intento",x);	
					else System.out.printf("[Cliente saliente] Se disparo transicion: %d en el intento %d\n", x,i);
	                getOut();
	            }
	        }
	    }
	    System.out.printf("[Cliente tipo %d] Hilo %d interrumpido y finalizado.\n", tipo, Thread.currentThread().getId());
	}

	/*
	 * Métodos para simular el comportamiento de los clientes al entrar de la agencia.
	 */
	private void getIn() {
		try {
			System.out.printf("[Cliente entrante] Transiciones de entrada completadas. Durmiendo hilo:  %d \n", Thread.currentThread().getId());
			TimeUnit.MILLISECONDS.sleep(10);
			System.out.printf("[Cliente entrante] Despierta. Volviendo a intentar desde hilo: %d \n", Thread.currentThread().getId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); 
        }
	}

	/*	
	 * Métodos para simular el comportamiento de los clientes al salir de la agencia.
	 */
	private void getOut() {
		try {
			System.out.printf("[Cliente saliente] Transiciones de entrada completadas. Durmiendo hilo:  %d \n", Thread.currentThread().getId());
			TimeUnit.MILLISECONDS.sleep(10);
			System.out.printf("[Cliente saliente] Despierta. Volviendo a intentar desde hilo: %d \n", Thread.currentThread().getId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); 
        }
	}
	
}
