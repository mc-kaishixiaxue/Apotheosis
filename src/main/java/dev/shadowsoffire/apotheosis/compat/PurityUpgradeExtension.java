package dev.shadowsoffire.apotheosis.compat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.compat.jei.GemCuttingCategory.GemCuttingExtension;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.PurityUpgradeRecipe;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class PurityUpgradeExtension implements GemCuttingExtension<PurityUpgradeRecipe> {

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PurityUpgradeRecipe recipe, IFocusGroup focuses) {
        DynamicHolder<Gem> focus = GemItem.getGem(focuses.getFocuses(VanillaTypes.ITEM_STACK).findFirst().map(IFocus::getTypedValue).map(ITypedIngredient::getIngredient).orElse(ItemStack.EMPTY));
        List<ItemStack> inputs = new ArrayList<>();
        List<ItemStack> outputs = new ArrayList<>();

        if (focus.isBound()) {
            inputs.add(gemStack(focus.get(), recipe.purity()));
            outputs.add(gemStack(focus.get(), recipe.purity().next()));
        }
        else {
            GemRegistry.INSTANCE.getValues().stream().forEachOrdered(g -> {
                inputs.add(gemStack(g, recipe.purity()));
                outputs.add(gemStack(g, recipe.purity().next()));
            });
        }

        builder.addSlot(RecipeIngredientRole.INPUT, 48, 37).addIngredients(VanillaTypes.ITEM_STACK, inputs);

        builder.addSlot(RecipeIngredientRole.INPUT, 48, 4).addIngredients(VanillaTypes.ITEM_STACK, inputs);
        builder.addSlot(RecipeIngredientRole.INPUT, 19, 56).addIngredients(VanillaTypes.ITEM_STACK, recipe.left().stream().map(SizedIngredient::getItems).flatMap(Arrays::stream).toList());
        builder.addSlot(RecipeIngredientRole.INPUT, 76, 56).addIngredients(VanillaTypes.ITEM_STACK, recipe.right().stream().map(SizedIngredient::getItems).flatMap(Arrays::stream).toList());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 117, 35).addIngredients(VanillaTypes.ITEM_STACK, outputs);
    }

    private static ItemStack gemStack(Gem gem, Purity purity) {
        ItemStack stack = new ItemStack(Apoth.Items.GEM);
        GemItem.setGem(stack, gem);
        GemItem.setPurity(stack, purity);
        return stack;
    }

}
