package dev.shadowsoffire.apotheosis.boss;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.AdventureModule;
import dev.shadowsoffire.apotheosis.boss.MinibossRegistry.IEntityMatch;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.tiers.Constraints;
import dev.shadowsoffire.apotheosis.tiers.Constraints.Constrained;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights.Weighted;
import dev.shadowsoffire.apotheosis.util.NameHelper;
import dev.shadowsoffire.apotheosis.util.SupportingEntity;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.json.ChancedEffectInstance;
import dev.shadowsoffire.placebo.json.NBTAdapter;
import dev.shadowsoffire.placebo.json.RandomAttributeModifier;
import dev.shadowsoffire.placebo.systems.gear.GearSet;
import dev.shadowsoffire.placebo.systems.gear.GearSet.SetPredicate;
import dev.shadowsoffire.placebo.systems.gear.GearSetRegistry;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

public final class ApothMiniboss implements CodecProvider<ApothMiniboss>, Constrained, Weighted, IEntityMatch {

    public static final String NAME_GEN = "use_name_generation";

    /**
     * NBT key for a boolean value applied to entity persistent data to indicate a mob is a miniboss.
     */
    public static final String MINIBOSS_KEY = "apoth.miniboss";

    /**
     * NBT key for a string value applied to entity persistent data indicating the player that trigger's a miniboss's summoning.
     * <p>
     * Used to resolve the player when the miniboss is added to the world and initialized.
     */
    public static final String PLAYER_KEY = MINIBOSS_KEY + ".player";

