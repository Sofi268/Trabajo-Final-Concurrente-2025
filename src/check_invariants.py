import re
import sys

reemplazos_previos = {
    'T10': 'A',
    'T11': 'B',
}

with open("logTransiciones.txt") as file:
    texto = file.read()

for trans, letra in reemplazos_previos.items():
    texto = texto.replace(trans, letra)

RegExp = r'(.*?)(T0)(.*?)(T1)(.*?)((T2)(.*?)(T5)|(T3)(.*?)(T4))(.*?)((T6)(.*?)(T9)(.*?)(A)|(T7)(.*?)(T8))(.*?)(B)'
grupos = (r'\g<1>\g<3>\g<5>\g<8>\g<11>\g<13>\g<16>\g<18>\g<21>\g<23>')
cantidad_invariantes = 0
res = (texto, 1)

while True:
    res = re.subn(RegExp, grupos, res[0]) 
    cantidad_invariantes += res[1]
    if res[1] == 0:
        break

print(f"Cantidad de invariantes que se cumplieron: {cantidad_invariantes}")
if res[0] == '':
   print(" Todos los invariantes fueron reconocidos sin residuos.")
else:
   print("Transiciones fuera de secuencia:\n" + res[0])

print(f"INVARIANTES={cantidad_invariantes}")
