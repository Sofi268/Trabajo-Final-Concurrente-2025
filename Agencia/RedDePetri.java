/* Red de Petri: se encarga de mantener los marcados, transiciones y disparos de la red de Petri.
 * modelando el comportamiento de una agencia de viajes.
 */
package Agencia;
import java.util.ArrayList;

public class RedDePetri {
    private static RedDePetri uniqueInstance;  //Para implementacion de patron de diseño singleton.
    public Estadisticas estadisticas = Estadisticas.getInstance(); // Instancia de estadísticas para monitorear el estado de la red de Petri.

    private static Integer[] marcadoActual;
    private static Integer[] transicionSensible;         // Para saber si la transición está sensibilizada o no
    //private static Integer[] transicionSensibleAnterior; // Para comparar si cambió el estado de la transición
    private static Integer[] secuenciaDisparo;
    private static ArrayList<Integer> transicionesDisparadas;
    private static int disparosT0; //Cantidad de clientes que ingresan
    private static int disparosT2; //Cantidad de veces que gestiono la reserva el gestor1
    private static int disparosT3; //Cantidad de veces que gestiono la reserva el gestor2
    private static int disparosT6; //Cantidad de reservas confirmadas
    private static int disparosT7; //Cantidad de reservas canceladas
    private static int disparosT11; //Cantidad de clientes que salen
    private static Long[] timeStamp = new Long[getTransiciones()]; //Tiempos de sensibilizado de cada transición
    //private static long tiempoInicio; //Tiempo de inicio del sistema
    //private static boolean primeraCopia = true; // Para saber si es la primera vez que se llama a sensibilizarT()
    private static Politicas politica = Politicas.getInstance("Balanceada"); // "Balanceada" o "Prioridad".

