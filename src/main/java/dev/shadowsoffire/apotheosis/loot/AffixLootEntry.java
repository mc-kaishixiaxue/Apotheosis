package dev.shadowsoffire.apotheosis.loot;

import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.tiers.Constraints;
import dev.shadowsoffire.apotheosis.tiers.Constraints.Constrained;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights.Weighted;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import net.minecraft.world.item.ItemStack;

/**
 * A loot entry represents a possible item that can come out of a loot roll.
 * It is classified into a type, which is used to determine possible affixes.
 *
 * @param weights     The weights for selecting this loot entry.
 * @param constraints Restrictions on when this loot entry may appear.
 * @param stack       The item stack that will be generated.
 * @param rarities    The possible rarities this entry may generate with.
 */
public record AffixLootEntry(TieredWeights weights, Constraints constraints, ItemStack stack, Set<LootRarity> rarities) implements CodecProvider<AffixLootEntry>, Weighted, Constrained {

    public static final Codec<AffixLootEntry> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            TieredWeights.CODEC.fieldOf("weights").forGetter(Weighted::weights),
            Constraints.CODEC.optionalFieldOf("constraints", Constraints.EMPTY).forGetter(Constrained::constraints),
            ItemStack.CODEC.fieldOf("stack").forGetter(AffixLootEntry::stack),
            PlaceboCodecs.setOf(LootRarity.CODEC).fieldOf("rarities").forGetter(AffixLootEntry::rarities))
        .apply(inst, AffixLootEntry::new));

    public AffixLootEntry(TieredWeights weights, ItemStack stack) {
        this(weights, Constraints.EMPTY, stack, Set.of());
    }

    public LootCategory getType() {
        return LootCategory.forItem(this.stack);
    }

    @Override
    public Codec<? extends AffixLootEntry> getCodec() {
        return CODEC;
    }

    /**
     * Always return a copy of the underlying stack so that consumers do not need to worry about creating their own.
     * <p>
     * In effectively all usecases, the stack must be copied anyway.
     */
    @Override
    public ItemStack stack() {
        return this.stack.copy();
    }

}
