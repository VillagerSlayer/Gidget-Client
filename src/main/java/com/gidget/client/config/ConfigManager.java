package com.gidget.client.config;

import com.google.gson.*;
import com.gidget.client.GidgetClient;
import com.gidget.client.gui.GidgetTheme;
import com.gidget.client.macro.Macro;
import com.gidget.client.macro.MacroManager;
import com.gidget.client.module.Module;
import com.gidget.client.module.ModuleManager;
import com.gidget.client.settings.Setting;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/** Loads and saves module state/settings, client settings, and macros to JSON — either the default file or a named profile. */
public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("gidgetclient");
    private static final Path DEFAULT_FILE = CONFIG_DIR.resolve("gidgetclient.json");
    private static final Path PROFILES_DIR = CONFIG_DIR.resolve("profiles");

    private ConfigManager() {
    }

    public static void save() {
        save(DEFAULT_FILE);
    }

    public static void load() {
        load(DEFAULT_FILE);
    }

    public static void saveProfile(String name) {
        save(profilePath(name));
    }

    public static void loadProfile(String name) {
        load(profilePath(name));
    }

    public static void deleteProfile(String name) {
        try {
            Files.deleteIfExists(profilePath(name));
        } catch (IOException e) {
            GidgetClient.LOGGER.error("Failed to delete profile " + name, e);
        }
    }

    public static List<String> listProfiles() {
        List<String> names = new ArrayList<>();
        if (!Files.isDirectory(PROFILES_DIR)) return names;

        try (Stream<Path> files = Files.list(PROFILES_DIR)) {
            files.filter(p -> p.toString().endsWith(".json"))
                .forEach(p -> names.add(p.getFileName().toString().replace(".json", "")));
        } catch (IOException e) {
            GidgetClient.LOGGER.error("Failed to list profiles", e);
        }
        return names;
    }

    private static Path profilePath(String name) {
        return PROFILES_DIR.resolve(name + ".json");
    }

    private static void save(Path file) {
        JsonObject root = new JsonObject();
        JsonObject modulesObj = new JsonObject();

        for (Module module : ModuleManager.get().getModules()) {
            JsonObject moduleObj = new JsonObject();
            moduleObj.addProperty("active", module.isActive());
            moduleObj.addProperty("keyCode", module.getKeyCode());

            JsonObject settingsObj = new JsonObject();
            for (Setting<?> setting : module.getSettings().all()) {
                settingsObj.add(setting.getName(), setting.toJson());
            }
            moduleObj.add("settings", settingsObj);

            modulesObj.add(module.getName(), moduleObj);
        }
        root.add("modules", modulesObj);

        JsonObject clientObj = new JsonObject();
        clientObj.addProperty("accentColor", GidgetTheme.ACCENT);
        clientObj.addProperty("hudShowModules", ClientSettings.get().hudShowModules);
        clientObj.addProperty("hudShowFps", ClientSettings.get().hudShowFps);
        clientObj.addProperty("hudShowPing", ClientSettings.get().hudShowPing);
        clientObj.addProperty("hudShowCoords", ClientSettings.get().hudShowCoords);
        root.add("clientSettings", clientObj);

        JsonArray macrosArr = new JsonArray();
        for (Macro macro : MacroManager.get().getMacros()) {
            JsonObject macroObj = new JsonObject();
            macroObj.addProperty("name", macro.name);
            macroObj.addProperty("keyCode", macro.keyCode);
            macroObj.addProperty("message", macro.message);
            macrosArr.add(macroObj);
        }
        root.add("macros", macrosArr);

        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(root));
        } catch (IOException e) {
            GidgetClient.LOGGER.error("Failed to save config to " + file, e);
        }
    }

    private static void load(Path file) {
        if (!Files.exists(file)) return;

        try {
            String content = Files.readString(file);
            JsonObject root = JsonParser.parseString(content).getAsJsonObject();

            if (root.has("modules")) {
                JsonObject modulesObj = root.getAsJsonObject("modules");
                for (Module module : ModuleManager.get().getModules()) {
                    // Reset first so switching profiles fully replaces state instead of merging with it.
                    module.setActive(false);
                    module.setKeyCode(GLFW.GLFW_KEY_UNKNOWN);

                    if (!modulesObj.has(module.getName())) continue;
                    JsonObject moduleObj = modulesObj.getAsJsonObject(module.getName());

                    if (moduleObj.has("keyCode")) {
                        module.setKeyCode(moduleObj.get("keyCode").getAsInt());
                    }

                    if (moduleObj.has("settings")) {
                        JsonObject settingsObj = moduleObj.getAsJsonObject("settings");
                        for (Setting<?> setting : module.getSettings().all()) {
                            if (settingsObj.has(setting.getName())) {
                                setting.fromJson(settingsObj.get(setting.getName()));
                            }
                        }
                    }

                    if (moduleObj.has("active") && moduleObj.get("active").getAsBoolean()) {
                        module.setActive(true);
                    }
                }
            }

            if (root.has("clientSettings")) {
                JsonObject clientObj = root.getAsJsonObject("clientSettings");
                if (clientObj.has("accentColor")) GidgetTheme.ACCENT = clientObj.get("accentColor").getAsInt();
                if (clientObj.has("hudShowModules")) ClientSettings.get().hudShowModules = clientObj.get("hudShowModules").getAsBoolean();
                if (clientObj.has("hudShowFps")) ClientSettings.get().hudShowFps = clientObj.get("hudShowFps").getAsBoolean();
                if (clientObj.has("hudShowPing")) ClientSettings.get().hudShowPing = clientObj.get("hudShowPing").getAsBoolean();
                if (clientObj.has("hudShowCoords")) ClientSettings.get().hudShowCoords = clientObj.get("hudShowCoords").getAsBoolean();
            }

            if (root.has("macros")) {
                MacroManager.get().getMacros().clear();
                for (JsonElement element : root.getAsJsonArray("macros")) {
                    JsonObject macroObj = element.getAsJsonObject();
                    String name = macroObj.has("name") ? macroObj.get("name").getAsString() : "Macro";
                    MacroManager.get().add(new Macro(name, macroObj.get("keyCode").getAsInt(), macroObj.get("message").getAsString()));
                }
            }
        } catch (IOException | JsonParseException | ClassCastException | IllegalStateException e) {
            // A malformed or unexpectedly-shaped config file must never take the whole client down —
            // log it and fall back to defaults instead. (RuntimeException here is deliberately broad:
            // Gson's getAsXxx() throws unchecked exceptions like ClassCastException/IllegalStateException
            // for shape mismatches, and there's no way to validate every field upfront.)
            GidgetClient.LOGGER.error("Failed to load config from " + file + " — using defaults instead", e);
        }
    }
}
