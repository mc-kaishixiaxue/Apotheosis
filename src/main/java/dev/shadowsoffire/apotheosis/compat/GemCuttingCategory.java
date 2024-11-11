package dev.shadowsoffire.apotheosis.compat;

import dev.shadowsoffire.apotheosis.Apoth.Blocks;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingBlock;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public abstract class GemCuttingCategory<T extends GemCuttingRecipe> implements IRecipeCategory<T> {

    public static final ResourceLocation TEXTURES = Apotheosis.loc("textures/gui/gem_cutting_jei.png");

    private final IDrawable background;
    private final IDrawable icon;

    public GemCuttingCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.drawableBuilder(TEXTURES, 0, 0, 148, 78).addPadding(0, 0, 0, 0).build();
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Blocks.GEM_CUTTING_TABLE.value()));
    }

    @Override
    public Component getTitle() {
        return GemCuttingBlock.NAME;
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

}