    public RedDePetri(){ 
        System.out.println("Red de Petri creada.");
    }
    public static RedDePetri getInstance(){
        if(uniqueInstance == null){
            uniqueInstance = new RedDePetri();
            startRedDePetri();
        }
        return uniqueInstance;
    }

//-----------------------------------------------------------------------------------------------
    public static void startRedDePetri() {
        System.out.println("Iniciando Red de Petri...");
        marcadoActual = Constantes.marcadoInicial.clone(); 
        System.out.println("Marcado inicial: " + java.util.Arrays.toString(marcadoActual));
        //contadores a 0.
        disparosT0 = 0;
        disparosT2 = 0;
        disparosT3 = 0;
        disparosT6 = 0;
        disparosT7 = 0;
        disparosT11 = 0;
        transicionesDisparadas = new ArrayList<>();
        transicionSensible = new Integer[getTransiciones()];
        //transicionSensibleAnterior = new Integer[getTransiciones()];
        //tiempoInicio = System.currentTimeMillis(); // Guarda el tiempo de inicio
        //setCtesTiempo(); // Inicializa los tiempos de sensibilizado en 0
        secuenciaDisparo = new Integer[getTransiciones()]; 
        for (int i = 0; i < getTransiciones(); i++) {
            transicionSensible[i] = 0;
            //transicionSensibleAnterior[i] = 0;
        }
        sensibilizarT();
    }
    /**
     * @brief Comprueba que transiciones ahora son sensibles y actualiza su estado en el vector
     */
    private static void sensibilizarT() {
        /*if (!primeraCopia) {
            transicionSensibleAnterior = transicionSensible.clone(); // Copia el estado actual
        }*/
        for (int j = 0; j < getTransiciones() ; j++) {
            boolean sensibilizada = true;
            // Verifica para cada plaza si tiene suficientes tokens para disparar la transición
            for (int i = 0; i < Constantes.plazas ; i++) {
                if (Constantes.matrizIncidencia[i][j] < 0 && marcadoActual[i] + Constantes.matrizIncidencia[i][j] < 0) {
                    sensibilizada = false;
                    break;
                }
            }
            transicionSensible[j] = sensibilizada ? 1 : 0;
        }
        /*if (!primeraCopia) {
            setTimeStamp(); // Actualiza los timestamps de las transiciones sensibilizadas
        } else {
            primeraCopia = false;
        }*/
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
    public boolean disparar(int t) {
        if (!isSensible(t)) { //verifica si la transicion esta sensibilizada nuevamente...
            System.out.println("La transición " + t + " no está sensibilizada.");
            return false;
        }        
        setSecuencia(t);
        System.out.println("Disparando transición " + t);
        actualizarMarcado();
        transicionesDisparadas.add(t);
        System.out.println("Marcado actual: " + java.util.Arrays.toString(marcadoActual));
        sensibilizarT();
        if(t==0) disparosT0++;
        if(t==11) disparosT11++;
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
        comprobarInvariantes(t);
        return true;
    }
    
    public ArrayList<Integer> getTransicionesDisparadas(){
    	return transicionesDisparadas;
    }
    
    /**
     * @brief Actualiza el marcado actual luego de dispararse una transicion
     * Utiliza la Ecuación fundamental marcadoActual = Constantes. + Constantes.matrizIncidencia * secuenciaDisparo
     */
    private void actualizarMarcado() {
        for (int i = 0; i < Constantes.plazas ; i++) {
            int nuevoMarcado = marcadoActual[i]; 
            for (int j = 0; j < getTransiciones() ; j++) {
                nuevoMarcado += Constantes.matrizIncidencia[i][j] * secuenciaDisparo[j];
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
        for (int i = 0; i < Constantes.invariantes.length; i++) {
            int suma = 0;
            for (int plaza : Constantes.invariantes[i]) {
                suma += marcadoActual[plaza];
            }
            if (suma != Constantes.invariantesPlazas[i]) {
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
    public static int getTransiciones() {
    	return Constantes.NumTransiciones;
    }
	    
	/**
	 * @brief Inicializa secuenciaDisparo con ceros y marcar solo la transición disparada
	 * @param t transicion a disparar
	 */
    private void setSecuencia(int t) {
    	for (int i = 0; i < getTransiciones(); i++) {
            secuenciaDisparo[i] = (i == t) ? 1 : 0;
        }
    }
    
    public Integer[] getTSensibles() {
    	return transicionSensible;
    }
    
    public Integer[] getMarcadoActual() {
    	return marcadoActual;
    }
    public int getDisparosT0() {
    	return disparosT0;
    }
    public int getDisparosT11() {
    	return disparosT11;
    }
    
    public int getDisparosT2() {
    	return disparosT2;
    }
    
    public int getDisparosT3() {
    	return disparosT3;
    }
    
    public int getDisparosT6() {
    	return disparosT6;
    }
    
    public int getDisparosT7() {
    	return disparosT7;
    }
    //------------------------------------------------------------------------------------
    // Disparos temporales:
    // private static Long getTiempoActual() {
    //     return System.currentTimeMillis() - tiempoInicio; // Tiempo relativo desde el inicio
    // }
    // /**
    //  * Al momento de crearse la red, inicia todos los tiempos de sensibilizado en 0
    //  */
    // public static void setCtesTiempo(){
    //     for (int i = 0; i < getTransiciones(); i++) {
    //         timeStamp[i] = getTiempoActual();//System.currentTimeMillis();
    //     }
    // }
    // /**Obtiene las transiciones que han sido hablitadas y calcula el tiempo desde que desde que esta se sensiblizó y en base a eso, comprueba si se puede disparar
    //  * chequeando los intervalos de tiempo configurados con alfa y beta:
    //  * tMin =< tActual => tMax : La transición se puede disparar
    //  * tActual < tMin : El hilo se va a dormir hasta que entre en el intervalo de tiempo de sensiblizado, soltando el mutex. Luego que transcurre este tiempo,
    //  * intenta tomar el mutex para comprobar que aún se encuentre en el intervalo mencionado. sino, ocurre el siguiente caso:
    //  * tActual > tMin : La transición no se dispara y el hilo sale del monitor sin dispararla
    //  * @param T: Transición a disparar
    //  * @return 0 si está dentro del intervalo temporal, null si se pasó o un valor que indica el tiempo que se tiene que dormir
    //  */
    // public Long tiempoSensibilizado(Integer t){
    //     Long tActual = getTiempoActual();
    //         System.out.println("---Tiempo actual: " + tActual +" ms");
    //     Long tMin = timeStamp[t] + Constantes.ALFA ;
    //         System.out.println("---Tiempo mínimo: " + tMin +" ms");
    //     Long tMax = timeStamp[t] + Constantes.BETA;
    //         System.out.println("---Tiempo máximo: " + tMax +" ms");
    //     if ((tActual >= tMin) && (tActual <= tMax)){  // Se encuentra dentro del intervalo temporal
    //         return 0L;
    //     }else if (tActual<tMin) {          // Se encuentra antes del intervalo temporal
    //         return tMin - tActual;
    //     }else{                             // Se encuentra después del intervalo temporal
    //         return null; // Se pasó de la ventana
    //     }
    // }
    // /**
    //  * @return tiempos de sensibilizados de cada transición
    //  */
    // public Long[] getTimeStamp(){
    //     return timeStamp;
    // }
    // /**
    //  * Actualiza los timeStaps de las transiciones que pasaron de no sensibilizadas a sensibilizadas
    //  */
    // public static void setTimeStamp() {
    //     for (int i = 0; i < getTransiciones(); i++) {
    //         if(transicionSensibleAnterior[i] == 0 && transicionSensible[i] == 1){ // Si la transición pasó de no sensibilizada a sensibilizada
    //             timeStamp[i] = getTiempoActual();
    //         }
    //     }
    // }
}