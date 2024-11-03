package dev.shadowsoffire.apotheosis.tiers;

import java.util.function.IntFunction;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

/**
 * World Tiers for Apothic content, each increasing the quality of loot received and the overall strength of the monsters in the world.
 * <p>
 * The final tier, Apotheosis, allows for endlessly increasing the difficulty in exchange for additional rewards, but does not
 * adjust weights or availability of content.
 */
public enum WorldTier implements StringRepresentable {
    HAVEN("haven"),
    FRONTIER("frontier"),
    ASCENT("ascent"),
    SUMMIT("summit"),
    APOTHEOSIS("apotheosis");

    public static final IntFunction<WorldTier> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<WorldTier> CODEC = StringRepresentable.fromValues(WorldTier::values);
    public static final StreamCodec<ByteBuf, WorldTier> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);

    private String name;

    private WorldTier(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
