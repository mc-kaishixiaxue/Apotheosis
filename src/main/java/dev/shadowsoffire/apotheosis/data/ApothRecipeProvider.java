package dev.shadowsoffire.apotheosis.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import dev.shadowsoffire.apotheosis.Apoth.Blocks;
import dev.shadowsoffire.apotheosis.Apoth.Items;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.UnnamingRecipe;
import dev.shadowsoffire.apotheosis.affix.reforging.ReforgingRecipe;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingRecipe;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingRecipe.OutputData;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.recipe.CharmInfusionRecipe;
import dev.shadowsoffire.apotheosis.recipe.PotionCharmRecipe;
import dev.shadowsoffire.apotheosis.socket.AddSocketsRecipe;
import dev.shadowsoffire.apotheosis.socket.SocketingRecipe;
import dev.shadowsoffire.apotheosis.socket.WithdrawalRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.PurityUpgradeRecipe;
import dev.shadowsoffire.apotheosis.util.AffixItemIngredient;
import dev.shadowsoffire.apotheosis.util.GemIngredient;
import dev.shadowsoffire.apothic_enchanting.Ench;
import dev.shadowsoffire.apothic_enchanting.table.EnchantingStatRegistry.Stats;
import dev.shadowsoffire.placebo.datagen.LegacyRecipeProvider;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class ApothRecipeProvider extends LegacyRecipeProvider {

    public ApothRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Apotheosis.MODID);
    }

    @Override
    protected void genRecipes(RecipeOutput out, HolderLookup.Provider registries) {
        out.accept(Apotheosis.loc("socketing"), new SocketingRecipe(), null);
        out.accept(Apotheosis.loc("unnaming"), new UnnamingRecipe(), null);
        out.accept(Apotheosis.loc("widthdrawal"), new WithdrawalRecipe(), null);
        addSockets("sigil_add_sockets", ingredient(Items.SIGIL_OF_SOCKETING), 3);
        addAffixSalvaging("common", Items.COMMON_MATERIAL);
        addAffixSalvaging("uncommon", Items.UNCOMMON_MATERIAL);
        addAffixSalvaging("rare", Items.RARE_MATERIAL);
        addAffixSalvaging("epic", Items.EPIC_MATERIAL);
        addAffixSalvaging("mythic", Items.MYTHIC_MATERIAL);
        addGemSalvaging(Purity.CRACKED, 1, 2);
        addGemSalvaging(Purity.CHIPPED, 1, 3);
        addGemSalvaging(Purity.FLAWED, 2, 4);
        addGemSalvaging(Purity.NORMAL, 3, 5);
        addGemSalvaging(Purity.FLAWLESS, 4, 7);
        addGemSalvaging(Purity.PERFECT, 5, 10);
        addOtherSalvaging("leather_horse_armor", Ingredient.of(Items.LEATHER_HORSE_ARMOR), new OutputData(Items.LEATHER, 3, 8));
        addOtherSalvaging("iron_horse_armor", Ingredient.of(Items.IRON_HORSE_ARMOR), new OutputData(Items.IRON_INGOT, 3, 8));
        addOtherSalvaging("golden_horse_armor", Ingredient.of(Items.GOLDEN_HORSE_ARMOR), new OutputData(Items.GOLD_INGOT, 3, 8));
        addOtherSalvaging("diamond_horse_armor", Ingredient.of(Items.DIAMOND_HORSE_ARMOR), new OutputData(Items.DIAMOND, 3, 8));
        addOtherSalvaging("wolf_armor", Ingredient.of(Items.WOLF_ARMOR), new OutputData(Items.ARMADILLO_SCUTE, 1, 3));
        addReforging("common", 1, 0, 2, Blocks.SIMPLE_REFORGING_TABLE, Blocks.REFORGING_TABLE);
        addReforging("uncommon", 2, 1, 5, Blocks.SIMPLE_REFORGING_TABLE, Blocks.REFORGING_TABLE);
        addReforging("rare", 2, 2, 15, Blocks.SIMPLE_REFORGING_TABLE, Blocks.REFORGING_TABLE);
        addReforging("epic", 2, 4, 30, Blocks.REFORGING_TABLE);
        addReforging("mythic", 3, 5, 50, Blocks.REFORGING_TABLE);

        addShaped(Blocks.AUGMENTING_TABLE, 3, 3, null, Items.NETHER_STAR, null, Items.MYTHIC_MATERIAL, Items.ENCHANTING_TABLE, Items.MYTHIC_MATERIAL, Items.POLISHED_BLACKSTONE, Items.POLISHED_BLACKSTONE, Items.POLISHED_BLACKSTONE);
        addShaped(Blocks.GEM_CUTTING_TABLE, 3, 3, Items.SMOOTH_STONE, Items.SHEARS, Items.SMOOTH_BASALT, ItemTags.PLANKS, Items.GEM_DUST, ItemTags.PLANKS, ItemTags.PLANKS, null, ItemTags.PLANKS);
        addShaped(new ItemStack(Items.GEM_FUSED_SLATE, 8), 3, 3, Items.DEEPSLATE, Items.DEEPSLATE, Items.DEEPSLATE, Items.DEEPSLATE, Items.GEM_DUST, Items.DEEPSLATE, Items.DEEPSLATE, Items.DEEPSLATE, Items.DEEPSLATE);
        addShaped(Blocks.REFORGING_TABLE, 3, 3, null, Tags.Items.INGOTS_NETHERITE, null, Items.EPIC_MATERIAL, Items.SIMPLE_REFORGING_TABLE, Items.EPIC_MATERIAL, Items.NETHER_BRICKS, Items.NETHER_BRICKS, Items.NETHER_BRICKS);
        addShaped(Blocks.SALVAGING_TABLE, 3, 3, Tags.Items.INGOTS_COPPER, Tags.Items.INGOTS_COPPER, Tags.Items.INGOTS_COPPER, Items.IRON_PICKAXE, Items.SMITHING_TABLE, Items.IRON_AXE, Items.GEM_DUST, Items.LAVA_BUCKET, Items.GEM_DUST);
        addShaped(Blocks.SIMPLE_REFORGING_TABLE, 3, 3, null, Tags.Items.INGOTS_IRON, null, Items.GEM_DUST, Items.ENCHANTING_TABLE, Items.GEM_DUST, Items.SMOOTH_STONE, Items.SMOOTH_STONE, Items.SMOOTH_STONE);

        addShaped(new ItemStack(Items.SIGIL_OF_ENHANCEMENT, 4), 3, 3, Items.GEM_DUST, Items.GEM_FUSED_SLATE, Items.GEM_DUST, Items.GEM_FUSED_SLATE, Items.MYTHIC_MATERIAL, Items.GEM_FUSED_SLATE, Items.GEM_DUST, Items.GEM_FUSED_SLATE,
            Items.GEM_DUST);
        addShaped(new ItemStack(Items.SIGIL_OF_REBIRTH, 6), 3, 3, Items.GEM_FUSED_SLATE, Items.GEM_FUSED_SLATE, Items.GEM_FUSED_SLATE, Items.GEM_DUST, Items.GEM_DUST, Items.GEM_DUST, Items.GEM_FUSED_SLATE, Items.GEM_FUSED_SLATE,
            Items.GEM_FUSED_SLATE);
        addShaped(new ItemStack(Items.SIGIL_OF_SOCKETING, 3), 3, 3, Items.GEM_DUST, Ench.Items.INFUSED_BREATH, Items.GEM_DUST, Items.GEM_FUSED_SLATE, Items.GEM_FUSED_SLATE, Items.GEM_FUSED_SLATE, Items.GEM_DUST, Items.AMETHYST_SHARD,
            Items.GEM_DUST);
        addShaped(new ItemStack(Items.SIGIL_OF_UNNAMING, 6), 3, 3, Items.GEM_FUSED_SLATE, Items.GEM_FUSED_SLATE, Items.GEM_FUSED_SLATE, Items.FLINT, Items.FLINT, Items.FLINT, Items.GEM_FUSED_SLATE, Items.GEM_FUSED_SLATE,
            Items.GEM_FUSED_SLATE);
        addShaped(new ItemStack(Items.SIGIL_OF_WITHDRAWAL, 4), 3, 3, Items.GEM_FUSED_SLATE, Items.BLAZE_ROD, Items.GEM_FUSED_SLATE, Tags.Items.ENDER_PEARLS, Items.LAVA_BUCKET, Tags.Items.ENDER_PEARLS, Items.GEM_FUSED_SLATE,
            Items.GEM_DUST, Items.GEM_FUSED_SLATE);

        List<Holder<Item>> rarityMaterials = List.of(Items.COMMON_MATERIAL, Items.UNCOMMON_MATERIAL, Items.RARE_MATERIAL, Items.EPIC_MATERIAL, Items.MYTHIC_MATERIAL);
        for (int i = 0; i < Purity.values().length - 1; i++) {
            Purity purity = Purity.BY_ID.apply(i);
            List<Holder<Item>> materials = rarityMaterials.subList(Math.max(i - 1, 0), Math.min(i + 2, rarityMaterials.size()));
            addPurityUpgrade(purity, 1 + i * 2, materials, i == 0 ? 3 : 9);
        }

        out.accept(Apotheosis.loc("potion_charm"), new PotionCharmRecipe("", CraftingBookCategory.MISC, charmPattern()), null);

        out.accept(Apotheosis.loc("infusion/potion_charm"), new CharmInfusionRecipe(
            new Stats(15F, 100F, 8.5F, 32.5F, 0),
            new Stats(15F, 100F, 13.5F, 37.5F, 0)),
            null);
    }

    private ShapedRecipePattern charmPattern() {
        Map<Character, Ingredient> key = new HashMap<>();
        key.put('B', Ingredient.of(Items.BLAZE_POWDER));
        key.put('P', Ingredient.of(Items.POTION));
        return ShapedRecipePattern.of(key, "BBB", "PPP", "BBB");
    }

    private void addPurityUpgrade(Purity purity, int gemDust, List<Holder<Item>> materials, int zerothMatCost) {
        SizedIngredient dustIng = SizedIngredient.of(Items.GEM_DUST.value(), gemDust);
        List<SizedIngredient> materialIngs = new ArrayList<>();
        int matAmount = zerothMatCost;
        for (Holder<Item> mat : materials) {
            materialIngs.add(SizedIngredient.of(mat.value(), matAmount));
            matAmount /= 3;
        }
        var recipe = new PurityUpgradeRecipe(purity, List.of(dustIng), materialIngs);
        this.recipeOutput.accept(Apotheosis.loc("gem_cutting/" + purity.name().toLowerCase(Locale.ROOT)), recipe, null);
    }

    @SafeVarargs
    private void addReforging(String rarity, int mats, int sigils, int levels, Holder<Block>... tables) {
        DynamicHolder<LootRarity> lRarity = RarityRegistry.INSTANCE.holder(Apotheosis.loc(rarity));
        this.recipeOutput.accept(Apotheosis.loc("reforging/" + rarity), new ReforgingRecipe(lRarity, mats, sigils, levels, HolderSet.direct(tables)), null);
    }

    private void addGemSalvaging(Purity purity, int min, int max) {
        Ingredient input = new Ingredient(new GemIngredient(purity));
        OutputData output = new OutputData(new ItemStack(Items.GEM_DUST), min, max);
        addSalvaging("gem/" + purity.getSerializedName(), input, output);
    }

    private void addAffixSalvaging(String rarity, Holder<Item> material) {
        Ingredient input = new Ingredient(new AffixItemIngredient(RarityRegistry.INSTANCE.holder(Apotheosis.loc(rarity))));
        OutputData output = new OutputData(new ItemStack(material), 1, 4);
        addSalvaging("affix_item/" + rarity, input, output);
    }

    private void addOtherSalvaging(String path, Ingredient input, OutputData output) {
        addSalvaging("salvaging/other/" + path, input, List.of(output));
    }

    private void addSalvaging(String path, Ingredient input, OutputData output) {
        addSalvaging("salvaging/" + path, input, List.of(output));
    }

    private void addSalvaging(String path, Ingredient input, List<OutputData> outputs) {
        this.recipeOutput.accept(Apotheosis.loc(path), new SalvagingRecipe(input, outputs), null);
    }

    private void addSockets(String path, Ingredient input, int maxSockets) {
        this.recipeOutput.accept(Apotheosis.loc(path), new AddSocketsRecipe(input, maxSockets), null);
    }

    private static <T extends ItemLike> Ingredient ingredient(Holder<T> holder) {
        return Ingredient.of(holder.value());
    }
}
