package com.bosonshiggs.chatgpt.helpers;

import com.google.appinventor.components.common.OptionList;
import java.util.HashMap;
import java.util.Map;

public enum VisionModel implements OptionList<String> {
    GPT_4O_MINI("gpt-4o-mini"),
    GPT_4O("gpt-4o"),
    GPT_4_TURBO("gpt-4-turbo"),
    CHATGPT_4O_LATEST("chatgpt-4o-latest");

    private String model;

    VisionModel(String model) {
        this.model = model;
    }

    @Override
    public String toUnderlyingValue() {
        return model;
    }

    private static final Map<String, VisionModel> lookup = new HashMap<>();

    static {
        for (VisionModel model : VisionModel.values()) {
            lookup.put(model.toUnderlyingValue(), model);
        }
    }

    public static VisionModel fromUnderlyingValue(String model) {
        return lookup.getOrDefault(model, GPT_4O_MINI); // Default to GPT_4O_MINI if not found
    }
}