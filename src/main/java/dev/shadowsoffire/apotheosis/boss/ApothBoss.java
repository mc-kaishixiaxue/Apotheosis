package dev.shadowsoffire.apotheosis.boss;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.Apoth.Attachments;
import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.attachments.BonusLootTables;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootController;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.tiers.Constraints;
import dev.shadowsoffire.apotheosis.tiers.Constraints.Constrained;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights.Weighted;
import dev.shadowsoffire.apotheosis.util.NameHelper;
import dev.shadowsoffire.apotheosis.util.SupportingEntity;
import dev.shadowsoffire.apothic_enchanting.asm.EnchHooks;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.json.ChancedEffectInstance;
import dev.shadowsoffire.placebo.json.NBTAdapter;
import dev.shadowsoffire.placebo.json.RandomAttributeModifier;
import dev.shadowsoffire.placebo.systems.gear.GearSet;
import dev.shadowsoffire.placebo.systems.gear.GearSet.SetPredicate;
import dev.shadowsoffire.placebo.systems.gear.GearSetRegistry;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;

public final class ApothBoss implements CodecProvider<ApothBoss>, Constrained, Weighted {

    /**
     * NBT key for a boolean value applied to entity persistent data to indicate a mob is an apoth boss.
     */
    public static final String BOSS_KEY = "apoth.boss";

    /**
     * NBT key for a string value applied to entity persistent data indicating a boss's rarity.
     */
    public static final String RARITY_KEY = BOSS_KEY + ".rarity";

