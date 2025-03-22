package Agencia;

public class Cliente implements Runnable{
	private int id;
	
	public Cliente(int i) {
		id = i;
	}
	
	public int getId() {
		return id;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
