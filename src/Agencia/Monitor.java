package Agencia;

public class Monitor implements MonitorInterface{
	private RedDePetri red;
	public Monitor() {
		 red = new RedDePetri();
	}
	
	@Override
	public boolean fireTransition(int transition) {
		if (red.isSensible(transition)) {
			System.out.println("La transicion se puede disparar");
			red.disparar(transition);
			
			System.out.println("La transicion fue disparada");
			return true;
		}
		System.out.println("No es posible disparar la transicion");
		return false;
	}

}
