import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Algoritmo genetico para el problema del cambio minimo.
 *
 * <p>El cromosoma contiene seis genes. Cada gen representa la cantidad de
 * monedas de 100, 50, 25, 10, 5 y 1 centavos, respectivamente. Esta version
 * conserva la idea del ejemplo JGAP del enunciado, pero usa solamente Java
 * estandar para poder ejecutarse directamente desde Zed.</p>
 */
public final class AlgoritmoGeneticoCambio {
    private static final int[] VALORES = {100, 50, 25, 10, 5, 1};
    private static final String[] NOMBRES = {
        "100 centavos", "50 centavos", "25 centavos",
        "10 centavos", "5 centavos", "1 centavo"
    };

    private static final int TAMANO_POBLACION = 180;
    private static final int MAX_GENERACIONES = 900;
    private static final int MIN_GENERACIONES = 40;
    private static final int ELITE = 6;
    private static final int TAMANO_TORNEO = 4;
    private static final double PROBABILIDAD_CRUCE = 0.90;
    private static final double PROBABILIDAD_MUTACION = 0.18;
    private static final long SEMILLA_PREDETERMINADA = 20_260_831L;

    private AlgoritmoGeneticoCambio() {
    }

    /** Resultado inmutable de una ejecucion. */
    public record Resultado(
            int objetivo,
            int[] genes,
            int total,
            int numeroMonedas,
            int optimoExacto,
            int generaciones,
            long semilla) {

        public Resultado {
            genes = genes.clone();
        }

        @Override
        public int[] genes() {
            return genes.clone();
        }

        public boolean esOptimo() {
            return total == objetivo && numeroMonedas == optimoExacto;
        }

        public String resumen() {
            StringBuilder salida = new StringBuilder();
            salida.append("Monto objetivo: ").append(objetivo).append(" centavos\n");
            salida.append("Semilla: ").append(semilla).append('\n');
            salida.append("Generaciones ejecutadas: ").append(generaciones).append('\n');
            salida.append("Mejor cromosoma: ").append(Arrays.toString(genes)).append('\n');
            for (int i = 0; i < genes.length; i++) {
                salida.append(String.format("  %2d moneda(s) de %s%n", genes[i], NOMBRES[i]));
            }
            salida.append("Total representado: ").append(total).append(" centavos\n");
            salida.append("Numero total de monedas: ").append(numeroMonedas).append('\n');
            salida.append("Optimo exacto de referencia: ").append(optimoExacto).append(" monedas\n");
            salida.append("Comprobacion: ").append(esOptimo() ? "SOLUCION OPTIMA" : "SOLUCION FACTIBLE");
            return salida.toString();
        }
    }

    private static final class Individuo {
        private final int[] genes;
        private double aptitud;

        private Individuo(int[] genes) {
            this.genes = genes;
        }

        private Individuo copia() {
            Individuo copia = new Individuo(genes.clone());
            copia.aptitud = aptitud;
            return copia;
        }
    }

    /** Ejecuta el algoritmo genetico para un monto y una semilla determinados. */
    public static Resultado resolver(int objetivo, long semilla, boolean detallado) {
        validarObjetivo(objetivo);
        Random aleatorio = new Random(semilla);
        int optimoExacto = minimoMonedasDinamico(objetivo);

        List<Individuo> poblacion = new ArrayList<>(TAMANO_POBLACION);
        for (int i = 0; i < TAMANO_POBLACION; i++) {
            Individuo individuo = crearIndividuoAleatorio(objetivo, aleatorio);
            evaluar(individuo, objetivo);
            poblacion.add(individuo);
        }

        Comparator<Individuo> porAptitud = Comparator.comparingDouble(
                (Individuo individuo) -> individuo.aptitud).reversed();
        poblacion.sort(porAptitud);
        Individuo mejorGlobal = poblacion.get(0).copia();
        int generacionFinal = 0;

        if (detallado) {
            imprimirProgreso(0, mejorGlobal, objetivo);
        }

        for (int generacion = 1; generacion <= MAX_GENERACIONES; generacion++) {
            List<Individuo> siguiente = new ArrayList<>(TAMANO_POBLACION);

            // Elitismo: los mejores pasan sin cambios a la siguiente generacion.
            poblacion.sort(porAptitud);
            for (int i = 0; i < ELITE; i++) {
                siguiente.add(poblacion.get(i).copia());
            }

            while (siguiente.size() < TAMANO_POBLACION) {
                Individuo padre1 = seleccionarPorTorneo(poblacion, aleatorio);
                Individuo padre2 = seleccionarPorTorneo(poblacion, aleatorio);
                Individuo hijo = cruzar(padre1, padre2, aleatorio);
                mutar(hijo, objetivo, aleatorio);
                reparar(hijo.genes, objetivo, aleatorio);
                evaluar(hijo, objetivo);
                siguiente.add(hijo);
            }

            poblacion = siguiente;
            poblacion.sort(porAptitud);
            if (poblacion.get(0).aptitud > mejorGlobal.aptitud) {
                mejorGlobal = poblacion.get(0).copia();
            }
            generacionFinal = generacion;

            if (detallado && (generacion % 20 == 0 || generacion == 1)) {
                imprimirProgreso(generacion, mejorGlobal, objetivo);
            }

            // Se ejecuta un minimo de generaciones para que la evolucion sea observable.
            if (generacion >= MIN_GENERACIONES
                    && contarMonedas(mejorGlobal.genes) == optimoExacto
                    && calcularTotal(mejorGlobal.genes) == objetivo) {
                break;
            }
        }

        return new Resultado(
                objetivo,
                mejorGlobal.genes,
                calcularTotal(mejorGlobal.genes),
                contarMonedas(mejorGlobal.genes),
                optimoExacto,
                generacionFinal,
                semilla);
    }

