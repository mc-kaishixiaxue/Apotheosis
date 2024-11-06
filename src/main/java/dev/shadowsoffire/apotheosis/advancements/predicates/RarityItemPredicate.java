package dev.shadowsoffire.apotheosis.advancements.predicates;

import java.util.Set;

import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public record RarityItemPredicate(Set<DynamicHolder<LootRarity>> rarities) implements SingleComponentItemPredicate<DynamicHolder<LootRarity>> {

    @Override
    public DataComponentType<DynamicHolder<LootRarity>> componentType() {
        return Components.RARITY;
    }

    @Override
    public boolean matches(ItemStack stack, DynamicHolder<LootRarity> rarity) {
        return rarity.isBound() && rarities.contains(rarity);
    }
}
