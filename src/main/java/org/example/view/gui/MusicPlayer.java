package org.example.view.gui;

import javazoom.jl.player.Player;
import java.io.InputStream;

public class MusicPlayer {

    private static final int SKIP_BYTES = 9 * 16_000; // ~9s at 128kbps

    private volatile boolean running = false;
    private volatile Player  current = null;
    private Thread playThread;

    public void play() {
        running = true;
        playThread = new Thread(() -> {
            while (running) {
                try (InputStream raw = MusicPlayer.class.getResourceAsStream("/music.mp3")) {
                    if (raw == null) return; // archivo no encontrado, silencio
                    raw.skip(SKIP_BYTES);
                    current = new Player(raw);
                    current.play();
                } catch (Exception e) {
                    if (!running) break;
                }
            }
        }, "uno-music");
        playThread.setDaemon(true);
        playThread.start();
    }

    public void stop() {
        running = false;
        Player p = current;
        if (p != null) p.close();
    }
}
