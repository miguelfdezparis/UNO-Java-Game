package org.example.view.gui;

import javazoom.jl.decoder.*;

import javax.sound.sampled.*;
import java.io.InputStream;

public class MusicPlayer {

    private static final int SKIP_BYTES = 10 * 16_000;

    private volatile float        volumeScale = 1.0f;
    private volatile boolean      muted       = false;
    private volatile boolean      ducked      = false;
    private volatile boolean      running     = false;
    private volatile SourceDataLine line       = null;
    private Thread playThread;

    public void play() {
        running = true;
        playThread = new Thread(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException e) { return; }
            while (running) {
                try (InputStream raw = MusicPlayer.class.getResourceAsStream("/music.mp3")) {
                    if (raw == null) return;
                    raw.skip(SKIP_BYTES);

                    Bitstream  bitstream = new Bitstream(raw);
                    Decoder    decoder   = new Decoder();
                    Header     firstFrame = bitstream.readFrame();
                    if (firstFrame == null) break;

                    int sampleRate = firstFrame.frequency();
                    int channels   = firstFrame.mode() == Header.SINGLE_CHANNEL ? 1 : 2;
                    AudioFormat fmt = new AudioFormat(sampleRate, 16, channels, true, false);

                    line = AudioSystem.getSourceDataLine(fmt);
                    line.open(fmt, sampleRate * channels * 2 / 5); // 200ms buffer
                    line.start();

                    Header frame = firstFrame;
                    while (running && frame != null) {
                        SampleBuffer out = (SampleBuffer) decoder.decodeFrame(frame, bitstream);
                        short[] samples = out.getBuffer();
                        int len = out.getBufferLength();
                        byte[] bytes = new byte[len * 2];
                        float vol = volumeScale;
                        for (int i = 0; i < len; i++) {
                            short s = (short) (samples[i] * vol);
                            bytes[i * 2]     = (byte) (s & 0xFF);
                            bytes[i * 2 + 1] = (byte) (s >> 8);
                        }
                        line.write(bytes, 0, bytes.length);
                        bitstream.closeFrame();
                        frame = bitstream.readFrame();
                    }
                    line.drain();
                    line.close();
                    line = null;
                } catch (Exception e) {
                    if (!running) break;
                }
            }
        }, "uno-music");
        playThread.setDaemon(true);
        playThread.start();
    }

    public void duck()   { ducked = true;  if (!muted) volumeScale = 0.12f; }
    public void unduck() { ducked = false; volumeScale = muted ? 0.0f : 1.0f; }

    public void toggle() {
        muted = !muted;
        volumeScale = muted ? 0.0f : (ducked ? 0.12f : 1.0f);
    }

    public boolean isMuted() { return muted; }

    public void stop() {
        running = false;
        SourceDataLine l = line;
        if (l != null) l.close();
    }
}
