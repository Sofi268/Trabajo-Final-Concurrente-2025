/* Main:
* */
package Agencia;
public class Main {
    public static final Monitor monitor = Monitor.getInstance();
    public static void main(String[] args) {
        //Puestos de trabajo
        Thread[] hilosTrabajo = new Thread[9]; //creamos un arreglo de 9 hilos.
        CrearPuestos(hilosTrabajo);
        IniciarPuestos(hilosTrabajo);
        IniciarEstadisticas();
        try {
            for (Thread hilo : hilosTrabajo) {
                if (hilo != null) {
                    hilo.join();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Error al esperar los hilos.");
        }
    }
    // -----------------------------------------------------------------------------------------------------------------
    // INICIO METODOS DE UTILIDAD:
    /**
     * @brief Creación de puestos de trabajo y asignación de hilos.
     */
    private static void CrearPuestos(Thread[] hilosTrabajo){
        Cliente clienteEntrada = new Cliente(monitor, 1);
        hilosTrabajo[0] = new Thread(clienteEntrada);
        Cliente clienteSalida = new Cliente(monitor, 2);
        hilosTrabajo[1] = new Thread(clienteSalida);

        GestorReservas gestorTipo1 = new GestorReservas(monitor, 1);
        hilosTrabajo[2] = new Thread(gestorTipo1);
        GestorReservas gestorTipo2 = new GestorReservas(monitor, 2);
        hilosTrabajo[3] = new Thread(gestorTipo2);

        Agente agenteTipo1 = new Agente(monitor, 1);
        hilosTrabajo[4] = new Thread(agenteTipo1);
        Agente agenteTipo2 = new Agente(monitor, 2);
        hilosTrabajo[5] = new Thread(agenteTipo2);
        Agente agenteTipo3 = new Agente(monitor, 2);
        hilosTrabajo[6] = new Thread(agenteTipo3);
    }
    /**
     * @brief Inicia los hilos que han sido creados.
     */
    private static void IniciarPuestos(Thread[] hilosTrabajo){
        for (Thread hilo : hilosTrabajo) {
            if (hilo != null) {
                hilo.start();
            }
        }
    }

    /**
     * @brief Inicia la escritura en el log de las estadisticas del programa.
     */
    private static void IniciarEstadisticas(){
        Estadisticas estadisticas = new Estadisticas();
        Thread stats = new Thread(estadisticas);
        stats.start();
    }
}