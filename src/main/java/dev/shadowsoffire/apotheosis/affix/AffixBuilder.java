package dev.shadowsoffire.apotheosis.affix;

import java.util.Set;
import java.util.function.UnaryOperator;

import dev.shadowsoffire.apotheosis.tiers.TieredWeights;

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

}
