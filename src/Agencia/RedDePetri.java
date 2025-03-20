package Agencia;

public class RedDePetri {
    final int plazas = 15;
    final int transiciones = 12;
    private static final Integer[] marcadoInicial = {5, 1, 0, 0, 5, 0, 1, 1, 0, 0, 1, 0, 0, 0, 0};
    private static Integer[] marcadoActual;
    private static Integer[][] matrizIncidencia;
    private static Integer[] transicionSensible;
    private static Integer[] secuenciaDisparo;

    public RedDePetri() {
        marcadoActual = marcadoInicial.clone(); 
        System.out.println("Marcado inicial: " + java.util.Arrays.toString(marcadoActual));
        
        matrizIncidencia = new Integer[][]{
            {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {-1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0},
            {-1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 1, 0, 0, -1, 0, 0, 0, 0, 0, 0},
            {0, 0, -1, 0, 0, 1, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, -1, 1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, -1, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, -1, -1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, -1, -1, 1, 0, 1, 0},
            {0, 0, 0, 0, 0, 0, 1, 0, 0, -1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 1, -1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -1, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, -1}
        };
        transicionSensible = new Integer[transiciones];
        secuenciaDisparo = new Integer[transiciones]; 
        sensibilizarT();
    }
    
    // Actualiza el vector de las transiciones sensibilizadas
    private void sensibilizarT() {
        for (int j = 0; j < transiciones; j++) {
            boolean sensibilizada = true;

            // Verifica para cada plaza si tiene suficientes tokens para disparar la transición
            for (int i = 0; i < plazas; i++) {
                // Comprueba si la transición requiere más tokens de los que hay en la plaza
                if (matrizIncidencia[i][j] < 0 && marcadoActual[i] + matrizIncidencia[i][j] < 0) {
                    sensibilizada = false;
                    break;
                }
            }
            
            transicionSensible[j] = sensibilizada ? 1 : 0;
        }
        System.out.println("Transiciones sensibilizadas: " + java.util.Arrays.toString(transicionSensible));
    }

    
    public boolean isSensible(int t) {
        return transicionSensible[t] == 1;
    }
    
    public void disparar(int t) {
        if (!isSensible(t)) {
            System.out.println("La transición " + t + " no está sensibilizada.");
            return;
        }
        
        // Inicializa secuenciaDisparo con ceros y marcar solo la transición disparada
        for (int i = 0; i < transiciones; i++) {
            secuenciaDisparo[i] = (i == t) ? 1 : 0;
        }
        System.out.println("Disparando transición " + t);
        
        // Ecuación fundamental marcadoActual = marcadoInicial + matrizIncidencia * ssecuenciaDisparo
        for (int i = 0; i < plazas; i++) {
            int nuevoMarcado = marcadoActual[i]; 
            for (int j = 0; j < transiciones; j++) {
                nuevoMarcado += matrizIncidencia[i][j] * secuenciaDisparo[j];
            }
            marcadoActual[i] = nuevoMarcado; 
        }
        
        System.out.println("Marcado actual: " + java.util.Arrays.toString(marcadoActual));
        sensibilizarT(); // Recalcula transiciones sensibilizadas
    }

}

