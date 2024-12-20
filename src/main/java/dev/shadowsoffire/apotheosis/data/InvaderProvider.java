package dev.shadowsoffire.apotheosis.data;

import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import org.spongepowered.include.com.google.common.base.Preconditions;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.mobs.registries.InvaderRegistry;
import dev.shadowsoffire.apotheosis.mobs.types.Invader;
import dev.shadowsoffire.apotheosis.mobs.util.BasicBossData;
import dev.shadowsoffire.apotheosis.tiers.Constraints;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import dev.shadowsoffire.placebo.util.StepFunction;
import dev.shadowsoffire.placebo.util.data.DynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.Tags;

public class InvaderProvider extends DynamicRegistryProvider<Invader> {

    public static final int DEFAULT_WEIGHT = 100;
    public static final int DEFAULT_QUALITY = 0;

    public InvaderProvider(PackOutput output, CompletableFuture<Provider> registries) {
        super(output, registries, InvaderRegistry.INSTANCE);
    }

    @Override
    public String getName() {
        return "Apothic Invaders";
    }

    @Override
    public void generate() {
        HolderLookup.Provider registries = this.lookupProvider.join();
        RegistryLookup<Biome> biomes = registries.lookup(Registries.BIOME).get();

        LootRarity uncommon = rarity("uncommon");
        LootRarity rare = rarity("rare");
        LootRarity epic = rarity("epic");
        LootRarity mythic = rarity("mythic");

        addBoss("overworld/zombie", b -> basicMeleeStats(b)
            .entity(EntityType.ZOMBIE)
            .size(0.6, 1.95)
            .basicData(c -> meleeGear(c)
                .name(Component.literal(BasicBossData.NAME_GEN))
                .weights(TieredWeights.forAllTiers(DEFAULT_WEIGHT, DEFAULT_QUALITY))
                .constraints(Constraints.forDimension(Level.OVERWORLD))
                .bonusLoot(Apoth.LootTables.BONUS_BOSS_DROPS)));

        addBoss("overworld/husk", b -> basicMeleeStats(b)
            .entity(EntityType.HUSK)
            .size(0.6, 1.95)
            .basicData(c -> meleeGear(c)
                .name(Component.literal(BasicBossData.NAME_GEN))
                .weights(TieredWeights.forAllTiers(50, 2))
                .constraints(Constraints.forDimension(Level.OVERWORLD))
                .bonusLoot(Apoth.LootTables.BONUS_BOSS_DROPS)));

        addBoss("overworld/husk_in_dry", b -> basicMeleeStats(b)
            .entity(EntityType.HUSK)
            .size(0.6, 1.95)
            .basicData(c -> meleeGear(c)
                .name(Component.literal(BasicBossData.NAME_GEN))
                .weights(TieredWeights.forAllTiers(120, DEFAULT_QUALITY))
                .constraints(Constraints.forBiomes(biomes, Tags.Biomes.IS_DRY_OVERWORLD))
                .bonusLoot(Apoth.LootTables.BONUS_BOSS_DROPS)));

        addBoss("overworld/enderman", b -> basicMeleeStats(b)
            .entity(EntityType.ENDERMAN)
            .size(0.6, 2.95)
            .basicData(c -> meleeGear(c)
                .name(Component.literal(BasicBossData.NAME_GEN))
                .weights(TieredWeights.forTiersAbove(WorldTier.ASCENT, 150, 5))
                .constraints(Constraints.forDimension(Level.OVERWORLD))
                .bonusLoot(Apoth.LootTables.BONUS_BOSS_DROPS)));

        // TODO: Figure out how to make drowned not act stupid...

        addBoss("overworld/skeleton", b -> basicRangedStats(b)
            .entity(EntityType.SKELETON)
            .size(0.6, 1.95)
            .basicData(c -> rangedGear(c)
                .name(Component.literal(BasicBossData.NAME_GEN))
                .weights(TieredWeights.forAllTiers(DEFAULT_WEIGHT, DEFAULT_QUALITY))
                .constraints(Constraints.forDimension(Level.OVERWORLD))
                .bonusLoot(Apoth.LootTables.BONUS_BOSS_DROPS)));

        addBoss("overworld/stray", b -> basicRangedStats(b)
            .entity(EntityType.STRAY)
            .size(0.6, 1.95)
            .basicData(c -> rangedGear(c)
                .name(Component.literal(BasicBossData.NAME_GEN))
                .weights(TieredWeights.forAllTiers(50, 2))
                .constraints(Constraints.forDimension(Level.OVERWORLD))
                .bonusLoot(Apoth.LootTables.BONUS_BOSS_DROPS)));

        addBoss("overworld/bogged", b -> basicRangedStats(b)
            .entity(EntityType.BOGGED)
            .size(0.6, 1.95)
            .basicData(c -> rangedGear(c)
                .name(Component.literal(BasicBossData.NAME_GEN))
                .weights(TieredWeights.forAllTiers(50, 2))
                .constraints(Constraints.forDimension(Level.OVERWORLD))
                .bonusLoot(Apoth.LootTables.BONUS_BOSS_DROPS)));

        addBoss("overworld/stray_in_cold", b -> basicRangedStats(b)
            .entity(EntityType.STRAY)
            .size(0.6, 1.95)
            .basicData(c -> rangedGear(c)
                .name(Component.literal(BasicBossData.NAME_GEN))
                .weights(TieredWeights.forAllTiers(120, DEFAULT_QUALITY))
                .constraints(Constraints.forBiomes(biomes, Tags.Biomes.IS_COLD_OVERWORLD))
                .bonusLoot(Apoth.LootTables.BONUS_BOSS_DROPS)));

        addBoss("overworld/bogged_in_wet", b -> basicRangedStats(b)
            .entity(EntityType.BOGGED)
            .size(0.6, 1.95)
            .basicData(c -> rangedGear(c)
                .name(Component.literal(BasicBossData.NAME_GEN))
                .weights(TieredWeights.forAllTiers(120, DEFAULT_QUALITY))
                .constraints(Constraints.forBiomes(biomes, Tags.Biomes.IS_WET_OVERWORLD))
                .bonusLoot(Apoth.LootTables.BONUS_BOSS_DROPS)));
    }

