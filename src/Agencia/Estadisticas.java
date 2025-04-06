package Agencia;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Estadisticas implements Runnable {
    private ArrayList<Integer> transicionesDisparadas;
    private boolean listo;
    private static RedDePetri red;
    private static int cantidadInvariantes;
	private int porcentajeT2;
	private int porcentajeT3;
    private int porcentajeT6;
	private int porcentajeT7;
	

    public Estadisticas(RedDePetri redPetri) {
        transicionesDisparadas = new ArrayList<>();
        listo = false;
        red = redPetri;
        cantidadInvariantes = 0;
        porcentajeT2 = 0;
        porcentajeT3 = 0;
        porcentajeT6 = 0;
        porcentajeT7 = 0;
    }

    @Override
    public void run() {
        System.out.println("Hilo de estadísticas iniciado");
        try (FileWriter file = new FileWriter("log1.txt");
             PrintWriter pw = new PrintWriter(file)) {
            
            pw.printf("************** Inicio del registro: %s *********\n", new Date());
            pw.flush();

            while (true) {
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
//                if (comprobarInvariantes()) {
//                    listo = true;
//                }
            }
            //pw.printf("Finalizando registro. Se han disparado %d invariantes.\n", cantidadInvariantes);
            //pw.flush();
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
            /*pw.printf("\n La transicion T2 fue disparada " + porcentajeT2 + "% de las veces");
            // System.out.println("El gestor 1 gestiono el " + porcentajeT2 + "% de las reservas");	
           pw.printf("\n La transicion T3 fue disparada " + porcentajeT3 + "% de las veces");
        // System.out.println("El gestor 2 gestiono el " + porcentajeT3 + "% de las reservas");
           pw.printf("\n La transicion T6 fue disparada " + porcentajeT6 + "% de las veces");
         //System.out.println("El agente aprobo el " + porcentajeT6 + "% de las reservas");
           pw.printf("\n La transicion T7 fue disparada " + porcentajeT7 + "% de las veces");
         //System.out.println("El agente rechazo el " + porcentajeT7 + "% de las reservas");*/
            pw.printf("La transicion T2 fue disparada %d%% de las veces\n", porcentajeT2);
            pw.printf("La transicion T3 fue disparada %d%% de las veces\n", porcentajeT3);
            pw.printf("La transicion T6 fue disparada %d%% de las veces\n", porcentajeT6);
            pw.printf("La transicion T7 fue disparada %d%% de las veces\n", porcentajeT7);

        }
        pw.flush();
    }

    public boolean comprobarInvariantes() {
        return getCantInvariantes() >= 186;
    }

    public int getCantInvariantes() {
        int invariantes = 0;
        // TODO: Implementar lógica para contar invariantes
        return invariantes;
    }
    

}
