/**
 * @file Constantes.java
 * @brief Contiene las constantes de configuración global del sistema de agencia.
 */
package Agencia;

public class Constantes {
    public static final Integer cantidadGestoresReserva = 2; //encargados de la reserva.
    public static final Integer cantidadGestoresCaja = 1;  //encargados de la confirmacion/pago o de la cancelacion.
    public static final int[] conflictos = {2,3,6,7}; //transiciones que no pueden dispararse al mismo tiempo. 
    public static final Integer[] tTemporales = {1,4,5,8,9,10}; //T4, T5, T6, T7, T8, T10, T11, T12
    public static final Integer cantidadTransiciones = 12; //cantidad de transiciones de la red de petri.
    public static final Integer cantidadPlazas = 15;//cantidad de plazas de la red de petri.
    public static final Integer[] marcadoI = {5, 1, 0, 0, 5, 0, 1, 1, 0, 0, 1, 0, 0, 0, 0};
    public static final Integer[] invarP1 = {1,2}; // P1 + P2 = 1
    public static final Integer[] invarP2 = {2,3,4}; // P2 + P3 + P4 = 5
    public static final Integer[] invarP3 = {5,6}; // P5 + P6 = 1
    public static final Integer[] invarP4 = {7,8}; // P7 + P8 = 1
    public static final Integer[] invarP5 = {10,11,12,13}; //P10 + P11 + P12 + P13 = 1
    public static final Integer[] invarP6 = {0,2,3,5,8,9,11,12,13,14}; // P0 + P2 + P3 + P5 + P8 + P9 + P11 + P12 + P13 + P14 = 5
    public static final int in1 = 1;
    public static final int in2 = 5;
    public static final int in3 = 1;
    public static final int in4 = 1;
    public static final int in5 = 1;
    public static final int in6 = 5;
    public static final long ALFA = 5; // En ms
    public static final long BETA = 5000; // En ms
}