package Agencia;
public class Constantes {
    //Red de petri:


    //antiguas:
    public static final Integer cantidadGestoresReserva = 2; //encargados de la reserva.
    public static final Integer cantidadGestoresCaja = 1;  //encargados de la confirmacion/pago o de la cancelacion.
    public static final int[] conflictos = {2,3,6,7}; //transiciones que no pueden dispararse al mismo tiempo. 
    public static final Integer[] tTemporales = {1,4,5,8,9,10}; //T4, T5, T6, T7, T8, T10, T11, T12
    public static final Integer cantidadTransiciones = 12; //cantidad de transiciones de la red de petri.
    public static final long ALFA = 20; // En ms
    public static final long BETA = 500;
}