package dev.shadowsoffire.apotheosis.socket;

import java.util.ArrayList;
import java.util.List;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.event.CanSocketGemEvent;
import dev.shadowsoffire.apotheosis.event.ItemSocketingEvent;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.UnsocketedGem;
import dev.shadowsoffire.apotheosis.util.ApothSmithingRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;

public class SocketingRecipe extends ApothSmithingRecipe {

    public SocketingRecipe() {
        super(Ingredient.EMPTY, Ingredient.of(Apoth.Items.GEM.value()), ItemStack.EMPTY);
    }

    /**
     * Used to check if a recipe matches current crafting inventory
     */
    @Override
    public boolean matches(SmithingRecipeInput inv, Level pLevel) {
        ItemStack input = inv.getItem(BASE);
        ItemStack gemStack = inv.getItem(ADDITION);
        UnsocketedGem gem = UnsocketedGem.of(gemStack);

        if (!gem.isValid() || !SocketHelper.hasEmptySockets(input)) {
            return false;
        }

        CanSocketGemEvent event = NeoForge.EVENT_BUS.post(new CanSocketGemEvent(input, gemStack));
        return !event.isCanceled() && gem.canApplyTo(input);
    }

    /**
     * Returns an Item that is the result of this recipe
     */
    @Override
    public ItemStack assemble(SmithingRecipeInput inv, HolderLookup.Provider regs) {
        ItemStack input = inv.getItem(BASE);
        ItemStack gemStack = inv.getItem(ADDITION);
        UnsocketedGem gem = UnsocketedGem.of(gemStack);

        if (!gem.isValid() || !SocketHelper.hasEmptySockets(input)) {
            return ItemStack.EMPTY;
        }

        ItemStack result = input.copy();
        result.setCount(1);
        int socket = SocketHelper.getFirstEmptySocket(result);
        List<GemInstance> gems = new ArrayList<>(SocketHelper.getGems(result).gems());
        ItemStack gemToInsert = gemStack.copy();
        gemToInsert.setCount(1);
        gems.set(socket, GemInstance.socketed(result, gemStack.copy(), socket));
        SocketHelper.setGems(result, new SocketedGems(gems));

        ItemSocketingEvent event = NeoForge.EVENT_BUS.post(new ItemSocketingEvent(input, gemToInsert, result));
        return event.getOutput();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Apoth.RecipeSerializers.SOCKETING.value();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
}
