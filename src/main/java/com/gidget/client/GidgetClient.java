package com.gidget.client;

import com.gidget.client.config.ConfigManager;
import com.gidget.client.hud.ModuleListHudElement;
import com.gidget.client.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GidgetClient implements ClientModInitializer {
    public static final String MOD_ID = "gidgetclient";
    public static final Logger LOGGER = LoggerFactory.getLogger("Gidget Client");

    @Override
    public void onInitializeClient() {
        ModuleManager.get().init();
        ConfigManager.load();

        ClientTickEvents.END_CLIENT_TICK.register(mc -> ModuleManager.get().onTick());
        ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> ConfigManager.save());

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "module_list"), new ModuleListHudElement());

        LOGGER.info("Gidget Client initialized");
    }
}
