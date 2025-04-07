/**
 * @file Agente.java
 * @brief Empleado encargado de aceptar o rechazar la reserva del viaje
 */
package Agencia;

import java.util.concurrent.TimeUnit;

public class Agente implements Runnable {
    private Monitor monitor;
    private int[] transiciones1 = {6,9,10};	
    private int[] transiciones2 = {7,8};
    int tipo;
    boolean first = true;
    boolean fin = false;

    /**
     * @brief Constructor del agente
     * @param monitor Referencia al monitor que maneja la red de Petri
     * @param tipo Tipo de agente: 1 para aprobación, 2 para rechazo
     */
    public Agente(Monitor monitor, int tipo) {
        this.monitor = monitor;
        this.tipo = tipo;
    }

    /**
     * @brief Lógica de ejecución del hilo del agente
     *
     * Dispara las transiciones asociadas según el tipo de agente
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
                //aproveReserva();
                first = false;
            }
        } else {
            while(!fin) {
                if(!first ) System.out.printf("Reintentando disparar transiciones de hilo: %d%n \n", Thread.currentThread().getId());
                for(int x: transiciones2) {
                    monitor.fireTransition(x);
                }
                //rejectReservation();
                first = false;
            }
        }			
    }

    /**
     * @brief Simula una pausa después de aprobar una reserva
     */
    private void aproveReservation() {
        try {
            TimeUnit.MILLISECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @brief Simula una pausa después de rechazar una reserva
     */
    private void rejectReservation() {
        try {
            TimeUnit.MILLISECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
