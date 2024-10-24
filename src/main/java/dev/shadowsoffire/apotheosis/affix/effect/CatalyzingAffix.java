package dev.shadowsoffire.apotheosis.affix.effect;

import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.affix.AffixType;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * When blocking an explosion, gain great power.
 */
public class CatalyzingAffix extends Affix {

    public static final Codec<CatalyzingAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            GemBonus.VALUES_CODEC.fieldOf("values").forGetter(a -> a.values))
        .apply(inst, CatalyzingAffix::new));

    protected final Map<LootRarity, StepFunction> values;

    public CatalyzingAffix(Map<LootRarity, StepFunction> values) {
        super(AffixType.ABILITY);
        this.values = values;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return cat == LootCategory.SHIELD && this.values.containsKey(rarity);
    }

    @Override
    public float onShieldBlock(AffixInstance inst, LivingEntity entity, DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            int time = this.values.get(inst.getRarity()).getInt(inst.level());
            int modifier = 1 + (int) (Math.log(amount) / Math.log(3));
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, time, modifier));
        }

        return super.onShieldBlock(inst, entity, source, amount);
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }

}
