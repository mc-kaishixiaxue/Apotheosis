package dev.shadowsoffire.apotheosis.affix;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.util.CachedObject;
import dev.shadowsoffire.placebo.util.CachedObject.CachedObjectSource;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;

public class AffixHelper {

    public static final ResourceLocation AFFIX_CACHED_OBJECT = Apotheosis.loc("affixes");

    public static final String DISPLAY = "display";
    public static final String LORE = "Lore";

    public static final String AFFIX_DATA = "affix_data";
    public static final String AFFIXES = "affixes";
    public static final String RARITY = "rarity";
    public static final String NAME = "name";

    // Used to encode the loot category of the shooting item on arrows.
    public static final String CATEGORY = "category";

    /**
     * Adds this specific affix to the Item's NBT tag.
     */
    public static void applyAffix(ItemStack stack, AffixInstance inst) {
        ItemAffixes.Builder builder = stack.getOrDefault(Components.AFFIXES, ItemAffixes.EMPTY).toBuilder();
        builder.upgrade(inst.affix(), inst.level());
        setAffixes(stack, builder.build());
    }

    public static void setAffixes(ItemStack stack, ItemAffixes affixes) {
        stack.set(Components.AFFIXES, affixes);
    }

    public static void setName(ItemStack stack, Component name) {
        stack.set(Components.AFFIX_NAME, name.copy());
    }

    @Nullable
    public static Component getName(ItemStack stack) {
        return stack.get(Components.AFFIX_NAME);
    }

    /**
     * Gets the affixes of an item. The returned map is immutable.
     * <p>
     * Due to potential reloads, it is possible for an affix instance to become unbound but still remain cached.
     *
     * @param stack The stack being queried.
     * @return An immutable map of all affixes on the stack, or an empty map if none were found.
     * @apiNote Prefer using {@link #streamAffixes(ItemStack)} where applicable, since invalid instances will be pre-filtered.
     */
    public static Map<DynamicHolder<Affix>, AffixInstance> getAffixes(ItemStack stack) {
        if (AffixRegistry.INSTANCE.getValues().isEmpty()) return Collections.emptyMap(); // Don't enter getAffixesImpl if the affixes haven't loaded yet.
        return CachedObjectSource.getOrCreate(stack, AFFIX_CACHED_OBJECT, AffixHelper::getAffixesImpl, CachedObject.hashComponents(Components.AFFIXES, Components.RARITY));
    }

    public static Map<DynamicHolder<Affix>, AffixInstance> getAffixesImpl(ItemStack stack) {
        if (stack.isEmpty()) return Collections.emptyMap();
        Map<DynamicHolder<Affix>, AffixInstance> map = new HashMap<>();
        ItemAffixes affixes = stack.getOrDefault(Components.AFFIXES, ItemAffixes.EMPTY);
        if (!affixes.isEmpty()) {
            DynamicHolder<LootRarity> rarity = getRarity(stack);
            if (!rarity.isBound()) {
                rarity = RarityRegistry.getMinRarity();
            }
            LootCategory cat = LootCategory.forItem(stack);
            for (DynamicHolder<Affix> affix : affixes.keySet()) {
                if (!affix.isBound() || !affix.get().canApplyTo(stack, cat, rarity.get())) continue;
                float lvl = affixes.getLevel(affix);
                map.put(affix, new AffixInstance(affix, stack, rarity, lvl));
            }
        }
        return Collections.unmodifiableMap(map);
    }

    public static Stream<AffixInstance> streamAffixes(ItemStack stack) {
        return getAffixes(stack).values().stream().filter(AffixInstance::isValid);
    }

    public static boolean hasAffixes(ItemStack stack) {
        return !getAffixes(stack).isEmpty();
    }

    public static void setRarity(ItemStack stack, LootRarity rarity) {
        stack.set(Components.RARITY, RarityRegistry.INSTANCE.holder(rarity));
    }

    public static void copyFrom(ItemStack stack, Entity entity) {
        if (stack.hasTag() && stack.getTagElement(AFFIX_DATA) != null) {
            CompoundTag afxData = stack.getTagElement(AFFIX_DATA).copy();
            afxData.putString(CATEGORY, LootCategory.forItem(stack).getName());
            entity.getPersistentData().put(AFFIX_DATA, afxData);
        }
    }

    @Nullable
    public static LootCategory getShooterCategory(Entity entity) {
        CompoundTag afxData = entity.getPersistentData().getCompound(AFFIX_DATA);
        if (afxData != null && afxData.contains(CATEGORY)) {
            return LootCategory.byId(afxData.getString(CATEGORY));
        }
        return null;
    }

    public static Map<DynamicHolder<? extends Affix>, AffixInstance> getAffixes(AbstractArrow arrow) {
        Map<DynamicHolder<? extends Affix>, AffixInstance> map = new HashMap<>();
        CompoundTag afxData = arrow.getPersistentData().getCompound(AFFIX_DATA);

        if (afxData != null && afxData.contains(AFFIXES)) {
            CompoundTag affixes = afxData.getCompound(AFFIXES);
            DynamicHolder<LootRarity> rarity = getRarity(afxData);
            if (!rarity.isBound()) rarity = RarityRegistry.getMinRarity();
            for (String key : affixes.getAllKeys()) {
                DynamicHolder<Affix> affix = AffixRegistry.INSTANCE.holder(new ResourceLocation(key));
                if (!affix.isBound()) continue;
                float lvl = affixes.getFloat(key);
                map.put(affix, new AffixInstance(affix, ItemStack.EMPTY, rarity, lvl));
            }
        }
        return map;
    }

    public static Stream<AffixInstance> streamAffixes(AbstractArrow arrow) {
        return getAffixes(arrow).values().stream();
    }

    /**
     * May be unbound
     */
    public static DynamicHolder<LootRarity> getRarity(ItemStack stack) {
        return stack.getOrDefault(Components.RARITY, RarityRegistry.INSTANCE.emptyHolder());
    }

    /**
     * May be unbound
     */
    public static DynamicHolder<LootRarity> getRarity(@Nullable CompoundTag afxData) {
        if (afxData != null) {
            try {
                return RarityRegistry.byLegacyId(afxData.getString(RARITY));
            }
            catch (IllegalArgumentException e) {
                afxData.remove(RARITY);
                return RarityRegistry.byLegacyId("empty");
            }
        }
        return RarityRegistry.INSTANCE.emptyHolder();
    }

    public static Collection<DynamicHolder<Affix>> byType(AffixType type) {
        return AffixRegistry.INSTANCE.getTypeMap().get(type);
    }

    public static StepFunction step(float min, int steps, float step) {
        return new StepFunction(min, steps, step);
    }

}
