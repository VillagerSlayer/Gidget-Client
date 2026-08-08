package com.gidget.client.settings;

import java.util.ArrayList;
import java.util.List;

public final class SettingGroup {
    private final List<Setting<?>> settings = new ArrayList<>();

    public <T, S extends Setting<T>> S add(S setting) {
        settings.add(setting);
        return setting;
    }

    public List<Setting<?>> all() {
        return settings;
    }
}
