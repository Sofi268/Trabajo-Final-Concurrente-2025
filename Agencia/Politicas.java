/**
 * @file Politicas.java
 * @brief Clase que implementa diferentes politicas para el disparo de transiciones en la Red de Petri
 *
 * Controla cuándo se puede disparar cada transicion segun la politica seleccionada (Balanceada o Prioridad)
 */
package Agencia;

public class Politicas {
    private static Politicas uniqueInstance;
    private static String nombre;
    private static int cantDisparosT2;
    private static int cantDisparosT3;
    private static int cantDisparosT6;
    private static int cantDisparosT7;
    private int tipoPolitica;

    private boolean permitirT2;
    private boolean permitirT3;
    private boolean permitirT6;
    private boolean permitirT7;

    public Politicas(String politica) {
        nombre = politica;
        cantDisparosT2 = 0;
        cantDisparosT3 = 0;
        cantDisparosT6 = 0;
        cantDisparosT7 = 0;

        permitirT2 = true;
        permitirT3 = false;
        permitirT6 = true;
        permitirT7 = false;

        if (politica.equals("Balanceada")) iniciarPoliticaBalanceada();
        else if (politica.equals("Prioridad")) iniciarPoliticaPrioridad();
        else System.out.println("No existe esa politica");
    }

    /**
     * @brief Retorna la instancia única de Politicas (singleton).
     * 
     * @param politica nombre de la política a instanciar
     * @return instancia única de Politicas
     */
    public static Politicas getInstance(String politica) { //mejorar:  hacer con un enum.
        if (uniqueInstance == null){
            System.out.println("Instanciando Politicas...");
            uniqueInstance = new Politicas(politica);
        } else {
            System.out.println("Ya existe una instancia de políticas");
        }
        return uniqueInstance;
    }

    // Inicializa política balanceada
    private void iniciarPoliticaBalanceada(){
        tipoPolitica = 1;
    }

    // Inicializa política de prioridad
    private void iniciarPoliticaPrioridad(){
        tipoPolitica = 2;
    }

    /**
     * @brief Consulta si se puede disparar una transicion segun la politica activa
     * 
     * @param t numero de transicion
     * @return true si esta permitido disparar, false en caso contrario
     */
    public boolean sePuedeDisparar(int t) {
        switch (t) {
            case 2: return manejarT2();
            case 3: return manejarT3();
            case 6: return manejarT6();
            case 7: return manejarT7();
            default: return true;
        }
    }
    //---------------------------------------------------------------------------------------------------------
    // Metodos para manejo y actualizacion individual de cada transición (T2, T3, T6, T7)

    private boolean manejarT2() {
        if (!permitirT2) return false;
        return true;
    }

    private boolean manejarT3() {
        if (!permitirT3) return false;
        return true;
    }

    private boolean manejarT6() {
        if (!permitirT6) return false;
        return true;
    }

    private boolean manejarT7() {
        if (!permitirT7) return false;
        return true;
    }

    public void actualizarPolitica(int t){
        switch (t) {
            case 2: actualizarT2(); break;
            case 3: actualizarT3(); break;
            case 6: actualizarT6(); break;
            case 7: actualizarT7(); break;
            default: return; // No se actualiza nada para otras transiciones
        }
    }

    private void actualizarT2(){
        cantDisparosT2++;

        if (tipoPolitica == 1 && cantDisparosT2 >= cantDisparosT3) {
            permitirT3 = true;
            permitirT2 = false;
        }
        if (tipoPolitica == 2 && cantDisparosT2 % 3 == 0) { // 75%
            permitirT3 = true;
            permitirT2 = false;
        }
    }

    private void actualizarT3(){
        cantDisparosT3++;

        permitirT2 = true;
        permitirT3 = false;
    }     

    private void actualizarT6(){
        cantDisparosT6++;

        if (tipoPolitica == 1 && cantDisparosT6 >= cantDisparosT7) {
            permitirT7 = true;
            permitirT6 = false;
        }
        if (tipoPolitica == 2 && cantDisparosT6 % 4 == 0) { // 80%
            permitirT7 = true;
            permitirT6 = false;
        }
    }

    private void actualizarT7(){
        cantDisparosT7++;

        permitirT6 = true;
        permitirT7 = false;
    }

    //---------------------------------------------------------------------------------------------------------

    /**
     * @brief Obtiene el nombre de la política activa.
     * 
     * @return nombre de la política
     */
    public String getNombre() {
        return nombre;
    }    

    public int getDisparosT2() {
    	return cantDisparosT2;
    }
    
    public int getDisparosT3() {
    	return cantDisparosT3;
    }
    
    public int getDisparosT6() {
    	return cantDisparosT6;
    }
    
    public int getDisparosT7() {
    	return cantDisparosT7;
    }
}