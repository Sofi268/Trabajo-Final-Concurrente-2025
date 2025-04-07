package Agencia;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FinDelPrograma implements Runnable {
	private RedDePetri red = RedDePetri.getInstance();
	private Thread[] hilos;
	private boolean fin;
	Thread estadisticas;
	private static ArrayList<Integer> transicionesDisparadas;
	private boolean aprobado = false;
    private String expresionRegular;
    private static String cadena;
    private static int cantidadInvariantes;
	
	public FinDelPrograma(Thread[] hilos, Thread stats) {
		this.hilos = hilos;
		fin = false;
		estadisticas = stats;
		expresionRegular = "(T0)(.*?)(T1)(.*?)(T3)(.*?)(T4)(.*?)(T7)(.*?)(T8)(.*?)(T11)|" +
                "(T0)(.*?)(T1)(.*?)(T3)(.*?)(T4)(.*?)(T6)(.*?)(T9)(.*?)(T10)(.*?)(T11)|" +
                "(T0)(.*?)(T1)(.*?)(T2)(.*?)(T5)(.*?)(T7)(.*?)(T8)(.*?)(T11)|" +
                "(T0)(.*?)(T1)(.*?)(T2)(.*?)(T5)(.*?)(T6)(.*?)(T9)(.*?)(T10)(.*?)(T11)";
        cantidadInvariantes = 0;
	}
	
	@Override
	public void run() {
		System.out.println("Inicio de FinDelPrograma");
		while(!fin) {			
			try {
	            TimeUnit.MILLISECONDS.sleep(50);
	        } catch (InterruptedException e) {
	            System.out.println("Hilo de finalizacion interrumpido");
	            Thread.currentThread().interrupt();
	            return;
	        }
	        comprobarFin();
		}
		
		red.getTransicionesDisparadas();
		System.out.println("\n \n \n \n ESTOY INTERRUMPIENDO TODOS LOS HILOS\n \n \n");
		for(Thread x :hilos) {
			x.interrupt();
		}
		
//		comprobarInvariantesTransicion();		
//		if(aprobado)System.out.println("Se cumplieron los 186 invariantes. Fin del programa");
//		else System.out.println("No se cumplieron los invariantes. Fin del programa");
	}

	private void comprobarInvariantesTransicion() {
		cantidadInvariantes = contarInvariantes(transicionesDisparadas, expresionRegular);
		//hacer replaceAll() para que el transiciones Disparadas quede vacio
		if(cantidadInvariantes ==186) aprobado=true;		
	}

	private void comprobarFin() {
		fin = red.finT11();
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
