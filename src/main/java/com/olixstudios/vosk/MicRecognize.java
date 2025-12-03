package com.olixstudios.vosk;

import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;

public class MicRecognize {

    private static final String MODEL_PATH = "/Users/olixstudios/Documents/workspace/Projects/vosk-demo/models/vosk-model-small-en-us-0.15/";

    // Audio format compatible with Vosk
    private static AudioFormat getAudioFormat() {
        float sampleRate = 16000.0f;       // 16 kHz
        int sampleSizeInBits = 16;         // 16-bit
        int channels = 1;                  // mono
        boolean signed = true;            
        boolean bigEndian = false;         // little-endian PCM
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    private static TargetDataLine getDeviceByName(String partialName, AudioFormat format) throws LineUnavailableException {

        Mixer.Info[] mixers = AudioSystem.getMixerInfo();

        for (Mixer.Info info : mixers) {
            if (info.getName().toLowerCase().contains(partialName.toLowerCase())) {
                Mixer mixer = AudioSystem.getMixer(info);

                // Ask mixer explicitly for a TargetDataLine that supports our format
                DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
                if (mixer.isLineSupported(dataLineInfo)) {
                    Line line = mixer.getLine(dataLineInfo);
                    if (line instanceof TargetDataLine) {
                        return (TargetDataLine) line;
                    }
                }

                // Fallback: inspect target lines and pick a TargetDataLine if present
                for (Line.Info lineInfo : mixer.getTargetLineInfo()) {
                    if (lineInfo instanceof DataLine.Info) {
                        DataLine.Info dlInfo = (DataLine.Info) lineInfo;
                        if (TargetDataLine.class.isAssignableFrom(dlInfo.getLineClass())) {
                            Line line = mixer.getLine(dlInfo);
                            if (line instanceof TargetDataLine) {
                                return (TargetDataLine) line;
                            }
                        }
                    }
                }
            }
        }

        throw new LineUnavailableException("No TargetDataLine found for device name: " + partialName);
    }

    public static void main(String[] args) {
        LibVosk.setLogLevel(LogLevel.WARNINGS); // Adjust LogLevel as needed (e.g., DEBUG, INFO, WARNINGS, ERROR)

        AudioFormat format = new AudioFormat(16000f, 16, 1, true, false);

        try (Model model = new Model(MODEL_PATH);
             Recognizer recognizer = new Recognizer(model, 16000)) {

            // CHANGE THIS STRING TO YOUR DEVICE NAME
            String deviceName = "PD200X Podcast Microphone"; // or "USB", "AirPods", etc.

            TargetDataLine mic = getDeviceByName(deviceName, format);
            mic.open(format);
            mic.start();

            System.out.println("Using device: " + mic.getLineInfo());
            System.out.println("🎙️ Speak now... (Ctrl+C to stop)");

            byte[] buffer = new byte[4096];

            while (true) {
                int n = mic.read(buffer, 0, buffer.length);
                if (n > 0) {
                    if (recognizer.acceptWaveForm(buffer, n)) {
                        System.out.println("RESULT: " + recognizer.getResult());
                    } else {
                        System.out.println("PARTIAL: " + recognizer.getPartialResult());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}