    public static final Codec<ApothMiniboss> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            TieredWeights.CODEC.fieldOf("weights").forGetter(Weighted::weights),
            Constraints.CODEC.optionalFieldOf("constraints", Constraints.EMPTY).forGetter(Constrained::constraints),
            Codec.floatRange(0, 1).fieldOf("success_chance").forGetter(a -> a.chance),
            ComponentSerialization.CODEC.optionalFieldOf("name", CommonComponents.EMPTY).forGetter(a -> a.name),
            RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).fieldOf("entities").forGetter(a -> a.entities),
            BossStats.CODEC.fieldOf("stats").forGetter(a -> a.stats),
            Codec.BOOL.optionalFieldOf("affixed", false).forGetter(a -> a.affixed),
            SetPredicate.CODEC.listOf().optionalFieldOf("valid_gear_sets", Collections.emptyList()).forGetter(a -> a.gearSets),
            NBTAdapter.EITHER_CODEC.optionalFieldOf("nbt").forGetter(a -> a.nbt),
            SupportingEntity.CODEC.listOf().optionalFieldOf("supporting_entities", Collections.emptyList()).forGetter(a -> a.support),
            SupportingEntity.CODEC.optionalFieldOf("mount").forGetter(a -> a.mount),
            Exclusion.CODEC.listOf().optionalFieldOf("exclusions", Collections.emptyList()).forGetter(a -> a.exclusions),
            Codec.BOOL.optionalFieldOf("finalize", false).forGetter(a -> a.finalize))
        .apply(inst, ApothMiniboss::new));

    /**
     * Weight relative to other minibosses that may apply to the same entity.
     */
    protected final TieredWeights weights;

    /**
     * Application constraints that may remove this miniboss from the available pool.
     */
    protected final Constraints constraints;

    /**
     * Chance that this miniboss item is applied, if selected. Selection runs for every entity spawn.
     * This chance is rolled after weight selection is completed.
     */
    protected final float chance;

    /**
     * Name of the miniboss. Can be a lang key. Empty or null will cause no name to be set. The special string "use_name_generation" will invoke NameHelper (like
     * normal bosses).
     */
    protected final Component name;

    /**
     * List of matching entities.
     */
    protected final HolderSet<EntityType<?>> entities;

    /**
     * Stats that are applied to the miniboss.
     */
    protected final BossStats stats;

    /**
     * If the miniboss will be given an affix item like a normal boss.
     * Rarity selection follows the affix convert rarities of the dimension.
     */
    protected final boolean affixed;

    /**
     * Valid armor sets for this miniboss.
     */
    protected final List<SetPredicate> gearSets;

    /**
     * Entity NBT
     */
    protected final Optional<CompoundTag> nbt;

    /**
     * A list of supporting entities that will be spawned if this miniboss is activated.+
     */
    protected final List<SupportingEntity> support;

    /**
     * The entity the miniboss will mount.
     */
    protected final Optional<SupportingEntity> mount;

    /**
     * List of rules that may prevent this miniboss from being selected.
     *
     * @see {@link Exclusion}
     */
    protected final List<Exclusion> exclusions;

    /**
     * If true, the SpecialSpawn/FinalizeSpawn event is not cancelled, and {@link Mob#finalizeSpawn} will still be called.<br>
     * Finalization will happen before the miniboss data is applied, since miniboss data is delayed until {@link EntityJoinLevelEvent}.
     */
    protected final boolean finalize;

    public ApothMiniboss(TieredWeights weights, Constraints constraints, float chance,
        Component name, HolderSet<EntityType<?>> entities, BossStats stats,
        boolean affixed, List<SetPredicate> gearSets, Optional<CompoundTag> nbt, List<SupportingEntity> support,
        Optional<SupportingEntity> mount, List<Exclusion> exclusions, boolean finalize) {
        this.weights = weights;
        this.constraints = constraints;
        this.chance = chance;
        this.name = name;
        this.entities = entities;
        this.stats = stats;
        this.affixed = affixed;
        this.gearSets = gearSets;
        this.nbt = nbt;
        this.support = support;
        this.mount = mount;
        this.exclusions = exclusions;
        this.finalize = finalize;
    }

    @Override
    public TieredWeights weights() {
        return this.weights;
    }

    @Override
    public Constraints constraints() {
        return this.constraints;
    }

    public float getChance() {
        return this.chance;
    }

    @Override
    public HolderSet<EntityType<?>> getEntities() {
        return this.entities;
    }

    /**
     * Transforms a mob into this miniboss, spawning any supporting entities or mounts as needed.
     *
     * @param mob    The mob being transformed.
     * @param random A random, used for selection of boss stats.
     * @return The newly created boss, or it's mount, if it had one.
     */
    public void transformMiniboss(ServerLevelAccessor level, Mob mob, GenContext ctx) {
        var pos = mob.getPosition(0);
        if (this.nbt.isPresent()) {
            CompoundTag nbt = this.nbt.get();
            if (nbt.contains(Entity.PASSENGERS_TAG)) {
                ListTag passengers = nbt.getList(Entity.PASSENGERS_TAG, 10);
                for (int i = 0; i < passengers.size(); ++i) {
                    Entity entity = EntityType.loadEntityRecursive(passengers.getCompound(i), level.getLevel(), Function.identity());
                    if (entity != null) {
                        entity.startRiding(mob, true);
                    }
                }
            }
        }
        mob.setPos(pos);
        this.initBoss(mob, ctx);
        // readAdditionalSaveData should leave unchanged any tags that are not in the NBT data.
        if (this.nbt.isPresent()) {
            mob.readAdditionalSaveData(this.nbt.get());
        }

        if (this.mount.isPresent()) {
            Mob mountedEntity = this.mount.get().create(mob.level(), mob.getX() + 0.5, mob.getY(), mob.getZ() + 0.5);
            mob.startRiding(mountedEntity, true);
            level.addFreshEntity(mountedEntity);
        }

        if (this.support != null) {
            for (var support : this.support) {
                Mob supportingMob = support.create(mob.level(), mob.getX() + 0.5, mob.getY(), mob.getZ() + 0.5);
                level.addFreshEntity(supportingMob);
            }
        }
    }

    /**
     * Initializes an entity as a boss, based on the stats of this BossItem.
     *
     * @param rand
     * @param mob
     */
    public void initBoss(Mob mob, GenContext ctx) {
        RandomSource rand = ctx.rand();
        mob.getPersistentData().putBoolean("apoth.miniboss", true);

        int duration = mob instanceof Creeper ? 6000 : Integer.MAX_VALUE;

        for (ChancedEffectInstance inst : this.stats.effects()) {
            if (rand.nextFloat() <= inst.chance()) {
                mob.addEffect(inst.create(rand, duration));
            }
        }

        for (RandomAttributeModifier modif : this.stats.modifiers()) {
            modif.apply(rand, mob);
        }

        String nameStr = this.name.getString();
        if (NAME_GEN.equals(nameStr)) {
            NameHelper.setEntityName(rand, mob);
        }
        else if (!nameStr.isBlank()) {
            mob.setCustomName(this.name);
        }

        if (mob.hasCustomName()) mob.setCustomNameVisible(true);

        if (!this.gearSets.isEmpty()) {
            GearSet set = GearSetRegistry.INSTANCE.getRandomSet(rand, ctx.luck(), this.gearSets);
            Preconditions.checkNotNull(set, String.format("Failed to find a valid gear set for the miniboss %s.", MinibossRegistry.INSTANCE.getKey(this)));
            set.apply(mob);
        }

        int guaranteed = -1;
        if (this.affixed) {
            boolean anyValid = false;

            for (EquipmentSlot t : EquipmentSlot.values()) {
                ItemStack s = mob.getItemBySlot(t);
                if (!s.isEmpty() && !LootCategory.forItem(s).isNone()) {
                    anyValid = true;
                    break;
                }
            }

            if (!anyValid) {
                Apotheosis.LOGGER.error("Attempted to affix a miniboss with ID " + MinibossRegistry.INSTANCE.getKey(this) + " but it is not wearing any affixable items!");
                return;
            }

            guaranteed = rand.nextInt(6);

            ItemStack temp = mob.getItemBySlot(EquipmentSlot.values()[guaranteed]);
            while (temp.isEmpty() || LootCategory.forItem(temp) == LootCategory.NONE) {
                guaranteed = rand.nextInt(6);
                temp = mob.getItemBySlot(EquipmentSlot.values()[guaranteed]);
            }

            // TODO: Change `boolean affixed` to an AffixData class with a specified set of rarities to pull from.
            var rarity = LootRarity.randomFromHolders(ctx, AdventureConfig.AFFIX_CONVERT_RARITIES.get(mob.level().dimension().location()));
            ApothBoss.modifyBossItem(temp, mob.hasCustomName() ? mob.getCustomName().getString() : "", ctx, rarity, this.stats, mob.level().registryAccess());
            mob.setCustomName(((MutableComponent) mob.getCustomName()).withStyle(Style.EMPTY.withColor(rarity.getColor())));
            mob.setDropChance(EquipmentSlot.values()[guaranteed], 2F);
        }

        for (EquipmentSlot s : EquipmentSlot.values()) {
            ItemStack stack = mob.getItemBySlot(s);
            if (!stack.isEmpty() && s.ordinal() != guaranteed && rand.nextFloat() < this.stats.enchantChance()) {
                ApothBoss.enchantBossItem(rand, stack, stats.enchLevels().secondary(), true, mob.level().registryAccess());
                mob.setItemSlot(s, stack);
            }
        }
        mob.setHealth(mob.getMaxHealth());
    }

    @Override
    public Codec<? extends ApothMiniboss> getCodec() {
        return CODEC;
    }

    public boolean requiresNbtAccess() {
        return this.exclusions.stream().anyMatch(Exclusion::requiresNbtAccess);
    }

    public boolean isExcluded(Mob mob, ServerLevelAccessor level, MobSpawnType type) {
        CompoundTag tag = this.requiresNbtAccess() ? mob.saveWithoutId(new CompoundTag()) : null;
        return this.exclusions.stream().anyMatch(ex -> ex.isExcluded(mob, level, type, tag));
    }

    public boolean shouldFinalize() {
        return this.finalize;
    }

}
