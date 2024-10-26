package dev.shadowsoffire.apotheosis.affix;

import java.util.function.Function;

import com.mojang.serialization.Codec;

import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.floats.FloatCollection;
import it.unimi.dsi.fastutil.floats.FloatCollections;
import it.unimi.dsi.fastutil.objects.Object2FloatMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/**
 * Immutable data holder for the affixes on an {@link ItemStack}.
 * <p>
 * This object cannot be used to evaluate affixes, as evaluation requires context of the owning item stack.
 * Instead, use the map provided by {@link AffixHelper#getAffixes(ItemStack)}.
 */
public final class ItemAffixes {

    public static final Codec<ItemAffixes> CODEC = Codec.unboundedMap(AffixRegistry.INSTANCE.holderCodec(), Codec.floatRange(0, 1)).xmap(Object2FloatOpenHashMap::new, Function.identity()).xmap(ItemAffixes::new, i -> i.affixes);

    public static final StreamCodec<ByteBuf, ItemAffixes> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.map(Object2FloatOpenHashMap::new, AffixRegistry.INSTANCE.holderStreamCodec(), ByteBufCodecs.FLOAT),
        ia -> ia.affixes,
        ItemAffixes::new);

    public static final ItemAffixes EMPTY = new ItemAffixes(new Object2FloatOpenHashMap<>());

    private final Object2FloatOpenHashMap<DynamicHolder<Affix>> affixes;

    private ItemAffixes(Object2FloatOpenHashMap<DynamicHolder<Affix>> affixes) {
        this.affixes = affixes;

        for (Entry<DynamicHolder<Affix>> entry : affixes.object2FloatEntrySet()) {
            float level = entry.getFloatValue();
            if (level < 0 || level > 255) {
                throw new IllegalArgumentException("Affix " + entry.getKey() + " has invalid level " + level);
            }
        }
    }

    public float getLevel(DynamicHolder<Affix> key) {
        return this.affixes.getFloat(key);
    }

    public boolean isEmpty() {
        return this.affixes.isEmpty();
    }

    public int size() {
        return this.affixes.size();
    }

    public ObjectSet<Entry<DynamicHolder<Affix>>> entrySet() {
        return ObjectSets.unmodifiable(this.affixes.object2FloatEntrySet());
    }

    public ObjectSet<DynamicHolder<Affix>> keySet() {
        return ObjectSets.unmodifiable(this.affixes.keySet());
    }

    public FloatCollection values() {
        return FloatCollections.unmodifiable(this.affixes.values());
    }

    public boolean containsKey(Object key) {
        return this.affixes.containsKey(key);
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof ItemAffixes iafxs && iafxs.affixes.equals(this.affixes);
    }

    @Override
    public int hashCode() {
        return this.affixes.hashCode();
    }

    @Override
    public String toString() {
        return "ItemAffixes{affixes=" + this.affixes + "}";
    }

}
