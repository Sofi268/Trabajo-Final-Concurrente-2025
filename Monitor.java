package Agencia;

//import java.util.concurrent.Semaphore;

public class Monitor implements MonitorInterface{
	private static RedDePetri redPetri;
//	private static Politica politica;
//	private static Semaphore mutex;
//	private static Semaphore[] colasCondicion;
	
	public Monitor(RedDePetri red) {
		
		 redPetri = red;
//		 mutex = new Semaphore(1);
		 //setPolitica("Balanceada");
//		 for (int i = 0; i < redPetri.getTransiciones(); i++) {
//             colasCondicion[i] =  new Semaphore(0);
//         }
	}
	
	@Override
	public boolean fireTransition(int transition) {
		if (redPetri.isSensible(transition)) {
			System.out.println("La transicion se puede disparar");
			redPetri.disparar(transition);
			
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
