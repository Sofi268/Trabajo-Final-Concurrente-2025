/**
 * @file RedDePetri.java
 * @brief Red de modelado del sistema
 */
package Agencia;

import java.util.ArrayList;

public class RedDePetri {
    private static RedDePetri uniqueInstance;  
    private static final Integer[] marcadoInicial = Constantes.marcadoI;
    private static Integer[] marcadoActual;
    private static Integer[][] matrizIncidencia;
    private static Integer[] transicionSensible;
    private static Integer[] transicionSensibleAnterior; // Para comparar si cambió el estado de la transición
    private static Integer[] secuenciaDisparo;
    public static final int plazas = Constantes.cantidadPlazas;
    public static final int transiciones = Constantes.cantidadTransiciones;
    public static final Integer[] invarianteP1 = Constantes.invarP1;
    public static final Integer[] invarianteP2 = Constantes.invarP2;
    public static final Integer[] invarianteP3 = Constantes.invarP3;
    public static final Integer[] invarianteP4 = Constantes.invarP4;
    public static final Integer[] invarianteP5 = Constantes.invarP5;
    public static final Integer[] invarianteP6 = Constantes.invarP6;
    public static final int inv1 = Constantes.in1;
    public static final int inv2 = Constantes.in2;
    public static final int inv3 = Constantes.in3;
    public static final int inv4 = Constantes.in4;
    public static final int inv5 = Constantes.in5;
    public static final int inv6 = Constantes.in6;
    private Integer[][] invariantes = {invarianteP1, invarianteP2, invarianteP3, invarianteP4, invarianteP5, invarianteP6};
    private Integer[] invariantesPlazas = {inv1, inv2, inv3, inv4, inv5, inv6};
    private static ArrayList<Integer> transicionesDisparadas;
    private static int disparosT2; // Veces que gestiono la reserva el gestor1
    private static int disparosT3; // Veces que gestiono la reserva el gestor2
    private static int disparosT6; // Reservas confirmadas
    private static int disparosT7; // Reservas canceladas
    private static int cantDisparosT0; 
    private static int cantDisparosT11;
    private boolean sePuedeT0 = true;
    private boolean sePuedeT11 = true;
    private static Long[] timeStamp = new Long[Constantes.cantidadTransiciones]; //Tiempos de sensibilizado de cada transición
    private static long tiempoInicio; //Tiempo de inicio del sistema
    private static boolean primeraCopia = true; // Para saber si es la primera vez que se llama a sensibilizarT()
    private static Politicas politica = Politicas.getInstance("Balanceada");
    
    /**
     * @brief Constructor vacio
     */
    public RedDePetri(){ }
    public static RedDePetri getInstance(){
        if(uniqueInstance == null){
            uniqueInstance = new RedDePetri();
            startRedDePetri();
        }
        return uniqueInstance;
    }

