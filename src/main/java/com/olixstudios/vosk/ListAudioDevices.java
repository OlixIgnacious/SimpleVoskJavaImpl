package com.olixstudios.vosk;

import javax.sound.sampled.*;

public class ListAudioDevices {

    public static void main(String[] args) {
        Mixer.Info[] mixers = AudioSystem.getMixerInfo();
        int index = 0;

        for (Mixer.Info mixerInfo : mixers) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);

            Line.Info[] targetLines = mixer.getTargetLineInfo();

            // Only print devices that can be used for microphone input
            if (targetLines != null && targetLines.length > 0) {
                System.out.println("[" + index + "] " + mixerInfo.getName() + " - " + mixerInfo.getDescription());
            }
            index++;
        }
    }
}