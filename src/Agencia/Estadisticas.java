package Agencia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Estadisticas implements Runnable {
    private static Estadisticas uniqueInstance;
    public static RedDePetri rdp = RedDePetri.getInstance();
    public static final Monitor monitor = Monitor.getInstance();

    private static ArrayList<Integer> transicionesDisparadas;
    private static int porcentajeT2;
    private static int porcentajeT3;
    private static int porcentajeT6;
    private static int porcentajeT7;
    private static int canTransi;
    private static boolean stop;
    private final int MAX_INVARIANETS = 186;

    public Estadisticas() {}

    public static Estadisticas getInstance() {
        if (uniqueInstance == null) {
            System.out.println("Instanciando Estadisticas...");
            uniqueInstance = new Estadisticas();
            startEstadisticas();
        }
        return uniqueInstance;
    }

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
            int i = 1;
            int invariantes = 0;
            while (!stop) {
                try {
                    TimeUnit.MILLISECONDS.sleep(5);
                } catch (InterruptedException e) {
                    System.out.println("Hilo interrumpido");
                    Thread.currentThread().interrupt();
                    return;
                }

                actualizarDisparos();
                actualizarStats();
                pw.printf("Vuelta %d, cantidad de T0: %d\n", i, rdp.getDisparosT0());
                pw.printf("Vuelta %d, cantidad de T11: %d\n", i, rdp.getDisparosT11());
                imprimir(pw);
                i++;
                writeTransitions();
                invariantes = contarInvariantes();
                pw.printf("Hay: %d invariantes\n",invariantes);
                System.out.printf("Se alcanzaron %d invariantes\n\n", invariantes);
                if (invariantes >= MAX_INVARIANETS) {
                    setStop();
                    System.out.println("Se alcanzaron los invariantes. Deteniendo el sistema.");
                    rdp.setFin();
                }
            }
            
            System.out.println("SE ESTA DETENIENDO EL HILO DE ESTADISTICAS");

            pw.printf("Finalizando registro. Se han disparado %d transiciones.\n", canTransi);
            pw.flush();
            monitor.setFin();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    private void actualizarStats() {
        int disparosT2 = rdp.getDisparosT2();
        int disparosT3 = rdp.getDisparosT3();
        int disparosT6 = rdp.getDisparosT6();
        int disparosT7 = rdp.getDisparosT7();

        int totalT23 = disparosT2 + disparosT3;
        int totalT67 = disparosT6 + disparosT7;

        porcentajeT2 = totalT23 == 0 ? 0 : (disparosT2 * 100) / totalT23;
        porcentajeT3 = totalT23 == 0 ? 0 : (disparosT3 * 100) / totalT23;
        porcentajeT6 = totalT67 == 0 ? 0 : (disparosT6 * 100) / totalT67;
        porcentajeT7 = totalT67 == 0 ? 0 : (disparosT7 * 100) / totalT67;

    }

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
     * Método que se puede llamar al finalizar el sistema para
     * escribir todas las transiciones restantes y hacer la verificación final.
     */
    public void verificacionFinal() {
        writeTransitions();
        int invariantes = contarInvariantes();
        System.out.println("Verificación final: invariantes = " + invariantes);
    }

    /**
     * Escribe la secuencia completa de transiciones disparadas en logTransiciones.txt
     * agregando al final (append), para no perder información previa.
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
     * Invoca el script Python check_invariants.py pasando logTransiciones.txt
     * para contar invariantes.
     */
private int contarInvariantes() {
    int cantidad = 0;
    try {
        ProcessBuilder pb = new ProcessBuilder("python", "check_invariants.py");

        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                System.out.println("[Python] " + linea);
                if (linea.startsWith("INVARIANTES=")) {
                    String valorStr = linea.substring("INVARIANTES=".length()).trim();
                    try {
                        cantidad = Integer.parseInt(valorStr);
                        System.out.println(">> Cantidad de invariantes parseada: " + cantidad);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parseando cantidad de invariantes: " + valorStr);
                    }
                }
            }
        }

        process.waitFor();

    } catch (IOException | InterruptedException e) {
        e.printStackTrace();
        Thread.currentThread().interrupt();
    }
    return cantidad;
}



    private void setStop() {
        stop = true;
    }

}
