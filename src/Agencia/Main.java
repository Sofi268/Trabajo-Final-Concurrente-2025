package Agencia;
import java.util.*;

public class Main {
	
	public static void main(String[] args){
		
		RedDePetri red = new RedDePetri();
		Monitor monitor = new Monitor(red);
		monitor.fireTransition(0);
		monitor.fireTransition(1);
		monitor.fireTransition(3);
		monitor.fireTransition(1);
		monitor.fireTransition(4);
	}
}
