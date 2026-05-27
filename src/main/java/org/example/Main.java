package org.example;

import org.example.controller.GameController;
import org.example.view.gui.*;

import javax.swing.*;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

public class Main {

    public static void main(String[] args) {
        // Forzar UTF-8 y locale español sin importar la config del sistema/IES
        Locale.setDefault(new Locale("es", "ES"));
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        } catch (Exception ignored) {}

        boolean console = Arrays.asList(args).contains("--console");
        if (console) {
            new GameController().start();
            return;
        }

        // Launch GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            UnoGameFrame frame = new UnoGameFrame();

            // Show setup dialog before making the frame visible
            SetupDialog.SetupConfig config = SetupDialog.show(frame);
            if (config == null) {
                System.exit(0);
                return;
            }

            frame.setVisible(true);

            // Start game logic in background thread
            GUIGameController controller = new GUIGameController(frame, config);
            controller.start();
        });
    }
}
