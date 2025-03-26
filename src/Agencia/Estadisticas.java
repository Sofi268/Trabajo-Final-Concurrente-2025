/**
 * @file Estadisticas.java
 * @brief Crea y actualiza un log con las transiciones disparadas de la red
 */
package Agencia;

import java.util.ArrayList;

public class Estadisticas {
	private ArrayList<Integer> transicionesDisparadas;
	
	public Estadisticas() {
		transicionesDisparadas = new ArrayList<Integer>();
	}
	
	public void agregarDisparo(int t) {
		transicionesDisparadas.add(t);
	}
	
	//Crear log
	public void tomarStats() {
		//TODO
	}
	
	//Primero ver expresiones regulares	
	private void eliminarDisparos(){
		//TODO
	}

}

