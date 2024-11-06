package dev.shadowsoffire.apotheosis.advancements.predicates;

import java.util.Set;

import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public record PurityItemPredicate(Set<Purity> purities) implements SingleComponentItemPredicate<Purity> {

    @Override
    public DataComponentType<Purity> componentType() {
        return Components.PURITY;
    }

    @Override
    public boolean matches(ItemStack stack, Purity value) {
        return this.purities.contains(value);
    }
}
