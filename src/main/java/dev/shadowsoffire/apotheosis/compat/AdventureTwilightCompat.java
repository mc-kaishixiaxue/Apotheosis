package dev.shadowsoffire.apotheosis.compat;

import java.util.Map;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.color.GradientColor;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import twilightforest.TwilightForestMod;
import twilightforest.entity.monster.Redcap;
import twilightforest.init.TFDataAttachments;
import twilightforest.init.TFSounds;

public class AdventureTwilightCompat {

    protected static final Holder<Item> ORE_MAGNET = DeferredHolder.create(Registries.ITEM, TwilightForestMod.prefix("ore_magnet"));
    protected static final Supplier<EntityType<Redcap>> REDCAP = DeferredHolder.create(Registries.ENTITY_TYPE, TwilightForestMod.prefix("redcap"));

    public static void register() {
        GemBonus.CODEC.register(Apotheosis.loc("twilight_ore_magnet"), OreMagnetBonus.CODEC);
        GemBonus.CODEC.register(Apotheosis.loc("twilight_treasure_goblin"), TreasureGoblinBonus.CODEC);
        GemBonus.CODEC.register(Apotheosis.loc("twilight_fortification"), FortificationBonus.CODEC);
        NeoForge.EVENT_BUS.addListener(AdventureTwilightCompat::doGoblins);
    }

    public static class OreMagnetBonus extends GemBonus {

