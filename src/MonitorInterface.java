// Definición de una interfaz pública llamada MonitorInterface.
public interface MonitorInterface {
    // Declaración de un método abstracto que toma un entero como parámetro y devuelve un booleano
    // Este método se utiliza para disparar una transición específica en un monitor
    boolean fireTransition(int transition);
}