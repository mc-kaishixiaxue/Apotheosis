package dev.shadowsoffire.apotheosis.advancements.predicates;

import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

public class SpawnEggItemPredicate implements ItemSubPredicate {

    @Override
    public boolean matches(ItemStack stack) {
        return stack.getItem() instanceof SpawnEggItem;
    }
}
