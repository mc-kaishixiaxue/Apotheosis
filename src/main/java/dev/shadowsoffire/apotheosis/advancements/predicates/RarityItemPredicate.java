package dev.shadowsoffire.apotheosis.advancements.predicates;

import java.util.Set;

import com.mojang.serialization.Codec;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public record RarityItemPredicate(Set<DynamicHolder<LootRarity>> rarities) implements SingleComponentItemPredicate<DynamicHolder<LootRarity>>, TypeAwareISP<RarityItemPredicate> {

    public static final Codec<RarityItemPredicate> CODEC = PlaceboCodecs.setOf(RarityRegistry.INSTANCE.holderCodec()).fieldOf("rarities").xmap(RarityItemPredicate::new, RarityItemPredicate::rarities).codec();

    @Override
    public DataComponentType<DynamicHolder<LootRarity>> componentType() {
        return Components.RARITY;
    }

    @Override
    public boolean matches(ItemStack stack, DynamicHolder<LootRarity> rarity) {
        return rarity.isBound() && this.rarities.contains(rarity);
    }

    @Override
    public Type<RarityItemPredicate> type() {
        return Apoth.ItemSubPredicates.ITEM_WITH_RARITY;
    }
}