    private Invader.Builder basicMeleeStats(Invader.Builder builder) {
        LootRarity uncommon = rarity("uncommon");
        LootRarity rare = rarity("rare");
        LootRarity epic = rarity("epic");
        LootRarity mythic = rarity("mythic");

        return builder
            .stats(uncommon, c -> c
                .enchantChance(0.25F)
                .enchLevels(20, 12)
                .effect(1, MobEffects.FIRE_RESISTANCE)
                .modifier(Attributes.MAX_HEALTH, Operation.ADD_VALUE, 20, 60)
                .modifier(Attributes.MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_BASE, 0.15F, 0.20F)
                .modifier(Attributes.ATTACK_DAMAGE, Operation.ADD_MULTIPLIED_BASE, 0.2F, 0.4F)
                .modifier(Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, StepFunction.constant(0.3F)))
            .stats(rare, c -> c
                .enchantChance(0.35F)
                .enchLevels(23, 15)
                .effect(1, MobEffects.FIRE_RESISTANCE)
                .modifier(Attributes.MAX_HEALTH, Operation.ADD_VALUE, 40, 90)
                .modifier(Attributes.MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_BASE, 0.15F, 0.25F)
                .modifier(Attributes.ATTACK_DAMAGE, Operation.ADD_MULTIPLIED_BASE, 0.25F, 0.5F)
                .modifier(Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, StepFunction.constant(0.4F)))
            .stats(epic, c -> c
                .enchantChance(0.6F)
                .enchLevels(35, 25)
                .effect(1, MobEffects.FIRE_RESISTANCE)
                .modifier(Attributes.MAX_HEALTH, Operation.ADD_VALUE, 80, 150)
                .modifier(Attributes.MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_BASE, 0.25F, 0.40F)
                .modifier(Attributes.ATTACK_DAMAGE, Operation.ADD_MULTIPLIED_BASE, 0.30F, 0.65F)
                .modifier(Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, StepFunction.constant(0.5F))
                .modifier(Attributes.ARMOR, Operation.ADD_VALUE, StepFunction.constant(4F))
                .modifier(Attributes.ARMOR_TOUGHNESS, Operation.ADD_VALUE, StepFunction.constant(10F)))
            .stats(mythic, c -> c
                .enchantChance(0.75F)
                .enchLevels(35, 25)
                .effect(1, MobEffects.FIRE_RESISTANCE)
                .modifier(Attributes.MAX_HEALTH, Operation.ADD_VALUE, 110, 180)
                .modifier(Attributes.MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_BASE, 0.3F, 0.65F)
                .modifier(Attributes.ATTACK_DAMAGE, Operation.ADD_MULTIPLIED_BASE, 0.7F, 1.05F)
                .modifier(Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, StepFunction.constant(0.8F))
                .modifier(Attributes.ARMOR, Operation.ADD_VALUE, StepFunction.constant(10F))
                .modifier(Attributes.ARMOR_TOUGHNESS, Operation.ADD_VALUE, StepFunction.constant(20F)));
    }

