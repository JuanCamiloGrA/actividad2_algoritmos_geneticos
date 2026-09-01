import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/** Interfaz grafica sencilla para obtener una captura similar a la del enunciado. */
public final class InterfazCambio extends JFrame {
    private static final Color AZUL = new Color(0, 153, 204);

    private final JTextField campoMonto = new JTextField("353", 10);
    private final JTextField campoSemilla = new JTextField("20261184", 12);
    private final JTextArea salida = new JTextArea();
    private final JButton botonCalcular = new JButton("Calcular");

    private InterfazCambio() {
        super("Cambio minimo mediante algoritmo genetico");
        construirInterfaz();
    }

    private void construirInterfaz() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(720, 520));
        setLocationByPlatform(true);

        JPanel cabecera = new JPanel(new GridBagLayout());
        cabecera.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));
        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.insets = new Insets(4, 6, 4, 6);
        restricciones.anchor = GridBagConstraints.WEST;

        restricciones.gridx = 0;
        restricciones.gridy = 0;
        cabecera.add(new JLabel("Monto (centavos):"), restricciones);
        restricciones.gridx = 1;
        cabecera.add(campoMonto, restricciones);
        restricciones.gridx = 2;
        cabecera.add(new JLabel("Semilla:"), restricciones);
        restricciones.gridx = 3;
        cabecera.add(campoSemilla, restricciones);
        restricciones.gridx = 4;
        botonCalcular.setBackground(AZUL);
        botonCalcular.setForeground(Color.WHITE);
        cabecera.add(botonCalcular, restricciones);

        JLabel titulo = new JLabel("Actividad 2 - Algoritmos geneticos");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        titulo.setForeground(AZUL);
        restricciones.gridx = 0;
        restricciones.gridy = 1;
        restricciones.gridwidth = 5;
        restricciones.insets = new Insets(14, 6, 2, 6);
        cabecera.add(titulo, restricciones);

        salida.setEditable(false);
        salida.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        salida.setMargin(new Insets(12, 12, 12, 12));
        salida.setText("Introduce un monto y pulsa Calcular.");

        add(cabecera, BorderLayout.NORTH);
        add(new JScrollPane(salida), BorderLayout.CENTER);
        getRootPane().setDefaultButton(botonCalcular);
        botonCalcular.addActionListener(evento -> calcular());

        pack();
        setLocationRelativeTo(null);
    }

    private void calcular() {
        final int monto;
        final long semilla;
        try {
            monto = Integer.parseInt(campoMonto.getText().trim());
            semilla = Long.parseLong(campoSemilla.getText().trim());
            if (monto < 1 || monto >= 10_000) {
                throw new IllegalArgumentException("El monto debe estar entre 1 y 9999.");
            }
        } catch (NumberFormatException error) {
            JOptionPane.showMessageDialog(this, "El monto y la semilla deben ser numeros enteros.");
            return;
        } catch (IllegalArgumentException error) {
            JOptionPane.showMessageDialog(this, error.getMessage());
            return;
        }

        botonCalcular.setEnabled(false);
        salida.setText("Evolucionando la poblacion...\n");

        SwingWorker<AlgoritmoGeneticoCambio.Resultado, Void> tarea = new SwingWorker<>() {
            @Override
            protected AlgoritmoGeneticoCambio.Resultado doInBackground() {
                return AlgoritmoGeneticoCambio.resolver(monto, semilla, false);
            }

            @Override
            protected void done() {
                try {
                    salida.setText(get().resumen());
                    salida.setCaretPosition(0);
                } catch (Exception error) {
                    salida.setText("No fue posible completar la ejecucion: " + error.getMessage());
                } finally {
                    botonCalcular.setEnabled(true);
                }
            }
        };
        tarea.execute();
    }

    public static void mostrar() {
        SwingUtilities.invokeLater(() -> new InterfazCambio().setVisible(true));
    }
}
