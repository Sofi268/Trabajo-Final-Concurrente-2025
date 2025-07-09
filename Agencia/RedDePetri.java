/**
 * @file RedDePetri.java
 * @brief Clase que representa la Red de Petri de una agencia de viajes.
 *
 * Se encarga de modelar el comportamiento del sistema mediante marcado, disparos, 
 * transiciones temporales y verificación de invariantes. Implementa el patrón singleton.
 */
package Agencia;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RedDePetri {
    private static RedDePetri uniqueInstance;  //Para implementacion de patron de diseño singleton.
    private static Integer[] marcadoActual;
    private static Integer[] transicionSensible;         // Para saber si la transición está sensibilizada o no
    private static Integer[] transicionSensibleAnterior; // Para comparar si cambió el estado de la transición
    private static Integer[] secuenciaDisparo;
    private static ArrayList<Integer> transicionesDisparadas;
    private static int disparosT0; //Cantidad de clientes que ingresan
    private static int disparosT11; //Cantidad de clientes que salen
    private static Long[] timeStamp = new Long[Constantes.cantidadTransiciones]; //Tiempos de sensibilizado de cada transición
    private static long tiempoInicio; //Tiempo de inicio del sistema
    private static boolean primeraCopia = true; // Para saber si es la primera vez que se llama a sensibilizarT()
    private boolean fin = false;

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
        disparosT0 = 0;
        disparosT11 = 0;
        transicionesDisparadas = new ArrayList<>();
        transicionSensible = new Integer[Constantes.transiciones];
        transicionSensibleAnterior = new Integer[Constantes.transiciones];
        tiempoInicio = System.currentTimeMillis(); // Guarda el tiempo de inicio
        setCtesTiempo(); // Inicializa los tiempos de sensibilizado en 0
        secuenciaDisparo = new Integer[Constantes.transiciones]; 
        for (int i = 0; i < Constantes.transiciones; i++) {
            transicionSensible[i] = 0;
            transicionSensibleAnterior[i] = 0;
        }
        sensibilizarT();
    }

    /**
     * @brief Comprueba que Constantes.transiciones ahora son sensibles segun marcado y politica y actualiza su estado en el vector
     */
    private static void sensibilizarT() {
        if (!primeraCopia) {
            transicionSensibleAnterior = transicionSensible.clone(); // Copia el estado actual
        }
        for (int j = 0; j < Constantes.transiciones; j++) {
            boolean sensibilizada = true;
            
            // Verifica para cada plaza si tiene suficientes tokens para disparar la transición 
            for (int i = 0; i < Constantes.plazas; i++) {
                if (Constantes.matrizIncidencia[i][j] < 0 && marcadoActual[i] + Constantes.matrizIncidencia[i][j] < 0) {
                    sensibilizada = false;
                    break;
                }
            }
            transicionSensible[j] = sensibilizada ? 1 : 0;
        }
        if (!primeraCopia) {
            setTimeStamp(); // Actualiza los timestamps de las Constantes.transiciones sensibilizadas
        } else {
            primeraCopia = false;
        }
        System.out.println("Transiciones sensibilizadas: " + java.util.Arrays.toString(transicionSensible));
    }

    /**
     * @brief Comprueba si la transicion dada esta sensibilizada por numero de tokens y politica
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
        if(!fin){
            if(t==11&& disparosT11 >= 186) {
                System.out.println("La transicion T11 ya fue disparada 186 veces, no se puede volver a disparar.");
                setFin();
                return false;
            }
            setSecuencia(t);
            System.out.println("Disparando transicion " + t);
            actualizarMarcado();
            transicionesDisparadas.add(t);
            System.out.println("Marcado actual: " + java.util.Arrays.toString(marcadoActual));
            sensibilizarT();
            if(t==11)disparosT11++;
            if(t==0)disparosT0++;
            comprobarInvariantes();
            return true;
        }
        else {
            comprobarInvariantes();
            imprimirMarcado();
            System.out.println("La red de Petri ha finalizado, no se pueden disparar mas Constantes.transiciones.");
            return false;
        }
    }

    /**
     * @brief Evalua si una transición puede dispararse, considerando si es temporal o no
     * 
     * Si la transición es temporal, verifica que esté dentro de la ventana de tiempo y sensibilizada
     * Si no es temporal, simplemente verifica su sensibilidad
     * 
     * @param t Número de transicion a evaluar
     * @return 0 si se puede disparar
     *        -1 si no está sensibilizada (no temporal)
     *         o un valor positivo indicando el tiempo que debe dormir hasta que entre en la ventana
     */
    public int sePuedeDisparar(int t) {
        if(esTemporal(t)){ //Disparos temporales
            if(checkTemporaryShot(t)&&isSensible(t)) { // Esta dentro de la ventana de tiempo y es sensible
                System.out.println("La transicion " + t + " esta sensibilizada y dentro de la ventana de tiempo.");
                return 0;
            }
            else{ // No esta dentro de la ventana de tiempo
               return tiempoSensibilizado(t).intValue();
            }
        }
        else{ // Disparos no temporales
            if(isSensible(t)) { // Sensibles
                return 0;
            }
            else{ // No sensibles
                return -1; 
            }
        } 
    }

    /**
     * @brief Retorna la lista de transiciones disparadas durante la ejecucion de la red
     * 
     * @return ArrayList con las transiciones disparadas.
     */
    public ArrayList<Integer> getTransicionesDisparadas(){
    	return transicionesDisparadas;
    }
    
    /**
     * @brief Actualiza el marcado actual luego de dispararse una transicion
     * Utiliza la Ecuación fundamental marcadoActual = marcadoActual + matrizIncidencia * secuenciaDisparo
     */
    private void actualizarMarcado() {
        for (int i = 0; i < Constantes.plazas; i++) {
            int nuevoMarcado = marcadoActual[i]; 
            for (int j = 0; j < Constantes.transiciones; j++) {
                nuevoMarcado += Constantes.matrizIncidencia[i][j] * secuenciaDisparo[j];
            }
            marcadoActual[i] = nuevoMarcado; 
        }
    }
    
    /**
     * @brief Comprueba que se cumplan los invariantes de plaza luego de un disparo
     * @param t numero de disparo realizado
     */
    public void comprobarInvariantes() {    	
    	boolean cumpleTodos = true;
        for (int i = 0; i < Constantes.invariantes.length; i++) {
            int suma = 0;
            for (int plaza : Constantes.invariantes[i]) {
                suma += marcadoActual[plaza];
            }
            if (suma != Constantes.invariantesPlazas[i]) {
                System.out.println("No se cumple el invariante de plaza " + (i + 1) + " al disparar la transicion");
                cumpleTodos = false;
            }
        }
        if(cumpleTodos)System.out.println("Se cumplen todos los invariantes de plaza luego del disparo" );
    }
    
    /**
     * @brief Devuelve la cantidad de Constantes.transiciones
     * @return numero de Constantes.transiciones de la red
     */
    public int getTransiciones() {
    	return Constantes.transiciones;
    }
	    
	/**
	 * @brief Inicializa secuenciaDisparo con ceros y marcar solo la transición disparada
	 * @param t transicion a disparar
	 */
    private void setSecuencia(int t) {
    	for (int i = 0; i < Constantes.transiciones; i++) {
            secuenciaDisparo[i] = (i == t) ? 1 : 0;
        }
    }
    
    //-----------------------------------------------------------------------------------------------
    //Getters para obtener informacion de la red de Petri

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
    
    //-----------------------------------------------------------------------------------------------

    /**
     * @brief Marca la red de Petri como finalizada (se alcanzaron los invariantes esperados)
     * 
     * Una vez invocado, evita que se sigan disparando transiciones
     */
    public void setFin(){
        fin = true;
    }

    /**
     * @brief Imprime el marcado actual de la red de Petri por consola
     */
    public void imprimirMarcado(){
        System.out.println("Marcado actual: " + java.util.Arrays.toString(marcadoActual));
    }

