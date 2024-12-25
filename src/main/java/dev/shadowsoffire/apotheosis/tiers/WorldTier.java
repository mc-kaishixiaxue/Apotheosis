package dev.shadowsoffire.apotheosis.tiers;

import java.util.Arrays;
import java.util.Map;
import java.util.function.IntFunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.MapCodec;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.Attachments;
import dev.shadowsoffire.apotheosis.net.WorldTierPayload;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment.Target;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugmentRegistry;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

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

    public ResourceLocation getUnlockAdvancement() {
        return switch (this) {
            case HAVEN -> Apoth.Advancements.WORLD_TIER_HAVEN;
            case FRONTIER -> Apoth.Advancements.WORLD_TIER_FRONTIER;
            case ASCENT -> Apoth.Advancements.WORLD_TIER_ASCENT;
            case SUMMIT -> Apoth.Advancements.WORLD_TIER_SUMMIT;
            case PINNACLE -> Apoth.Advancements.WORLD_TIER_PINNACLE;
        };
    }

    public static WorldTier getTier(Player player) {
        return player.getData(Attachments.WORLD_TIER);
    }

    public static void setTier(Player player, WorldTier tier) {
        WorldTier oldTier = player.getData(Attachments.WORLD_TIER);
        if (oldTier == tier) {
            return;
        }

        player.setData(Attachments.WORLD_TIER, tier);
        if (player instanceof ServerPlayer sp) {
            PacketDistributor.sendToPlayer(sp, new WorldTierPayload(tier));

            for (TierAugment aug : TierAugmentRegistry.getAugments(oldTier, Target.PLAYERS)) {
                aug.remove((ServerLevel) sp.level(), player);
            }

            for (TierAugment aug : TierAugmentRegistry.getAugments(tier, Target.PLAYERS)) {
                aug.apply((ServerLevel) sp.level(), player);
            }

            player.setData(Attachments.TIER_AUGMENTS_APPLIED, true);
        }

    }

    public static boolean isUnlocked(Player player, WorldTier tier) {
        return ApothMiscUtil.hasAdvancement(player, tier.getUnlockAdvancement());
    }

    public static <T> MapCodec<Map<WorldTier, T>> mapCodec(Codec<T> elementCodec) {
        return Codec.simpleMap(WorldTier.CODEC, elementCodec,
            Keyable.forStrings(() -> Arrays.stream(WorldTier.values()).map(StringRepresentable::getSerializedName)));
    }
}
