package org.example.view.gui;

import javax.sound.sampled.*;

public class SoundEffect {

    public static void playCard()  { tone(880, 80, 0.3f); }
    public static void drawCard()  { tone(330, 120, 0.25f); }
    public static void uno()       { tone(1046, 80, 0.4f); tone(1318, 150, 0.5f); }
    public static void skip()      { tone(500, 60, 0.3f); tone(350, 100, 0.3f); }
    public static void reverse()   { tone(600, 80, 0.3f); tone(750, 80, 0.3f); }
    public static void drawTwo()   { tone(330, 80, 0.3f); tone(330, 80, 0.3f); }
    public static void drawFour()  { for (int i = 0; i < 4; i++) tone(280, 60, 0.3f); }
    public static void wild()      { int[] f = {600,700,800,900}; for (int x : f) tone(x, 60, 0.35f); }
    public static void win()       { int[] f = {523,659,784,1046}; for (int x : f) tone(x, 130, 0.5f); }

    private static void tone(int hz, int ms, float vol) {
        Thread t = new Thread(() -> {
            try {
                AudioFormat fmt = new AudioFormat(44100, 16, 1, true, false);
                SourceDataLine line = AudioSystem.getSourceDataLine(fmt);
                line.open(fmt, 4410);
                line.start();
                int samples = 44100 * ms / 1000;
                byte[] buf = new byte[samples * 2];
                for (int i = 0; i < samples; i++) {
                    double angle = 2 * Math.PI * i * hz / 44100;
                    double fade  = Math.min(1.0, Math.min((double)i / 200, (double)(samples - i) / 200));
                    short v = (short) (Math.sin(angle) * 32767 * vol * fade);
                    buf[i * 2]     = (byte) (v & 0xFF);
                    buf[i * 2 + 1] = (byte) (v >> 8);
                }
                line.write(buf, 0, buf.length);
                line.drain();
                line.close();
            } catch (Exception ignored) {}
        });
        t.setDaemon(true);
        t.start();
        try { t.join(ms + 50); } catch (InterruptedException ignored) {}
    }
}
