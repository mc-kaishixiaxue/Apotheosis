package dev.shadowsoffire.apotheosis.data;

import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import org.spongepowered.include.com.google.common.base.Preconditions;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.affix.AffixType;
import dev.shadowsoffire.apotheosis.affix.AttributeAffix;
import dev.shadowsoffire.apotheosis.affix.effect.DamageReductionAffix;
import dev.shadowsoffire.apotheosis.affix.effect.DamageReductionAffix.DamageType;
import dev.shadowsoffire.apotheosis.affix.effect.MobEffectAffix;
import dev.shadowsoffire.apotheosis.affix.effect.MobEffectAffix.Target;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.util.StepFunction;
import dev.shadowsoffire.placebo.util.data.DynamicRegistryProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.common.NeoForgeMod;

public class AffixProvider extends DynamicRegistryProvider<Affix> {

    public static final int DEFAULT_WEIGHT = 25;
    public static final int DEFAULT_QUALITY = 0;

    public static final LootCategory[] ARMOR = new LootCategory[] { LootCategory.HELMET, LootCategory.CHESTPLATE, LootCategory.LEGGINGS, LootCategory.BOOTS };

    public AffixProvider(PackOutput output, CompletableFuture<Provider> registries) {
        super(output, registries, AffixRegistry.INSTANCE);
    }

    @Override
    public String getName() {
        return "Affixes";
    }

    @Override
    public void generate() {
        LootRarity common = rarity("common");
        LootRarity uncommon = rarity("uncommon");
        LootRarity rare = rarity("rare");
        LootRarity epic = rarity("epic");
        LootRarity mythic = rarity("mythic");

        // Generic Attributes
        addAttribute("global", "lucky", Attributes.LUCK, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.VALUES.toArray(new LootCategory[0]))
            .step(0.25F)
            .value(uncommon, 1F, 1.5F)
            .value(rare, 2F, 3F)
            .value(epic, 2.5F, 4F)
            .value(mythic, 3F, 6F));

