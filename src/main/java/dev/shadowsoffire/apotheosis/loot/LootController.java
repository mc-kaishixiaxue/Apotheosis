package dev.shadowsoffire.apotheosis.loot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.affix.AffixType;
import dev.shadowsoffire.apotheosis.affix.ItemAffixes;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;

public class LootController {

    /**
     * @see {@link LootController#createLootItem(ItemStack, LootCategory, LootRarity, Random)}
     */
    public static ItemStack createLootItem(ItemStack stack, LootRarity rarity, RandomSource rand, WorldTier tier, float luck) {
        LootCategory cat = LootCategory.forItem(stack);
        if (cat.isNone()) return stack;
        return createLootItem(stack, cat, rarity, rand, tier, luck);
    }

    static Random jRand = new Random();

    /**
     * Modifies an ItemStack with affixes of the target category and rarity.
     *
     * @param stack  The ItemStack.
     * @param cat    The LootCategory. Should be valid for the item being passed.
     * @param rarity The target Rarity.
     * @param rand   The Random
     * @return The modifed ItemStack (note the original is not preserved, but the stack is returned for simplicity).
     */
    public static ItemStack createLootItem(ItemStack stack, LootCategory cat, LootRarity rarity, RandomSource rand, WorldTier tier, float luck) {
        stack.set(Components.AFFIXES, ItemAffixes.EMPTY);
        AffixHelper.setRarity(stack, rarity);

        for (LootRule rule : rarity.getRules()) {
            rule.execute(stack, rand, rarity, tier, luck);
        }

        ItemAffixes loaded = stack.getOrDefault(Components.AFFIXES, ItemAffixes.EMPTY);
        if (loaded.size() == 0) {
            throw new RuntimeException(String.format("Failed to locate any affixes for %s{%s} with category %s and rarity %s.", stack.getItem(), stack.getComponents(), cat, rarity));
        }

        List<Affix> nameList = new ArrayList<>(loaded.size());
        for (DynamicHolder<Affix> a : loaded.keySet()) {
            nameList.add(a.get());
        }

        jRand.setSeed(rand.nextLong());
        Collections.shuffle(nameList, jRand);
        String key = nameList.size() > 1 ? "misc.apotheosis.affix_name.three" : "misc.apotheosis.affix_name.two";
        MutableComponent name = Component.translatable(key, nameList.get(0).getName(true), "", nameList.size() > 1 ? nameList.get(1).getName(false) : "").withStyle(Style.EMPTY.withColor(rarity.getColor()));
        AffixHelper.setName(stack, name);

        return stack;
    }

    /**
     * Pulls a random LootRarity and AffixLootEntry, and generates an Affix Item
     *
     * @param rand   Random
     * @param rarity The rarity, or null if it should be randomly selected.
     * @param luck   The player's luck level
     * @param level  The world, since affix loot entries are per-dimension.
     * @return An affix item, or an empty ItemStack if no entries were available for the dimension.
     */
    public static ItemStack createRandomLootItem(RandomSource rand, @Nullable LootRarity rarity, Player player, ServerLevelAccessor level) {
        AffixLootEntry entry = AffixLootRegistry.INSTANCE.getRandomItem(rand, player);
        if (entry == null) return ItemStack.EMPTY;
        if (rarity == null) rarity = LootRarity.random(rand, WorldTier.getTier(player), player.getLuck(), entry.rarities());
        return createLootItem(entry.stack(), entry.getType(), rarity, rand, WorldTier.getTier(player), player.getLuck());
    }

    /**
     * Returns the pool of available affixes for an item, given the existing affixes present.
     *
     * @param stack  The item stack the affixes may be applied to
     * @param rarity The rarity of the item stack
     * @param type   The type of affix to target
     * @return A list of available affixes for the item. May be empty.
     */
    public static Stream<DynamicHolder<Affix>> getAvailableAffixes(ItemStack stack, LootRarity rarity, AffixType type) {
        LootCategory cat = LootCategory.forItem(stack);
        ItemAffixes current = stack.getOrDefault(Components.AFFIXES, ItemAffixes.EMPTY);
        return AffixHelper.byType(type)
            .stream()
            .filter(a -> a.get().canApplyTo(stack, cat, rarity))
            .filter(a -> !current.keySet().contains(a) && a.get().isCompatibleWith(current));
    }

    public static List<WeightedEntry.Wrapper<Affix>> getWeightedAffixes(ItemStack stack, LootRarity rarity, AffixType type, WorldTier tier, float luck) {
        return getAvailableAffixes(stack, rarity, type).map(a -> a.get().<Affix>wrap(tier, luck)).toList();
    }

}
