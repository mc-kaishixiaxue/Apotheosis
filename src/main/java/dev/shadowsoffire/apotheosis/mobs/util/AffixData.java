package dev.shadowsoffire.apotheosis.mobs.util;

import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.mobs.types.Elite;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;

/**
 * Affix data for {@link Elite}.
 *
 * @param enabled  If one of the miniboss's items (from the selected gear set) will become affixes.
 * @param rarities A pool of rarities; if empty, all rarities will be used.
 */
public record AffixData(float chance, Set<LootRarity> rarities) {

    public static final AffixData DEFAULT = new AffixData(-1, Set.of());

    public static final Codec<AffixData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        Codec.FLOAT.fieldOf("affix_chance").forGetter(AffixData::chance),
        PlaceboCodecs.setOf(LootRarity.CODEC).optionalFieldOf("rarities", Set.of()).forGetter(AffixData::rarities))
        .apply(inst, AffixData::new));

}
