package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket.Action;

public final class AutoRespawnModule extends Module {
    public AutoRespawnModule() {
        super(Category.PLAYER, "auto-respawn", "Skips the death screen and respawns immediately.");
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.screen == null) return;
        if (mc.screen instanceof DeathScreen) {
            mc.player.connection.send(new ServerboundClientCommandPacket(Action.PERFORM_RESPAWN));
        }
    }
}
