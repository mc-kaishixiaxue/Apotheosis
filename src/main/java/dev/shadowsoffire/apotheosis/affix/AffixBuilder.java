package dev.shadowsoffire.apotheosis.affix;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.placebo.util.StepFunction;

/**
 * Base builder class for {@link Affix affixes}.
 */
@SuppressWarnings("unchecked")
public class AffixBuilder<T extends AffixBuilder<T>> {

    protected AffixDefinition definition;

    public T definition(AffixType type, UnaryOperator<AffixDefinition.Builder> op) {
        this.definition = op.apply(new AffixDefinition.Builder(type)).build();
        return (T) this;
    }

    public T definition(AffixType type, int weight, float quality) {
        this.definition = new AffixDefinition(type, Set.of(), TieredWeights.forAllTiers(weight, quality));
        return (T) this;
    }

    public static class ValuedAffixBuilder<T extends ValuedAffixBuilder<T>> extends AffixBuilder<T> {
        protected final Map<LootRarity, StepFunction> values = new HashMap<>();
        protected float step = 0.01F;

        public T step(float step) {
            this.step = step;
            return (T) this;
        }

        public T value(LootRarity rarity, float min, float max) {
            return this.value(rarity, StepFunction.fromBounds(min, max, this.step));
        }

        public T value(LootRarity rarity, float value) {
            return this.value(rarity, StepFunction.constant(value));
        }

        public T value(LootRarity rarity, StepFunction function) {
            this.values.put(rarity, function);
            return (T) this;
        }

    }

}
