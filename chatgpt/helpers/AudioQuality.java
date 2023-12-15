package com.bosonshiggs.chatgpt.helpers;

import com.google.appinventor.components.common.OptionList;
import java.util.HashMap;
import java.util.Map;

public enum AudioQuality implements OptionList<String> {
    TTS1("tts-1"),
    TTS1HD("tts-1-hd");

    private String voice;

    AudioQuality(String voice) {
        this.voice = voice;
    }

    @Override
    public String toUnderlyingValue() {
        return voice;
    }

    private static final Map<String, AudioQuality> lookup = new HashMap<>();

    static {
        for (AudioQuality voice : AudioQuality.values()) {
            lookup.put(voice.toUnderlyingValue(), voice);
        }
    }

    public static AudioQuality fromUnderlyingValue(String voice) {
        return lookup.getOrDefault(voice, TTS1); // Default to TTS1 if not found
    }
}
