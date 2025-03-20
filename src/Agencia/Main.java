package Agencia;
import java.util.*;

public class Main {
	
	public static void main(String[] args){
		
		Monitor monitor = new Monitor();
		monitor.fireTransition(0);
		monitor.fireTransition(1);
		monitor.fireTransition(3);
	}
}

