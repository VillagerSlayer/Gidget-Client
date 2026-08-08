package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import com.gidget.client.settings.IntSetting;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

import java.util.Map;

public final class AutoReconnectModule extends Module {
    public final IntSetting delaySeconds = getSettings().add(new IntSetting(
        "delay", "Seconds to wait before reconnecting.", 3, 1, 30, v -> {}
    ));

    private ServerData lastServer;
    private int ticksUntilReconnect = -1;

    public AutoReconnectModule() {
        super(Category.MISC, "auto-reconnect", "Reconnects automatically after a disconnect.");
    }

    @Override
    public void onTick() {
        if (mc.getCurrentServer() != null) {
            lastServer = mc.getCurrentServer();
        }

        if (ticksUntilReconnect >= 0) {
            if (--ticksUntilReconnect <= 0) {
                ticksUntilReconnect = -1;
                reconnect();
            }
            return;
        }

        if (mc.screen instanceof DisconnectedScreen && lastServer != null) {
            ticksUntilReconnect = delaySeconds.get() * 20;
        }
    }

    private void reconnect() {
        if (lastServer == null) return;

        ServerAddress address = ServerAddress.parseString(lastServer.ip);
        ConnectScreen.startConnecting(new TitleScreen(), mc, address, lastServer, false, new TransferState(Map.of(), Map.of(), false));
    }
}