        // Armor Attributes
        addAttribute("armor", "aquatic", NeoForgeMod.SWIM_SPEED, Operation.ADD_MULTIPLIED_TOTAL, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BOOTS)
            .value(common, 0.2F, 0.3F)
            .value(uncommon, 0.2F, 0.3F)
            .value(rare, 0.3F, 0.5F)
            .value(epic, 0.3F, 0.5F)
            .value(mythic, 0.4F, 0.7F));

        addAttribute("armor", "blessed", Attributes.MAX_HEALTH, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(ARMOR)
            .step(0.25F)
            .value(common, 2, 4)
            .value(uncommon, 2, 5)
            .value(rare, 3, 6)
            .value(epic, 3, 8)
            .value(mythic, 5, 8));

        addAttribute("armor", "elastic", Attributes.STEP_HEIGHT, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BOOTS)
            .step(0.25F)
            .value(common, 0.5F)
            .value(uncommon, 0.5F)
            .value(rare, 0.5F, 1.5F)
            .value(epic, 0.5F, 1.5F)
            .value(mythic, 1, 2));

        addAttribute("armor", "fortunate", Attributes.LUCK, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(ARMOR)
            .step(0.25F)
            .value(common, 0.5F, 1.5F)
            .value(uncommon, 0.5F, 1.5F)
            .value(rare, 1.5F, 3)
            .value(epic, 1.5F, 3.5F)
            .value(mythic, 3, 5));

        addAttribute("armor", "gravitational", Attributes.GRAVITY, Operation.ADD_MULTIPLIED_TOTAL, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.CHESTPLATE)
            .step(-0.01F)
            .value(common, -0.1F, -0.2F)
            .value(uncommon, -0.1F, -0.25F)
            .value(rare, -0.15F, -0.30F)
            .value(epic, -0.15F, -0.35F)
            .value(mythic, -0.20F, -0.5F));

        addAttribute("armor", "ironforged", Attributes.ARMOR, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(ARMOR)
            .step(0.25F)
            .value(common, 1, 1.5F)
            .value(uncommon, 1, 1.5F)
            .value(rare, 1.5F, 3)
            .value(epic, 2, 5)
            .value(mythic, 4, 8));

        addAttribute("armor", "adamantine", Attributes.ARMOR, Operation.ADD_MULTIPLIED_BASE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.CHESTPLATE, LootCategory.LEGGINGS)
            .value(rare, 0.15F, 0.3F)
            .value(epic, 0.15F, 0.3F)
            .value(mythic, 0.25F, 0.4F));

        addAttribute("armor", "spiritual", ALObjects.Attributes.HEALING_RECEIVED, Operation.ADD_MULTIPLIED_BASE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.CHESTPLATE, LootCategory.LEGGINGS)
            .value(rare, 0.10F, 0.25F)
            .value(epic, 0.15F, 0.30F)
            .value(mythic, 0.20F, 0.40F));

        addAttribute("armor", "stalwart", Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(ARMOR)
            .value(uncommon, 0.05F, 0.1F)
            .value(rare, 0.05F, 0.1F)
            .value(epic, 0.15F, 0.20F)
            .value(mythic, 0.25F, 0.35F));

        addAttribute("armor", "steel_touched", Attributes.ARMOR_TOUGHNESS, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(ARMOR)
            .step(0.25F)
            .value(rare, 1)
            .value(epic, 1.5F, 3F)
            .value(mythic, 2F, 6F));

        addAttribute("armor", "windswept", Attributes.MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_TOTAL, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.LEGGINGS, LootCategory.BOOTS)
            .value(common, 0.1F, 0.2F)
            .value(uncommon, 0.1F, 0.25F)
            .value(rare, 0.15F, 0.3F)
            .value(epic, 0.15F, 0.4F)
            .value(mythic, 0.2F, 0.45F));

        addAttribute("armor", "winged", ALObjects.Attributes.ELYTRA_FLIGHT, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.CHESTPLATE)
            .value(epic, 1)
            .value(mythic, 1));

        addAttribute("armor", "unbound", NeoForgeMod.CREATIVE_FLIGHT, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, d -> d
                .weights(TieredWeights.onlyFor(WorldTier.APOTHEOSIS, 20, 5))
                .exclusiveWith(afx("armor/attribute/winged")))
            .categories(LootCategory.CHESTPLATE)
            .value(mythic, 1));

        addAttribute("armor", "fireproof", Attributes.BURNING_TIME, Operation.ADD_MULTIPLIED_BASE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.HELMET)
            .step(-0.05F)
            .value(uncommon, -0.15F, -0.25F)
            .value(rare, -0.20F, -0.30F)
            .value(epic, -0.25F, -0.35F)
            .value(mythic, -0.30F, -0.45F));

        addAttribute("armor", "oxygenated", Attributes.OXYGEN_BONUS, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.HELMET)
            .value(uncommon, 0.2F, 0.3F)
            .value(rare, 0.35F, 0.5F)
            .value(epic, 0.55F, 0.7F)
            .value(mythic, 0.85F, 1.25F));

        // TODO: Potentially add offensive stats as armor affixes?

        // Breaker Attributes

        addAttribute("breaker", "destructive", ALObjects.Attributes.MINING_SPEED, Operation.ADD_MULTIPLIED_TOTAL, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BREAKER)
            .value(common, 0.15F, 0.3F)
            .value(uncommon, 0.15F, 0.3F)
            .value(rare, 0.25F, 0.5F)
            .value(epic, 0.25F, 0.7F)
            .value(mythic, 0.55F, 0.85F));

        addAttribute("breaker", "experienced", ALObjects.Attributes.EXPERIENCE_GAINED, Operation.ADD_MULTIPLIED_TOTAL, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BREAKER)
            .value(common, 0.25F, 0.4F)
            .value(uncommon, 0.25F, 0.4F)
            .value(rare, 0.35F, 0.5F)
            .value(epic, 0.35F, 0.6F)
            .value(mythic, 0.55F, 0.65F));

        addAttribute("breaker", "lengthy", Attributes.BLOCK_INTERACTION_RANGE, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BREAKER)
            .step(0.25F)
            .value(common, 0.5F, 1.5F)
            .value(uncommon, 0.5F, 1.5F)
            .value(rare, 1F, 2.5F)
            .value(epic, 1F, 2.5F)
            .value(mythic, 1.5F, 4));

        addAttribute("breaker", "submerged", Attributes.SUBMERGED_MINING_SPEED, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BREAKER)
            .value(common, 0.1F, 0.3F)
            .value(uncommon, 0.2F, 0.3F)
            .value(rare, 0.3F, 0.5F)
            .value(epic, 0.4F, 0.55F)
            .value(mythic, 0.5F, 0.8F));

        // TODO: We need more of these, the pool here is too limited.

        // Ranged Attributes

        addAttribute("ranged", "agile", ALObjects.Attributes.DRAW_SPEED, Operation.ADD_MULTIPLIED_TOTAL, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BOW)
            .value(common, 0.2F, 0.4F)
            .value(uncommon, 0.2F, 0.4F)
            .value(rare, 0.3F, 0.5F)
            .value(epic, 0.5F, 0.6F)
            .value(mythic, 0.5F, 0.65F));

        addAttribute("ranged", "elven", ALObjects.Attributes.ARROW_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BOW, LootCategory.TRIDENT)
            .value(common, 0.15F, 0.20F)
            .value(uncommon, 0.15F, 0.25F)
            .value(rare, 0.20F, 0.30F)
            .value(epic, 0.25F, 0.35F)
            .value(mythic, 0.25F, 0.40F));

        addAttribute("ranged", "streamlined", ALObjects.Attributes.ARROW_VELOCITY, Operation.ADD_MULTIPLIED_TOTAL, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BOW, LootCategory.TRIDENT)
            .value(common, 0.15F, 0.20F)
            .value(uncommon, 0.15F, 0.20F)
            .value(rare, 0.15F, 0.25F)
            .value(epic, 0.15F, 0.30F)
            .value(mythic, 0.15F, 0.35F));

        addAttribute("ranged", "windswept", Attributes.MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_TOTAL, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BOW, LootCategory.TRIDENT)
            .value(common, 0.15F, 0.25F)
            .value(uncommon, 0.15F, 0.3F)
            .value(rare, 0.15F, 0.3F)
            .value(epic, 0.15F, 0.35F)
            .value(mythic, 0.2F, 0.4F));

        // Shield Attributes

        addAttribute("shield", "ironforged", Attributes.ARMOR, Operation.ADD_MULTIPLIED_BASE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.SHIELD)
            .value(common, 0.10F, 0.15F)
            .value(uncommon, 0.10F, 0.15F)
            .value(rare, 0.15F, 0.20F)
            .value(epic, 0.15F, 0.25F)
            .value(mythic, 0.20F, 0.30F));

        addAttribute("shield", "stalwart", Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.SHIELD)
            .value(common, 0.10F, 0.20F)
            .value(uncommon, 0.10F, 0.20F)
            .value(rare, 0.20F, 0.30F)
            .value(epic, 0.20F, 0.30F)
            .value(mythic, 0.25F, 0.35F));

        addAttribute("shield", "steel_touched", Attributes.ARMOR_TOUGHNESS, Operation.ADD_MULTIPLIED_BASE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.SHIELD)
            .value(rare, 0.15F, 0.20F)
            .value(epic, 0.15F, 0.20F)
            .value(mythic, 0.20F, 0.30F));

        // Melee Weapon Attributes

        addAttribute("melee", "vampiric", ALObjects.Attributes.LIFE_STEAL, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, d -> d
                .weights(TieredWeights.forAllTiers(DEFAULT_WEIGHT, DEFAULT_QUALITY))
                .exclusiveWith(afx("armor/attribute/berserking")))
            .categories(LootCategory.MELEE_WEAPON, LootCategory.TRIDENT)
            .value(common, 0.15F, 0.20F)
            .value(uncommon, 0.15F, 0.20F)
            .value(rare, 0.15F, 0.25F)
            .value(epic, 0.15F, 0.30F)
            .value(mythic, 0.25F, 0.40F));

        addAttribute("melee", "murderous", Attributes.ATTACK_DAMAGE, Operation.ADD_MULTIPLIED_BASE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.MELEE_WEAPON, LootCategory.TRIDENT)
            .value(rare, 0.15F, 0.43F)
            .value(epic, 0.18F, 0.48F)
            .value(mythic, 0.25F, 0.55F));

        addAttribute("melee", "violent", Attributes.ATTACK_DAMAGE, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.MELEE_WEAPON, LootCategory.TRIDENT)
            .step(0.25F)
            .value(common, 2F, 3F)
            .value(uncommon, 2F, 3F)
            .value(rare, 3F, 5F)
            .value(epic, 4F, 6F)
            .value(mythic, 5F, 8F));

        addAttribute("melee", "piercing", ALObjects.Attributes.ARMOR_PIERCE, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.MELEE_WEAPON, LootCategory.TRIDENT)
            .step(0.25F)
            .value(common, 2F, 4F)
            .value(uncommon, 2F, 4F)
            .value(rare, 4F, 8F)
            .value(epic, 5F, 10F)
            .value(mythic, 5F, 12F));

        addAttribute("melee", "lacerating", ALObjects.Attributes.CRIT_DAMAGE, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.MELEE_WEAPON, LootCategory.TRIDENT)
            .value(common, 0.10F, 0.20F)
            .value(uncommon, 0.10F, 0.20F)
            .value(rare, 0.15F, 0.25F)
            .value(epic, 0.15F, 0.25F)
            .value(mythic, 0.25F, 0.40F));

        addAttribute("melee", "intricate", ALObjects.Attributes.CRIT_DAMAGE, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.MELEE_WEAPON, LootCategory.TRIDENT)
            .value(common, 0.10F, 0.20F)
            .value(uncommon, 0.10F, 0.20F)
            .value(rare, 0.10F, 0.25F)
            .value(epic, 0.15F, 0.35F)
            .value(mythic, 0.25F, 0.55F));

        addAttribute("melee", "infernal", ALObjects.Attributes.FIRE_DAMAGE, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, d -> d
                .weights(TieredWeights.forAllTiers(DEFAULT_WEIGHT, DEFAULT_QUALITY))
                .exclusiveWith(afx("armor/attribute/glacial")))
            .categories(LootCategory.MELEE_WEAPON, LootCategory.TRIDENT)
            .step(0.25F)
            .value(uncommon, 2F, 4F)
            .value(rare, 2F, 5F)
            .value(epic, 4F, 6F)
            .value(mythic, 4F, 10F));

        addAttribute("melee", "graceful", Attributes.ATTACK_SPEED, Operation.ADD_MULTIPLIED_TOTAL, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.MELEE_WEAPON, LootCategory.TRIDENT)
            .value(common, 0.15F, 0.25F)
            .value(uncommon, 0.20F, 0.30F)
            .value(rare, 0.20F, 0.35F)
            .value(epic, 0.25F, 0.50F)
            .value(mythic, 0.40F, 0.85F));

        addAttribute("melee", "glacial", ALObjects.Attributes.COLD_DAMAGE, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, d -> d
                .weights(TieredWeights.forAllTiers(DEFAULT_WEIGHT, DEFAULT_QUALITY))
                .exclusiveWith(afx("armor/attribute/infernal")))
            .categories(LootCategory.MELEE_WEAPON, LootCategory.TRIDENT)
            .step(0.25F)
            .value(uncommon, 2F, 4F)
            .value(rare, 2F, 5F)
            .value(epic, 4F, 6F)
            .value(mythic, 4F, 10F));

        addAttribute("melee", "lengthy", Attributes.ENTITY_INTERACTION_RANGE, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.MELEE_WEAPON, LootCategory.TRIDENT)
            .step(0.25F)
            .value(common, 0.5F, 1.5F)
            .value(uncommon, 0.5F, 1.5F)
            .value(rare, 0.75F, 2.5F)
            .value(epic, 1F, 2.5F)
            .value(mythic, 1.5F, 3));

        addAttribute("melee", "forceful", Attributes.ENTITY_INTERACTION_RANGE, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.MELEE_WEAPON)
            .step(0.25F)
            .value(common, 0.5F, 1F)
            .value(uncommon, 0.5F, 1.5F)
            .value(rare, 0.75F, 2.5F)
            .value(epic, 1F, 2.5F)
            .value(mythic, 1.5F, 3));

        addAttribute("melee", "berserking", ALObjects.Attributes.OVERHEAL, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, d -> d
                .weights(TieredWeights.forAllTiers(DEFAULT_WEIGHT, DEFAULT_QUALITY))
                .exclusiveWith(afx("armor/attribute/vampiric")))
            .categories(LootCategory.MELEE_WEAPON)
            .value(common, 0.10F, 0.20F)
            .value(uncommon, 0.10F, 0.20F)
            .value(rare, 0.15F, 0.25F)
            .value(epic, 0.15F, 0.30F)
            .value(mythic, 0.20F, 0.45F));

        addAttribute("melee", "giant_slaying", ALObjects.Attributes.CURRENT_HP_DAMAGE, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, d -> d
                .weights(TieredWeights.onlyFor(WorldTier.APOTHEOSIS, 20, 5)))
            .categories(LootCategory.MELEE_WEAPON)
            .value(epic, 0.15F, 0.25F)
            .value(mythic, 0.15F, 0.35F));

        // TODO: Armor Shred and Prot Shred affixes for melee + bow

        // Damage Reduction Affixes

        addDamageReduction("armor", "blast_forged", DamageType.EXPLOSION, b -> b
            .definition(AffixType.ABILITY, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.CHESTPLATE, LootCategory.LEGGINGS)
            .value(common, 0.05F, 0.10F)
            .value(uncommon, 0.10F, 0.15F)
            .value(rare, 0.15F, 0.20F)
            .value(epic, 0.15F, 0.25F)
            .value(mythic, 0.15F, 0.35F));

        addDamageReduction("armor", "blockading", DamageType.PHYSICAL, b -> b
            .definition(AffixType.ABILITY, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.CHESTPLATE, LootCategory.LEGGINGS)
            .value(common, 0.01F, 0.05F)
            .value(uncommon, 0.01F, 0.05F)
            .value(rare, 0.05F, 0.10F)
            .value(epic, 0.05F, 0.10F)
            .value(mythic, 0.05F, 0.15F));

        addDamageReduction("armor", "runed", DamageType.MAGIC, b -> b
            .definition(AffixType.ABILITY, d -> d
                .weights(TieredWeights.forAllTiers(DEFAULT_WEIGHT, DEFAULT_QUALITY))
                .exclusiveWith(afx("armor/dmg_reduction/blockading")))
            .categories(LootCategory.HELMET, LootCategory.CHESTPLATE, LootCategory.LEGGINGS, LootCategory.BOOTS)
            .value(common, 0.01F, 0.05F)
            .value(uncommon, 0.01F, 0.05F)
            .value(rare, 0.05F, 0.10F)
            .value(epic, 0.05F, 0.10F)
            .value(mythic, 0.05F, 0.15F));

        addDamageReduction("armor", "feathery", DamageType.FALL, b -> b
            .definition(AffixType.ABILITY, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BOOTS)
            .value(common, 0.05F, 0.10F)
            .value(uncommon, 0.10F, 0.15F)
            .value(rare, 0.15F, 0.25F)
            .value(epic, 0.15F, 0.25F)
            .value(mythic, 0.15F, 0.40F));

        addDamageReduction("armor", "deflective", DamageType.PROJECTILE, b -> b
            .definition(AffixType.ABILITY, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.HELMET)
            .value(common, 0.05F, 0.10F)
            .value(uncommon, 0.10F, 0.15F)
            .value(rare, 0.15F, 0.20F)
            .value(epic, 0.15F, 0.20F)
            .value(mythic, 0.15F, 0.30F));

        addDamageReduction("armor", "grounded", DamageType.LIGHTNING, b -> b
            .definition(AffixType.ABILITY, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.HELMET, LootCategory.BOOTS)
            .value(common, 0.05F, 0.10F)
            .value(uncommon, 0.10F, 0.15F)
            .value(rare, 0.15F, 0.25F)
            .value(epic, 0.15F, 0.25F)
            .value(mythic, 0.15F, 0.40F));

        // Armor Mob Effects

        addMobEffect("armor", "revitalizing", MobEffects.HEAL, Target.HURT_SELF, b -> b
            .definition(AffixType.POTION, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.CHESTPLATE, LootCategory.LEGGINGS)
            .value(epic, 1, 0, 300)
            .value(mythic, StepFunction.constant(1), StepFunction.fromBounds(0, 1F, 0.25F), 240));

        addMobEffect("armor", "nimble", MobEffects.MOVEMENT_SPEED, Target.HURT_SELF, b -> b
            .definition(AffixType.POTION, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.LEGGINGS, LootCategory.BOOTS)
            .value(rare, 200, 400, 0, 800)
            .value(epic, 200, 400, StepFunction.fromBounds(0, 2, 0.25F), 700)
            .value(mythic, 200, 400, StepFunction.fromBounds(0, 2, 0.5F), 600));

        addMobEffect("armor", "bursting", ALObjects.MobEffects.VITALITY, Target.HURT_SELF, b -> b
            .definition(AffixType.POTION, d -> d
                .weights(TieredWeights.forAllTiers(DEFAULT_WEIGHT, DEFAULT_QUALITY))
                .exclusiveWith(afx("armor/mob_effect/revitalizing")))
            .categories(LootCategory.CHESTPLATE, LootCategory.LEGGINGS)
            .value(epic, 200, 0, 300)
            .value(mythic, StepFunction.constant(200), StepFunction.fromBounds(0, 1F, 0.25F), 300));

        addMobEffect("armor", "bolstering", MobEffects.DAMAGE_RESISTANCE, Target.HURT_SELF, b -> b
            .definition(AffixType.POTION, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.CHESTPLATE, LootCategory.LEGGINGS)
            .value(rare, 80, 120, 0, 240)
            .value(epic, 80, 140, StepFunction.fromBounds(0, 1, 0.2F), 240)
            .value(mythic, 80, 160, StepFunction.fromBounds(0, 1, 0.5F), 240));

        addMobEffect("armor", "blinding", MobEffects.BLINDNESS, Target.HURT_ATTACKER, b -> b
            .definition(AffixType.POTION, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.HELMET)
            .value(rare, 40, 80, 0, 240)
            .value(epic, 40, 80, 0, 240)
            .value(mythic, 60, 100, 0, 200));

        // TODO : Grievous, Regeneration, Fire Res / Water Breathing, Slow Fall (maybe), Invisibility, Weakness
    }

    private void addMobEffect(String type, String name, Holder<MobEffect> effect, MobEffectAffix.Target target, UnaryOperator<MobEffectAffix.Builder> config) {
        var builder = new MobEffectAffix.Builder(effect, target);
        config.apply(builder);
        this.add(Apotheosis.loc(type + "/mob_effect/" + name), builder.build());
    }

    private void addDamageReduction(String type, String name, DamageReductionAffix.DamageType dType, UnaryOperator<DamageReductionAffix.Builder> config) {
        var builder = new DamageReductionAffix.Builder(dType);
        config.apply(builder);
        this.add(Apotheosis.loc(type + "/dmg_reduction/" + name), builder.build());
    }

    private void addAttribute(String type, String name, Holder<Attribute> attribute, Operation op, UnaryOperator<AttributeAffix.Builder> config) {
        var builder = new AttributeAffix.Builder(attribute, op);
        config.apply(builder);
        this.add(Apotheosis.loc(type + "/attribute/" + name), builder.build());
    }

    private static LootRarity rarity(String path) {
        return Preconditions.checkNotNull(RarityRegistry.INSTANCE.getValue(Apotheosis.loc(path)));
    }

    private static DynamicHolder<Affix> afx(String path) {
        return AffixRegistry.INSTANCE.holder(Apotheosis.loc(path));
    }

}
