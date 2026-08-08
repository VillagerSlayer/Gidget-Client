package com.gidget.client.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.function.Consumer;

public final class IntSetting extends Setting<Integer> {
    private final int min;
    private final int max;

    public IntSetting(String name, String description, int defaultValue, int min, int max, Consumer<Integer> onChanged) {
        super(name, description, defaultValue, onChanged);
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    @Override
    protected boolean isValid(Integer value) {
        return value != null && value >= min && value <= max;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void fromJson(JsonElement element) {
        set(element.getAsInt());
    }
}