	/**
	 * @brief Inicializa los valores de la red 
	 * Asigna el marcado actual como el inicial y carga la matriz de incidencia.
	 * Actualiza las transiciones sensibilizadas
	* */
    private static void startRedDePetri() {
        marcadoActual = marcadoInicial.clone(); 
        System.out.println("Marcado inicial: " + java.util.Arrays.toString(marcadoActual));
        
        matrizIncidencia = new Integer[][]{
            {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {-1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0},
            {-1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 1, 0, 0, -1, 0, 0, 0, 0, 0, 0},
            {0, 0, -1, 0, 0, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, -1, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, -1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, -1, -1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, -1, -1, 1, 0, 1, 0},
            {0, 0, 0, 0, 0, 0, 1, 0, 0, -1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, -1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -1, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, -1}
        };
        
        disparosT2 = 0;
        disparosT3 = 0;
        disparosT6 = 0;
        disparosT7 = 0;
        cantDisparosT0 = 0;
        cantDisparosT11 = 0;
        transicionSensible = new Integer[transiciones];
        secuenciaDisparo = new Integer[transiciones]; 
        transicionesDisparadas = new ArrayList<>();
        transicionSensibleAnterior = new Integer[transiciones];
        tiempoInicio = System.currentTimeMillis(); // Guarda el tiempo de inicio
        setCtesTiempo(); // Inicializa los tiempos de sensibilizado en 0
        for (int i = 0; i < transiciones; i++) {
            transicionSensible[i] = 0;
            transicionSensibleAnterior[i] = 0;
        }
        sensibilizarT();
        
    }
    
    /**
     * @brief Comprueba que transiciones ahora son sensibles y actualiza su estado en el vector
     */
    private static void sensibilizarT() {
        if (!primeraCopia) {
            transicionSensibleAnterior = transicionSensible.clone(); // Copia el estado actual
        }
        for (int j = 0; j < transiciones; j++) {
            boolean sensibilizada = true;
            // Verifica para cada plaza si tiene suficientes tokens para disparar la transición
            for (int i = 0; i < plazas; i++) {
                if (matrizIncidencia[i][j] < 0 && marcadoActual[i] + matrizIncidencia[i][j] < 0) {
                    sensibilizada = false;
                    break;
                }
            }
            transicionSensible[j] = sensibilizada ? 1 : 0;
        }
        if (!primeraCopia) {
            setTimeStamp(); // Actualiza los timestamps de las transiciones sensibilizadas
        } else {
            primeraCopia = false;
        }
        System.out.println("Transiciones sensibilizadas: " + java.util.Arrays.toString(transicionSensible));
    }

    /**
     * @brief Comprueba si la transicion dada esta sensibilizada
     * @param t numero de transicion para comprobar sensibilizado
     * @return true si esta sensibilizada
     */
    public boolean isSensible(int t) {
        return transicionSensible[t] == 1;
    }
    
    /**
     * @brief Intenta disparar la transicion
     * @param t numero de transicion 
     */
    public int disparar(int t) {
    	if((t == 0) && (!sePuedeT0)) {
    		System.out.println("****** No se puede disparar mas T0. Llego a los 186 disparos *****");
    		System.out.println("****** Voy a intentar ir a senializar otros hilos***** ");
    		return 3;
    	}
    	
        if (!isSensible(t)) {
            System.out.println("La transición " + t + " no está sensibilizada.");
            return 1;
        }        
        setSecuencia(t);
        System.out.println("Disparando transición " + t);
        actualizarMarcado();
        transicionesDisparadas.add(t);
        System.out.println("Marcado actual: " + java.util.Arrays.toString(marcadoActual));
        sensibilizarT(); 
        comprobarInvariantes(t);
        if(t==2){
            disparosT2++;
            politica.actualizarT2();
        }
       if(t==3){
           disparosT3++;
           politica.actualizarT3();
      }
       if(t==6){
           disparosT6++;
           politica.actualizarT6();
      }
       if(t==7){
           disparosT7++;
           politica.actualizarT7();
      }
        if(t==11) {
        	cantDisparosT11++;
        	if(checkT11()) { // Caso en que ya salieron 186 clientes
            	frenarPrograma();
            }
        	
        }
        if(t==0) {
        	cantDisparosT0++;
        	if(checkT0()) { // Caso en que ya salieron 186 clientes
            	frenarDisparoT0();
            }
        }
        
        return 0;
    }
    
    private boolean checkT0() {
    	return (cantDisparosT0 == 186) ? true : false; 
    }
    
    private boolean checkT11() {
    	return (cantDisparosT11 == 186) ? true : false; 
    }
    
    public boolean finPrograma() {
    	return (!sePuedeT11);
    }
    
    private void frenarDisparoT0() {
		sePuedeT0 = false;
	}
    
    private void frenarPrograma() {
		sePuedeT11 = false;
	}
    
	public ArrayList<Integer> getTransicionesDisparadas(){
    	return transicionesDisparadas;
    }
    
    /**
     * @brief Actualiza el marcado actual luego de dispararse una transicion
     * Utiliza la Ecuación fundamental marcadoActual = marcadoInicial + matrizIncidencia * secuenciaDisparo
     */
    private void actualizarMarcado() {
        for (int i = 0; i < plazas; i++) {
            int nuevoMarcado = marcadoActual[i]; 
            for (int j = 0; j < transiciones; j++) {
                nuevoMarcado += matrizIncidencia[i][j] * secuenciaDisparo[j];
            }
            marcadoActual[i] = nuevoMarcado; 
        }
    }
    
    /**
     * @brief Comprueba que se cumplan los invariantes de plaza luego de un disparo
     * @param t numero de disparo realizado
     */
    private void comprobarInvariantes(int t) {    	
    	boolean cumpleTodos = true;
        for (int i = 0; i < invariantes.length; i++) {
            int suma = 0;
            for (int plaza : invariantes[i]) {
                suma += marcadoActual[plaza];
            }
            if (suma != invariantesPlazas[i]) {
                System.out.println("No se cumple el invariante de plaza " + (i + 1) + " al disparar la transición");
                cumpleTodos = false;
            }
        }
        if(cumpleTodos)System.out.println("Se cumplen todos los invariantes luego del disparo de: T" + t);
    }
    
    /**
     * @brief Devuelve la cantidad de transiciones
     * @return numero de transiciones de la red
     */
    public int getTransiciones() {
    	return transiciones;
    }
	    
	/**
	 * @brief Inicializa secuenciaDisparo con ceros y marcar solo la transición disparada
	 * @param t transicion a disparar
	 */
    private void setSecuencia(int t) {
    	for (int i = 0; i < transiciones; i++) {
            secuenciaDisparo[i] = (i == t) ? 1 : 0;
        }
    }

    /**
     * @brief Devuelve el vector de transiciones sensibles actuales
     * @return Arreglo de enteros con las transiciones sensibles
     */
    public Integer[] getTSensibles() {
        return transicionSensible;
    }

    /**
     * @brief Devuelve el marcado actual de la red de Petri
     * @return Arreglo de enteros con el marcado actual
     */
    public Integer[] getMarcadoActual() {
        return marcadoActual;
    }

    /**
     * @brief Devuelve la cantidad de veces que se disparó la transición T2
     * @return Número de disparos de T2
     */
    public int getDisparosT2() {
        return disparosT2;
    }

    /**
     * @brief Devuelve la cantidad de veces que se disparó la transición T3
     * @return Número de disparos de T3
     */
    public int getDisparosT3() {
        return disparosT3;
    }

    /**
     * @brief Devuelve la cantidad de veces que se disparó la transición T6
     * @return Número de disparos de T6
     */
    public int getDisparosT6() {
        return disparosT6;
    }

    /**
     * @brief Devuelve la cantidad de veces que se disparó la transición T7
     * @return Número de disparos de T7
     */
    public int getDisparosT7() {
        return disparosT7;
    }
    
    //------------------------------------------------------------------------------------
    // Disparos temporales:
    private static Long getTiempoActual() {
        return System.currentTimeMillis() - tiempoInicio; // Tiempo relativo desde el inicio
    }
    /**
     * Al momento de crearse la red, inicia todos los tiempos de sensibilizado en 0
     */
    public static void setCtesTiempo(){
        for (int i = 0; i < Constantes.cantidadTransiciones; i++) {
            timeStamp[i] = getTiempoActual();//System.currentTimeMillis();
        }
    }
    /**Obtiene las transiciones que han sido hablitadas y calcula el tiempo desde que desde que esta se sensiblizó y en base a eso, comprueba si se puede disparar
     * chequeando los intervalos de tiempo configurados con alfa y beta:
     * tMin =< tActual => tMax : La transición se puede disparar
     * tActual < tMin : El hilo se va a dormir hasta que entre en el intervalo de tiempo de sensiblizado, soltando el mutex. Luego que transcurre este tiempo,
     * intenta tomar el mutex para comprobar que aún se encuentre en el intervalo mencionado. sino, ocurre el siguiente caso:
     * tActual > tMin : La transición no se dispara y el hilo sale del monitor sin dispararla
     * @param T: Transición a disparar
     * @return 0 si está dentro del intervalo temporal, null si se pasó o un valor que indica el tiempo que se tiene que dormir
     */
    public Long tiempoSensibilizado(Integer t){
        Long tActual = getTiempoActual();
            System.out.println("---Tiempo actual: " + tActual +" ms");
        Long tMin = timeStamp[t] + Constantes.ALFA;
            System.out.println("---Tiempo mínimo: " + tMin +" ms");
        Long tMax = timeStamp[t] + Constantes.BETA;
            System.out.println("---Tiempo máximo: " + tMax +" ms");
        if ((tActual >= tMin) && (tActual <= tMax)){  // Se encuentra dentro del intervalo temporal
            return 0L;
        }else if (tActual < tMin) {          // Se encuentra antes del intervalo temporal
            return tMin - tActual;
        }else{                             // Se encuentra después del intervalo temporal
            return null; // Se pasó de la ventana
        }
    }
    /**
     * @return tiempos de sensibilizados de cada transición
     */
    public Long[] getTimeStamp(){
        return timeStamp;
    }
    /**
     * Actualiza los timeStaps de las transiciones que pasaron de no sensibilizadas a sensibilizadas
     */
    public static void setTimeStamp() {
        for (int i = 0; i < Constantes.cantidadTransiciones; i++) {
            if(transicionSensibleAnterior[i] == 0 && transicionSensible[i] == 1){ // Si la transición pasó de no sensibilizada a sensibilizada
                timeStamp[i] = getTiempoActual();
            }
        }
    }
    
    public int getCantT0() {
    	return cantDisparosT0;
    }
    
    public int getCantT11() {
    	return cantDisparosT11;
    }
    
    public boolean finT0() {
    	return !sePuedeT0;
    }
    
    public boolean finT11() {
    	return !sePuedeT11;
    }

}