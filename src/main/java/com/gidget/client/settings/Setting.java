package com.gidget.client.settings;

import com.google.gson.JsonElement;

import java.util.function.Consumer;

public abstract class Setting<T> {
    private final String name;
    private final String description;
    protected T value;
    protected final T defaultValue;
    private final Consumer<T> onChanged;

    protected Setting(String name, String description, T defaultValue, Consumer<T> onChanged) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.onChanged = onChanged;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        if (!isValid(value)) return;

        this.value = value;
        if (onChanged != null) onChanged.accept(value);
    }

    public void reset() {
        set(defaultValue);
    }

    protected boolean isValid(T value) {
        return value != null;
    }

    public abstract JsonElement toJson();

    public abstract void fromJson(JsonElement element);
}
