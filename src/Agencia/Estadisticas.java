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

    public Estadisticas(RedDePetri redPetri) {
        transicionesDisparadas = new ArrayList<>();
        listo = false;
        red = redPetri;
        cantidadInvariantes = 0;
    }

    @Override
    public void run() {
        System.out.println("Hilo de estadísticas iniciado");
        try (FileWriter file = new FileWriter("log.txt");
             PrintWriter pw = new PrintWriter(file)) {
            
            pw.printf("************** Inicio del registro: %s *********\n", new Date());
            pw.flush();

            while (!listo) {
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    System.out.println("Hilo interrumpido");
                    Thread.currentThread().interrupt();
                    return;
                }
                
                actualizarDisparos();
                imprimir(pw);
                if (comprobarInvariantes()) {
                    listo = true;
                }
            }
            pw.printf("Finalizando registro. Se han disparado %d invariantes.\n", cantidadInvariantes);
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void actualizarDisparos() {
    	if (transicionesDisparadas != null) {
    		transicionesDisparadas = red.getTransicionesDisparadas();
    	}
    }

    public void imprimir(PrintWriter pw) {
        if (transicionesDisparadas == null ) {
        	if(transicionesDisparadas.isEmpty()) {
        		pw.printf("No hay transiciones disparadas aún.\n");
        	}
     
        } else {
            pw.printf("Transiciones disparadas: %s\n", transicionesDisparadas.toString());
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