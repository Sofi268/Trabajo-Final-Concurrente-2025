/**
 * @file RedDePetri.java
 * @brief red de modelado del sistema
 */
package Agencia;

public class RedDePetri {
    private static RedDePetri uniqueInstance;  //Para implementacion de patron de diseño singleton.
    private static final Integer[] marcadoInicial = {5, 1, 0, 0, 5, 0, 1, 1, 0, 0, 1, 0, 0, 0, 0};
    private static Integer[] marcadoActual;
    private static Integer[][] matrizIncidencia;
    private static Integer[] transicionSensible;
    private static Integer[] secuenciaDisparo;
    public static final int plazas = 15;
    public static final int transiciones = 12;
    public static final Integer[] invarianteP1 = {1,2}; // P1 + P2 = 1
    public static final Integer[] invarianteP2 = {2,3,4}; // P2 + P3 + P4 = 5
    public static final Integer[] invarianteP3 = {5,6}; // P5 + P6 = 1
    public static final Integer[] invarianteP4 = {7,8}; // P7 + P8 = 1
    public static final Integer[] invarianteP5 = {10,11,12,13}; //P10 + P11 + P12 + P13 = 1
    public static final Integer[] invarianteP6 = {0,2,3,5,8,9,11,12,13,14}; // P0 + P2 + P3 + P5 + P8 + P9 + P11 + P12 + P13 + P14 = 5
    public static final int inv1 = 1;
    public static final int inv2 = 5;
    public static final int inv3 = 1;
    public static final int inv4 = 1;
    public static final int inv5 = 1;
    public static final int inv6 = 5;
    private Integer[][] invariantes = {invarianteP1, invarianteP2, invarianteP3, invarianteP4, invarianteP5, invarianteP6};
    private Integer[] invariantesPlazas = {inv1, inv2, inv3, inv4, inv5, inv6};
    
    /**
     * @brief Constructor vacio
     */
    public RedDePetri(){}
    
    /**
     * @brief Se asegura que se utilice el patron Singleton
     * Solo se puede crear una Red de Petri 
     * @return Red de Petri
     */
    public static RedDePetri getInstance(){
        if(uniqueInstance == null){
            uniqueInstance = new RedDePetri();
            uniqueInstance.iniciarRedDePetri();
        }
        else{
            System.out.println("Ya existe una instancia de Red de Petri");
        }
        return uniqueInstance;
    }
    
	/**
	 * @brief Inicializa los valores de la red 
	 * Asigna el marcado actual como el inicial y carga la matriz de incidencia
	 * Actualiza las transiciones sensibilizadas
	* */
    public void iniciarRedDePetri() {
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
        transicionSensible = new Integer[transiciones];
        secuenciaDisparo = new Integer[transiciones]; 
        sensibilizarT();
    }
    
    /**
     * @brief Comprueba que transiciones ahora son sensibles y actualiza su estado en el vector
     */
    private void sensibilizarT() {
        for (int j = 0; j < transiciones; j++) {
            boolean sensibilizada = true;
            // Verifica para cada plaza si tiene suficientes tokens para disparar la transición
            for (int i = 0; i < plazas; i++) {
                // Comprueba si la transición requiere más tokens de los que hay en la plaza
                if (matrizIncidencia[i][j] < 0 && marcadoActual[i] + matrizIncidencia[i][j] < 0) {
                    sensibilizada = false;
                    break;
                }
            }
            transicionSensible[j] = sensibilizada ? 1 : 0;
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
    public void disparar(int t) {
        if (!isSensible(t)) {
            System.out.println("La transición " + t + " no está sensibilizada.");
            return;
        }        
        setSecuencia(t);
        System.out.println("Disparando transición " + t);
        actualizarMarcado();
        System.out.println("Marcado actual: " + java.util.Arrays.toString(marcadoActual));
        sensibilizarT(); 
        comprobarInvariantes(t);
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
}
