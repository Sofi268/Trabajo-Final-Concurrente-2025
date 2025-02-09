import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Monitor implements MonitorInterface {
    private final Lock lock = new ReentrantLock();   // Declaración de un objeto Lock para manejar la sincronización
    private static Monitor instance;     // Instancia única de la clase Monitor

    private Monitor() {} // Constructor privado para evitar la instanciación directa
     
    // Método estático para obtener la instancia única de la clase Monitor
    public static Monitor getInstance() {
        if (instance == null) {
            synchronized (Monitor.class) {
                if (instance == null) {
                    instance = new Monitor();
                }
            }
        }
        return instance;
    }

    // Implementación del método fireTransition de la interfaz MonitorInterface
    @Override
    public boolean fireTransition(int transition) {
        // Adquiere el bloqueo para asegurar la exclusión mutua
        lock.lock();
        try {
            // Imprime un mensaje indicando la transición que se está ejecutando
            System.out.println("Ejecutando transición: T" + transition);
            // Retorna true indicando que la transición se ejecutó correctamente
            return true;
        } catch (Exception e) { // Captura cualquier excepción que ocurra durante la ejecución de la transición.
            e.printStackTrace();
            return false;    
        } finally {
            // Libera el bloqueo para permitir que otros hilos puedan adquirirlo
            lock.unlock();
        }
    }
}