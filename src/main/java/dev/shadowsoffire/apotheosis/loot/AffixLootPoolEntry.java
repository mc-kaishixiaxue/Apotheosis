package dev.shadowsoffire.apotheosis.loot;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * An Affix Loot Pool Entry allows explicit injection of affix loot into a loot table.
 * <p>
 * This is independent of any {@link LootModifier}s that may apply affix loot into loot tables.
 */
public class AffixLootPoolEntry extends ContextualLootPoolEntry {

    public static final MapCodec<AffixLootPoolEntry> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        PlaceboCodecs.setOf(RarityRegistry.INSTANCE.holderCodec()).optionalFieldOf("rarities", Set.of()).forGetter(a -> a.rarities),
        PlaceboCodecs.setOf(AffixLootRegistry.INSTANCE.holderCodec()).optionalFieldOf("entries", Set.of()).forGetter(a -> a.entries))
        .and(LootPoolSingletonContainer.singletonFields(inst))
        .apply(inst, AffixLootPoolEntry::new));

    public static final LootPoolEntryType TYPE = new LootPoolEntryType(CODEC);

    private final Set<DynamicHolder<LootRarity>> rarities;
    private final Set<DynamicHolder<AffixLootEntry>> entries;

    /**
     * Creates a new affix loot pool entry.
     *
     * @param rarities   A set of rarities. If non-empty, this set overrides the rarities specified by the selected affix loot entry.
     * @param entries    A set of entries. If non-empty, this set specifies the subset of affix loot entries that may be used, otherwise all entries are tested.
     * @param weight     The weight of this entry, relative to others in the pool.
     * @param quality    The quality of this entry, relative to others in the pool.
     * @param conditions Loot conditions requires to trigger this entry.
     * @param functions  Functions to apply to the generated loot.
     */
    public AffixLootPoolEntry(Set<DynamicHolder<LootRarity>> rarities, Set<DynamicHolder<AffixLootEntry>> entries, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
        super(weight, quality, conditions, functions);
        this.rarities = rarities;
        this.entries = entries;
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> list, LootContext ctx, GenContext gCtx) {
        ItemStack stack;
        if (this.entries.isEmpty()) {
            LootRarity selectedRarity = LootRarity.randomFromHolders(gCtx, this.rarities);
            stack = LootController.createRandomLootItem(gCtx, selectedRarity);
        }
        else {
            Set<AffixLootEntry> resolved = this.entries.stream().filter(AffixLootPoolEntry::checkBound).map(DynamicHolder::get).collect(Collectors.toSet());
            AffixLootEntry entry = AffixLootRegistry.INSTANCE.getRandomItem(gCtx, resolved::contains);
            LootRarity rarity;

            if (this.rarities.isEmpty()) {
                rarity = LootRarity.random(gCtx, entry.rarities());
            }
            else {
                rarity = LootRarity.randomFromHolders(gCtx, this.rarities);
            }

            stack = LootController.createLootItem(entry.stack().copy(), rarity, gCtx);
        }
        if (!stack.isEmpty()) list.accept(stack);
    }

    @Override
    public LootPoolEntryType getType() {
        return TYPE;
    }

    public static LootPoolSingletonContainer.Builder<?> builder(Set<DynamicHolder<LootRarity>> rarities, Set<DynamicHolder<AffixLootEntry>> entries) {
        return LootPoolSingletonContainer.simpleBuilder(ctor(rarities, entries));
    }

    private static EntryConstructor ctor(Set<DynamicHolder<LootRarity>> rarities, Set<DynamicHolder<AffixLootEntry>> entries) {
        return (weight, quality, conditions, functions) -> new AffixLootPoolEntry(rarities, entries, weight, quality, conditions, functions);
    }

    /**
     * Unwraps the holder to its object, if present, otherwise returns null and logs an error.
     */
    private static boolean checkBound(DynamicHolder<AffixLootEntry> holder) {
        if (!holder.isBound()) {
            Apotheosis.LOGGER.error("An AffixLootPoolEntry failed to resolve the Affix Loot Entry {}!", holder.getId());
            return false;
        }
        return true;
    }

}
