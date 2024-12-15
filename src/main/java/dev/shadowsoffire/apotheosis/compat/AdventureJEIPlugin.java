package dev.shadowsoffire.apotheosis.compat;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.RecipeTypes;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingRecipe;
import dev.shadowsoffire.apotheosis.socket.AddSocketsRecipe;
import dev.shadowsoffire.apotheosis.socket.ReactiveSmithingRecipe;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.PurityUpgradeRecipe;
import dev.shadowsoffire.apotheosis.util.ApothSmithingRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.block.Blocks;

@JeiPlugin
public class AdventureJEIPlugin implements IModPlugin {

    public static final RecipeType<SmithingRecipe> APO_SMITHING = RecipeType.create(Apotheosis.MODID, "smithing", ApothSmithingRecipe.class);
    public static final RecipeType<SalvagingRecipe> SALVAGING = RecipeType.create(Apotheosis.MODID, "salvaging", SalvagingRecipe.class);
    public static final RecipeType<GemCuttingRecipe> GEM_CUTTING = RecipeType.create(Apotheosis.MODID, "gem_cutting", PurityUpgradeRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return Apotheosis.loc("adventure_module");
    }

    @Override
    @SuppressWarnings("removal")
    public void registerRecipes(IRecipeRegistration reg) {
        ItemStack gem = new ItemStack(Apoth.Items.GEM);
        Optional<Gem> gemObj = GemRegistry.INSTANCE.getValues().stream().findAny();
        if (gemObj.isPresent()) {
            GemItem.setGem(gem, gemObj.get());
            GemItem.setPurity(gem, Purity.PERFECT);
            reg.addIngredientInfo(gem, VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.socketing"));
            reg.addIngredientInfo(new ItemStack(Apoth.Items.GEM_DUST), VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.gem_crushing"));
            reg.addIngredientInfo(new ItemStack(Apoth.Items.SIGIL_OF_UNNAMING), VanillaTypes.ITEM_STACK, Component.translatable("info.apotheosis.unnaming"));
            ApothSmithingCategory.registerExtension(AddSocketsRecipe.class, new AddSocketsExtension());
        }

        reg.addRecipes(APO_SMITHING, Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.SMITHING).stream()
            .map(RecipeHolder::value)
            .filter(ReactiveSmithingRecipe.class::isInstance)
            .toList());

        List<SalvagingRecipe> salvagingRecipes = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(RecipeTypes.SALVAGING).stream()
            .sorted(Comparator.comparing(RecipeHolder::id)) // TODO: Prioritize apoth recipes so that the main affix/gem salvaging is always first.
            .map(RecipeHolder::value)
            .toList();
        reg.addRecipes(SALVAGING, salvagingRecipes);

        reg.addRecipes(GEM_CUTTING, Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(RecipeTypes.GEM_CUTTING).stream()
            .map(RecipeHolder::value)
            .toList());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration reg) {
        reg.addRecipeCategories(new ApothSmithingCategory(reg.getJeiHelpers().getGuiHelper()));
        reg.addRecipeCategories(new SalvagingCategory(reg.getJeiHelpers().getGuiHelper()));
        reg.addRecipeCategories(new GemCuttingCategory(reg.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration reg) {
        reg.addRecipeCatalyst(new ItemStack(Blocks.SMITHING_TABLE), APO_SMITHING);
        reg.addRecipeCatalyst(new ItemStack(Apoth.Blocks.SALVAGING_TABLE.value()), SALVAGING);
        reg.addRecipeCatalyst(new ItemStack(Apoth.Blocks.GEM_CUTTING_TABLE.value()), GEM_CUTTING);
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration reg) {
        reg.registerSubtypeInterpreter(Apoth.Items.GEM.value(), new GemSubtypes());
    }

    private static final List<ItemStack> DUMMY_INPUTS = Arrays.asList(Items.GOLDEN_SWORD, Items.DIAMOND_PICKAXE, Items.STONE_AXE, Items.IRON_CHESTPLATE, Items.TRIDENT).stream().map(ItemStack::new).toList();

    static class AddSocketsExtension implements ApothSmithingCategory.Extension<AddSocketsRecipe> {
        private static final List<ItemStack> DUMMY_OUTPUTS = DUMMY_INPUTS.stream().map(ItemStack::copy).map(s -> {
            SocketHelper.setSockets(s, 1);
            return s;
        }).toList();

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, AddSocketsRecipe recipe, IFocusGroup focuses) {
            builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).addIngredients(VanillaTypes.ITEM_STACK, DUMMY_INPUTS);
            builder.addSlot(RecipeIngredientRole.INPUT, 50, 1).addIngredients(recipe.getInput());
            builder.addSlot(RecipeIngredientRole.OUTPUT, 108, 1).addItemStacks(DUMMY_OUTPUTS);
        }

        @Override
        public void draw(AddSocketsRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gfx, double mouseX, double mouseY) {
            Component text = Component.translatable("text.apotheosis.socket_limit", recipe.getMaxSockets());
            Font font = Minecraft.getInstance().font;
            gfx.drawString(font, text, 125 / 2 - font.width(text) / 2, 23, 0, false);
        }

    }

    /**
     * A Gem Stack is unique to JEI based on the Gem's ID and Rarity.
     */
    static class GemSubtypes implements IIngredientSubtypeInterpreter<ItemStack> {

        @Override
        public String apply(ItemStack stack, UidContext context) {
            GemInstance inst = GemInstance.unsocketed(stack);
            if (!inst.isValidUnsocketed()) {
                return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            }
            return inst.gem().getId() + "@" + inst.purity().getSerializedName();
        }

    }

}
