package dev.shadowsoffire.apotheosis.affix.effect;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;

import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.affix.AffixType;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class DurableAffix extends Affix {

    public static final Codec<DurableAffix> CODEC = Codec.unit(DurableAffix::new);

    public DurableAffix() {
        super(AffixType.DURABILITY);
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return stack.isDamageableItem();
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        return Component.translatable("affix." + this.getId() + ".desc", fmt(inst.level() * 100));
    }

    @Override
    public float getDurabilityBonusPercentage(AffixInstance inst, @Nullable ServerPlayer user) {
        return inst.level();
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }

    /**
     * A reduction that computes the diminishing return value of multiple durability bonuses.<br>
     * For this computation, the first bonus is applied in full, but further bonuses are only applied to the reduced value.
     *
     * @param result  The current result value.
     * @param element The next element.
     * @return The updated result, after applying the element.
     */
    public static double duraProd(double result, double element) {
        return result + (1 - result) * element;
    }

}
