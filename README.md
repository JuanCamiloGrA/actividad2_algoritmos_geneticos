# Actividad 2: Programación de algoritmos genéticos

Trabajo de la asignatura **Computación Bioinspirada** del grado de **Ingeniería Informática**.

## Descripción

Este proyecto implementa un algoritmo genético para resolver el problema del **cambio mínimo**: encontrar una combinación de monedas que represente exactamente un monto solicitado usando el menor número total de monedas.

La implementación fue reescrita en Java estándar a partir del ejemplo docente, conservando los elementos esenciales del algoritmo genético:

- Representación mediante cromosomas.
- Función de aptitud.
- Selección por torneo.
- Cruce de un punto.
- Mutación.
- Elitismo.
- Reparación de soluciones.

No se necesitan NetBeans, JGAP ni dependencias externas. El código puede editarse en Zed y ejecutarse desde su terminal integrada.

## Representación de las soluciones

Cada cromosoma contiene seis genes enteros no negativos. Cada posición indica la cantidad de monedas de una denominación:

| Gen | `g0` | `g1` | `g2` | `g3` | `g4` | `g5` |
|---|---:|---:|---:|---:|---:|---:|
| Valor | 100 ct | 50 ct | 25 ct | 10 ct | 5 ct | 1 ct |

Para un cromosoma `g`, el valor representado y el número total de monedas se calculan como:

```text
T(g) = sum(g_i * v_i)
N(g) = sum(g_i)
```

La función de aptitud prioriza que la suma coincida con el monto objetivo y, entre las soluciones exactas, favorece las que utilizan menos monedas:

```text
F(g) = max(1, 1 000 000 - 10 000 * abs(M - T(g)) - N(g))
```

Después del cruce o la mutación se aplica una reparación: si la combinación excede el objetivo, se eliminan monedas; si queda por debajo, se añaden denominaciones válidas hasta completarlo. La moneda de un centavo permite reparar cualquier monto entero del intervalo utilizado.

## Parámetros del algoritmo

| Parámetro | Valor | Propósito |
|---|---:|---|
| Población | 180 | Mantener diversidad sin elevar demasiado el tiempo de ejecución. |
| Generaciones máximas | 900 | Condición de parada de seguridad. |
| Élite | 6 | Preservar los mejores individuos. |
| Probabilidad de cruce | 90 % | Combinar información de los padres. |
| Probabilidad de mutación | 18 % | Introducir diversidad genética. |
| Tamaño del torneo | 4 | Aplicar presión selectiva moderada. |

## Operadores genéticos

1. **Selección por torneo:** se eligen cuatro individuos al azar y se conserva el de mayor aptitud como progenitor. El proceso se repite para obtener el segundo padre.
2. **Cruce de un punto:** el hijo recibe los genes anteriores al punto de corte del primer padre y los restantes del segundo.
3. **Mutación:** con una probabilidad del 18 %, se modifican uno o dos genes.
4. **Elitismo:** los seis mejores individuos pasan sin cambios a la siguiente generación.

## Estructura principal

- `src/AlgoritmoGeneticoCambio.java`: núcleo del algoritmo genético y ejecución de los casos de prueba.
- `src/InterfazCambio.java`: interfaz gráfica Swing para mostrar una solución.
- `capturas/`: capturas de código, compilación, resultados e interfaz utilizadas como evidencias del informe.
- `assets/`: ubicación opcional para el logo y la firma de la portada del informe.
- `out/`: directorio generado para las clases compiladas.

## Compilación y ejecución

Desde la raíz del proyecto, en la terminal integrada de Zed:

```bash
mkdir -p out && javac -encoding UTF-8 -d out src/*.java
```

Para ejecutar los tres casos de prueba reproducibles:

```bash
java -cp out AlgoritmoGeneticoCambio --demo
```

## Resultados

Se probaron los montos **353**, **155** y **263** centavos, utilizando una semilla distinta para cada caso. En los tres casos, el algoritmo alcanzó el mínimo exacto después de 40 generaciones:

| Monto | Cromosoma | Monedas | Óptimo | Estado |
|---:|---|---:|---:|---|
| 353 ct | `[3, 1, 0, 0, 0, 3]` | 7 | 7 | Óptimo |
| 155 ct | `[1, 1, 0, 0, 1, 0]` | 3 | 3 | Óptimo |
| 263 ct | `[2, 1, 0, 1, 0, 3]` | 7 | 7 | Óptimo |

Las semillas utilizadas fueron:

- 353 centavos: `20261184`
- 155 centavos: `20260986`
- 263 centavos: `20261094`

El número de monedas se compara al final con un mínimo exacto calculado mediante programación dinámica. Esta técnica se utiliza únicamente como verificación y no para generar la población.

## Interfaz gráfica

`InterfazCambio.java` proporciona una interfaz Swing similar a la del material docente y permite visualizar la solución para un monto, incluyendo el caso de 353 centavos.

## Conclusión

El experimento muestra que un algoritmo genético puede encontrar el cambio mínimo mediante una población de soluciones, evaluación, selección, cruce, mutación, elitismo y reparación. Las tres ejecuciones estudiadas obtuvieron soluciones exactas y óptimas. La implementación utiliza Java estándar y puede ejecutarse desde Zed sin instalar NetBeans ni librerías adicionales.
