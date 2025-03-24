/*Main : encargado de la inicializacion del programa.
*
* */
package Agencia;
import java.util.*;

public class Main {

	public static final Monitor monitor = Monitor.getInstance();

	public static void main(String[] args){
		//CrearPuestos();  // crea los puestos de treabajo.
		//IniciarPuestos();  // inicia el trabajo de los empleados en los mpuestos de trabajo.
		//IniciarEstadisticas();  //iniciar el resgistro de las estadisticas en un log.

		//prueba de la red de petri:
		monitor.fireTransition(0);
		monitor.fireTransition(1);
		monitor.fireTransition(3);
		monitor.fireTransition(1);
		monitor.fireTransition(4);
	}
	// -----------------------------------------------------------------------------------------------------------------
	// INICIO METODOS DE UTILIDAD:
	/* *
	 * Creacion de puestos de trabajo.
	 * */
	private static void CrearPuestos(){
		//codigo...
	}
	/* *
	 * inicia el trabajo de los empleados en sus puestos de trabajo.
	 * */
	private static void IniciarPuestos(){

	}
	/* *
	*  Inicia la escritura en el log de las estadisticas del programa.
	* */
	private static void IniciarEstadisticas(){
		//codigo...
	}
}

