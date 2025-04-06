package Agencia;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Monitor implements MonitorInterface {
    private static Monitor uniqueInstance;
    private static RedDePetri rdp = RedDePetri.getInstance();
    private static Politicas politica;
    private static Semaphore mutex = new Semaphore(1, true);
    private static Semaphore colaEntrada = new Semaphore(1, true);
    private static Semaphore[] colasCondicion;
    private static int[] colasConHilos;
    private static int ultimoOrden;

    public Monitor() {
    }
    /* Aplico patron Singleton ...
    * */
    public static Monitor getInstance(){
        if(uniqueInstance == null){
            uniqueInstance = new Monitor();
            startMonitor();
        }
        return uniqueInstance;
    }
    /* startMonitor:
    * */
    public static void startMonitor(){
    	politica = Politicas.getInstance("Prioridad"); // "Balanceada" o "Prioridad".
        colasCondicion = new Semaphore[rdp.getTransiciones()];
        colasConHilos = new int[rdp.getTransiciones()];
        ultimoOrden = -1;
        for (int i = 0; i < rdp.getTransiciones(); i++) {
            colasCondicion[i] = new Semaphore(0);
            colasConHilos[i] = 0; // Inicializa todas las colas como vacías
        }
        System.out.println("Monitor inicializado con " + rdp.getTransiciones() + " transiciones.");
    }
    // -----------------------------------------------------------------------------------------------------------------
    // logica de monitor.
    @Override
    public boolean fireTransition(int transition) {
        return fire(transition);
    }
    private static boolean fire(int transition) {
        try {
            System.out.println("Intentando agarrar colaEntrada: " + transition);
            colaEntrada.acquire();
            System.out.println("#1-Mutex Entrada - adquirido para transición: " + transition);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return (puertaMonitor(transition));
    }
    private static boolean puertaMonitor(int transition){
        System.out.println("Intentando agarrar mutex: " + transition);
        agarrarMutex();
        System.out.println("#2-Mutex Puerta - adquirido para transición: " + transition);
        return (entrarMonitor(transition));
    }
    //-------------------------------------------------------------------------------------------------------------
    /**
     * Se intenta agarrar el mutex:
     */
    private static void agarrarMutex(){
        try {
            mutex.acquire();
        }
        catch (InterruptedException e) {
            System.out.println("Error en fireTransition: " + e.getMessage());
            System.exit(1);
        }
    }
    private static void liberarMutex() {
        if (mutex.availablePermits() == 0) {
            System.out.println("Liberando mutex.");
            mutex.release();
        }
    }
    private static void liberarColaEntrada(){
        /*if (colaEntrada.hasQueuedThreads()) {
            System.out.println("Liberando cola entrada.");
            colaEntrada.release();
        }*/
        System.out.println("Liberando cola entrada.");
        colaEntrada.release();
    }
    //-------------------------------------------------------------------------------------------------------------
    private static boolean entrarMonitor(int t) {
        if (rdp.isSensible(t) && checkPolicy(t)){   //esta sensibilizada la transicion
            System.out.println("Entrando al monitor con transición: " + t);
            if (esTemporal(t)){ // si es temporal
                int check = checkTemporaryShot(t);
                if(check == 1){ //no se puede disparar.
                    System.out.println("TTT Transición " + t + " no se puede disparar todavia por tiempo. Derivando a cola.");
                    liberarColaEntrada();
                    liberarMutex();
                    fire(t);
                    return false;
                }
                /*
                if(check == 1 || check == (-1)){  // no se puede disparar.
                    if(check == -1){  //llego tarde a la ventana. debo des-Sensibilizar la transicion.
                        System.out.println("Transición " + t + " se pasó de la ventana. Des-sensibilizando.");
                        return false; // salgo del monitor con False, para que se reintente el disparo.
                    }
                    if(check == 1){  //llego antes
                        System.out.println("Transición " + t + " llego antes, no está en la ventana. Esperando " + check + " ms.");
                        System.out.println(">>>>>"+ check + " ms de espera.");
                        //derivarACola(t); // voy a la cola de entrada a disparar la transicion y pelear por el semaforo colaEntrada.
                        liberarMutex(); // liberamos el mutex del monitor.
                        liberarColaEntrada(); // liberamos el mutex de entrada.
                        fire(t);
                    }
                }
                */

            } // si es 0 continuo. esta en la ventana.
            ejecutarDisparo(t);     // no es temporal o esta habilitado por la ventana de tiempo.
            int tSensible = hayColaCondicion();
            if (tSensible >= 0) {
                System.out.println("Señalizando siguiente transición en cola: " + tSensible);
                señalizarSig(tSensible);
            } else {
                System.out.println("No hay transiciones en cola. Liberando cola de Entrada.");
                liberarColaEntrada();    // liberamos el mutex de entrada.
            }
            liberarMutex();         // liberamos el mutex del monitor.
        }else{                      // no es sensible.
            System.out.println("Transición " + t + " no es sensible. Derivando a cola.");
            derivarACola(t);
        }
        return true;
    }
    //-------------------------------------------------------------------------------------------------------------
    //politicas:
    private static boolean checkPolicy(int t) {
        boolean check = true;
        for (int conflicto : Constantes.conflictos) { // Itera sobre el arreglo de enteros conflictoi
            if (conflicto == t) { // Verifica si el valor pasado por parámetro se encuentra en el arreglo
                if(!politica.sePuedeDisparar(t)) {
                    System.out.println("Transición " + t + " no se puede disparar todavia por la politica "+ politica.getNombre() + ". Derivando a cola.");
                    check = false; // Si se encuentra, cambia el valor de check a false
                }
            }
        }
        return check;
    }
    //------------------------------------------------------------------------------------------------------------
    //Disparos  -----------------------------------------------------
    /**
     * Se realiza el disparo de la transición si está en la ventana.
     * En el caso de encontrarse antes, el hilo suelta el mútex y se duerme.
     * Cuando sale del sleep, intenta tomar el mútex y dispara nuevamente
     * Si la transición se pasó de la ventana, sale del método y el hilo sale del monitor y se cae el sistema.
     */
    public static int checkTemporaryShot(int t){
        System.out.println("-->Verificando disparo temporal para transición: " + t);
        Long tiempoTransicion = rdp.tiempoSensibilizado(t);  // se pide el tiempo de sesiblizado de la transicion.
        System.out.println("Tiempo de transición: " + tiempoTransicion);
        if (tiempoTransicion == 0){             //está en la ventana.
            System.out.println("transicion " + t + " está en la ventana.");
            return 0; 
        }else{
            if(tiempoTransicion == null){       // se paso de la ventana.
                System.out.println("transicion " + t + " se paso la ventana");
                System.exit(1);
            }  
        }
                        System.out.println("transicion " + t + " está " + tiempoTransicion +" ms antes de la ventana");
                //return tiempoTransicion; // se devuelve el tiempo que falta para que la transicion sea disparable.
                return 1;
    }
    private static Boolean esTemporal(Integer t){ // se chequea si la transicion es temporal o no
        List<Integer> lista = Arrays.asList(Arrays.stream(Constantes.tTemporales).toArray(Integer[]::new)); // se convierte el array a una lista para poder usar el contains
        if (lista.contains(t)){
            return true;
        }
        return false;
    }
    /**
     * Dispara la transicion dentro de la red e incrementa la cantidad de invariantes en la clase Politicas, luego
     * chequea si hay Transiciones en la cola de concicion, si hay la dispara, incrementa la cantidad de invariantes en
     * la clase Politica y saca a la transicion de la cola de Condicion
     * @param t transición a disparar
     */
    private static void ejecutarDisparo(Integer t){
        boolean disparo = rdp.disparar(t);
        if (!disparo || tieneMarcadoNegativo()) {
            System.out.println("Error: Marcado negativo detectado tras disparar T" + t);
            System.out.println("No se pudo disparar transición " + t + ". Derivando a cola.");
            derivarACola(t);
            System.exit(1);
        }
        System.out.println("[OK]Transición " + t + " disparada exitosamente.");
    }
    private static boolean tieneMarcadoNegativo() {
        for (Integer valor : rdp.getMarcadoActual()) {
            if (valor < 0) {
                System.out.println("Marcado negativo detectado.");
                return true;
            }
        }
        return false;
    }
    //--------------------------------------------------------------------------------------------------------------
    // manejo de colas de condicion.
    private static int hayColaCondicion() {
        int n = 12;
        int inicio = (ultimoOrden + 1) % n;
        System.out.println("Buscando transiciones en cola de condición.");
        for (int i = inicio; i < n; i++) {
            if ((colasConHilos[i]>0) && rdp.isSensible(i)) {
                ultimoOrden = i;
                System.out.println("Encontrada transición en cola: " + i);
                return i;
            }
        }
        for (int i = 0; i < inicio; i++) {
            if ((colasConHilos[i]>0) && rdp.isSensible(i)) {
                ultimoOrden = i;
                System.out.println("Encontrada transición en cola: " + i);
                return i;
            }
        }
        System.out.println("No se encontraron transiciones en cola de condición.");
        return -1;
    }
    private static void señalizarSig(int tSensible) {
        System.out.println("Liberando hilo en cola de transición " + tSensible);
        colasCondicion[tSensible].release();
        colasConHilos[tSensible]--; // se libera la cola de condicion.
    }

    private static void derivarACola(int t) {
        try {
            System.out.println("Colocando transición " + t + " en cola de condición.");
            colasConHilos[t] ++;  //sumamos un hilo a esa cola de condicion.
            liberarColaEntrada();   //#1
            liberarMutex();         //#2  
            colasCondicion[t].acquire(); // se duerme el hilo en la cola de condicion.
            System.out.println("Hilo de transición " + t + " liberado de la cola.");
            puertaMonitor(t);
        } catch (InterruptedException e) {
            System.out.println("No se pudo derivar a la cola de condición");
            e.printStackTrace();
        }
    }
}

