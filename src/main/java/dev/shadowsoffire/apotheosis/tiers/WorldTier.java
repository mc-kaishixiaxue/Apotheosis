package dev.shadowsoffire.apotheosis.tiers;

import java.util.Arrays;
import java.util.Map;
import java.util.function.IntFunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.MapCodec;

import dev.shadowsoffire.apotheosis.Apoth.Attachments;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;

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
    PINNACLE("pinnacle");

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

    public static WorldTier getTier(Player player) {
        return player.getData(Attachments.WORLD_TIER);
    }

    public static void setTier(Player player, WorldTier tier) {
        player.setData(Attachments.WORLD_TIER, tier);
    }

    public static <T> MapCodec<Map<WorldTier, T>> mapCodec(Codec<T> elementCodec) {
        return Codec.simpleMap(WorldTier.CODEC, elementCodec,
            Keyable.forStrings(() -> Arrays.stream(WorldTier.values()).map(StringRepresentable::getSerializedName)));
    }
}
