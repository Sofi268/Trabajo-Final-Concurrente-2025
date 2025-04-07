/**
 * @file GestorReservas.java
 * @brief Modela el comportamiento de un gestor de reservas en la agencia
 */
package Agencia;

import java.util.concurrent.TimeUnit;

public class GestorReservas implements Runnable {

    private Monitor monitor;
    private int[] transiciones1 = {2,5};
    private int[] transiciones2 = {3,4};
    private int tipo;
    private boolean fin = false;
    private boolean first = true;

    /**
     * @brief Constructor del gestor de reservas
     * @param monitor Monitor que maneja la red de Petri
     * @param tipo Tipo de gestor: 1 para camino 1 encargado de T2 y 5, otro valor para camino 2 encargado de T3 y 4
     */
    public GestorReservas(Monitor monitor, int tipo) {
        this.monitor = monitor;
        this.tipo = tipo;
    }

    /**
     * @brief Lógica de ejecución del hilo del gestor
     *
     * Dispara las transiciones asociadas según el tipo de gestor
     * Reintenta de forma indefinida hasta que se indique finalizar
     */
    @Override
	public void run() {
		if(tipo==1) {
			while(!fin) {
				if(!first ) System.out.printf("Reintentando disparar transiciones de hilo: %d%n \n", Thread.currentThread().getId());
				for(int x: transiciones1) {
					monitor.fireTransition(x);
				}
				
				//reserve();
				first = false;
			}
		}
		
		else {
			while(!fin) {
				if(!first ) System.out.printf("Reintentando disparar transiciones de hilo: %d%n \n", Thread.currentThread().getId());
				for(int x: transiciones2) {
					monitor.fireTransition(x);
				}
				
				//reserve();
				first = false;
			}
		}
	}

    /**
     * @brief Simula el tiempo de la realizacion de la reserva después de dispararla
     */
    private void reserve() {
        try {
            TimeUnit.MILLISECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @brief Finaliza la ejecución del hilo del gestor
     */
    public void setFin() {
        fin = true;
    }
}
