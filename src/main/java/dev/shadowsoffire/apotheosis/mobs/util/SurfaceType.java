package dev.shadowsoffire.apotheosis.mobs.util;

import java.util.function.BiPredicate;

import com.mojang.serialization.Codec;

import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public enum SurfaceType implements BiPredicate<ServerLevelAccessor, BlockPos> {
    NEEDS_SKY(ServerLevelAccessor::canSeeSky),
    NEEDS_SURFACE(
        (level, pos) -> pos.getY() >= level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ())),
    BELOW_SURFACE(
        (level, pos) -> pos.getY() < level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ())),
    CANNOT_SEE_SKY((level, pos) -> !level.canSeeSky(pos)),
    SURFACE_OUTER_END(
        (level, pos) -> NEEDS_SURFACE.test(level, pos) && (Mth.abs(pos.getX()) > 1024 || Mth.abs(pos.getZ()) > 1024)),
    ANY((level, pos) -> true);

    public static final Codec<SurfaceType> CODEC = PlaceboCodecs.enumCodec(SurfaceType.class);

    BiPredicate<ServerLevelAccessor, BlockPos> pred;

    private SurfaceType(BiPredicate<ServerLevelAccessor, BlockPos> pred) {
        this.pred = pred;
    }

    @Override
    public boolean test(ServerLevelAccessor t, BlockPos u) {
        return this.pred.test(t, u);
    }
}
