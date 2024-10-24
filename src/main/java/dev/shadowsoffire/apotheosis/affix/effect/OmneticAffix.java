package dev.shadowsoffire.apotheosis.affix.effect;

import java.util.Arrays;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth.Affixes;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.affix.AffixType;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.json.ItemAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeMod;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.HarvestCheck;

public class OmneticAffix extends Affix {

    public static final Codec<OmneticAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            LootRarity.mapCodec(OmneticData.CODEC).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, OmneticAffix::new));

    protected final Map<LootRarity, OmneticData> values;

    public OmneticAffix(Map<LootRarity, OmneticData> values) {
        super(AffixType.ABILITY);
        this.values = values;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return cat.isBreaker() && this.values.containsKey(rarity);
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        return Component.translatable("affix." + this.getId() + ".desc", Component.translatable("misc.apotheosis." + this.values.get(inst.getRarity()).name));
    }

    public void harvest(HarvestCheck e) {
        ItemStack stack = e.getEntity().getMainHandItem();
        if (!stack.isEmpty()) {
            AffixInstance inst = AffixHelper.getAffixes(stack).get(Affixes.OMNETIC);
            if (inst != null && inst.isValid()) {
                OmneticData data = this.values.get(inst.rarity().get());
                for (ItemStack item : data.items()) {
                    if (item.isCorrectToolForDrops(e.getTargetBlock())) {
                        e.setCanHarvest(true);
                        return;
                    }
                }
            }
        }
    }

    // EventPriority.HIGHEST
    public void speed(BreakSpeed e) {
        ItemStack stack = e.getEntity().getMainHandItem();
        if (!stack.isEmpty()) {
            AffixInstance inst = AffixHelper.getAffixes(stack).get(Affixes.OMNETIC);
            if (inst != null && inst.isValid()) {
                float speed = e.getOriginalSpeed();
                OmneticData data = this.values.get(inst.rarity().get());
                for (ItemStack item : data.items()) {
                    speed = Math.max(getBaseSpeed(e.getEntity(), item, e.getState(), e.getPosition().orElse(BlockPos.ZERO)), speed);
                }
                e.setNewSpeed(speed);
            }
        }
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }

    static record OmneticData(String name, ItemStack[] items) {

        public static Codec<OmneticData> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                Codec.STRING.fieldOf("name").forGetter(OmneticData::name),
                Codec.list(ItemAdapter.CODEC).xmap(l -> l.toArray(new ItemStack[0]), Arrays::asList).fieldOf("items").forGetter(OmneticData::items))
            .apply(inst, OmneticData::new));

    }

    static float getBaseSpeed(Player player, ItemStack tool, BlockState state, BlockPos pos) {
        float f = tool.getDestroySpeed(state);
        if (f > 1.0F) {
            int i = EnchantmentHelper.getBlockEfficiency(player);
            ItemStack itemstack = player.getMainHandItem();
            if (i > 0 && !itemstack.isEmpty()) {
                f += i * i + 1;
            }
        }

        if (MobEffectUtil.hasDigSpeed(player)) {
            f *= 1.0F + (MobEffectUtil.getDigSpeedAmplification(player) + 1) * 0.2F;
        }

        if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            float f1 = switch (player.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
                case 0 -> 0.3F;
                case 1 -> 0.09F;
                case 2 -> 0.0027F;
                case 3 -> 8.1E-4F;
                default -> 8.1E-4F;
            };
            f *= f1;
        }

        if (player.isEyeInFluidType(ForgeMod.WATER_TYPE.get()) && !EnchantmentHelper.hasAquaAffinity(player)) {
            f /= 5.0F;
        }

        if (!player.onGround()) {
            f /= 5.0F;
        }
        return f;
    }

}
