/**
 * @file Agente.java
 * @brief Empleado encargado de aceptar o rechazar la reserva del viaje
 */
package Agencia;

import java.util.concurrent.TimeUnit;

public class GestorColaCondicional implements Runnable {
	private Monitor monitor = Monitor.getInstance();
	
	public GestorColaCondicional() {
	}
	
	@Override
	public void run() {
		while(true) {
			monitor.fireTransition(20);  //transicion de control.
			try {
                System.out.printf("[GestorColasCondicion] Intentando esperar sensibilizado temporal. Durmiendo hilo:  %d \n", Thread.currentThread().getId());
                TimeUnit.MILLISECONDS.sleep(2);
                System.out.printf("[GestorColasCondicion] Despierta. Ahora a probar despertar hilos desde el hilo: %d \n", Thread.currentThread().getId());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
		}
	}
}