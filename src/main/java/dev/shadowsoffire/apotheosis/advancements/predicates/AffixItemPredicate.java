package dev.shadowsoffire.apotheosis.advancements.predicates;

import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.affix.ItemAffixes;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public class AffixItemPredicate implements SingleComponentItemPredicate<ItemAffixes> {

    @Override
    public DataComponentType<ItemAffixes> componentType() {
        return Components.AFFIXES;
    }

    @Override
    public boolean matches(ItemStack stack, ItemAffixes value) {
        return !value.isEmpty();
    }

}
