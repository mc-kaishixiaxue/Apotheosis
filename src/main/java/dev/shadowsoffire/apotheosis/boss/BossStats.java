package dev.shadowsoffire.apotheosis.boss;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.json.ChancedEffectInstance;
import dev.shadowsoffire.placebo.json.RandomAttributeModifier;

/**
 * Boss Stats, aka everything that a boss might need to buff itself.
 *
 * @param enchantChance Specifies the chance that boss items (aside from the affix item) are enchanted.
 * @param enchLevels    The enchantment levels to use for the boss's items.
 * @param effects       List of effects that could be applied to this boss. May be empty, but may not be null.
 * @param modifiers     List of attribute modifiers to apply to this boss when spawned. May be empty, but may not be null.
 */
public record BossStats(float enchantChance, EnchantmentLevels enchLevels, List<ChancedEffectInstance> effects, List<RandomAttributeModifier> modifiers) {

    public static final Codec<BossStats> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            Codec.FLOAT.fieldOf("enchant_chance").forGetter(BossStats::enchantChance),
            EnchantmentLevels.CODEC.fieldOf("enchantment_levels").forGetter(BossStats::enchLevels),
            ChancedEffectInstance.CODEC.listOf().fieldOf("effects").forGetter(BossStats::effects),
            RandomAttributeModifier.CODEC.listOf().fieldOf("attribute_modifiers").forGetter(BossStats::modifiers))
        .apply(inst, BossStats::new));

    /**
     * Enchantment levels for boss equipment.
     *
     * @param primary   The enchantment level to use for the primary (affixed) item.
     * @param secondary The enchantment level to use for all other items.
     */
    public static record EnchantmentLevels(int primary, int secondary) {

        public static final Codec<EnchantmentLevels> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                Codec.INT.fieldOf("primary").forGetter(EnchantmentLevels::primary),
                Codec.INT.fieldOf("secondary").forGetter(EnchantmentLevels::secondary))
            .apply(inst, EnchantmentLevels::new));
    }

}
