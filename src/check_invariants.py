import re
import sys

# reemplazos_previos = {
#     'T10': 'A',
#     'T11': 'B',
# }

with open("logTransiciones.txt") as file:
    texto = file.read()

# for trans, letra in reemplazos_previos.items():
#     texto = texto.replace(trans, letra)

RegExp = r'(T0)(.*?)(T1)(.*?)((T2)(.*?)(T5)(.*?)|(T3)(.*?)(T4)(.*?))((T6)(.*?)(T9)(.*?)(T10)|(T7)(.*?)(T8))(.*?)(T11)'

cantidad_invariantes = 0
res = (texto, 1)

while True:
    res = re.subn(RegExp, '', res[0]) 
    cantidad_invariantes += res[1]
    if res[1] == 0:
        break

print(f"Cantidad de invariantes que se cumplieron: {cantidad_invariantes}")
if res[0] == '':
   print(" Todos los invariantes fueron reconocidos sin residuos.")
else:
   print("Transiciones fuera de secuencia:\n" + res[0])

print(f"INVARIANTES={cantidad_invariantes}")
 

