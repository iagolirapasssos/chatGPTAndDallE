package com.bosonshiggs.chatgpt.helpers;

import com.google.appinventor.components.common.OptionList;
import java.util.HashMap;
import java.util.Map;

public enum AudioFormats implements OptionList<String> {
    MP3("mp3"),
    OPUS("opus"),
    AAC("aac"),
    FLAC("flac");

    private String voice;

    AudioFormats(String voice) {
        this.voice = voice;
    }

    @Override
    public String toUnderlyingValue() {
        return voice;
    }

    private static final Map<String, AudioFormats> lookup = new HashMap<>();

    static {
        for (AudioFormats voice : AudioFormats.values()) {
            lookup.put(voice.toUnderlyingValue(), voice);
        }
    }

    public static AudioFormats fromUnderlyingValue(String voice) {
        return lookup.getOrDefault(voice, MP3); // Default to MP3 if not found
    }
}
