/**
* @file Estadisticas.java
* @brief Clase encargada de recolectar, calcular y registrar estadísticas del sistema en ejecución
* Registra transiciones disparadas, porcentajes de uso, e invoca la verificación de invariantes
*/
package Agencia;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Estadisticas implements Runnable {
    public static Estadisticas uniqueInstance;
    public static RedDePetri rdp = RedDePetri.getInstance();
    public static Politicas politica = Politicas.getInstance("Balanceada");

    private static ArrayList<Integer> transicionesDisparadas;
    private static int porcentajeT2;
    private static int porcentajeT3;
    private static int porcentajeT6;
    private static int porcentajeT7;
    private static int canTransi;
    private static boolean stop;

    public Estadisticas() {}

    /**
     * @brief Devuelve la instancia única de Estadisticas. Si no existe, la crea e inicializa
     */
    public static Estadisticas getInstance() {
        if (uniqueInstance == null) {
            System.out.println("Instanciando Estadisticas...");
            uniqueInstance = new Estadisticas();
            startEstadisticas();
        }
        return uniqueInstance;
    }

    /**
     * @brief Inicializa las variables necesarias para comenzar a recolectar estadísticas
     */
    public static void startEstadisticas() {
        System.out.println("Estadistico creado.");
        transicionesDisparadas = new ArrayList<>();
        canTransi = 0;
        porcentajeT2 = 0;
        porcentajeT3 = 0;
        porcentajeT6 = 0;
        porcentajeT7 = 0;
        stop = false;
    }

    @Override
    public void run() {
        System.out.println("Hilo de estadísticas iniciado");
        try (FileWriter file = new FileWriter("statsLog.txt");
             PrintWriter pw = new PrintWriter(file)) {

            pw.printf("************** Inicio del registro: %s *********\n", new Date());
            pw.flush();
            int invariantes = 0;
            while (!stop) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    System.out.println("Hilo interrumpido");
                    Thread.currentThread().interrupt();
                    return;
                }

                actualizarDisparos();
                actualizarStats();
                pw.printf("Cantidad de T0: %d\n", rdp.getDisparosT0());
                pw.printf("Cantidad de T11: %d\n", rdp.getDisparosT11());
                imprimir(pw);
                writeTransitions();
                invariantes = contarInvariantes();
                pw.printf("Hay: %d invariantes\n",invariantes);
                System.out.printf("Se alcanzaron %d invariantes\n\n", invariantes);
                if (invariantes >= Constantes.MAX_INVARIANETS) {
                    setStop();
                    System.out.println("Se alcanzaron los invariantes. Deteniendo el sistema.");
                }
            }
            
            System.out.println("\nSE ESTA DETENIENDO EL HILO DE ESTADISTICAS\n");

            pw.printf("Finalizando registro. Se han disparado %d transiciones.\n", canTransi);
            pw.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @brief Actualiza la lista de transiciones disparadas desde la red de Petri
     */
    private void actualizarDisparos() {
        ArrayList<Integer> disparos = rdp.getTransicionesDisparadas();
        if (disparos == null) {
            System.out.println("Advertencia: La lista de transiciones disparadas es null.");
            transicionesDisparadas = new ArrayList<>();
        } else {
            transicionesDisparadas = new ArrayList<>(disparos);
        }
        canTransi = transicionesDisparadas.size();
        System.out.println("Cantidad de transiciones disparadas: " + canTransi);
    }

    /**
     * @brief Calcula los porcentajes de disparo de T2/T3 y T6/T7
     */
    private void actualizarStats() {
        int disparosT2 = politica.getDisparosT2();
        int disparosT3 = politica.getDisparosT3();
        int disparosT6 = politica.getDisparosT6();
        int disparosT7 = politica.getDisparosT7();

        int totalT23 = disparosT2 + disparosT3;
        int totalT67 = disparosT6 + disparosT7;

        porcentajeT2 = totalT23 == 0 ? 0 : (disparosT2 * 100) / totalT23;
        porcentajeT3 = totalT23 == 0 ? 0 : (disparosT3 * 100) / totalT23;
        porcentajeT6 = totalT67 == 0 ? 0 : (disparosT6 * 100) / totalT67;
        porcentajeT7 = totalT67 == 0 ? 0 : (disparosT7 * 100) / totalT67;

    }

    /**
     * @brief Imprime las transiciones disparadas y porcentajes al archivo de estadísticas
     */
    private void imprimir(PrintWriter pw) {
        if (transicionesDisparadas == null || transicionesDisparadas.isEmpty()) {
            pw.printf("No hay transiciones disparadas aún.\n");
        } else {
            pw.printf("Transiciones disparadas: %s\n", transicionesDisparadas.toString());
            pw.printf("La transicion T2 fue disparada %d%% de las veces\n", porcentajeT2);
            pw.printf("La transicion T3 fue disparada %d%% de las veces\n", porcentajeT3);
            pw.printf("La transicion T6 fue disparada %d%% de las veces\n", porcentajeT6);
            pw.printf("La transicion T7 fue disparada %d%% de las veces\n", porcentajeT7);
            pw.printf("La cantidad de transiciones disparadas  es: %d\n", canTransi);
        }
        pw.flush();
    }

    /**
     * @brief Al finalizar el sistema escribe todas las transiciones restantes y hacer la verificación final
     */
    public void verificacionFinal() {
        writeTransitions();
        int invariantes = contarInvariantes();
        System.out.println("Verificación final: invariantes = " + invariantes);
    }

    /**
     * @brief Escribe la secuencia de transiciones disparadas en logTransiciones.txt una vez que se alcanzaron los invariantes
     */
    private void writeTransitions() {
        File logFile = new File("logTransiciones.txt");

        try (FileWriter writer = new FileWriter(logFile)) {
            for (Integer t : transicionesDisparadas) {
                writer.write("T" + t);
            }
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error al escribir logTransiciones.txt: " + e.getMessage());
        }
    }

    /**
     * @brief Llama a la función externa que cuenta los invariantes reconocidos en el log
     * @return cantidad de invariantes detectados.
     */
    private int contarInvariantes() {
        return CheckInvariants.invariantes();
    }

    /**
     * @brief Detiene el bucle de ejecución del hilo de estadísticas
     */
    private void setStop() {
        stop = true;
    }

}
