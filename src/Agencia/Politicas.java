/**
 * @file Politicas.java
 * @brief Politicas para la determinacion de transiciones a ser disparadas estocasticamente ante conflictos estructurales
 */
package Agencia;

public class Politicas {
    private static Politicas uniqueInstance;
    
    public Politicas() {}
    
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
     * Aplicando patron de diseño singleton nos aseguramos que solo haya 1 politica activa.
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

    /* inicializar Balanceada
    * */
    public void iniciarPoliticaBalanceada(){
        // TODO
    }
    
    /* inicializar politica con Prioridad
     * */
     public void iniciarPoliticaPrioridad(){
         // TODO
     }
    
}