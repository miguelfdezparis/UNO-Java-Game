package org.example.view.gui;

import javax.sound.sampled.*;

public class SoundEffect {

    public static void playCard()  { tone(880, 80, 0.3f); }
    public static void drawCard()  { tone(330, 120, 0.25f); }
    public static void skip()      { tone(500, 60, 0.3f); tone(350, 100, 0.3f); }
    public static void reverse()   { tone(600, 80, 0.3f); tone(750, 80, 0.3f); }
    public static void drawTwo()   { tone(330, 80, 0.3f); tone(330, 80, 0.3f); }
    public static void drawFour()  { for (int i = 0; i < 4; i++) tone(280, 60, 0.3f); }
    public static void wild()      { int[] f = {600,700,800,900}; for (int x : f) tone(x, 60, 0.35f); }

    public static void uno(MusicPlayer music) {
        Thread t = new Thread(() -> {
            music.duck();
            party();
            speakUno();
            music.unduck();
        });
        t.setDaemon(true);
        t.start();
    }

    public static void win(MusicPlayer music, String playerName) {
        Thread t = new Thread(() -> {
            music.duck();
            int[] melody = {523, 659, 784, 1046, 784, 1046, 1318};
            int[] dur    = {100, 100, 100, 200,  80,  80,  400};
            for (int i = 0; i < melody.length; i++) tone(melody[i], dur[i], 0.5f);
            party();
            speak(playerName + " ha ganado!");
            music.unduck();
        });
        t.setDaemon(true);
        t.start();
    }

    // Rafaga de tonos festivos tipo confeti
    public static void party() {
        int[] freqs = {880, 1046, 784, 1318, 659, 1046, 880, 1175, 988, 1318};
        int[] durs  = { 60,   50,  60,   50,  60,   50,  60,   50,  60,   80};
        for (int i = 0; i < freqs.length; i++) tone(freqs[i], durs[i], 1.0f);
    }

    private static final String TTS_INIT =
        "Add-Type -AssemblyName System.Speech; " +
        "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
        "$v = $s.GetInstalledVoices() | Where-Object { $_.VoiceInfo.Culture.Name -like 'es*' } | Select-Object -First 1; " +
        "if ($v) { $s.SelectVoice($v.VoiceInfo.Name) }; ";

    // TTS via PowerShell (Windows) — fuerza voz española si está instalada
    public static void speak(String text) {
        try {
            String safeText = text.replace("'", "");
            String script = TTS_INIT +
                String.format("$s.Rate = 1; $s.Volume = 100; $s.Speak('%s');", safeText);
            new ProcessBuilder("powershell", "-NoProfile", "-Command", script)
                .redirectErrorStream(true)
                .start()
                .waitFor();
        } catch (Exception ignored) {}
    }

    // "Queda una carta" normal + "UNOOO!" lento y potente
    private static void speakUno() {
        try {
            String script = TTS_INIT +
                "$s.Volume = 100; $s.Rate = 1; $s.Speak('Queda una carta...'); " +
                "$s.Rate = -3; $s.Volume = 100; $s.Speak('UNOOO!');";
            new ProcessBuilder("powershell", "-NoProfile", "-Command", script)
                .redirectErrorStream(true)
                .start()
                .waitFor();
        } catch (Exception ignored) {}
    }

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
