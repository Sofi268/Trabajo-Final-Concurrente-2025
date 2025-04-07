/**
 * @file Estadisticas.java
 * @brief Recolecta y registra estadísticas de ejecución de una Red de Petri
 */
package Agencia;

import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Estadisticas implements Runnable {

    private ArrayList<Integer> transicionesDisparadas;
    private static RedDePetri red;
    private int porcentajeT2;
    private int porcentajeT3;
    private int porcentajeT6;
    private int porcentajeT7;

    /**
     * @brief Constructor que inicializa las estadísticas
     * @param redPetri Red de Petri a monitorear
     */
    public Estadisticas(RedDePetri redPetri) {
        transicionesDisparadas = new ArrayList<>();
        red = redPetri;
        porcentajeT2 = 0;
        porcentajeT3 = 0;
        porcentajeT6 = 0;
        porcentajeT7 = 0;
    }

    /**
     * @brief Método principal del hilo que ejecuta la recolección de estadísticas y su impresion en el archivo log.txt
     * Las levanta cada 500 ms y hasta que se cumplan 186 invariantes de transicion
     */
    public void run() {
        System.out.println("Hilo de estadísticas iniciado");
        try (FileWriter file = new FileWriter("log5.txt");
             PrintWriter pw = new PrintWriter(file)) {
            
            pw.printf("************** Inicio del registro: %s *********\n", new Date());
            pw.flush();

            while (true){
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    System.out.println("Hilo interrumpido");
                    Thread.currentThread().interrupt();
                    return;
                }
                
                actualizarDisparos();
                actualizarStats();
                imprimir(pw);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void actualizarDisparos() {
        transicionesDisparadas = new ArrayList<>(red.getTransicionesDisparadas());
    }

    public void actualizarStats() {
    	int disparosT2 = red.getDisparosT2();
    	int disparosT3 = red.getDisparosT3();
    	int disparosT6 = red.getDisparosT6();
    	int disparosT7 = red.getDisparosT7();
    	
    	int totalT23 = disparosT2 + disparosT3;
    	int totalT67 = disparosT6 + disparosT7;

    	porcentajeT2 = totalT23 == 0 ? 0 : (disparosT2 * 100) / totalT23;
    	porcentajeT3 = totalT23 == 0 ? 0 : (disparosT3 * 100) / totalT23;
    	porcentajeT6 = totalT67 == 0 ? 0 : (disparosT6 * 100) / totalT67;
    	porcentajeT7 = totalT67 == 0 ? 0 : (disparosT7 * 100) / totalT67;

    }
    
    public void imprimir(PrintWriter pw) {
        if (transicionesDisparadas == null ) {
        	if(transicionesDisparadas.isEmpty()) {
        		pw.printf("No hay transiciones disparadas aún.\n");
        	}
     
        } else {
        	
            pw.printf("Transiciones disparadas: %s\n", transicionesDisparadas.toString());
            
            pw.printf("La transicion T2 fue disparada %d%% de las veces\n", porcentajeT2);
            pw.printf("La transicion T3 fue disparada %d%% de las veces\n", porcentajeT3);
            pw.printf("La transicion T6 fue disparada %d%% de las veces\n", porcentajeT6);
            pw.printf("La transicion T7 fue disparada %d%% de las veces\n", porcentajeT7);
            pw.printf("La transicion T0 fue disparada %d veces por lo que el bool sePuedeT0 es %b%n \n ", red.getCantT0(), !red.finT0());
            pw.printf("La transicion T11 fue disparada %d veces por lo que el bool sePuedeT11 es %b%n \n ", red.getCantT0(), !red.finT11());

        }
        pw.flush();
    }
    	   
}