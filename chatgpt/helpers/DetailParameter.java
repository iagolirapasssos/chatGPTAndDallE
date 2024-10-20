package com.bosonshiggs.chatgpt.helpers;

import com.google.appinventor.components.common.OptionList;
import java.util.HashMap;
import java.util.Map;

public enum DetailParameter implements OptionList<String> {
    LOW("low"),
    HIGH("high"),
    AUTO("auto");

    private String model;

    DetailParameter(String model) {
        this.model = model;
    }

    @Override
    public String toUnderlyingValue() {
        return model;
    }

    private static final Map<String, DetailParameter> lookup = new HashMap<>();

    static {
        for (DetailParameter model : DetailParameter.values()) {
            lookup.put(model.toUnderlyingValue(), model);
        }
    }

    public static DetailParameter fromUnderlyingValue(String model) {
        return lookup.getOrDefault(model, AUTO); // Default to AUTO if not found
    }
}