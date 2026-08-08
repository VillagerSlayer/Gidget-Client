package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import com.gidget.client.settings.IntSetting;
import com.gidget.client.settings.StringSetting;

public final class SpamModule extends Module {
    public final StringSetting message = getSettings().add(new StringSetting(
        "message", "What to send in chat. Starts with / to run a command instead.", "GIDGET LIKES TREATS!", v -> {}
    ));

    public final IntSetting interval = getSettings().add(new IntSetting(
        "interval", "Ticks between each send (20 ticks = 1 second).", 20, 1, 200, v -> {}
    ));

    private int ticksUntilSend;

    public SpamModule() {
        super(Category.MISC, "spam", "Repeatedly sends a message in chat.");
    }

    @Override
    protected void onActivate() {
        ticksUntilSend = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (--ticksUntilSend > 0) return;
        ticksUntilSend = interval.get();

        String text = message.get();
        if (text.isEmpty()) return;

        if (text.startsWith("/")) {
            mc.player.connection.sendCommand(text.substring(1));
        } else {
            mc.player.connection.sendChat(text);
        }
    }
}