    private Invader.Builder basicRangedStats(Invader.Builder builder) {
        LootRarity uncommon = rarity("uncommon");
        LootRarity rare = rarity("rare");
        LootRarity epic = rarity("epic");
        LootRarity mythic = rarity("mythic");

        return builder
            .stats(uncommon, c -> c
                .enchantChance(0.25F)
                .enchLevels(20, 12)
                .effect(1, MobEffects.FIRE_RESISTANCE)
                .modifier(Attributes.MAX_HEALTH, Operation.ADD_VALUE, 20, 50)
                .modifier(Attributes.MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_BASE, 0.05F, 0.10F)
                .modifier(ALObjects.Attributes.ARROW_DAMAGE, Operation.ADD_MULTIPLIED_BASE, 0.2F, 0.4F)
                .modifier(Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, StepFunction.constant(0.25F)))
            .stats(rare, c -> c
                .enchantChance(0.35F)
                .enchLevels(23, 15)
                .effect(1, MobEffects.FIRE_RESISTANCE)
                .modifier(Attributes.MAX_HEALTH, Operation.ADD_VALUE, 45, 70)
                .modifier(Attributes.MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_BASE, 0.15F, 0.25F)
                .modifier(ALObjects.Attributes.ARROW_DAMAGE, Operation.ADD_MULTIPLIED_BASE, 0.3F, 0.55F)
                .modifier(Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, StepFunction.constant(0.4F)))
            .stats(epic, c -> c
                .enchantChance(0.6F)
                .enchLevels(35, 25)
                .effect(1, MobEffects.FIRE_RESISTANCE)
                .modifier(Attributes.MAX_HEALTH, Operation.ADD_VALUE, 95, 120)
                .modifier(Attributes.MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_BASE, 0.25F, 0.40F)
                .modifier(ALObjects.Attributes.ARROW_DAMAGE, Operation.ADD_MULTIPLIED_BASE, 0.45F, 0.65F)
                .modifier(Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, StepFunction.constant(0.5F))
                .modifier(Attributes.ARMOR, Operation.ADD_VALUE, StepFunction.constant(4F))
                .modifier(Attributes.ARMOR_TOUGHNESS, Operation.ADD_VALUE, StepFunction.constant(10F)))
            .stats(mythic, c -> c
                .enchantChance(0.75F)
                .enchLevels(35, 25)
                .effect(1, MobEffects.FIRE_RESISTANCE)
                .modifier(Attributes.MAX_HEALTH, Operation.ADD_VALUE, 125, 150)
                .modifier(Attributes.MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_BASE, 0.3F, 0.65F)
                .modifier(ALObjects.Attributes.ARROW_DAMAGE, Operation.ADD_MULTIPLIED_BASE, 0.75F, 1.2F)
                .modifier(Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, StepFunction.constant(0.8F))
                .modifier(Attributes.ARMOR, Operation.ADD_VALUE, StepFunction.constant(10F))
                .modifier(Attributes.ARMOR_TOUGHNESS, Operation.ADD_VALUE, StepFunction.constant(20F)));
    }

    private BasicBossData.Builder meleeGear(BasicBossData.Builder builder) {
        builder.gearSets(WorldTier.HAVEN, "#haven_melee");
        builder.gearSets(WorldTier.FRONTIER, "#frontier_melee");
        builder.gearSets(WorldTier.ASCENT, "#ascent_melee");
        builder.gearSets(WorldTier.SUMMIT, "#summit_melee");
        builder.gearSets(WorldTier.APOTHEOSIS, "#apotheosis_melee");
        return builder;
    }

    private BasicBossData.Builder rangedGear(BasicBossData.Builder builder) {
        builder.gearSets(WorldTier.HAVEN, "#haven_ranged");
        builder.gearSets(WorldTier.FRONTIER, "#frontier_ranged");
        builder.gearSets(WorldTier.ASCENT, "#ascent_ranged");
        builder.gearSets(WorldTier.SUMMIT, "#summit_ranged");
        builder.gearSets(WorldTier.APOTHEOSIS, "#apotheosis_ranged");
        return builder;
    }

    private void addBoss(String name, UnaryOperator<Invader.Builder> builder) {
        this.add(Apotheosis.loc(name), builder.apply(Invader.builder()).build());
    }

    private static LootRarity rarity(String path) {
        return Preconditions.checkNotNull(RarityRegistry.INSTANCE.getValue(Apotheosis.loc(path)));
    }

}
