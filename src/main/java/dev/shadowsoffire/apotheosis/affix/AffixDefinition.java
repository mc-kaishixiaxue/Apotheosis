package dev.shadowsoffire.apotheosis.affix;

import java.util.Set;

import dev.shadowsoffire.apotheosis.loot.LootCategory;
import net.minecraft.core.HolderSet;

public record AffixDefinition(Set<LootCategory> categories, HolderSet<Affix> exclusiveSet, int weight, float quality) {

}
