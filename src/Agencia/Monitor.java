/*Monitor: en la clase encargada de...
* */
package Agencia;

//import java.util.concurrent.Semaphore;

public class Monitor implements MonitorInterface{
	private static final RedDePetri rdp = RedDePetri.getInstance();  //traemos la red de petri a nuestro monitor.
	private static Monitor uniqueInstance;
//	private static Politica politica;
//	private static Semaphore mutex;
//	private static Semaphore[] colasCondicion;

	/**
	 * Aplicando el patron Singleton nos aseguramos que solo exista 1 monitor en nuestro programa.
	 * @return Monitor
	 */
	public static Monitor getInstance(){
		if(uniqueInstance == null){
			uniqueInstance = new Monitor();
		}else{
			System.out.println("Ya existe una instancia de Monitor");
		}
		return uniqueInstance;
	}

	/* Constructor basico (debe estar vacio) */
	public Monitor() {}

	/*  prueba de sofi...
	mutex = new Semaphore(1);
	setPolitica("Balanceada");
		 for (int i = 0; i < redPetri.getTransiciones(); i++) {
             colasCondicion[i] =  new Semaphore(0);
    }*/

	@Override
	public boolean fireTransition(int transition) {
		if (rdp.isSensible(transition)) {
			System.out.println("La transicion se puede disparar");
			rdp.disparar(transition);
			
			System.out.println("La transicion fue disparada");
			return true;
		}
		System.out.println("No es posible disparar la transicion " + transition);
		return false;
	}
	
//
//	private void setPolitica(String politicaElegida) {
//		if(politicaElegida.equals("Prioridad")){
//			 politica = new PoliticaPrioridad();
//		}
//		else politica = new PoliticaBalanceada();
//	}

}
