import re

# Reemplazos previos para evitar conflicto con \g<10> etc.
reemplazos_previos = {
    'T10': 'A',
    'T11': 'B',
}

# Leer log original
with open("logTransiciones.txt") as file:
    texto = file.read()

# Aplicar reemplazos
for trans, letra in reemplazos_previos.items():
    texto = texto.replace(trans, letra)

# Expresión regular con letras en lugar de T10-T11
RegExp = r'(T0)(.*?)(T1)(.*?)((T2)(.*?)(T5)(.*?)|(T3)(.*?)((T4)(.*?)))((T6)(.*?)(T9)(.*?)(A)|(T7)(.*?)(T8))(.*?)(B)'

cantidad_invariantes = 0
res = (texto, 1)  # Inicializar con el texto original

while True:
    res = re.subn(RegExp, '', res[0])  # Reemplazo con cadena vacía para eliminar la secuencia completa
    cantidad_invariantes += res[1]
    if res[1] == 0:
        break

print(f"\nCantidad de invariantes que se cumplieron: {cantidad_invariantes}\n")

if res[0] == '':
    print("✅ Todos los invariantes fueron reconocidos sin residuos.\n")
else:
    print("❌ Se encontraron transiciones fuera de una secuencia válida:\n" + res[0] + '\n')
