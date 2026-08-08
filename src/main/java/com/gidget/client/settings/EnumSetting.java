package com.gidget.client.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.function.Consumer;

public final class EnumSetting<E extends Enum<E>> extends Setting<E> {
    private final Class<E> type;

    public EnumSetting(String name, String description, Class<E> type, E defaultValue, Consumer<E> onChanged) {
        super(name, description, defaultValue, onChanged);
        this.type = type;
    }

    public E[] getValues() {
        return type.getEnumConstants();
    }

    public void cycle() {
        E[] values = getValues();
        set(values[(value.ordinal() + 1) % values.length]);
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value.name());
    }

    @Override
    public void fromJson(JsonElement element) {
        try {
            set(Enum.valueOf(type, element.getAsString()));
        } catch (IllegalArgumentException ignored) {
        }
    }
}
