package dev.shadowsoffire.apotheosis.loot;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

public class LootRarity implements CodecProvider<LootRarity>, Weighted {

    public static final Codec<LootRarity> LOAD_CODEC = RecordCodecBuilder.create(inst -> inst.group(
        TextColor.CODEC.fieldOf("color").forGetter(LootRarity::getColor),
        ItemStack.ITEM_NON_AIR_CODEC.fieldOf("material").forGetter(r -> r.material),
        TieredWeights.CODEC.fieldOf("weights").forGetter(Weighted::weights),
        LootRule.CODEC.listOf().fieldOf("rules").forGetter(LootRarity::getRules))
        .apply(inst, LootRarity::new));

    /**
     * Direct resolution codec. Only for use in other datapack objects which load after the {@link RarityRegistry}.
     */
    public static final Codec<LootRarity> CODEC = Codec.lazyInitialized(() -> RarityRegistry.INSTANCE.holderCodec().xmap(DynamicHolder::get, RarityRegistry.INSTANCE::holder));

    private final Holder<Item> material;
    private final TextColor color;
    private final TieredWeights weights;
    private final List<LootRule> rules; // TODO: Map<LootCategory, List<LootRule>> to permit per-category rules

    public LootRarity(TextColor color, Holder<Item> material, TieredWeights weights, List<LootRule> rules) {
        this.color = color;
        this.material = material;
        this.weights = weights;
        this.rules = rules;
    }

    public Item getMaterial() {
        return this.material.value();
    }

    public TextColor getColor() {
        return this.color;
    }

    @Override
    public TieredWeights weights() {
        return this.weights;
    }

    public List<LootRule> getRules() {
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
    public Codec<? extends LootRarity> getCodec() {
        return LOAD_CODEC;
    }

    public static LootRarity random(GenContext ctx) {
        return RarityRegistry.INSTANCE.getRandomItem(ctx);
    }

    public static LootRarity random(GenContext ctx, Set<LootRarity> pool) {
        return RarityRegistry.INSTANCE.getRandomItem(ctx, pool.isEmpty() ? Predicates.alwaysTrue() : pool::contains);
    }

    public static LootRarity randomFromHolders(GenContext ctx, Set<DynamicHolder<LootRarity>> pool) {
        return random(ctx, pool.stream().filter(DynamicHolder::isBound).map(DynamicHolder::get).collect(Collectors.toSet()));
    }

    public static <T> Codec<Map<LootRarity, T>> mapCodec(Codec<T> codec) {
        return Codec.unboundedMap(LootRarity.CODEC, codec);
    }
}