    private static Individuo crearIndividuoAleatorio(int objetivo, Random aleatorio) {
        int[] genes = new int[VALORES.length];
        int restante = objetivo;

        // Se crean combinaciones exactas y diversas; el gen de 1 centavo garantiza factibilidad.
        for (int i = 0; i < VALORES.length - 1; i++) {
            int maximo = restante / VALORES[i];
            int cantidad = maximo == 0 ? 0 : aleatorio.nextInt(maximo + 1);
            genes[i] = cantidad;
            restante -= cantidad * VALORES[i];
        }
        genes[VALORES.length - 1] = restante;
        return new Individuo(genes);
    }

    private static Individuo seleccionarPorTorneo(List<Individuo> poblacion, Random aleatorio) {
        Individuo mejor = null;
        for (int i = 0; i < TAMANO_TORNEO; i++) {
            Individuo candidato = poblacion.get(aleatorio.nextInt(poblacion.size()));
            if (mejor == null || candidato.aptitud > mejor.aptitud) {
                mejor = candidato;
            }
        }
        return mejor;
    }

    private static Individuo cruzar(Individuo padre1, Individuo padre2, Random aleatorio) {
        if (aleatorio.nextDouble() >= PROBABILIDAD_CRUCE) {
            return padre1.copia();
        }

        int punto = 1 + aleatorio.nextInt(VALORES.length - 1);
        int[] genesHijo = new int[VALORES.length];
        for (int i = 0; i < VALORES.length; i++) {
            genesHijo[i] = i < punto ? padre1.genes[i] : padre2.genes[i];
        }
        return new Individuo(genesHijo);
    }

    private static void mutar(Individuo individuo, int objetivo, Random aleatorio) {
        if (aleatorio.nextDouble() >= PROBABILIDAD_MUTACION) {
            return;
        }

        int gen = aleatorio.nextInt(VALORES.length);
        int maximo = objetivo / VALORES[gen];
        int variacion = aleatorio.nextInt(5) - 2;
        individuo.genes[gen] = Math.max(0, Math.min(maximo, individuo.genes[gen] + variacion));

        // En algunas mutaciones se cambia un segundo gen para aumentar la diversidad.
        if (aleatorio.nextBoolean()) {
            int segundoGen = aleatorio.nextInt(VALORES.length);
            int segundoMaximo = objetivo / VALORES[segundoGen];
            individuo.genes[segundoGen] = aleatorio.nextInt(segundoMaximo + 1);
        }
    }

    /** Ajusta un cromosoma despues del cruce o la mutacion para que represente el monto exacto. */
    private static void reparar(int[] genes, int objetivo, Random aleatorio) {
        int total = calcularTotal(genes);

        while (total > objetivo) {
            List<Integer> removibles = new ArrayList<>();
            for (int i = 0; i < genes.length; i++) {
                if (genes[i] > 0) {
                    removibles.add(i);
                }
            }
            int gen = removibles.get(aleatorio.nextInt(removibles.size()));
            genes[gen]--;
            total -= VALORES[gen];
        }

        int restante = objetivo - total;
        while (restante > 0) {
            List<Integer> posibles = new ArrayList<>();
            for (int i = 0; i < VALORES.length; i++) {
                if (VALORES[i] <= restante) {
                    posibles.add(i);
                }
            }

            // Una ligera preferencia por monedas grandes acelera la convergencia sin eliminar azar.
            int gen = aleatorio.nextDouble() < 0.35
                    ? posibles.get(0)
                    : posibles.get(aleatorio.nextInt(posibles.size()));
            genes[gen]++;
            restante -= VALORES[gen];
        }
    }

