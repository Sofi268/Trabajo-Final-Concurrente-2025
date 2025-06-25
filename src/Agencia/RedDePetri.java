/* Red de Petri: se encarga de mantener los marcados, transiciones y disparos de la red de Petri.
 * modelando el comportamiento de una agencia de viajes.
 */
package Agencia;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RedDePetri {
    private static RedDePetri uniqueInstance;  //Para implementacion de patron de diseño singleton.
    public Estadisticas estadisticas = Estadisticas.getInstance(); // Instancia de estadísticas para monitorear el estado de la red de Petri.
    
    private static final Integer[] marcadoInicial = {5, 1, 0, 0, 5, 0, 1, 1, 0, 0, 1, 0, 0, 0, 0};
    private static Integer[] marcadoActual;
    private static Integer[][] matrizIncidencia;
    private static Integer[] transicionSensible;         // Para saber si la transición está sensibilizada o no
    private static Integer[] transicionSensibleAnterior; // Para comparar si cambió el estado de la transición
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
    private static ArrayList<Integer> transicionesDisparadas;
    private static int disparosT0; //Cantidad de clientes que ingresan
    private static int disparosT2; //Cantidad de veces que gestiono la reserva el gestor1
    private static int disparosT3; //Cantidad de veces que gestiono la reserva el gestor2
    private static int disparosT6; //Cantidad de reservas confirmadas
    private static int disparosT7; //Cantidad de reservas canceladas
    private static int disparosT11; //Cantidad de clientes que salen
    private static Long[] timeStamp = new Long[Constantes.cantidadTransiciones]; //Tiempos de sensibilizado de cada transición
    private static long tiempoInicio; //Tiempo de inicio del sistema
    private static boolean primeraCopia = true; // Para saber si es la primera vez que se llama a sensibilizarT()
    private static Politicas politica = Politicas.getInstance("Balanceada"); // "Balanceada" o "Prioridad".
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
        //contadores a 0.
        disparosT0 = 0;
        disparosT2 = 0;
        disparosT3 = 0;
        disparosT6 = 0;
        disparosT7 = 0;
        disparosT11 = 0;
        transicionesDisparadas = new ArrayList<>();
        transicionSensible = new Integer[transiciones];
        transicionSensibleAnterior = new Integer[transiciones];
        tiempoInicio = System.currentTimeMillis(); // Guarda el tiempo de inicio
        setCtesTiempo(); // Inicializa los tiempos de sensibilizado en 0
        secuenciaDisparo = new Integer[transiciones]; 
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
            if(j==2 || j==3 || j==6 || j==7) { // Transiciones con condiciones especiales
                if (!politica.sePuedeDisparar(j)) {
                    sensibilizada = false;
                    transicionSensible[j] = 0; // No está sensibilizada
                    continue;
                }
            }   
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
            //Doble chequeo de sensibilizado, intentar borrar despues
            if (!isSensible(t)) {
                System.out.println("La transicion " + t + " no esta sensibilizada.");
                System.out.println("\n\nNO DEBERIA LLEGAR ACA\n\n");
                return false;
            }
            else{
                setSecuencia(t);
                System.out.println("Disparando transicion " + t);
                actualizarMarcado();
                transicionesDisparadas.add(t);
                System.out.println("Marcado actual: " + java.util.Arrays.toString(marcadoActual));
                sensibilizarT();
                actualizarContadores(t);
                comprobarInvariantes();
                return true;
            }
        }
        else {
            comprobarInvariantes();
            imprimirMarcado();
            System.out.println("La red de Petri ha finalizado, no se pueden disparar mas transiciones.");
            return false;
        }
    }

    public int sePuedeDisparar(int t) {
        if(esTemporal(t)){ //Disparos temporales
            if(checkTemporaryShot(t)) { // Esta dentro de la ventana de tiempo
                if(isSensible(t)){// Sensibles
                    System.out.println("La transicion " + t + " esta sensibilizada y dentro de la ventana de tiempo.");
                    return 0;
                }
                else{
                    System.out.println("La transicion " + t + " esta dentro de la ventana de tiempo pero no esta sensibilizada.");
                    return -2; // No está sensibilizada
                }
            }
            else{ // No esta dentro de la ventana de tiempo
               return tiempoSensibilizado(t).intValue();
            }
        }
        else{ // Disparos no temporales
            if(isSensible(t)) { // Sensibles
                System.out.println("La transicion " + t + " no es temporal y esta sensibilizada.");
                return 0;
            }
            else{ // No sensibles
                System.out.println("La transicion " + t + " no es temporal pero no sensibilizada.");
                return -1; 
            }
        } 
    }
    
    private void actualizarContadores(int t) {
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
    public void comprobarInvariantes() {    	
    	boolean cumpleTodos = true;
        for (int i = 0; i < invariantes.length; i++) {
            int suma = 0;
            for (int plaza : invariantes[i]) {
                suma += marcadoActual[plaza];
            }
            if (suma != invariantesPlazas[i]) {
                System.out.println("No se cumple el invariante de plaza " + (i + 1) + " al disparar la transicion");
                cumpleTodos = false;
            }
        }
        if(cumpleTodos)System.out.println("Se cumplen todos los invariantes de plaza luego del disparo" );
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

    public void setFin(){
        fin = true;
    }

    public void imprimirMarcado(){
        System.out.println("Marcado actual: " + java.util.Arrays.toString(marcadoActual));
    }

//-----------------------------------------------------------------------------------------------
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

    private Boolean esTemporal(int t) {
        System.out.println("Verificando si transicion " + t + " es temporal...");
        List<Integer> lista = Arrays.asList(Arrays.stream(Constantes.tTemporales).toArray(Integer[]::new));
        return lista.contains(t);
    }

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
}
    