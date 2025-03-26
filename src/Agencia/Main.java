/**
 *@file main.java
 *@brief entry point del sistema
* */
package Agencia;
//import java.util.*;

public class Main {

	public static final Monitor monitor = Monitor.getInstance();

	public static void main(String[] args){
		//CrearPuestos();  // crea los puestos de treabajo.
		//IniciarPuestos();  // inicia el trabajo de los empleados en los mpuestos de trabajo.
		//IniciarEstadisticas();  //iniciar el resgistro de las estadisticas en un log.

		//prueba de la red de petri:
		for(int i = 0; i<10; i++) {
			if(monitor.fireTransition(i)) System.out.println("La transicion " + i + " se pudo disparar");
			else System.out.println("No fue posible disparar la transicion " + i);
		}
	}
	// -----------------------------------------------------------------------------------------------------------------
	// INICIO METODOS DE UTILIDAD:
	
	/**
	 * @brief Creacion de puestos de trabajo.
	 */
	private static void CrearPuestos(){
		//TODO
	}
	
	/**
	 * @brief Inicia el trabajo de los empleados en sus puestos de trabajo.
	 */
	private static void IniciarPuestos(){
		//TODO
	}
	
	/**
	 * @brief Inicia la escritura en el log de las estadisticas del programa.
	 */
	private static void IniciarEstadisticas(){
		//TODO
	}
}
