/**
 * @file Estadisticas.java
 * @brief Crea y actualiza un log con las transiciones disparadas de la red
 */
package Agencia;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Estadisticas implements Runnable{
	private ArrayList<Integer> transicionesDisparadas;
	private boolean listo;
	private static RedDePetri red;
	private static int cantidadInvariantes;

	
	/**
	 * @brief Inicia el arreglo para almacenar las transiciones disparadas 
	 */
	public Estadisticas(RedDePetri redPetri) {
		transicionesDisparadas = new ArrayList<Integer>();
		listo = false;
		red = redPetri;
		cantidadInvariantes = 0;
	}
	
    @Override
    public void run() {
        try(FileWriter file = new FileWriter("log.txt");PrintWriter pw = new PrintWriter(file);){
            pw.printf("************** Inicio del registro: %s ********* ", new Date());
            while (!listo) {
                try { TimeUnit.MILLISECONDS.sleep(500); } catch (InterruptedException e) { throw new RuntimeException(e); }
                actualizarDisparos();
                if(comprobarInvariantes()) listo = true;
                imprimir(pw);            
            }
        }catch (IOException e){ }
    }
	
	/**
	 * @brief Agrega la T a la lista de transiciones disparadas 
	 */
	public void actualizarDisparos() {
		transicionesDisparadas = red.getTransicionesDisparadas();
	}
	
	public void imprimir(PrintWriter pw){
        while(transicionesDisparadas!= null) {
        	pw.printf("------------------------------------------------------------------------\n");

            if(listo){
            	pw.printf("Se han disparado %d invarintes", cantidadInvariantes);
            	//TODO finalizar programa
            }
        }
    }
	
	public boolean comprobarInvariantes() {
		return getCantInvariantes()>=186 ? true : false;
	}
	
	public int getCantInvariantes() {
		int invariantes = 0;
		//TODO
		return invariantes;
	}
}

