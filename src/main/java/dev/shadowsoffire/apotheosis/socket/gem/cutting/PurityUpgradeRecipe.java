package dev.shadowsoffire.apotheosis.socket.gem.cutting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.GemItem;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public record PurityUpgradeRecipe(SizedIngredient left, SizedIngredient right) implements GemCuttingRecipe {

    public static MapCodec<PurityUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        SizedIngredient.FLAT_CODEC.fieldOf("left").forGetter(PurityUpgradeRecipe::left),
        SizedIngredient.FLAT_CODEC.fieldOf("right").forGetter(PurityUpgradeRecipe::right))
        .apply(inst, PurityUpgradeRecipe::new));

    public static StreamCodec<RegistryFriendlyByteBuf, PurityUpgradeRecipe> STREAM_CODEC = StreamCodec.composite(
        SizedIngredient.STREAM_CODEC, PurityUpgradeRecipe::left,
        SizedIngredient.STREAM_CODEC, PurityUpgradeRecipe::right,
        PurityUpgradeRecipe::new);

    @Override
    public ItemStack assemble(CuttingRecipeInput input, Provider registries) {
        ItemStack out = input.getBase().copy();
        GemItem.setPurity(out, GemItem.getPurity(out).next());
        return out;
    }

    @Override
    public ItemStack getResultItem(Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public void decrementInputs(CuttingRecipeInput input, Level level) {
        input.getTop().shrink(1);
        input.getLeft().shrink(this.left.count());
        input.getRight().shrink(this.right.count());
    }

    @Override
    public boolean matches(CuttingRecipeInput input, Level level) {
        GemInstance baseInst = GemInstance.unsocketed(input.getBase());
        GemInstance topInst = GemInstance.unsocketed(input.getTop());
        if (baseInst.isPerfect() || !baseInst.equalsUnsocketed(topInst)) {
            return false;
        }

        return this.left.test(input.getLeft()) && this.right.test(input.getRight());
    }

    @Override
    public boolean isValidBaseItem(CuttingRecipeInput input, ItemStack stack) {
        GemInstance inst = GemInstance.unsocketed(stack);
        return inst.isValidUnsocketed() && !inst.isPerfect();
    }

    @Override
    public boolean isValidTopItem(CuttingRecipeInput input, ItemStack stack) {
        GemInstance baseInst = GemInstance.unsocketed(input.getBase());
        if (!baseInst.isValidUnsocketed() || baseInst.isPerfect()) {
            return false;
        }
        GemInstance inst = GemInstance.unsocketed(stack);
        return baseInst.equalsUnsocketed(inst);
    }

    @Override
    public boolean isValidLeftItem(CuttingRecipeInput input, ItemStack stack) {
        return this.left.ingredient().test(stack);
    }

    @Override
    public boolean isValidRightItem(CuttingRecipeInput input, ItemStack stack) {
        return this.right.ingredient().test(stack);
    }

    public static class Serializer implements RecipeSerializer<PurityUpgradeRecipe> {

        public static final Serializer INSTANCE = new Serializer();

        @Override
        public MapCodec<PurityUpgradeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, PurityUpgradeRecipe> streamCodec() {
            return STREAM_CODEC;
        }

    }

}
