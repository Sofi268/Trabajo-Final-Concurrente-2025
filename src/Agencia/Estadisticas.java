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
    private static int cantidadInvariantes;
	private int porcentajeT2;
	private int porcentajeT3;
    private int porcentajeT6;
	private int porcentajeT7;
	private String expresionRegular;
	private static String cadena; 
	

    public Estadisticas(RedDePetri redPetri) {
        transicionesDisparadas = new ArrayList<>();
        red = redPetri;
        cantidadInvariantes = 0;
        porcentajeT2 = 0;
        porcentajeT3 = 0;
        porcentajeT6 = 0;
        porcentajeT7 = 0;
        expresionRegular = "(T0)(.*?)(T1)(.*?)(T3)(.*?)(T4)(.*?)(T7)(.*?)(T8)(.*?)(T11)|" +
                "(T0)(.*?)(T1)(.*?)(T3)(.*?)(T4)(.*?)(T6)(.*?)(T9)(.*?)(T10)(.*?)(T11)|" +
                "(T0)(.*?)(T1)(.*?)(T2)(.*?)(T5)(.*?)(T7)(.*?)(T8)(.*?)(T11)|" +
                "(T0)(.*?)(T1)(.*?)(T2)(.*?)(T5)(.*?)(T6)(.*?)(T9)(.*?)(T10)(.*?)(T11)";
    }

    @Override
    public void run() {
        System.out.println("Hilo de estadísticas iniciado");
        try (FileWriter file = new FileWriter("log2.txt");
             PrintWriter pw = new PrintWriter(file)) {
            
            pw.printf("************** Inicio del registro: %s *********\n", new Date());
            pw.flush();

            while (cantidadInvariantes<186){
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    System.out.println("Hilo interrumpido");
                    Thread.currentThread().interrupt();
                    return;
                }
                
                actualizarDisparos();
                actualizarStats();
                cantidadInvariantes = contarInvariantes(transicionesDisparadas, expresionRegular);
                imprimir(pw);
            }
            pw.printf("Finalizando registro. Se han disparado %d invariantes.\n", cantidadInvariantes);
            pw.flush();
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
            pw.printf("La cantidad de invariantes disparados hasta el momento es: %d\n", cantidadInvariantes);

        }
        pw.flush();
    }
    
	private static int contarInvariantes(ArrayList<Integer> transicionesD, String expresionRegular) {
	
	    cadena = convertirACadena(transicionesD).toString();
	
	    Pattern pattern = Pattern.compile(expresionRegular);
	    Matcher matcher = pattern.matcher(cadena);
	
	    int cantidad = 0;
	    while (matcher.find()) {
	        cantidad++;
	    }
	    
	    return cantidad;
    }
	
	private static StringBuilder convertirACadena(ArrayList<Integer> transicionesD) {
		StringBuilder sb = new StringBuilder();
	    for (int num : transicionesD) {
	        sb.append("T").append(num);
	    }
	    return sb;
	}
    
}
