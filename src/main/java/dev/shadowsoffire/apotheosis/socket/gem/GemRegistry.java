package dev.shadowsoffire.apotheosis.socket.gem;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;

import dev.shadowsoffire.apotheosis.AdventureModule;
import dev.shadowsoffire.apotheosis.Apoth.Items;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.apotheosis.tiers.Constraints;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.TieredDynamicRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class GemRegistry extends TieredDynamicRegistry<Gem> {

    public static final GemRegistry INSTANCE = new GemRegistry();

    public GemRegistry() {
        super(Apotheosis.LOGGER, "gems", true, false);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(Apotheosis.loc("gem"), Gem.CODEC);
    }

    public static ItemStack createGemStack(Gem gem, Purity purity) {
        ItemStack stack = new ItemStack(Items.GEM);
        GemItem.setGem(stack, gem);
        GemItem.setPurity(stack, purity);
        return stack;
    }

    @Override
    protected void validateItem(ResourceLocation key, Gem item) {
        super.validateItem(key, item);
        for (Purity p : Purity.values()) {
            if (p.isAtLeast(item.getMinPurity())) {
                boolean atLeastOne = false;
                for (GemBonus bonus : item.bonuses) {
                    if (bonus.supports(p)) atLeastOne = true;
                }
                Preconditions.checkArgument(atLeastOne, "No bonuses provided for supported purity %s. At least one bonus must be provided, or the minimum purity should be raised.", p.getName());
            }
        }
    }

    @Nullable
    public Gem getRandomItem(GenContext ctx) {
        return getRandomItem(ctx, Constraints.eval(ctx));
    }

    /**
     * Pulls a random Gem and Purity, then generates an item stack holding them.
     *
     * @param rand   A random
     * @param rarity The rarity, or null if it should be randomly selected.
     * @param luck   The player's luck level
     * @param filter The filter
     * @return A gem item, or an empty ItemStack if no entries were available for the dimension.
     */
    public static ItemStack createRandomGemStack(GenContext ctx) {
        Gem gem = GemRegistry.INSTANCE.getRandomItem(ctx);
        if (gem == null) return ItemStack.EMPTY;
        Purity purity = Purity.CRACKED; // TODO: Implement purity selection via TieredWeights. Need a place to store that data.
        return createGemStack(gem, purity);
    }

}
