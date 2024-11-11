package dev.shadowsoffire.apotheosis;

import net.minecraft.core.BlockPos;

@Deprecated(forRemoval = true)
public class AdventureModule {

    public static final boolean DEBUG = false;

    public static void debugLog(BlockPos pos, String name) {
        if (DEBUG) Apotheosis.LOGGER.info("Generated a {} at {} {} {}", name, pos.getX(), pos.getY(), pos.getZ());
    }

}
