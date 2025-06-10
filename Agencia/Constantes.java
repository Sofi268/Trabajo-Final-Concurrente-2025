package Agencia;
public class Constantes {
    //Red de petri:
    public static final Integer[] marcadoInicial = {5, 1, 0, 0, 5, 0, 1, 1, 0, 0, 1, 0, 0, 0, 0};
    public static final Integer[][] matrizIncidencia = new Integer[][]{
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
    public static final int plazas = 15;
    public static final int NumTransiciones = 12;
    public static final Integer[] invarianteP1 = {1,2}; // P1 + P2 = 1
    public static final Integer[] invarianteP2 = {2,3,4}; // P2 + P3 + P4 = 5
    public static final Integer[] invarianteP3 = {5,6}; // P5 + P6 = 1
    public static final Integer[] invarianteP4 = {7,8}; // P7 + P8 = 1
    public static final Integer[] invarianteP5 = {10,11,12,13}; //P10 + P11 + P12 + P13 = 1
    public static final Integer[] invarianteP6 = {0,2,3,5,8,9,11,12,13,14}; // P0 + P2 + P3 + P5 + P8 + P9 + P11 + P12 + P13 + P14 = 5
    public static final int inv1 = 1;
    public static final int inv2 = 5;
    public static final int inv3 = 1;
    public static final int inv4 = 1;
    public static final int inv5 = 1;
    public static final int inv6 = 5;
    public static Integer[][] invariantes = {invarianteP1, invarianteP2, invarianteP3, invarianteP4, invarianteP5, invarianteP6};
    public static Integer[] invariantesPlazas = {inv1, inv2, inv3, inv4, inv5, inv6};

    //antiguas:
    public static final Integer cantidadGestoresReserva = 2; //encargados de la reserva.
    public static final Integer cantidadGestoresCaja = 1;  //encargados de la confirmacion/pago o de la cancelacion.
    public static final int[] conflictos = {2,3,6,7}; //NumTque no pueden dispararse al mismo tiempo. 
    public static final Integer[] tTemporales = {1,4,5,8,9,10}; //T4, T5, T6, T7, T8, T10, T11, T12
    public static final long ALFA = 20; // En ms
    public static final long BETA = 500;
}