package dev.shadowsoffire.apotheosis.loot;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights.Weighted;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record LootRarity(TextColor color, Holder<Item> material, TieredWeights weights, List<LootRule> rules, Map<LootCategory, List<LootRule>> overrides) implements CodecProvider<LootRarity>, Weighted {

    public static final Codec<LootRarity> LOAD_CODEC = RecordCodecBuilder.create(inst -> inst.group(
        TextColor.CODEC.fieldOf("color").forGetter(LootRarity::color),
        ItemStack.ITEM_NON_AIR_CODEC.fieldOf("material").forGetter(LootRarity::material),
        TieredWeights.CODEC.fieldOf("weights").forGetter(Weighted::weights),
        LootRule.CODEC.listOf().fieldOf("rules").forGetter(LootRarity::rules),
        LootCategory.mapCodec(LootRule.CODEC.listOf()).fieldOf("overrides").forGetter(LootRarity::overrides))
        .apply(inst, LootRarity::new));

    /**
     * Direct resolution codec. Only for use in other datapack objects which load after the {@link RarityRegistry}.
     */
    public static final Codec<LootRarity> CODEC = Codec.lazyInitialized(() -> RarityRegistry.INSTANCE.holderCodec().xmap(DynamicHolder::get, RarityRegistry.INSTANCE::holder));

    public Item getMaterial() {
        return this.material.value();
    }

    public List<LootRule> getRules(LootCategory category) {
        return this.rules;
    }

    public Component toComponent() {
        return Component.translatable("rarity." + RarityRegistry.INSTANCE.getKey(this)).withStyle(Style.EMPTY.withColor(this.color));
    }

    @Override
    public String toString() {
        return "LootRarity{" + RarityRegistry.INSTANCE.getKey(this) + "}";
    }

    @Override
    public Codec<LootRarity> getCodec() {
        return LOAD_CODEC;
    }

    @Nullable
    public static LootRarity random(GenContext ctx) {
        return RarityRegistry.INSTANCE.getRandomItem(ctx);
    }

    @Nullable
    public static LootRarity random(GenContext ctx, Set<LootRarity> pool) {
        return RarityRegistry.INSTANCE.getRandomItem(ctx, pool.isEmpty() ? Predicates.alwaysTrue() : pool::contains);
    }

    @Nullable
    public static LootRarity randomFromHolders(GenContext ctx, Set<DynamicHolder<LootRarity>> pool) {
        return random(ctx, pool.stream().filter(DynamicHolder::isBound).map(DynamicHolder::get).collect(Collectors.toSet()));
    }

    public static <T> Codec<Map<LootRarity, T>> mapCodec(Codec<T> codec) {
        return Codec.unboundedMap(LootRarity.CODEC, codec);
    }
}