//-----------------------------------------------------------------------------------------------
    // Disparos temporales:

    /**
     * @brief Obtiene el tiempo actual relativo desde el inicio de la simulacion
     * 
     * @return Tiempo relativo desde el inicio
     */
    private static Long getTiempoActual() {
        return System.currentTimeMillis() - tiempoInicio; 
    }

    /**
     * @brief Al momento de crearse la red, inicia todos los tiempos de sensibilizado en 0
     */
    public static void setCtesTiempo(){
        for (int i = 0; i < Constantes.cantidadTransiciones; i++) {
            timeStamp[i] = getTiempoActual();//System.currentTimeMillis();
        }
    }

    /**
     * @brief Obtiene las Constantes.transiciones que han sido hablitadas y calcula el tiempo desde que desde que esta se sensiblizó y en base a eso, comprueba si se puede disparar
     * chequeando los intervalos de tiempo configurados con alfa y beta:
     * tMin =< tActual => tMax : La transición se puede disparar
     * tActual < tMin : El hilo se va a dormir hasta que entre en el intervalo de tiempo de sensiblizado, soltando el mutex. Luego que transcurre este tiempo,
     * intenta tomar el mutex para comprobar que aún se encuentre en el intervalo mencionado. sino, ocurre el siguiente caso:
     * tActual > tMin : La transición no se dispara y el hilo sale del monitor sin dispararla
     * @param T: Transición a disparar
     * @return 0 si está dentro del intervalo temporal, null si se pasó o un valor que indica el tiempo que se tiene que dormir
     */
    private Long tiempoSensibilizado(Integer t){
        Long tActual = getTiempoActual();
            System.out.println("---Tiempo actual: " + tActual +" ms");
        Long tMin = timeStamp[t] + Constantes.ALFA ;
            System.out.println("---Tiempo minimo: " + tMin +" ms");
        Long tMax = timeStamp[t] + Constantes.BETA;
            System.out.println("---Tiempo maximo: " + tMax +" ms");
        if ((tActual >= tMin) && (tActual <= tMax)){  // Se encuentra dentro del intervalo temporal
            return 0L;
        }else if (tActual<tMin) {          // Se encuentra antes del intervalo temporal
            return tMin - tActual;
        }else{                             // Se encuentra después del intervalo temporal
            return null; // Se paso de la ventana
        }
    }

    /**
     * @return tiempos de sensibilizados de cada transición
     */
    public Long[] getTimeStamp(){
        return timeStamp;
    }

    /**
     * @brief Actualiza los timeStaps de las Constantes.transiciones que pasaron de no sensibilizadas a sensibilizadas
     */
    public static void setTimeStamp() {
        for (int i = 0; i < Constantes.cantidadTransiciones; i++) {
            if(transicionSensibleAnterior[i] == 0 && transicionSensible[i] == 1){ // Si la transición pasó de no sensibilizada a sensibilizada
                timeStamp[i] = getTiempoActual();
            }
        }
    }

    /**
     * @brief Verifica si una transición es temporal
     * 
     * @param t Numero de la transicion
     * @return true si la transición es temporal, false en caso contrario
     */
    private Boolean esTemporal(int t) {
        System.out.println("Verificando si transicion " + t + " es temporal...");
        List<Integer> lista = Arrays.asList(Arrays.stream(Constantes.tTemporales).toArray(Integer[]::new));
        return lista.contains(t);
    }

    /**
     * @brief Verifica si la transicion temporal puede dispararse según el tiempo transcurrido desde que se sensibilizo.
     * 
     * Si el tiempo está dentro del intervalo [ALFA, BETA], se permite el disparo. Si falta tiempo, devuelve false.
     * Si se excede el tiempo, finaliza el programa.
     *
     * @param t Numero de la transicion temporal.
     * @return true si la transicion puede dispararse, false si debe esperar. Finaliza el programa si se paso del tiempo.
     */
    private boolean checkTemporaryShot(int t) {
        System.out.println("Verificando disparo temporal para transicion " + t + "...");
        Long tiempoRestanteTransicion = tiempoSensibilizado(t);
        System.out.println("Tiempo restante para transicion " + t + ": " + tiempoRestanteTransicion);
        if (tiempoRestanteTransicion != null) {
            if (tiempoRestanteTransicion == 0) {
                System.out.println("Transicion " + t + " esta en la ventana.");
                return true;
            } else {
                System.out.println("Transicion " + t + " esta " + tiempoRestanteTransicion + " ms antes de la ventana.");
                return false;
            }
        } else {
            System.out.println("Transicion " + t + " se paso de la ventana.");
            System.exit(1);
        }
        return false;
    }

    public boolean isFinalizado() {
        return fin;
    }
}
    
