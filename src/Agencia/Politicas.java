package Agencia;

public class Politicas {
    private static Politicas uniqueInstance;
    private static String nombre;
    private static int cantDisparosT2;
    private static int cantDisparosT3;
    private static int cantDisparosT6;
    private static int cantDisparosT7;
    private int tipoPolitica;

    private boolean permitirT2;
    private boolean permitirT3;
    private boolean permitirT6;
    private boolean permitirT7;

    public Politicas() {}

    public Politicas(String politica) {
        nombre = politica;
        cantDisparosT2 = 0;
        cantDisparosT3 = 0;
        cantDisparosT6 = 0;
        cantDisparosT7 = 0;

        permitirT2 = true;
        permitirT3 = false;
        permitirT6 = true;
        permitirT7 = false;

        if (politica.equals("Balanceada")) iniciarPoliticaBalanceada();
        else if (politica.equals("Prioridad")) iniciarPoliticaPrioridad();
        else System.out.println("No existe esa politica");
    }

    public static Politicas getInstance(String politica){
        if (uniqueInstance == null){
            uniqueInstance = new Politicas(politica);
        } else {
            System.out.println("Ya existe una instancia de políticas");
        }
        return uniqueInstance;
    }

    private void iniciarPoliticaBalanceada(){
        tipoPolitica = 1;
    }

    private void iniciarPoliticaPrioridad(){
        tipoPolitica = 2;
    }

    public boolean sePuedeDisparar(int t) {
        switch (t) {
            case 2: return manejarT2();
            case 3: return manejarT3();
            case 6: return manejarT6();
            case 7: return manejarT7();
            default: return true;
        }
    }
    
    private boolean manejarT2() {
        if (!permitirT2) return false;
        return true;
    }

    private boolean manejarT3() {
        if (!permitirT3) return false;
        return true;
    }

    private boolean manejarT6() {
        if (!permitirT6) return false;
        return true;
    }

    private boolean manejarT7() {
        if (!permitirT7) return false;
        return true;
    }

    public void actualizarT2(){
        cantDisparosT2++;

        if (tipoPolitica == 1 && cantDisparosT2 >= cantDisparosT3) {
            permitirT3 = true;
            permitirT2 = false;
        }
        if (tipoPolitica == 2 && cantDisparosT2 % 3 == 0) { // 75%
            permitirT3 = true;
            permitirT2 = false;
        }
    }
    public void actualizarT3(){
        cantDisparosT3++;

        permitirT2 = true;
        permitirT3 = false;
    }       
    public void actualizarT6(){
        cantDisparosT6++;

        if (tipoPolitica == 1 && cantDisparosT6 >= cantDisparosT7) {
            permitirT7 = true;
            permitirT6 = false;
        }
        if (tipoPolitica == 2 && cantDisparosT6 % 4 == 0) { // 80%
            permitirT7 = true;
            permitirT6 = false;
        }
    }
    public void actualizarT7(){
        cantDisparosT7++;

        permitirT6 = true;
        permitirT7 = false;
    }

    public String getNombre() {
        return nombre;
    }    
}

