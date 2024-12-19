package dev.shadowsoffire.apotheosis.mobs.util;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.attachments.BonusLootTables;
import dev.shadowsoffire.apotheosis.mobs.types.Elite;
import dev.shadowsoffire.apotheosis.mobs.types.Invader;
import dev.shadowsoffire.apotheosis.tiers.Constraints;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.util.NameHelper;
import dev.shadowsoffire.apotheosis.util.SupportingEntity;
import dev.shadowsoffire.placebo.json.NBTAdapter;
import dev.shadowsoffire.placebo.systems.gear.GearSet;
import dev.shadowsoffire.placebo.systems.gear.GearSet.SetPredicate;
import dev.shadowsoffire.placebo.systems.gear.GearSetRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;

/**
 * Basic boss information, shared between {@link Elite} and {@link Invader}.
 *
 * @param weights       The weights for this element, relative to other objects of the same type.
 * @param constraints   Application constraints that may remove this element from the available pool.
 * @param name          The entity name. May be a lang key. Empty or null will cause no name to be set.
 *                      The special string "use_name_generation" will invoke {@link NameHelper}.
 * @param bonusLoot     Any bonus loot tables that will be dropped by the entity.
 * @param gearSets      An optional list of {@link SetPredicates} controlling what gear sets may be applied. An empty optional will not equip any gear.
 *                      Individual predicates are logically OR'd. An empty list (but not an empty optional) will use all available gear sets.
 * @param nbt           Entity NBT to apply to the target mob.
 * @param mount         An optional {@link SupportingEntity} that the target entity will start riding.
 * @param support       A list of entities to spawn alongside the entity.
 * @param finalizeSpawn If {@link Mob#finalizeSpawn} will be called for the target entity.
 * @param exclusions    A list of exclusions that may prevent a selected entity from being spawned. Multiple exclusions are OR'd.
 */
public record BasicBossData(
    TieredWeights weights,
    Constraints constraints,
    Component name,
    BonusLootTables bonusLoot,
    Optional<List<SetPredicate>> gearSets,
    Optional<CompoundTag> nbt,
    Optional<SupportingEntity> mount,
    List<SupportingEntity> support,
    boolean finalizeSpawn,
    List<Exclusion> exclusions) {

    /**
     * Causes {@link BasicBossData#name()} to be filled in with a generated name.
     */
    public static final String NAME_GEN = "use_name_generation";

    public static final Codec<BasicBossData> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            TieredWeights.CODEC.fieldOf("weights").forGetter(BasicBossData::weights),
            Constraints.CODEC.optionalFieldOf("constraints", Constraints.EMPTY).forGetter(BasicBossData::constraints),
            ComponentSerialization.CODEC.optionalFieldOf("name", CommonComponents.EMPTY).forGetter(BasicBossData::name),
            BonusLootTables.CODEC.optionalFieldOf("bonus_loot", BonusLootTables.EMPTY).forGetter(BasicBossData::bonusLoot),
            SetPredicate.CODEC.listOf().optionalFieldOf("valid_gear_sets").forGetter(BasicBossData::gearSets),
            NBTAdapter.EITHER_CODEC.optionalFieldOf("nbt").forGetter(BasicBossData::nbt),
            SupportingEntity.CODEC.optionalFieldOf("mount").forGetter(BasicBossData::mount),
            SupportingEntity.CODEC.listOf().optionalFieldOf("supporting_entities", Collections.emptyList()).forGetter(BasicBossData::support),
            Codec.BOOL.optionalFieldOf("finalize", false).forGetter(BasicBossData::finalizeSpawn),
            Exclusion.CODEC.listOf().optionalFieldOf("exclusions", Collections.emptyList()).forGetter(BasicBossData::exclusions))
        .apply(inst, BasicBossData::new));

    public static final Codec<AABB> AABB_CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            Codec.DOUBLE.fieldOf("width").forGetter(a -> Math.abs(a.maxX - a.minX)),
            Codec.DOUBLE.fieldOf("height").forGetter(a -> Math.abs(a.maxY - a.minY)))
        .apply(inst, (width, height) -> new AABB(0, 0, 0, width, height, width)));

    public boolean hasGearSets() {
        return this.gearSets.isPresent();
    }

    public boolean hasNbt() {
        return this.nbt.isPresent();
    }

    public boolean hasMount() {
        return this.mount.isPresent();
    }

    public void applyEntityName(RandomSource rand, Mob mob) {
        String nameStr = this.name.getString();

        if (NAME_GEN.equals(nameStr)) {
            NameHelper.setEntityName(rand, mob);
        }
        else if (!nameStr.isBlank()) {
            mob.setCustomName(this.name);
        }

        if (mob.hasCustomName()) {
            mob.setCustomNameVisible(true);
        }
    }

    @Nullable
    public GearSet applyGearSet(RandomSource rand, float luck, Mob mob) {
        if (this.gearSets.isEmpty()) {
            return null;
        }

        GearSet set = GearSetRegistry.INSTANCE.getRandomSet(rand, luck, this.gearSets.get());
        if (set != null) {
            set.apply(mob);
        }
        return set;
    }

    /**
     * Creates the mounted entity stored in this boss data. This method does not add the entity to the level.
     */
    public Mob createMount(ServerLevelAccessor level, BlockPos pos, Mob rider) {
        if (!this.hasMount()) {
            Apotheosis.LOGGER.error("BasicBossData#createMount called when hasMount() was false!");
            return rider;
        }

        Mob mountedEntity = this.mount.get().create(level.getLevel(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        rider.startRiding(mountedEntity, true);
        return mountedEntity;
    }

    public boolean isExcluded(Mob mob, ServerLevelAccessor level, MobSpawnType type) {
        return Exclusion.isExcluded(this.exclusions, mob, level, type);
    }

    // TODO: Builder class

}
