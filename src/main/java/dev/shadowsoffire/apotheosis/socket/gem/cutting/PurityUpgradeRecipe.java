package dev.shadowsoffire.apotheosis.socket.gem.cutting;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public record PurityUpgradeRecipe(Purity purity, List<SizedIngredient> left, List<SizedIngredient> right) implements GemCuttingRecipe {

    public static MapCodec<PurityUpgradeRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        Purity.CODEC.fieldOf("purity").forGetter(PurityUpgradeRecipe::purity),
        SizedIngredient.FLAT_CODEC.listOf().fieldOf("left").forGetter(PurityUpgradeRecipe::left),
        SizedIngredient.FLAT_CODEC.listOf().fieldOf("right").forGetter(PurityUpgradeRecipe::right))
        .apply(inst, PurityUpgradeRecipe::new));

    public static StreamCodec<RegistryFriendlyByteBuf, PurityUpgradeRecipe> STREAM_CODEC = StreamCodec.composite(
        Purity.STREAM_CODEC, PurityUpgradeRecipe::purity,
        SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), PurityUpgradeRecipe::left,
        SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), PurityUpgradeRecipe::right,
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
        SizedIngredient left = this.left.stream().filter(i -> i.test(input.getLeft())).findFirst().get();
        SizedIngredient right = this.right.stream().filter(i -> i.test(input.getLeft())).findFirst().get();
        input.getLeft().shrink(left.count());
        input.getRight().shrink(right.count());
    }

    @Override
    public boolean matches(CuttingRecipeInput input, Level level) {
        GemInstance baseInst = GemInstance.unsocketed(input.getBase());
        GemInstance topInst = GemInstance.unsocketed(input.getTop());
        if (baseInst.purity() != this.purity || !baseInst.equalsUnsocketed(topInst)) {
            return false;
        }

        return this.left.stream().anyMatch(i -> i.test(input.getLeft())) && this.right.stream().anyMatch(i -> i.test(input.getRight()));
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
        return this.left.stream().map(SizedIngredient::ingredient).anyMatch(i -> i.test(stack));
    }

    @Override
    public boolean isValidRightItem(CuttingRecipeInput input, ItemStack stack) {
        return this.right.stream().map(SizedIngredient::ingredient).anyMatch(i -> i.test(stack));
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
