import re

RegExp = (
    r'(T0)((?:(?!T0).)*)(T1)((?:(?!T0).)*)(T3)((?:(?!T0).)*)(T4)((?:(?!T0).)*)(T7)((?:(?!T0).)*)(T8)((?:(?!T0).)*)(T11)|'
    r'(T0)((?:(?!T0).)*)(T1)((?:(?!T0).)*)(T3)((?:(?!T0).)*)(T4)((?:(?!T0).)*)(T6)((?:(?!T0).)*)(T9)((?:(?!T0).)*)(T10)((?:(?!T0).)*)(T11)|'
    r'(T0)((?:(?!T0).)*)(T1)((?:(?!T0).)*)(T2)((?:(?!T0).)*)(T5)((?:(?!T0).)*)(T7)((?:(?!T0).)*)(T8)((?:(?!T0).)*)(T11)|'
    r'(T0)((?:(?!T0).)*)(T1)((?:(?!T0).)*)(T2)((?:(?!T0).)*)(T5)((?:(?!T0).)*)(T6)((?:(?!T0).)*)(T9)((?:(?!T0).)*)(T10)((?:(?!T0).)*)(T11)'
)

grupos = ''

def check_invariants(contenido):
    total_invariantes = 0
    while True:
        match = re.search(RegExp, contenido)
        if not match:
            break
        invariante = match.group(0)
        contenido = contenido.replace(invariante, '', 1)
        total_invariantes += 1
    return contenido, total_invariantes


def main():
    with open("logTransiciones.txt", "r") as f:
        contenido = f.read().strip()

    resultado, total_eliminados = check_invariants(contenido)

    print(f"Total invariantes completos encontrados y eliminados: {total_eliminados}")

    if resultado == '':
        print("Todos los invariantes son correctos. No quedan transiciones sobrantes.")
    else:
        print("Quedan transiciones incompletas o sobrantes:")
        print(resultado)


if __name__ == "__main__":
    main()
