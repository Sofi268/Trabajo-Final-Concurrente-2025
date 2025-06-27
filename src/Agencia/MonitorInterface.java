/**
 * @file MonitorInterface.java
 * @brief Interfaz para monitores que controlan el disparo de transiciones en sistemas concurrentes
 */
package Agencia;

public interface MonitorInterface {

	/**
     * @brief Intenta disparar la transición indicada.
     *
     * @param transition número de la transición a disparar
     * @return true si el disparo fue exitoso, false en caso contrario
     */
	public boolean fireTransition(int transition);
}
