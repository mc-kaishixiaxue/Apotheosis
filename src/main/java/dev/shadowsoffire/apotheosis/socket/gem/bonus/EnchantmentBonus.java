package dev.shadowsoffire.apotheosis.socket.gem.bonus;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class EnchantmentBonus extends GemBonus {

    protected final Holder<Enchantment> ench;
    protected final boolean mustExist;
    protected final boolean global;
    protected final Map<Purity, Integer> values;

    public static Codec<EnchantmentBonus> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            gemClass(),
            Enchantment.CODEC.fieldOf("enchantment").forGetter(a -> a.ench),
            Codec.BOOL.optionalFieldOf("must_exist", false).forGetter(a -> a.mustExist),
            Codec.BOOL.optionalFieldOf("global", false).forGetter(a -> a.global),
            Purity.mapCodec(Codec.intRange(1, 127)).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, EnchantmentBonus::new));

    public EnchantmentBonus(GemClass gemClass, Holder<Enchantment> ench, boolean mustExist, boolean global, Map<Purity, Integer> values) {
        super(Apotheosis.loc("enchantment"), gemClass);
        this.ench = ench;
        this.values = values;
        this.mustExist = mustExist;
        this.global = global;
    }

    @Override
    public Component getSocketBonusTooltip(GemInstance gem, AttributeTooltipContext ctx) {
        int level = this.values.get(gem.purity());
        String desc = "bonus." + this.getId() + ".desc";
        if (this.global) {
            desc += ".global";
        }
        else if (this.mustExist) {
            desc += ".mustExist";
        }
        Component enchName = this.ench.value().description().plainCopy();
        return Component.translatable(desc, level, Component.translatable("misc.apotheosis.level" + (level > 1 ? ".many" : "")), enchName).withStyle(ChatFormatting.GREEN);
    }

    @Override
    public void getEnchantmentLevels(GemInstance gem, ItemEnchantments.Mutable enchantments) {
        int level = this.values.get(gem.purity());
        if (this.global) {
            for (Holder<Enchantment> e : enchantments.keySet()) {
                int current = enchantments.getLevel(e);
                if (current > 0) {
                    enchantments.upgrade(e, current + level);
                }
            }
        }
        else if (this.mustExist) {
            int current = enchantments.getLevel(this.ench);
            if (current > 0) {
                enchantments.upgrade(this.ench, current + level);
            }
        }
        else {
            enchantments.upgrade(this.ench, enchantments.getLevel(this.ench) + level);
        }
    }

    @Override
    public GemBonus validate() {
        Preconditions.checkNotNull(this.ench, "Invalid DamageReductionBonus with null type");
        Preconditions.checkNotNull(this.values, "Invalid DamageReductionBonus with null values");
        return this;
    }

    @Override
    public boolean supports(Purity purity) {
        return this.values.containsKey(purity);
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

}
