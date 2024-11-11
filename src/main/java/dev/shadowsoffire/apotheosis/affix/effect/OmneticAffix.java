package dev.shadowsoffire.apotheosis.affix.effect;

import java.util.Arrays;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.HarvestCheck;

public class OmneticAffix extends Affix {

    public static final Codec<OmneticAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            affixDef(),
            LootRarity.mapCodec(OmneticData.CODEC).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, OmneticAffix::new));

    protected final Map<LootRarity, OmneticData> values;

    public OmneticAffix(AffixDefinition def, Map<LootRarity, OmneticData> values) {
        super(def);
        this.values = values;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return cat.isBreaker() && this.values.containsKey(rarity);
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        return Component.translatable("affix." + this.id() + ".desc", Component.translatable("misc.apotheosis." + this.values.get(inst.getRarity()).name));
    }

    // TODO: Generify these and remove reliance on instanceof checks.

    public static void harvest(HarvestCheck e) {
        ItemStack stack = e.getEntity().getMainHandItem();
        if (!stack.isEmpty()) {
            AffixInstance inst = AffixHelper.streamAffixes(stack).filter(i -> i.getAffix() instanceof OmneticAffix).findFirst().orElse(null);
            if (inst != null && inst.isValid()) {
                OmneticData data = ((OmneticAffix) inst.getAffix()).values.get(inst.rarity().get());
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
    public static void speed(BreakSpeed e) {
        ItemStack stack = e.getEntity().getMainHandItem();
        if (!stack.isEmpty()) {
            AffixInstance inst = AffixHelper.getAffixes(stack).values().stream().filter(OmneticAffix.class::isInstance).findFirst().orElse(null);
            if (inst != null && inst.isValid()) {
                float speed = e.getOriginalSpeed();
                OmneticData data = ((OmneticAffix) inst.getAffix()).values.get(inst.rarity().get());
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

    /**
     * Resolves the base dig speed for a player. This is effectively a copy of {@link Player#getDigSpeed}
     * with the event-firing code near the end removed.
     */
    private static float getBaseSpeed(Player player, ItemStack tool, BlockState state, BlockPos pos) {
        float f = tool.getDestroySpeed(state);
        if (f > 1.0F) {
            f += (float) player.getAttributeValue(Attributes.MINING_EFFICIENCY);
        }

        if (MobEffectUtil.hasDigSpeed(player)) {
            f *= 1.0F + (MobEffectUtil.getDigSpeedAmplification(player) + 1) * 0.2F;
        }

        if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            f *= switch (player.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
                case 0 -> 0.3F;
                case 1 -> 0.09F;
                case 2 -> 0.0027F;
                default -> 8.1E-4F;
            };
        }

        f *= (float) player.getAttributeValue(Attributes.BLOCK_BREAK_SPEED);
        if (player.isEyeInFluidType(NeoForgeMod.WATER_TYPE.value())) {
            f *= (float) player.getAttribute(Attributes.SUBMERGED_MINING_SPEED).getValue();
        }

        if (!player.onGround()) {
            f /= 5.0F;
        }

        return f;
    }

    private static record OmneticData(String name, ItemStack[] items) {

        public static Codec<OmneticData> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                Codec.STRING.fieldOf("name").forGetter(OmneticData::name),
                Codec.list(ItemStack.CODEC).xmap(l -> l.toArray(new ItemStack[0]), Arrays::asList).fieldOf("items").forGetter(OmneticData::items))
            .apply(inst, OmneticData::new));

    }

}
