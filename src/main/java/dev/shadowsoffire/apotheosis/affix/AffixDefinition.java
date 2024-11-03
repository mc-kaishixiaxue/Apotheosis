package dev.shadowsoffire.apotheosis.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth.Registries;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;

// TODO: Replace AffixType with a data-driven grouping system for more expressive LootRules.
public record AffixDefinition(AffixType type, HolderSet<Affix> exclusiveSet, TieredWeights weights) {

    public static final Codec<AffixDefinition> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        AffixType.CODEC.fieldOf("affix_type").forGetter(AffixDefinition::type),
        RegistryCodecs.homogeneousList(Registries.AFFIX).fieldOf("exclusive_set").forGetter(AffixDefinition::exclusiveSet),
        TieredWeights.CODEC.fieldOf("weights").forGetter(AffixDefinition::weights))
        .apply(inst, AffixDefinition::new));

}
