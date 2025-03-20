package Agencia;

public class Monitor implements MonitorInterface{
	private RedDePetri redPetri;
	public Monitor(RedDePetri red) {
		 redPetri = red;
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

}

