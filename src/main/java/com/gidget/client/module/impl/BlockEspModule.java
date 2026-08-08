package com.gidget.client.module.impl;

import com.gidget.client.settings.BlockListSetting;
import com.gidget.client.settings.BoolSetting;
import com.gidget.client.settings.ColorSetting;
import com.gidget.client.settings.GidgetColor;
import com.gidget.client.settings.IntSetting;
import com.gidget.client.module.Category;
import com.gidget.client.module.Module;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chunk-indexed block scanner, adapted from Meteor Client's BlockESP/ESPChunk design: each chunk's
 * matching blocks are cached once instead of re-scanning a whole radius cube every tick.
 *
 * Two limits matter here that a full-column scan (like Meteor's, which relies on its own batched
 * renderer) doesn't need with Gizmos: the scan is bounded to a vertical window around the player
 * rather than bedrock-to-surface, and the render loop caps how many boxes it submits in one frame.
 * Skipping either one is how a single frame can end up asking BufferBuilder for tens of millions of
 * vertices (ore is common enough that "every ore in 13x13 chunks, full world height" overflows it).
 */
public final class BlockEspModule extends Module {
    private static final int MAX_RENDERED_PER_FRAME = 3000;
    private static final int VERTICAL_RADIUS = 32;
    private static final int Y_BUCKET_SIZE = 16;

    private record ChunkKey(int x, int z, int yBucket) {
    }

    public final BlockListSetting blocks = getSettings().add(new BlockListSetting(
        "blocks", "Blocks to highlight through walls.", XrayModule.DEFAULT_ORES, v -> rescanAll()
    ));

    public final IntSetting renderDistance = getSettings().add(new IntSetting(
        "render-distance", "How many chunks around the player to scan, in chunks.", 3, 1, 8, v -> rescanAll()
    ));

    public final ColorSetting color = getSettings().add(new ColorSetting(
        "color", "Highlight color.", new GidgetColor(0, 255, 200, 255), v -> {}
    ));

    public final BoolSetting tracers = getSettings().add(new BoolSetting(
        "tracers", "Draw a line from you to each highlighted block.", false, v -> {}
    ));

    private final Map<ChunkKey, List<BlockPos>> chunks = new ConcurrentHashMap<>();

    public BlockEspModule() {
        super(Category.RENDER, "block-esp", "Renders specified blocks through walls.");
    }

    @Override
    protected void onActivate() {
        rescanAll();
    }

    @Override
    protected void onDeactivate() {
        chunks.clear();
    }

    private void rescanAll() {
        chunks.clear();
    }

    /** Called by {@code ClientLevelMixin} when a watched block changes, so its chunk is rescanned. */
    public void onBlockChanged(BlockPos pos) {
        if (!isActive() || mc.level == null || mc.player == null) return;

        LevelChunk chunk = mc.level.getChunkAt(pos);
        int yBucket = Math.floorDiv(mc.player.blockPosition().getY(), Y_BUCKET_SIZE);
        chunks.put(new ChunkKey(chunk.getPos().x(), chunk.getPos().z(), yBucket), scanChunk(chunk, mc.player.blockPosition().getY()));
    }

    private List<BlockPos> scanChunk(ChunkAccess chunk, int centerY) {
        List<Block> whitelist = blocks.get();
        List<BlockPos> matches = new ArrayList<>();
        if (whitelist.isEmpty() || mc.level == null) return matches;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        int minY = Math.max(mc.level.getMinY(), centerY - VERTICAL_RADIUS);
        int maxY = Math.min(mc.level.getMaxY(), centerY + VERTICAL_RADIUS);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y <= maxY; y++) {
                    pos.set(minX + x, y, minZ + z);
                    BlockState state = chunk.getBlockState(pos);
                    if (whitelist.contains(state.getBlock())) {
                        matches.add(pos.immutable());
                    }
                }
            }
        }

        return matches;
    }

    @Override
    public void onTick() {
        if (mc.level == null || mc.player == null) return;

        BlockPos playerPos = mc.player.blockPosition();
        int centerX = playerPos.getX() >> 4;
        int centerZ = playerPos.getZ() >> 4;
        int yBucket = Math.floorDiv(playerPos.getY(), Y_BUCKET_SIZE);
        int dist = renderDistance.get();

        for (int cx = centerX - dist; cx <= centerX + dist; cx++) {
            for (int cz = centerZ - dist; cz <= centerZ + dist; cz++) {
                ChunkKey key = new ChunkKey(cx, cz, yBucket);
                if (chunks.containsKey(key)) continue;

                if (mc.level.hasChunk(cx, cz)) {
                    chunks.put(key, scanChunk(mc.level.getChunk(cx, cz), playerPos.getY()));
                }
            }
        }

        // Drop cached scans for chunks/Y-buckets that fell out of range, so memory and the render
        // loop don't grow unbounded as the player wanders.
        chunks.keySet().removeIf(key ->
            Math.abs(key.x() - centerX) > dist || Math.abs(key.z() - centerZ) > dist || key.yBucket() != yBucket
        );

        if (chunks.isEmpty()) return;

        GizmoStyle style = GizmoStyle.strokeAndFill(color.get().toArgb(), 2.0F, withAlpha(color.get(), 40));
        boolean drawTracers = tracers.get();
        Vec3 eyePos = mc.player.getEyePosition();
        int lineColor = color.get().toArgb();

        // Tracers share the same per-frame budget as the boxes (each counts toward "rendered") so
        // turning them on can't reopen the vertex-overflow crash a busy scan could otherwise cause.
        int rendered = 0;
        try (var ignored = mc.collectPerTickGizmos()) {
            outer:
            for (List<BlockPos> chunkBlocks : chunks.values()) {
                for (BlockPos pos : chunkBlocks) {
                    if (rendered++ >= MAX_RENDERED_PER_FRAME) break outer;
                    Gizmos.cuboid(pos, style).setAlwaysOnTop();

                    if (drawTracers) {
                        if (rendered++ >= MAX_RENDERED_PER_FRAME) break outer;
                        Gizmos.line(eyePos, Vec3.atCenterOf(pos), lineColor).setAlwaysOnTop();
                    }
                }
            }
        }
    }

    private static int withAlpha(GidgetColor c, int alpha) {
        return new GidgetColor(c.r, c.g, c.b, alpha).toArgb();
    }
}
