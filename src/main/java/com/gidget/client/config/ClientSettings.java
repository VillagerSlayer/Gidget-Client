package com.gidget.client.config;

/** Client-wide (non-module) settings: GUI/HUD preferences, backed by {@link ConfigManager}. */
public final class ClientSettings {
    private static final ClientSettings INSTANCE = new ClientSettings();

    public boolean hudShowModules = true;
    public boolean hudShowFps = false;
    public boolean hudShowPing = false;
    public boolean hudShowCoords = false;

    private ClientSettings() {
    }

    public static ClientSettings get() {
        return INSTANCE;
    }
}
