/**
 * @file Politicas.java
 * @brief Politicas para la determinacion de transiciones a ser disparadas estocasticamente ante conflictos estructurales
 */
package Agencia;

public class Politicas {
    private static Politicas uniqueInstance;
    
    /**
     * @brief Constructor vacio
     */
    public Politicas() {}
    /**
     * @brief Aplicando patron de diseño singleton nos aseguramos que solo haya 1 politica activa.
     * @return Políticas
     */
    public static Politicas getInstance(String politica){
        if (uniqueInstance == null){
            uniqueInstance = new Politicas(politica);
        } else {
            System.out.println("Ya existe una instancia de políticas");
        }
        return uniqueInstance;
    }
    /**
     * @brief Determina la creacion de la politica elegida
     * @param politica a implementar
     */
    public Politicas(String politica) {
    	if(politica.equals("Balanceada")) iniciarPoliticaBalanceada();
    	else {
    		if(politica.equals("Prioridad")) iniciarPoliticaBalanceada();
    		else {
    			System.out.println("No existe esa politica");
    		}
    	}
    }

    /**
     * @brief Crea instancia de Politica Balanceada
     */
    public void iniciarPoliticaBalanceada(){
        // TODO
    }
    
    /**
     * @brief Crea instancia de Politica con Prioridad
     */
     public void iniciarPoliticaPrioridad(){
         // TODO
     }
    
}