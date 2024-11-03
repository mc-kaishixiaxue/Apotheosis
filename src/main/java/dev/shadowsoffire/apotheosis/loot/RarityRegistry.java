package dev.shadowsoffire.apotheosis.loot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import dev.shadowsoffire.apotheosis.AdventureModule;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.affix.AffixType;
import dev.shadowsoffire.apotheosis.loot.LootRarity.LootRule;
import dev.shadowsoffire.apotheosis.tiers.TieredDynamicRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Handles loading the configurable portion of rarities.
 */
public class RarityRegistry extends TieredDynamicRegistry<LootRarity> {

    public static final RarityRegistry INSTANCE = new RarityRegistry();

    protected BiMap<Item, DynamicHolder<LootRarity>> materialMap = HashBiMap.create();

    private RarityRegistry() {
        super(AdventureModule.LOGGER, "rarities", true, false);
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
    }

    @Override
    protected void onReload() {
        super.onReload();
        for (LootRarity r : this.getValues()) {
            DynamicHolder<LootRarity> old = this.materialMap.put(r.getMaterial(), holder(r));
            if (old != null) {
                throw new RuntimeException("Two rarities may not share the same rarity material: " + this.getKey(r) + " conflicts with " + old.getId());
            }
        }
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(Apotheosis.loc("rarity"), LootRarity.LOAD_CODEC);
    }

    @Override
    protected void validateItem(ResourceLocation key, LootRarity item) {
        super.validateItem(key, item);
        Preconditions.checkNotNull(item.getColor());
        Preconditions.checkArgument(item.getMaterial() != null && item.getMaterial() != Items.AIR);
        Preconditions.checkArgument(!item.getRules().isEmpty(), "A rarity may not have no rules!");
    }

    public void validateLootRules() {
        for (LootRarity rarity : this.registry.values()) {
            Map<AffixType, List<LootRule>> sorted = new HashMap<>();
            rarity.getRules().stream().filter(r -> r.type().needsValidation()).forEach(rule -> {
                sorted.computeIfAbsent(rule.type(), r -> new ArrayList<>());
                sorted.get(rule.type()).add(rule);
            });
            sorted.forEach((type, rules) -> {
                for (LootCategory cat : LootCategory.VALUES) {
                    if (cat.isNone()) continue;
                    List<Affix> affixes = AffixRegistry.INSTANCE.getValues().stream().filter(a -> a.canApplyTo(ItemStack.EMPTY, cat, rarity) && a.getType() == type).toList();

                    if (affixes.size() < rules.size()) {
                        var errMsg = new StringBuilder();
                        errMsg.append("Insufficient number of affixes to satisfy the loot rules (ignoring backup rules) of rarity " + this.getKey(rarity) + " for category " + cat.getName());
                        errMsg.append("Required: " + rules.size());
                        errMsg.append("; Provided: " + affixes.size());
                        // errMsg.append("The following affixes exist for this category/rarity combination: ");
                        // affixes.forEach(a -> errMsg.append(a.getId() + " "));
                        AdventureModule.LOGGER.error(errMsg.toString());
                    }
                }
            });
        }
    }

}
