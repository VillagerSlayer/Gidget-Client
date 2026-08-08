package com.gidget.client.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Consumer;

public final class ColorSetting extends Setting<GidgetColor> {
    public ColorSetting(String name, String description, GidgetColor defaultValue, Consumer<GidgetColor> onChanged) {
        super(name, description, defaultValue, onChanged);
    }

    @Override
    public JsonElement toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("r", value.r);
        obj.addProperty("g", value.g);
        obj.addProperty("b", value.b);
        obj.addProperty("a", value.a);
        return obj;
    }

    @Override
    public void fromJson(JsonElement element) {
        JsonObject obj = element.getAsJsonObject();
        set(new GidgetColor(
            obj.get("r").getAsInt(),
            obj.get("g").getAsInt(),
            obj.get("b").getAsInt(),
            obj.get("a").getAsInt()
        ));
    }
}
