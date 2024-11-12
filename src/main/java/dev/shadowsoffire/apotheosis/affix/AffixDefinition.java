package dev.shadowsoffire.apotheosis.affix;

import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.reload.DynamicHolder;

// TODO: Replace AffixType with a data-driven grouping system for more expressive LootRules.
public record AffixDefinition(AffixType type, Set<DynamicHolder<Affix>> exclusiveSet, TieredWeights weights) {

    public static final Codec<AffixDefinition> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(inst -> inst.group(
        AffixType.CODEC.fieldOf("affix_type").forGetter(AffixDefinition::type),
        PlaceboCodecs.setOf(AffixRegistry.INSTANCE.holderCodec()).fieldOf("exclusive_set").forGetter(AffixDefinition::exclusiveSet),
        TieredWeights.CODEC.fieldOf("weights").forGetter(AffixDefinition::weights))
        .apply(inst, AffixDefinition::new)));

}