    /** La aptitud premia primero el monto exacto y despues el menor numero de monedas. */
    private static void evaluar(Individuo individuo, int objetivo) {
        int diferencia = Math.abs(objetivo - calcularTotal(individuo.genes));
        int monedas = contarMonedas(individuo.genes);
        individuo.aptitud = Math.max(1.0, 1_000_000.0 - diferencia * 10_000.0 - monedas);
    }

    private static int calcularTotal(int[] genes) {
        int total = 0;
        for (int i = 0; i < genes.length; i++) {
            total += genes[i] * VALORES[i];
        }
        return total;
    }

    private static int contarMonedas(int[] genes) {
        return Arrays.stream(genes).sum();
    }

    /** Calcula un valor optimo exacto solo para verificar el resultado del algoritmo genetico. */
    private static int minimoMonedasDinamico(int objetivo) {
        int[] minimo = new int[objetivo + 1];
        Arrays.fill(minimo, objetivo + 1);
        minimo[0] = 0;
        for (int cantidad = 1; cantidad <= objetivo; cantidad++) {
            for (int moneda : VALORES) {
                if (moneda <= cantidad) {
                    minimo[cantidad] = Math.min(minimo[cantidad], minimo[cantidad - moneda] + 1);
                }
            }
        }
        return minimo[objetivo];
    }

    private static void validarObjetivo(int objetivo) {
        if (objetivo < 1 || objetivo >= 10_000) {
            throw new IllegalArgumentException("El monto debe estar entre 1 y 9999 centavos.");
        }
    }

    private static void imprimirProgreso(int generacion, Individuo mejor, int objetivo) {
        System.out.printf(
                "Generacion %3d | monedas: %3d | total: %4d/%d | cromosoma: %s%n",
                generacion,
                contarMonedas(mejor.genes),
                calcularTotal(mejor.genes),
                objetivo,
                Arrays.toString(mejor.genes));
    }

    private static int leerMonto() {
        System.out.print("Monto en centavos (1-9999): ");
        try (Scanner scanner = new Scanner(System.in)) {
            return Integer.parseInt(scanner.nextLine().trim());
        }
    }

    private static void imprimirUso() {
        System.out.println("Uso:");
        System.out.println("  java -cp out AlgoritmoGeneticoCambio <monto> [semilla]");
        System.out.println("  java -cp out AlgoritmoGeneticoCambio --verbose <monto> [semilla]");
        System.out.println("  java -cp out AlgoritmoGeneticoCambio --demo");
        System.out.println("  java -cp out AlgoritmoGeneticoCambio --gui");
    }

    public static void main(String[] args) {
        try {
            if (args.length > 0 && "--gui".equals(args[0])) {
                InterfazCambio.mostrar();
                return;
            }

            if (args.length > 0 && "--demo".equals(args[0])) {
                int[] objetivos = {353, 155, 263};
                for (int objetivo : objetivos) {
                    long semilla = SEMILLA_PREDETERMINADA + objetivo;
                    System.out.println("=".repeat(62));
                    System.out.println(resolver(objetivo, semilla, false).resumen());
                }
                return;
            }

            boolean detallado = args.length > 0 && "--verbose".equals(args[0]);
            int desplazamiento = detallado ? 1 : 0;
            int objetivo = args.length > desplazamiento
                    ? Integer.parseInt(args[desplazamiento])
                    : leerMonto();
            long semilla = args.length > desplazamiento + 1
                    ? Long.parseLong(args[desplazamiento + 1])
                    : SEMILLA_PREDETERMINADA;

            Resultado resultado = resolver(objetivo, semilla, detallado);
            System.out.println();
            System.out.println(resultado.resumen());
        } catch (NumberFormatException error) {
            System.err.println("El monto y la semilla deben ser numeros enteros.");
            imprimirUso();
            System.exit(1);
        } catch (IllegalArgumentException error) {
            System.err.println("Error: " + error.getMessage());
            imprimirUso();
            System.exit(1);
        }
    }
}
