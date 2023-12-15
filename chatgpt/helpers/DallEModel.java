package com.bosonshiggs.chatgpt.helpers;

import com.google.appinventor.components.common.OptionList;
import java.util.HashMap;
import java.util.Map;

public enum DallEModel implements OptionList<String> {
    DALL_E_2("dall-e-2"),
    DALL_E_3("dall-e-3");

    private String model;

    DallEModel(String model) {
        this.model = model;
    }

    public String toUnderlyingValue() {
        return model;
    }

    private static final Map<String, DallEModel> lookup = new HashMap<>();

    static {
        for (DallEModel model : DallEModel.values()) {
            lookup.put(model.toUnderlyingValue(), model);
        }
    }

    public static DallEModel fromUnderlyingValue(String model) {
        return lookup.getOrDefault(model, DALL_E_2); // Default to DALL_E_3 if not found
    }
}
