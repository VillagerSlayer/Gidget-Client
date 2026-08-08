package com.gidget.client.module.impl;

import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import com.gidget.client.settings.BlockListSetting;
import com.gidget.client.settings.BoolSetting;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public final class XrayModule extends Module {
    public static final List<Block> DEFAULT_ORES = List.of(
        Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
        Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
        Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
        Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
        Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
        Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
        Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
        Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
        Blocks.NETHER_GOLD_ORE, Blocks.NETHER_QUARTZ_ORE,
        Blocks.ANCIENT_DEBRIS
    );

    public final BlockListSetting whitelist = getSettings().add(new BlockListSetting(
        "whitelist", "Blocks to keep visible; everything else is hidden.", DEFAULT_ORES, v -> refresh()
    ));

    public final BoolSetting seeThroughFluids = getSettings().add(new BoolSetting(
        "see-through-fluids", "Also hide water and lava so ores underneath are visible.", true, v -> refresh()
    ));

    public final BoolSetting fullbright = getSettings().add(new BoolSetting(
        "fullbright", "Force full brightness while active, so hidden-away ores aren't just exposed in the dark.", true, v -> refresh()
    ));

    public XrayModule() {
        super(Category.RENDER, "xray", "Only renders specified blocks. Good for mining.");
    }

    public boolean isWhitelisted(Block block) {
        return whitelist.get().contains(block);
    }

    public boolean isSeeThroughFluids() {
        return seeThroughFluids.get();
    }

    public boolean isFullbright() {
        return fullbright.get();
    }

    @Override
    protected void onActivate() {
        refresh();
    }

    @Override
    protected void onDeactivate() {
        refresh();
    }

    private void refresh() {
        if (mc.levelRenderer != null) mc.levelRenderer.allChanged();
    }
}
