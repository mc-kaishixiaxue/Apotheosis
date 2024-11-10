package dev.shadowsoffire.apotheosis.tiers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Logger;

import com.google.common.base.Predicates;

import dev.shadowsoffire.apotheosis.tiers.TieredWeights.Weighted;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.util.random.WeightedRandom;

public abstract class TieredDynamicRegistry<V extends CodecProvider<? super V> & Weighted> extends DynamicRegistry<V> {

    public TieredDynamicRegistry(Logger logger, String path, boolean synced, boolean subtypes) {
        super(logger, path, synced, subtypes);
    }

    /**
     * Gets a random item from this manager, re-calculating the weights based on luck.
     */
    @Nullable
    public V getRandomItem(GenContext ctx) {
        return this.getRandomItem(ctx, Predicates.alwaysTrue());
    }

    /**
     * Gets a random item from this manager, re-calculating the weights based on luck and omitting items based on a filter.
     */
    @Nullable
    @SafeVarargs
    public final V getRandomItem(GenContext ctx, Predicate<? super V>... filters) {
        List<Wrapper<V>> list = new ArrayList<>(this.registry.size());
        var stream = this.registry.values().stream();
        for (Predicate<? super V> filter : filters) {
            stream = stream.filter(filter);
        }
        stream.map(l -> l.<V>wrap(ctx.tier(), ctx.luck())).forEach(list::add);
        return WeightedRandom.getRandomItem(ctx.rand(), list).map(Wrapper::data).orElse(null);
    }

}