        public static final Codec<OreMagnetBonus> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                gemClass(),
                Purity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values))
            .apply(inst, OreMagnetBonus::new));

        protected final Map<Purity, StepFunction> values;

        public OreMagnetBonus(GemClass gemClass, Map<Purity, StepFunction> values) {
            super(Apotheosis.loc("twilight_ore_magnet"), gemClass);
            this.values = values;
        }

        @Override
        public InteractionResult onItemUse(GemInstance inst, UseOnContext ctx) {
            BlockState state = ctx.getLevel().getBlockState(ctx.getClickedPos());
            if (state.isAir()) return null;
            Level level = ctx.getLevel();
            Player player = ctx.getPlayer();
            player.startUsingItem(ctx.getHand());
            // The ore magnet only checks that the use duration (72000 - param) is > 10
            // https://github.com/TeamTwilight/twilightforest/blob/1.21.x/src/main/java/twilightforest/item/OreMagnetItem.java#L76
            ORE_MAGNET.value().releaseUsing(inst.gemStack(), level, player, 0);
            player.stopUsingItem();
            int cost = this.values.get(inst.purity()).getInt(0);
            ctx.getItemInHand().hurtAndBreak(cost, player, LivingEntity.getSlotForHand(ctx.getHand()));
            return super.onItemUse(inst, ctx);
        }

        @Override
        public Codec<? extends GemBonus> getCodec() {
            return CODEC;
        }

        @Override
        public GemBonus validate() {
            Preconditions.checkArgument(!this.values.isEmpty(), "No values provided!");
            return this;
        }

        @Override
        public boolean supports(Purity purity) {
            return this.values.containsKey(purity);
        }

        @Override
        public Component getSocketBonusTooltip(GemInstance gem, AttributeTooltipContext ctx) {
            return Component.translatable("bonus." + this.getId() + ".desc", this.values.get(gem.purity()).getInt(0)).withStyle(ChatFormatting.YELLOW);
        }

    }

    public static class TreasureGoblinBonus extends GemBonus {

        public static final Codec<TreasureGoblinBonus> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                gemClass(),
                Purity.mapCodec(Data.CODEC).fieldOf("values").forGetter(a -> a.values))
            .apply(inst, TreasureGoblinBonus::new));

        protected final Map<Purity, Data> values;

        public TreasureGoblinBonus(GemClass gemClass, Map<Purity, Data> values) {
            super(Apotheosis.loc("twilight_treasure_goblin"), gemClass);
            this.values = values;
        }

        @Override
        public void doPostAttack(GemInstance inst, LivingEntity user, Entity target) {
            Data d = this.values.get(inst.purity());
            if (Affix.isOnCooldown(makeUniqueId(inst), d.cooldown, user)) return;
            if (user.getRandom().nextFloat() <= d.chance) {
                Redcap goblin = REDCAP.get().create(user.level());
                CompoundTag tag = new CompoundTag();
                tag.putString("DeathLootTable", "apotheosis:entity/treasure_goblin");
                goblin.readAdditionalSaveData(tag);
                goblin.getPersistentData().putBoolean("apoth.treasure_goblin", true);
                goblin.setCustomName(Component.translatable("name.apotheosis.treasure_goblin").withStyle(s -> s.withColor(GradientColor.RAINBOW)));
                goblin.setCustomNameVisible(true);
                goblin.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(new AttributeModifier(Apotheosis.loc("very_fast"), 0.2, Operation.ADD_VALUE));
                goblin.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier(Apotheosis.loc("healmth"), 60, Operation.ADD_VALUE));
                goblin.setHealth(goblin.getMaxHealth());
                for (int i = 0; i < 8; i++) {
                    int x = Mth.nextInt(goblin.getRandom(), -5, 5);
                    int y = Mth.nextInt(goblin.getRandom(), -1, 1);
                    int z = Mth.nextInt(goblin.getRandom(), -5, 5);
                    goblin.setPos(target.position().add(x, y, z));
                    if (user.level().noCollision(goblin)) break;
                    if (i == 7) goblin.setPos(target.position());
                }
                goblin.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
                user.level().addFreshEntity(goblin);
                Affix.startCooldown(makeUniqueId(inst), user);
            }
        }

        @Override
        public Codec<? extends GemBonus> getCodec() {
            return CODEC;
        }

        @Override
        public GemBonus validate() {
            Preconditions.checkArgument(!this.values.isEmpty(), "No values provided!");
            return this;
        }

        @Override
        public boolean supports(Purity purity) {
            return this.values.containsKey(purity);
        }

        @Override
        public Component getSocketBonusTooltip(GemInstance inst, AttributeTooltipContext ctx) {
            Data d = this.values.get(inst.purity());
            Component cooldown = Component.translatable("affix.apotheosis.cooldown", StringUtil.formatTickDuration(d.cooldown, ctx.tickRate()));
            return Component.translatable("bonus." + this.getId() + ".desc", Affix.fmt(d.chance * 100), cooldown).withStyle(ChatFormatting.YELLOW);
        }

        protected static record Data(float chance, int cooldown) {

            public static final Codec<Data> CODEC = RecordCodecBuilder.create(inst -> inst
                .group(
                    Codec.FLOAT.fieldOf("chance").forGetter(Data::chance),
                    Codec.INT.fieldOf("cooldown").forGetter(Data::cooldown))
                .apply(inst, Data::new));

        }

    }

    @SubscribeEvent
    public static void doGoblins(EntityJoinLevelEvent e) {
        if (e.getEntity() instanceof Redcap r && r.getPersistentData().contains("apoth.treasure_goblin")) {
            r.targetSelector.removeAllGoals(Predicates.alwaysTrue());
            r.goalSelector.removeAllGoals(Predicates.alwaysTrue());
            r.goalSelector.addGoal(10, new AvoidEntityGoal<>(r, Player.class, 6, 1, 1.25));
        }
    }

    public static class FortificationBonus extends GemBonus {

        public static final Codec<FortificationBonus> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                gemClass(),
                Purity.mapCodec(Data.CODEC).fieldOf("values").forGetter(a -> a.values))
            .apply(inst, FortificationBonus::new));

        protected final Map<Purity, Data> values;

        public FortificationBonus(GemClass gemClass, Map<Purity, Data> values) {
            super(Apotheosis.loc("twilight_fortification"), gemClass);
            this.values = values;
        }

        @Override
        public void doPostHurt(GemInstance inst, LivingEntity user, DamageSource source) {
            Data d = this.values.get(inst.purity());
            if (Affix.isOnCooldown(makeUniqueId(inst), d.cooldown, user)) return;
            if (user.hasData(TFDataAttachments.FORTIFICATION_SHIELDS) && user.getRandom().nextFloat() <= d.chance) {
                user.getData(TFDataAttachments.FORTIFICATION_SHIELDS).setShields(user, 5, true);
                user.playSound(TFSounds.SHIELD_ADD.get(), 1.0F, (user.getRandom().nextFloat() - user.getRandom().nextFloat()) * 0.2F + 1.0F);
                Affix.startCooldown(makeUniqueId(inst), user);
            }
        }

        @Override
        public Codec<? extends GemBonus> getCodec() {
            return CODEC;
        }

        @Override
        public GemBonus validate() {
            Preconditions.checkArgument(!this.values.isEmpty(), "No values provided!");
            return this;
        }

        @Override
        public boolean supports(Purity purity) {
            return this.values.containsKey(purity);
        }

        @Override
        public Component getSocketBonusTooltip(GemInstance inst, AttributeTooltipContext ctx) {
            Data d = this.values.get(inst.purity());
            Component cooldown = Component.translatable("affix.apotheosis.cooldown", StringUtil.formatTickDuration(d.cooldown, ctx.tickRate()));
            return Component.translatable("bonus." + this.getId() + ".desc", Affix.fmt(d.chance * 100), cooldown).withStyle(ChatFormatting.YELLOW);
        }

        protected static record Data(float chance, int cooldown) {

            public static final Codec<Data> CODEC = RecordCodecBuilder.create(inst -> inst
                .group(
                    Codec.FLOAT.fieldOf("chance").forGetter(Data::chance),
                    Codec.INT.fieldOf("cooldown").forGetter(Data::cooldown))
                .apply(inst, Data::new));

        }

    }

}
