/**
 * @file Estadisticas.java
 * @brief Crea y actualiza un log con las transiciones disparadas de la red
 */
package Agencia;

import java.util.ArrayList;

public class Estadisticas {
	private ArrayList<Integer> transicionesDisparadas;
	
	/**
	 * @brief Inicia el arreglo para almacenar las transiciones disparadas 
	 */
	public Estadisticas() {
		transicionesDisparadas = new ArrayList<Integer>();
		iniciarLog();
	}
	
	/**
	 * @brief Agrega la T a la lista de transiciones disparadas 
	 */
	public void agregarDisparo(int t) {
		transicionesDisparadas.add(t);
	}
	
	/**
	 * @brief Inicia el log para las estadisticas
	 */
	private void iniciarLog(){
		//TODO
	}
	
	/**
	 * @brief Levanta las transiciones disparadas cada determinado tiempo
	 */
	public void tomarStats() {
		//TODO
	}
	
	//Ver expresiones regulares	
	
}