    public static final Codec<AABB> AABB_CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            Codec.DOUBLE.fieldOf("width").forGetter(a -> Math.abs(a.maxX - a.minX)),
            Codec.DOUBLE.fieldOf("height").forGetter(a -> Math.abs(a.maxY - a.minY)))
        .apply(inst, (width, height) -> new AABB(0, 0, 0, width, height, width)));

    public static final Codec<ApothBoss> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            TieredWeights.CODEC.fieldOf("weights").forGetter(Weighted::weights),
            Constraints.CODEC.optionalFieldOf("constraints", Constraints.EMPTY).forGetter(Constrained::constraints),
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(ApothBoss::getEntity),
            AABB_CODEC.fieldOf("size").forGetter(ApothBoss::getSize),
            LootRarity.mapCodec(BossStats.CODEC).fieldOf("stats").forGetter(a -> a.stats),
            SetPredicate.CODEC.listOf().fieldOf("valid_gear_sets").forGetter(a -> a.gearSets),
            NBTAdapter.EITHER_CODEC.optionalFieldOf("nbt").forGetter(a -> a.nbt),
            SupportingEntity.CODEC.optionalFieldOf("mount").forGetter(a -> a.mount),
            BonusLootTables.CODEC.optionalFieldOf("bonus_loot", BonusLootTables.EMPTY).forGetter(a -> a.bonusLoot))
        .apply(inst, ApothBoss::new));

    public static final Predicate<Goal> IS_VILLAGER_ATTACK = a -> a instanceof NearestAttackableTargetGoal && ((NearestAttackableTargetGoal<?>) a).targetType == Villager.class;

    protected final TieredWeights weights;
    protected final Constraints constraints;
    protected final EntityType<?> entity;
    protected final AABB size;
    protected final Map<LootRarity, BossStats> stats;
    protected final List<SetPredicate> gearSets;
    protected final Optional<CompoundTag> nbt;
    protected final Optional<SupportingEntity> mount;
    protected final BonusLootTables bonusLoot;

    public ApothBoss(TieredWeights weights, Constraints constraints, EntityType<?> entity, AABB size, Map<LootRarity, BossStats> stats, List<SetPredicate> armorSets, Optional<CompoundTag> nbt, Optional<SupportingEntity> mount,
        BonusLootTables bonusLoot) {
        this.weights = weights;
        this.constraints = constraints;
        this.entity = entity;
        this.size = size;
        this.stats = stats;
        this.gearSets = armorSets;
        this.nbt = nbt;
        this.mount = mount;
        this.bonusLoot = bonusLoot;
    }

    @Override
    public TieredWeights weights() {
        return this.weights;
    }

    @Override
    public Constraints constraints() {
        return this.constraints;
    }

    public AABB getSize() {
        return this.size;
    }

    public EntityType<?> getEntity() {
        return this.entity;
    }

    /**
     * @see #createBoss(ServerLevelAccessor, BlockPos, RandomSource, float, LootRarity)
     */
    public Mob createBoss(ServerLevelAccessor world, BlockPos pos, GenContext ctx) {
        return this.createBoss(world, pos, ctx, null);
    }

    /**
     * Generates (but does not spawn) the result of this BossItem.
     *
     * @param world  The world to create the entity in.
     * @param pos    The location to place the entity. Will be centered (+0.5, +0.5).
     * @param random A random, used for selection of boss stats.
     * @param luck   The player's luck value.
     * @param rarity A rarity override. This will be clamped to a valid rarity, and randomly generated if null.
     * @return The newly created boss, or it's mount, if it had one.
     */
    public Mob createBoss(ServerLevelAccessor world, BlockPos pos, GenContext ctx, @Nullable LootRarity rarity) {
        CompoundTag fakeNbt = this.nbt.orElse(new CompoundTag());
        fakeNbt.putString("id", EntityType.getKey(this.entity).toString());
        Mob entity = (Mob) EntityType.loadEntityRecursive(fakeNbt, world.getLevel(), Function.identity());

        this.initBoss(entity, ctx, rarity);
        // Re-read here so we can apply certain things after the boss has been modified
        // But only mob-specific things, not a full load()
        if (this.nbt.isPresent()) {
            entity.readAdditionalSaveData(this.nbt.get());
        }

        if (this.mount.isPresent()) {
            Mob mountedEntity = this.mount.get().create(world.getLevel(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            entity.startRiding(mountedEntity, true);
            entity = mountedEntity;
        }

        entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, ctx.rand().nextFloat() * 360.0F, 0.0F);
        return entity;
    }

    /**
     * Initializes an entity as a boss, based on the stats of this BossItem.
     *
     * @param rand
     * @param entity
     */
    public void initBoss(Mob entity, GenContext ctx, @Nullable LootRarity rarity) {
        RandomSource rand = ctx.rand();

        if (rarity == null) {
            rarity = LootRarity.random(ctx, this.stats.keySet());
        }

        BossStats stats = this.stats.get(rarity);
        int duration = entity instanceof Creeper ? 6000 : Integer.MAX_VALUE;

        for (ChancedEffectInstance inst : stats.effects()) {
            if (rand.nextFloat() <= inst.chance()) {
                entity.addEffect(inst.create(rand, duration));
            }
        }

        for (RandomAttributeModifier modif : stats.modifiers()) {
            modif.apply(rand, entity);
        }

        entity.goalSelector.getAvailableGoals().removeIf(IS_VILLAGER_ATTACK);
        String name = NameHelper.setEntityName(rand, entity);

        GearSet set = GearSetRegistry.INSTANCE.getRandomSet(rand, ctx.luck(), this.gearSets);
        set.apply(entity);

        boolean anyValid = false;

        for (EquipmentSlot t : EquipmentSlot.values()) {
            ItemStack s = entity.getItemBySlot(t);
            if (!s.isEmpty() && !LootCategory.forItem(s).isNone()) {
                anyValid = true;
                break;
            }
        }

        if (!anyValid) throw new RuntimeException("Attempted to apply boss gear set " + GearSetRegistry.INSTANCE.getKey(set) + " but it had no valid affix loot items generated.");

        int guaranteed = rand.nextInt(6);

        ItemStack temp = entity.getItemBySlot(EquipmentSlot.values()[guaranteed]);
        while (temp.isEmpty() || LootCategory.forItem(temp) == LootCategory.NONE) {
            guaranteed = rand.nextInt(6);
            temp = entity.getItemBySlot(EquipmentSlot.values()[guaranteed]);
        }

        for (EquipmentSlot s : EquipmentSlot.values()) {
            ItemStack stack = entity.getItemBySlot(s);
            if (stack.isEmpty()) continue;
            if (s.ordinal() == guaranteed) entity.setDropChance(s, 2F);
            if (s.ordinal() == guaranteed) {
                entity.setItemSlot(s, modifyBossItem(stack, name, ctx, rarity, stats, entity.level().registryAccess()));
                entity.setCustomName(((MutableComponent) entity.getCustomName()).withStyle(Style.EMPTY.withColor(rarity.color())));
            }
            else if (rand.nextFloat() < stats.enchantChance()) {
                enchantBossItem(rand, stack, stats.enchLevels().secondary(), true, entity.level().registryAccess());
                entity.setItemSlot(s, stack);
            }
        }
        entity.getPersistentData().putBoolean(BOSS_KEY, true);
        entity.getPersistentData().putString(RARITY_KEY, RarityRegistry.INSTANCE.getKey(rarity).toString());
        entity.setHealth(entity.getMaxHealth());
        if (AdventureConfig.bossGlowOnSpawn) {
            entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 3600));
        }
        entity.setData(Attachments.BONUS_LOOT_TABLES, this.bonusLoot);
    }

    public static void enchantBossItem(RandomSource rand, ItemStack stack, int level, boolean treasure, RegistryAccess reg) {
        Stream<Holder<Enchantment>> available = reg.registryOrThrow(Registries.ENCHANTMENT).getTag(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT).map(HolderSet::stream).orElse(Stream.empty());
        List<EnchantmentInstance> ench = EnchantmentHelper.selectEnchantment(rand, stack, level, available);
        ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(stack));
        ench.stream().filter(d -> !d.enchantment.is(EnchantmentTags.CURSE)).forEach(i -> builder.upgrade(i.enchantment, i.level));
        EnchantmentHelper.setEnchantments(stack, builder.toImmutable());
    }

    public static ItemStack modifyBossItem(ItemStack stack, String bossName, GenContext ctx, LootRarity rarity, BossStats stats, RegistryAccess reg) {
        RandomSource rand = ctx.rand();
        enchantBossItem(rand, stack, stats.enchLevels().primary(), true, reg);
        NameHelper.setItemName(rand, stack);
        stack = LootController.createLootItem(stack, LootCategory.forItem(stack), rarity, ctx);

        String bossOwnerName = String.format(NameHelper.ownershipFormat, bossName);
        Component name = AffixHelper.getName(stack);
        if (!bossName.isEmpty() && name.getContents() instanceof TranslatableContents tc) {
            String oldKey = tc.getKey();
            String newKey = "misc.apotheosis.affix_name.two".equals(oldKey) ? "misc.apotheosis.affix_name.three" : "misc.apotheosis.affix_name.four";
            Object[] newArgs = new Object[tc.getArgs().length + 1];
            newArgs[0] = bossOwnerName;
            for (int i = 1; i < newArgs.length; i++) {
                newArgs[i] = tc.getArgs()[i - 1];
            }
            Component copy = Component.translatable(newKey, newArgs).withStyle(name.getStyle().withItalic(false));
            AffixHelper.setName(stack, copy);
        }

        ItemEnchantments.Mutable enchMap = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (Object2IntMap.Entry<Holder<Enchantment>> e : EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet()) {
            if (e.getKey() != null) {
                enchMap.upgrade(e.getKey(), Math.min(EnchHooks.getMaxLevel(e.getKey().value()), e.getIntValue() + rand.nextInt(2)));
            }
        }

        if (AdventureConfig.curseBossItems) {
            List<Holder.Reference<Enchantment>> curses = reg.registryOrThrow(Registries.ENCHANTMENT).holders().filter(e -> e.is(EnchantmentTags.CURSE) && e.is(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT)).toList();
            if (!curses.isEmpty()) {
                Holder<Enchantment> curse = curses.get(rand.nextInt(curses.size()));
                enchMap.upgrade(curse, Mth.nextInt(rand, 1, EnchHooks.getMaxLevel(curse.value())));
            }
        }

        EnchantmentHelper.setEnchantments(stack, enchMap.toImmutable());
        stack.set(Components.FROM_BOSS, true);
        return stack;
    }

    @Override
    public Codec<? extends ApothBoss> getCodec() {
        return CODEC;
    }

}
