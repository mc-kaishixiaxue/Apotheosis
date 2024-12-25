package dev.shadowsoffire.apotheosis.loot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.tiers.TieredDynamicRegistry;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Handles loading the configurable portion of rarities.
 */
public class RarityRegistry extends TieredDynamicRegistry<LootRarity> {

    public static final RarityRegistry INSTANCE = new RarityRegistry();

    protected BiMap<Item, DynamicHolder<LootRarity>> materialMap = HashBiMap.create();
    protected List<LootRarity> sorted = new ArrayList<>();

    private RarityRegistry() {
        super(Apotheosis.LOGGER, "rarities", true, false);
    }

    /**
     * Checks if a given item is a rarity material.
     *
     * @param stack The item being checked.
     * @return True if the item is a rarity material.
     */
    public static boolean isMaterial(Item item) {
        return getMaterialRarity(item).isBound();
    }

    /**
     * Returns the rarity associated with the passed rarity material.
     * <p>
     * May be unbound.
     */
    public static DynamicHolder<LootRarity> getMaterialRarity(Item item) {
        return INSTANCE.materialMap.getOrDefault(item, INSTANCE.emptyHolder());
    }

    @Override
    protected void beginReload() {
        super.beginReload();
        this.materialMap = HashBiMap.create();
        this.sorted.clear();
    }

    @Override
    protected void onReload() {
        super.onReload();
        for (LootRarity r : this.getValues()) {
            DynamicHolder<LootRarity> old = this.materialMap.put(r.getMaterial(), this.holder(r));
            if (old != null) {
                throw new RuntimeException("Two rarities may not share the same rarity material: " + this.getKey(r) + " conflicts with " + old.getId());
            }
            this.sorted.add(r);
        }
        this.sorted.sort(Comparator.comparing(LootRarity::sortIndex));

    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(Apotheosis.loc("rarity"), LootRarity.LOAD_CODEC);
    }

    @Override
    protected void validateItem(ResourceLocation key, LootRarity item) {
        super.validateItem(key, item);
        Preconditions.checkNotNull(item.color());
        Preconditions.checkArgument(item.getMaterial() != null && item.getMaterial() != Items.AIR);
        Preconditions.checkArgument(!item.rules().isEmpty(), "A rarity must provide base rules.");
    }

    /**
     * Returns a component that contains all the weighted drop chances for each rarity (ignoring luck).
     */
    public static Component getDropChances(WorldTier tier) {
        int totalWeight = INSTANCE.registry.values().stream().mapToInt(r -> r.weights().getWeight(tier, 0)).sum();

        MutableComponent out = Component.empty();
        for (int i = 0; i < INSTANCE.sorted.size(); i++) {
            LootRarity rarity = INSTANCE.sorted.get(i);
            float percent = rarity.weights().getWeight(tier, 0) / (float) totalWeight;
            Component comp = Component.translatable("%s", Affix.fmt(100 * percent) + "%").withStyle(s -> s.withColor(rarity.color()));
            out.append(comp);
            if (i != INSTANCE.sorted.size() - 1) {
                out.append(Component.literal(" / "));
            }
        }

        return out;
    }

}
