// Plaza: Representa las plazas de la Red de Petri.
public class Plaza {
    private int id;// Identificador único de la plaza.
    private int tokens; // Cantidad de tokens en la plaza.

    // Constructor de la clase Plaza que inicializa el id y los tokens iniciales.
    public Plaza(int id, int tokensIniciales) {
        this.id = id; // Asigna el identificador único a la plaza.
        this.tokens = tokensIniciales; // Asigna la cantidad inicial de tokens a la plaza.
    }

    // Método para agregar un token a la plaza.
    public void agregarToken() {
        tokens++; // Incrementa en uno la cantidad de tokens.
    }

    // Método para remover un token de la plaza.
    public void removerToken() {
        if (tokens > 0) tokens--; // Decrementa en uno la cantidad de tokens si hay tokens disponibles.
        else System.out.println("Error: No hay tokens disponibles en la plaza " + id); // Imprime un mensaje de error si no hay tokens disponibles.
    }

    // Método para obtener la cantidad actual de tokens en la plaza.
    public int getTokens() {
        return tokens; // Retorna la cantidad de tokens.
    }
}