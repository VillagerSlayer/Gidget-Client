package com.gidget.client.module;

import com.gidget.client.module.impl.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ModuleManager {
    private static final ModuleManager INSTANCE = new ModuleManager();

    private final List<Module> modules = new ArrayList<>();

    private ModuleManager() {
    }

    public static ModuleManager get() {
        return INSTANCE;
    }

    public void init() {
        // Combat
        register(new AutoWeaponModule());
        register(new CriticalsModule());

        // Render
        register(new BlockEspModule());
        register(new XrayModule());
        register(new EntityEspModule());
        register(new FullbrightModule());
        register(new NametagsModule());
        register(new ItemEspModule());
        register(new FreecamModule());

        // Player
        register(new AutoToolModule());
        register(new AutoEatModule());
        register(new AutoArmorModule());
        register(new InventoryCleanerModule());
        register(new ChestStealerModule());
        register(new AutoTotemModule());
        register(new AutoRespawnModule());
        register(new MiddleClickExtraModule());
        register(new AutoLogModule());
        register(new RefillModule());
        register(new FastEatModule());

        // Movement
        register(new AutoSprintModule());
        register(new SafeWalkModule());
        register(new InventoryMoveModule());
        register(new NoSlowModule());
        register(new NoFallModule());
        register(new AutoBucketModule());
        register(new JesusModule());
        register(new StepModule());
        register(new SpiderModule());
        register(new FlyModule());
        register(new SpeedModule());

        // World
        register(new FastPlaceModule());
        register(new FastBreakModule());
        register(new ScaffoldModule());

        // Misc
        register(new AntiAfkModule());
        register(new SpamModule());
        register(new AutoReconnectModule());
    }

    private void register(Module module) {
        modules.add(module);
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public List<Module> getModules(Category category) {
        List<Module> result = new ArrayList<>();
        for (Module module : modules) {
            if (module.getCategory() == category) result.add(module);
        }
        return result;
    }

    public <T extends Module> T get(Class<T> type) {
        for (Module module : modules) {
            if (type.isInstance(module)) return type.cast(module);
        }
        throw new IllegalStateException("Module not registered: " + type.getSimpleName());
    }

    public Optional<Module> byName(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) return Optional.of(module);
        }
        return Optional.empty();
    }

    public void onKeyPressed(int keyCode) {
        for (Module module : modules) {
            if (module.matchesKey(keyCode)) module.toggle();
        }
    }

    public void onTick() {
        for (Module module : modules) {
            if (module.isActive()) module.onTick();
        }
    }
}
