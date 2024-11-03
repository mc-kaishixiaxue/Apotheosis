package dev.shadowsoffire.apotheosis.socket.gem;

import com.google.common.base.Preconditions;

import dev.shadowsoffire.apotheosis.AdventureModule;
import dev.shadowsoffire.apotheosis.Apoth.Items;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class GemRegistry extends WeightedDynamicRegistry<Gem> {

    public static final GemRegistry INSTANCE = new GemRegistry();

    public GemRegistry() {
        super(AdventureModule.LOGGER, "gems", true, false);
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

}
