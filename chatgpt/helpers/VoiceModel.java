package com.bosonshiggs.chatgpt.helpers;

import com.google.appinventor.components.common.OptionList;
import java.util.HashMap;
import java.util.Map;

public enum VoiceModel implements OptionList<String> {
    ALLOY("alloy"),
    ECHO("echo"),
    FABLE("fable"),
    ONYX("onyx"),
    NOVA("nova"),
    SHIMMER("shimmer");

    private String voice;

    VoiceModel(String voice) {
        this.voice = voice;
    }

    @Override
    public String toUnderlyingValue() {
        return voice;
    }

    private static final Map<String, VoiceModel> lookup = new HashMap<>();

    static {
        for (VoiceModel voice : VoiceModel.values()) {
            lookup.put(voice.toUnderlyingValue(), voice);
        }
    }

    public static VoiceModel fromUnderlyingValue(String voice) {
        return lookup.getOrDefault(voice, ALLOY); // Default to ALLOY if not found
    }
}
