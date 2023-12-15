package com.bosonshiggs.chatgpt.helpers;

import com.google.appinventor.components.common.OptionList;
import java.util.HashMap;
import java.util.Map;

public enum ImageSize implements OptionList<String> {
    SIZE_1024x1024("1024x1024"),
    SIZE_1024x1792("1024x1792"),
    SIZE_1792x1024("1792x1024");

    private String size;

    ImageSize(String size) {
        this.size = size;
    }

    public String toUnderlyingValue() {
        return size;
    }

    private static final Map<String, ImageSize> lookup = new HashMap<>();

    static {
        for (ImageSize imageSize : ImageSize.values()) {
            lookup.put(imageSize.toUnderlyingValue(), imageSize);
        }
    }

    public static ImageSize fromUnderlyingValue(String size) {
        return lookup.getOrDefault(size, SIZE_1024x1024);
    }
}
