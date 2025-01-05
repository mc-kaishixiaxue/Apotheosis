package dev.shadowsoffire.apotheosis.data;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import org.spongepowered.include.com.google.common.base.Preconditions;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixBuilder;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.affix.AffixType;
import dev.shadowsoffire.apotheosis.affix.AttributeAffix;
import dev.shadowsoffire.apotheosis.affix.effect.CatalyzingAffix;
import dev.shadowsoffire.apotheosis.affix.effect.CleavingAffix;
import dev.shadowsoffire.apotheosis.affix.effect.DamageReductionAffix;
import dev.shadowsoffire.apotheosis.affix.effect.DamageReductionAffix.DamageType;
import dev.shadowsoffire.apotheosis.affix.effect.EnchantmentAffix;
import dev.shadowsoffire.apotheosis.affix.effect.EnchantmentAffix.Mode;
import dev.shadowsoffire.apotheosis.affix.effect.EnlightenedAffix;
import dev.shadowsoffire.apotheosis.affix.effect.ExecutingAffix;
import dev.shadowsoffire.apotheosis.affix.effect.FestiveAffix;
import dev.shadowsoffire.apotheosis.affix.effect.MagicalArrowAffix;
import dev.shadowsoffire.apotheosis.affix.effect.MobEffectAffix;
import dev.shadowsoffire.apotheosis.affix.effect.MobEffectAffix.Target;
import dev.shadowsoffire.apotheosis.affix.effect.OmneticAffix;
import dev.shadowsoffire.apotheosis.affix.effect.PsychicAffix;
import dev.shadowsoffire.apotheosis.affix.effect.RadialAffix;
import dev.shadowsoffire.apotheosis.affix.effect.RetreatingAffix;
import dev.shadowsoffire.apotheosis.affix.effect.SpectralShotAffix;
import dev.shadowsoffire.apotheosis.affix.effect.StoneformingAffix;
import dev.shadowsoffire.apotheosis.affix.effect.TelepathicAffix;
import dev.shadowsoffire.apotheosis.affix.effect.ThunderstruckAffix;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.util.StepFunction;
import dev.shadowsoffire.placebo.util.data.DynamicRegistryProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.NeoForgeMod;

public class AffixProvider extends DynamicRegistryProvider<Affix> {

    public static final int DEFAULT_WEIGHT = 25;
    public static final int DEFAULT_QUALITY = 0;

    public static final LootCategory[] ARMOR = { LootCategory.HELMET, LootCategory.CHESTPLATE, LootCategory.LEGGINGS, LootCategory.BOOTS };

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

        RegistryLookup<Enchantment> enchants = this.lookupProvider.join().lookup(Registries.ENCHANTMENT).get();

        // Generic Attributes
        this.addAttribute("generic", "lucky", Attributes.LUCK, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.VALUES.toArray(new LootCategory[0]))
            .step(0.25F)
            .value(uncommon, 1F, 1.5F)
            .value(rare, 2F, 3F)
            .value(epic, 2.5F, 4F)
            .value(mythic, 3F, 6F));

        // Telepathic, which applies to a bunch of categories
        this.add(Apotheosis.loc("generic/telepathic"),
            new TelepathicAffix(
                AffixDefinition.builder(AffixType.BASIC_EFFECT).weights(TieredWeights.forAllTiers(DEFAULT_WEIGHT, DEFAULT_QUALITY)).build(),
                linkedSet(rare, epic, mythic)));

        // Armor Attributes
        this.addAttribute("armor", "aquatic", NeoForgeMod.SWIM_SPEED, Operation.ADD_MULTIPLIED_TOTAL, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BOOTS)
            .value(common, 0.2F, 0.3F)
            .value(uncommon, 0.2F, 0.3F)
            .value(rare, 0.3F, 0.5F)
            .value(epic, 0.3F, 0.5F)
            .value(mythic, 0.4F, 0.7F));

        this.addAttribute("armor", "blessed", Attributes.MAX_HEALTH, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(ARMOR)
            .step(0.25F)
            .value(common, 2, 4)
            .value(uncommon, 2, 5)
            .value(rare, 3, 6)
            .value(epic, 3, 8)
            .value(mythic, 5, 8));

        this.addAttribute("armor", "elastic", Attributes.STEP_HEIGHT, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BOOTS)
            .step(0.25F)
            .value(common, 0.5F)
            .value(uncommon, 0.5F)
            .value(rare, 0.5F, 1.5F)
            .value(epic, 0.5F, 1.5F)
            .value(mythic, 1, 2));

        this.addAttribute("armor", "fortunate", Attributes.LUCK, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(ARMOR)
            .step(0.25F)
            .value(common, 0.5F, 1.5F)
            .value(uncommon, 0.5F, 1.5F)
            .value(rare, 1.5F, 3)
            .value(epic, 1.5F, 3.5F)
            .value(mythic, 3, 5));

        this.addAttribute("armor", "gravitational", Attributes.GRAVITY, Operation.ADD_MULTIPLIED_TOTAL, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.CHESTPLATE)
            .step(-0.01F)
            .value(common, -0.1F, -0.2F)
            .value(uncommon, -0.1F, -0.25F)
            .value(rare, -0.15F, -0.30F)
            .value(epic, -0.15F, -0.35F)
            .value(mythic, -0.20F, -0.5F));

        this.addAttribute("armor", "ironforged", Attributes.ARMOR, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(ARMOR)
            .step(0.25F)
            .value(common, 1, 1.5F)
            .value(uncommon, 1, 1.5F)
            .value(rare, 1.5F, 3)
            .value(epic, 2, 5)
            .value(mythic, 4, 8));

        this.addAttribute("armor", "adamantine", Attributes.ARMOR, Operation.ADD_MULTIPLIED_BASE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.CHESTPLATE, LootCategory.LEGGINGS)
            .value(rare, 0.15F, 0.3F)
            .value(epic, 0.15F, 0.3F)
            .value(mythic, 0.25F, 0.4F));

        this.addAttribute("armor", "spiritual", ALObjects.Attributes.HEALING_RECEIVED, Operation.ADD_MULTIPLIED_BASE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.CHESTPLATE, LootCategory.LEGGINGS)
            .value(rare, 0.10F, 0.25F)
            .value(epic, 0.15F, 0.30F)
            .value(mythic, 0.20F, 0.40F));

        this.addAttribute("armor", "stalwart", Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(ARMOR)
            .value(uncommon, 0.05F, 0.1F)
            .value(rare, 0.05F, 0.1F)
            .value(epic, 0.15F, 0.20F)
            .value(mythic, 0.25F, 0.35F));

        this.addAttribute("armor", "steel_touched", Attributes.ARMOR_TOUGHNESS, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(ARMOR)
            .step(0.25F)
            .value(rare, 1)
            .value(epic, 1.5F, 3F)
            .value(mythic, 2F, 6F));

        this.addAttribute("armor", "windswept", Attributes.MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_TOTAL, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.LEGGINGS, LootCategory.BOOTS)
            .value(common, 0.1F, 0.2F)
            .value(uncommon, 0.1F, 0.25F)
            .value(rare, 0.15F, 0.3F)
            .value(epic, 0.15F, 0.4F)
            .value(mythic, 0.2F, 0.45F));

        this.addAttribute("armor", "winged", ALObjects.Attributes.ELYTRA_FLIGHT, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.CHESTPLATE)
            .value(epic, 1)
            .value(mythic, 1));

        this.addAttribute("armor", "unbound", NeoForgeMod.CREATIVE_FLIGHT, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, d -> d
                .weights(TieredWeights.onlyFor(WorldTier.PINNACLE, 20, 5))
                .exclusiveWith(afx("armor/attribute/winged")))
            .categories(LootCategory.CHESTPLATE)
            .value(mythic, 1));

        this.addAttribute("armor", "fireproof", Attributes.BURNING_TIME, Operation.ADD_MULTIPLIED_BASE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.HELMET)
            .step(-0.05F)
            .value(uncommon, -0.15F, -0.35F)
            .value(rare, -0.20F, -0.40F)
            .value(epic, -0.35F, -0.75F)
            .value(mythic, -0.60F, -1F));

        this.addAttribute("armor", "oxygenated", Attributes.OXYGEN_BONUS, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.HELMET)
            .value(uncommon, 0.2F, 0.3F)
            .value(rare, 0.35F, 0.5F)
            .value(epic, 0.55F, 0.7F)
            .value(mythic, 0.85F, 1.25F));

        // TODO: Potentially add offensive stats as armor affixes?

        // Breaker Attributes

        this.addAttribute("breaker", "destructive", ALObjects.Attributes.MINING_SPEED, Operation.ADD_MULTIPLIED_TOTAL, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BREAKER)
            .value(common, 0.15F, 0.3F)
            .value(uncommon, 0.15F, 0.3F)
            .value(rare, 0.25F, 0.5F)
            .value(epic, 0.25F, 0.7F)
            .value(mythic, 0.55F, 0.85F));

        this.addAttribute("breaker", "experienced", ALObjects.Attributes.EXPERIENCE_GAINED, Operation.ADD_MULTIPLIED_TOTAL, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BREAKER)
            .value(common, 0.25F, 0.4F)
            .value(uncommon, 0.25F, 0.4F)
            .value(rare, 0.35F, 0.5F)
            .value(epic, 0.35F, 0.6F)
            .value(mythic, 0.55F, 0.65F));

        this.addAttribute("breaker", "lengthy", Attributes.BLOCK_INTERACTION_RANGE, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BREAKER)
            .step(0.25F)
            .value(common, 0.5F, 1.5F)
            .value(uncommon, 0.5F, 1.5F)
            .value(rare, 1F, 2.5F)
            .value(epic, 1F, 2.5F)
            .value(mythic, 1.5F, 4));

        this.addAttribute("breaker", "submerged", Attributes.SUBMERGED_MINING_SPEED, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BREAKER)
            .value(common, 0.1F, 0.3F)
            .value(uncommon, 0.2F, 0.3F)
            .value(rare, 0.3F, 0.5F)
            .value(epic, 0.4F, 0.55F)
            .value(mythic, 0.5F, 0.8F));

        // TODO: We need more of these, the pool here is too limited.

        // Ranged Attributes

        this.addAttribute("ranged", "agile", ALObjects.Attributes.DRAW_SPEED, Operation.ADD_MULTIPLIED_TOTAL, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BOW)
            .value(common, 0.2F, 0.4F)
            .value(uncommon, 0.2F, 0.4F)
            .value(rare, 0.3F, 0.5F)
            .value(epic, 0.5F, 0.6F)
            .value(mythic, 0.5F, 0.65F));

        this.addAttribute("ranged", "elven", ALObjects.Attributes.ARROW_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BOW, LootCategory.TRIDENT)
            .value(common, 0.15F, 0.20F)
            .value(uncommon, 0.15F, 0.25F)
            .value(rare, 0.20F, 0.30F)
            .value(epic, 0.25F, 0.35F)
            .value(mythic, 0.25F, 0.40F));

        this.addAttribute("ranged", "streamlined", ALObjects.Attributes.ARROW_VELOCITY, Operation.ADD_MULTIPLIED_TOTAL, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BOW, LootCategory.TRIDENT)
            .value(common, 0.15F, 0.20F)
            .value(uncommon, 0.15F, 0.20F)
            .value(rare, 0.15F, 0.25F)
            .value(epic, 0.15F, 0.30F)
            .value(mythic, 0.15F, 0.35F));

        this.addAttribute("ranged", "windswept", Attributes.MOVEMENT_SPEED, Operation.ADD_MULTIPLIED_TOTAL, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BOW, LootCategory.TRIDENT)
            .value(common, 0.15F, 0.25F)
            .value(uncommon, 0.15F, 0.3F)
            .value(rare, 0.15F, 0.3F)
            .value(epic, 0.15F, 0.35F)
            .value(mythic, 0.2F, 0.4F));

        // Shield Attributes

        this.addAttribute("shield", "ironforged", Attributes.ARMOR, Operation.ADD_MULTIPLIED_BASE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.SHIELD)
            .value(common, 0.10F, 0.15F)
            .value(uncommon, 0.10F, 0.15F)
            .value(rare, 0.15F, 0.20F)
            .value(epic, 0.15F, 0.25F)
            .value(mythic, 0.20F, 0.30F));

        this.addAttribute("shield", "stalwart", Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.SHIELD)
            .value(common, 0.10F, 0.20F)
            .value(uncommon, 0.10F, 0.20F)
            .value(rare, 0.20F, 0.30F)
            .value(epic, 0.20F, 0.30F)
            .value(mythic, 0.25F, 0.35F));

        this.addAttribute("shield", "steel_touched", Attributes.ARMOR_TOUGHNESS, Operation.ADD_MULTIPLIED_BASE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.SHIELD)
            .value(rare, 0.15F, 0.20F)
            .value(epic, 0.15F, 0.20F)
            .value(mythic, 0.20F, 0.30F));

        // Melee Weapon Attributes

        this.addAttribute("melee", "vampiric", ALObjects.Attributes.LIFE_STEAL, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, d -> d
                .weights(TieredWeights.forAllTiers(DEFAULT_WEIGHT, DEFAULT_QUALITY))
                .exclusiveWith(afx("melee/attribute/berserking")))
            .categories(LootCategory.MELEE_WEAPON, LootCategory.TRIDENT)
            .value(common, 0.15F, 0.20F)
            .value(uncommon, 0.15F, 0.20F)
            .value(rare, 0.15F, 0.25F)
            .value(epic, 0.15F, 0.30F)
            .value(mythic, 0.25F, 0.40F));

        this.addAttribute("melee", "murderous", Attributes.ATTACK_DAMAGE, Operation.ADD_MULTIPLIED_BASE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.MELEE_WEAPON, LootCategory.TRIDENT)
            .value(rare, 0.15F, 0.43F)
            .value(epic, 0.18F, 0.48F)
            .value(mythic, 0.25F, 0.55F));

        this.addAttribute("melee", "violent", Attributes.ATTACK_DAMAGE, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.MELEE_WEAPON, LootCategory.TRIDENT)
            .step(0.25F)
            .value(common, 2F, 3F)
            .value(uncommon, 2F, 3F)
            .value(rare, 3F, 5F)
            .value(epic, 4F, 6F)
            .value(mythic, 5F, 8F));

        this.addAttribute("melee", "piercing", ALObjects.Attributes.ARMOR_PIERCE, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.MELEE_WEAPON, LootCategory.TRIDENT)
            .step(0.25F)
            .value(common, 2F, 4F)
            .value(uncommon, 2F, 4F)
            .value(rare, 4F, 8F)
            .value(epic, 5F, 10F)
            .value(mythic, 5F, 12F));

        this.addAttribute("melee", "lacerating", ALObjects.Attributes.CRIT_DAMAGE, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.MELEE_WEAPON, LootCategory.TRIDENT)
            .value(common, 0.10F, 0.20F)
            .value(uncommon, 0.10F, 0.20F)
            .value(rare, 0.15F, 0.25F)
            .value(epic, 0.15F, 0.25F)
            .value(mythic, 0.25F, 0.40F));

        this.addAttribute("melee", "intricate", ALObjects.Attributes.CRIT_CHANCE, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.MELEE_WEAPON, LootCategory.TRIDENT)
            .value(common, 0.10F, 0.20F)
            .value(uncommon, 0.10F, 0.20F)
            .value(rare, 0.10F, 0.25F)
            .value(epic, 0.15F, 0.35F)
            .value(mythic, 0.25F, 0.55F));

        this.addAttribute("melee", "infernal", ALObjects.Attributes.FIRE_DAMAGE, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, d -> d
                .weights(TieredWeights.forAllTiers(DEFAULT_WEIGHT, DEFAULT_QUALITY))
                .exclusiveWith(afx("melee/attribute/glacial")))
            .categories(LootCategory.MELEE_WEAPON, LootCategory.TRIDENT)
            .step(0.25F)
            .value(uncommon, 2F, 4F)
            .value(rare, 2F, 5F)
            .value(epic, 4F, 6F)
            .value(mythic, 4F, 10F));

        this.addAttribute("melee", "graceful", Attributes.ATTACK_SPEED, Operation.ADD_MULTIPLIED_TOTAL, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.MELEE_WEAPON, LootCategory.TRIDENT)
            .value(common, 0.15F, 0.25F)
            .value(uncommon, 0.20F, 0.30F)
            .value(rare, 0.20F, 0.35F)
            .value(epic, 0.25F, 0.50F)
            .value(mythic, 0.40F, 0.85F));

        this.addAttribute("melee", "glacial", ALObjects.Attributes.COLD_DAMAGE, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, d -> d
                .weights(TieredWeights.forAllTiers(DEFAULT_WEIGHT, DEFAULT_QUALITY))
                .exclusiveWith(afx("melee/attribute/infernal")))
            .categories(LootCategory.MELEE_WEAPON, LootCategory.TRIDENT)
            .step(0.25F)
            .value(uncommon, 2F, 4F)
            .value(rare, 2F, 5F)
            .value(epic, 4F, 6F)
            .value(mythic, 4F, 10F));

        this.addAttribute("melee", "lengthy", Attributes.ENTITY_INTERACTION_RANGE, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.MELEE_WEAPON, LootCategory.TRIDENT)
            .step(0.25F)
            .value(common, 0.5F, 1.5F)
            .value(uncommon, 0.5F, 1.5F)
            .value(rare, 0.75F, 2.5F)
            .value(epic, 1F, 2.5F)
            .value(mythic, 1.5F, 3));

        this.addAttribute("melee", "forceful", Attributes.ATTACK_KNOCKBACK, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.MELEE_WEAPON)
            .step(0.25F)
            .value(common, 0.5F, 1F)
            .value(uncommon, 0.5F, 1.5F)
            .value(rare, 0.75F, 2.5F)
            .value(epic, 1F, 2.5F)
            .value(mythic, 1.5F, 3));

        this.addAttribute("melee", "berserking", ALObjects.Attributes.OVERHEAL, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, d -> d
                .weights(TieredWeights.forAllTiers(DEFAULT_WEIGHT, DEFAULT_QUALITY))
                .exclusiveWith(afx("melee/attribute/vampiric")))
            .categories(LootCategory.MELEE_WEAPON)
            .value(common, 0.10F, 0.20F)
            .value(uncommon, 0.10F, 0.20F)
            .value(rare, 0.15F, 0.25F)
            .value(epic, 0.15F, 0.30F)
            .value(mythic, 0.20F, 0.45F));

        this.addAttribute("melee", "giant_slaying", ALObjects.Attributes.CURRENT_HP_DAMAGE, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, d -> d
                .weights(TieredWeights.onlyFor(WorldTier.PINNACLE, 20, 5)))
            .categories(LootCategory.MELEE_WEAPON)
            .value(epic, 0.15F, 0.25F)
            .value(mythic, 0.15F, 0.35F));

        // TODO: Armor Shred and Prot Shred affixes for melee + bow

        // Damage Reduction Affixes

        this.addDamageReduction("armor", "blockading", DamageType.PHYSICAL, b -> b
            .definition(AffixType.ABILITY, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.CHESTPLATE, LootCategory.LEGGINGS)
            .value(common, 0.01F, 0.05F)
            .value(uncommon, 0.01F, 0.05F)
            .value(rare, 0.05F, 0.10F)
            .value(epic, 0.05F, 0.10F)
            .value(mythic, 0.05F, 0.15F));

        this.addDamageReduction("armor", "runed", DamageType.MAGIC, b -> b
            .definition(AffixType.ABILITY, d -> d
                .weights(TieredWeights.forAllTiers(DEFAULT_WEIGHT, DEFAULT_QUALITY))
                .exclusiveWith(afx("armor/dmg_reduction/blockading")))
            .categories(LootCategory.HELMET, LootCategory.CHESTPLATE, LootCategory.LEGGINGS, LootCategory.BOOTS)
            .value(common, 0.01F, 0.05F)
            .value(uncommon, 0.01F, 0.05F)
            .value(rare, 0.05F, 0.10F)
            .value(epic, 0.05F, 0.10F)
            .value(mythic, 0.05F, 0.15F));

        // Armor Basic Effects

        this.addDamageReduction("armor", "blast_forged", DamageType.EXPLOSION, b -> b
            .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.CHESTPLATE, LootCategory.LEGGINGS)
            .value(common, 0.05F, 0.10F)
            .value(uncommon, 0.10F, 0.15F)
            .value(rare, 0.15F, 0.20F)
            .value(epic, 0.15F, 0.25F)
            .value(mythic, 0.15F, 0.35F));

        this.addDamageReduction("armor", "feathery", DamageType.FALL, b -> b
            .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BOOTS)
            .value(common, 0.05F, 0.10F)
            .value(uncommon, 0.10F, 0.15F)
            .value(rare, 0.15F, 0.25F)
            .value(epic, 0.15F, 0.25F)
            .value(mythic, 0.15F, 0.40F));

        this.addDamageReduction("armor", "deflective", DamageType.PROJECTILE, b -> b
            .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.HELMET, LootCategory.CHESTPLATE)
            .value(common, 0.05F, 0.10F)
            .value(uncommon, 0.10F, 0.15F)
            .value(rare, 0.15F, 0.20F)
            .value(epic, 0.15F, 0.20F)
            .value(mythic, 0.15F, 0.30F));

        this.addDamageReduction("armor", "grounded", DamageType.LIGHTNING, b -> b
            .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.HELMET, LootCategory.BOOTS)
            .value(common, 0.05F, 0.10F)
            .value(uncommon, 0.10F, 0.15F)
            .value(rare, 0.15F, 0.25F)
            .value(epic, 0.15F, 0.25F)
            .value(mythic, 0.15F, 0.40F));

        this.addMobEffect("armor", "revitalizing", MobEffects.HEAL, Target.HURT_SELF, b -> b
            .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.CHESTPLATE, LootCategory.LEGGINGS)
            .value(epic, 1, 0, 300)
            .value(mythic, StepFunction.constant(1), StepFunction.fromBounds(0, 1F, 0.25F), 240));

        this.addMobEffect("armor", "nimble", MobEffects.MOVEMENT_SPEED, Target.HURT_SELF, b -> b
            .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.LEGGINGS, LootCategory.BOOTS)
            .value(uncommon, 100, 300, 0, 800)
            .value(rare, 200, 400, 0, 800)
            .value(epic, 200, 400, StepFunction.fromBounds(0, 2, 0.25F), 700)
            .value(mythic, 200, 400, StepFunction.fromBounds(0, 2, 0.5F), 600));

        this.addMobEffect("armor", "bursting", ALObjects.MobEffects.VITALITY, Target.HURT_SELF, b -> b
            .definition(AffixType.BASIC_EFFECT, d -> d
                .weights(TieredWeights.forAllTiers(DEFAULT_WEIGHT, DEFAULT_QUALITY))
                .exclusiveWith(afx("armor/mob_effect/revitalizing")))
            .categories(LootCategory.CHESTPLATE, LootCategory.LEGGINGS)
            .value(epic, 200, 0, 300)
            .value(mythic, StepFunction.constant(200), StepFunction.fromBounds(0, 1F, 0.25F), 300));

        this.addMobEffect("armor", "bolstering", MobEffects.DAMAGE_RESISTANCE, Target.HURT_SELF, b -> b
            .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.CHESTPLATE, LootCategory.LEGGINGS)
            .value(uncommon, 40, 100, 0, 240)
            .value(rare, 80, 120, 0, 240)
            .value(epic, 80, 140, StepFunction.fromBounds(0, 1, 0.2F), 240)
            .value(mythic, 80, 160, StepFunction.fromBounds(0, 1, 0.5F), 240));

        this.addMobEffect("armor", "blinding", MobEffects.BLINDNESS, Target.HURT_ATTACKER, b -> b
            .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.HELMET)
            .value(uncommon, 40, 80, 0, 300)
            .value(rare, 40, 80, 0, 240)
            .value(epic, 40, 80, 0, 240)
            .value(mythic, 60, 100, 0, 200));

        // TODO : Grievous, Regeneration, Fire Res / Water Breathing, Slow Fall (maybe), Invisibility, Weakness

        // Breaker Basic Effects

        this.addMobEffect("breaker", "swift", MobEffects.DIG_SPEED, Target.BREAK_SELF, b -> b
            .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BREAKER)
            .value(uncommon, 100, 200, 0, 600)
            .value(rare, 200, 300, 0, 600)
            .value(epic, 200, 360, StepFunction.fromBounds(0, 1, 0.25F), 600)
            .value(mythic, 240, 400, StepFunction.fromBounds(0, 2, 0.25F), 600));

        this.addMobEffect("breaker", "spelunkers", MobEffects.MOVEMENT_SPEED, Target.BREAK_SELF, b -> b
            .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BREAKER)
            .value(uncommon, 200, 300, 0, 600)
            .value(rare, 300, 400, 0, 600)
            .value(epic, 300, 460, StepFunction.fromBounds(0, 1, 0.25F), 600)
            .value(mythic, 340, 500, StepFunction.fromBounds(0, 2, 0.25F), 600));

        Holder<Enchantment> fortune = enchants.getOrThrow(Enchantments.FORTUNE);
        this.addEnchantment("breaker", "prosperous", fortune, Mode.EXISTING, b -> b
            .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BREAKER)
            .step(0.5F)
            .value(uncommon, 1)
            .value(rare, 1, 2)
            .value(epic, 1, 3)
            .value(mythic, 2, 4));

        this.add(Apotheosis.loc("breaker/effect/omnetic"),
            new OmneticAffix.Builder()
                .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, 5)
                .value(rare, "iron", Items.IRON_AXE, Items.IRON_SHOVEL, Items.IRON_PICKAXE, Items.IRON_SWORD, Items.IRON_HOE)
                .value(epic, "diamond", Items.DIAMOND_AXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_PICKAXE, Items.DIAMOND_SWORD, Items.DIAMOND_HOE)
                .value(mythic, "netherite", Items.NETHERITE_AXE, Items.NETHERITE_SHOVEL, Items.NETHERITE_PICKAXE, Items.NETHERITE_SWORD, Items.NETHERITE_HOE)
                .build());

        this.add(Apotheosis.loc("breaker/effect/radial"),
            new RadialAffix.Builder()
                .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, 5)
                .value(uncommon, c -> c
                    .radii(1, 2, 0, 1)
                    .radii(1, 3)
                    .radii(3, 2, 0, 1))
                .value(rare, c -> c
                    .radii(1, 3)
                    .radii(3, 2, 0, 1))
                .value(epic, c -> c
                    .radii(3, 2, 0, 1)
                    .radii(3, 3)
                    .radii(5, 3))
                .value(mythic, c -> c
                    .radii(3, 3)
                    .radii(5, 3)
                    .radii(5, 5))
                .build());

        // Ranged Basic Effects

        this.addMobEffect("ranged", "shulkers", MobEffects.LEVITATION, Target.ARROW_TARGET, b -> b
            .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BOW)
            .value(epic, 20, 80, StepFunction.fromBounds(0, 1, 0.25F), 140)
            .value(mythic, 20, 100, StepFunction.fromBounds(0, 2, 0.25F), 140));

        this.addMobEffect("ranged", "acidic", ALObjects.MobEffects.SUNDERING, Target.ARROW_TARGET, b -> b
            .definition(AffixType.BASIC_EFFECT, d -> d
                .weights(TieredWeights.onlyFor(WorldTier.PINNACLE, 20, 5)))
            .categories(LootCategory.BOW)
            .stacking()
            .value(mythic, 80, 160, 0, 60));

        this.addMobEffect("ranged", "ensnaring", MobEffects.MOVEMENT_SLOWDOWN, Target.ARROW_TARGET, b -> b
            .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BOW, LootCategory.TRIDENT)
            .value(uncommon, 40, 80, 0, 160)
            .value(rare, 40, 100, 0, 160)
            .value(epic, 40, 120, StepFunction.fromBounds(0, 1, 0.25F), 160)
            .value(mythic, 80, 160, StepFunction.fromBounds(0, 2, 0.25F), 160));

        this.addMobEffect("ranged", "fleeting", MobEffects.MOVEMENT_SPEED, Target.ARROW_SELF, b -> b
            .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BOW, LootCategory.TRIDENT)
            .value(uncommon, 100, 200, 0, 0)
            .value(rare, 100, 200, 0, 0)
            .value(epic, 100, 200, StepFunction.fromBounds(0, 1, 0.25F), 0)
            .value(mythic, 100, 300, StepFunction.fromBounds(0, 2, 0.25F), 0));

        this.addMobEffect("ranged", "grievous", ALObjects.MobEffects.GRIEVOUS, Target.ARROW_TARGET, b -> b
            .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BOW, LootCategory.TRIDENT)
            .value(uncommon, 200, 200, 0, 500)
            .value(rare, 200, 300, 0, 500)
            .value(epic, 200, 300, StepFunction.fromBounds(0, 1, 0.25F), 400)
            .value(mythic, 200, 300, StepFunction.fromBounds(0, 2, 0.25F), 400));

        this.addMobEffect("ranged", "ivy_laced", MobEffects.POISON, Target.ARROW_TARGET, b -> b
            .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BOW, LootCategory.TRIDENT)
            .stacking()
            .value(rare, 100, 160, 0, 40)
            .value(epic, 100, 160, StepFunction.fromBounds(0, 1, 0.25F), 40)
            .value(mythic, 100, 200, StepFunction.fromBounds(0, 2, 0.25F), 40));

        this.addMobEffect("ranged", "blighted", MobEffects.WITHER, Target.ARROW_TARGET, b -> b
            .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BOW, LootCategory.TRIDENT)
            .value(epic, 160, 200, StepFunction.fromBounds(0, 1, 0.25F), 300)
            .value(mythic, 160, 200, StepFunction.fromBounds(0, 3, 0.25F), 300));

        this.addMobEffect("ranged", "deathbound", MobEffects.WITHER, Target.ARROW_TARGET, b -> b
            .definition(AffixType.BASIC_EFFECT, d -> d
                .weights(TieredWeights.onlyFor(WorldTier.PINNACLE, 20, 5))
                .exclusiveWith(afx("ranged/mob_effect/blighted")))
            .categories(LootCategory.BOW, LootCategory.TRIDENT)
            .stacking()
            .value(mythic, 100, 200, 1, 40));

        // Melee Basic Effects

        this.addMobEffect("melee", "bloodletting", ALObjects.MobEffects.BLEEDING, Target.ATTACK_TARGET, b -> b
            .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.MELEE_WEAPON, LootCategory.TRIDENT)
            .stacking()
            .value(uncommon, 100, 100, 0, 80)
            .value(rare, 100, 120, 0, 80)
            .value(epic, 100, 120, 0, 80)
            .value(mythic, 100, 120, StepFunction.fromBounds(0, 1, 0.125F), 80));

        this.addMobEffect("melee", "caustic", ALObjects.MobEffects.SUNDERING, Target.ATTACK_TARGET, b -> b
            .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.MELEE_WEAPON, LootCategory.TRIDENT)
            .stacking()
            .value(rare, 100, 200, 0, 30)
            .value(epic, 100, 200, StepFunction.fromBounds(0, 1, 0.125F), 300)
            .value(mythic, 200, 400, StepFunction.fromBounds(0, 1, 0.25F), 300));

        this.addMobEffect("melee", "sophisticated", ALObjects.MobEffects.KNOWLEDGE, Target.ATTACK_SELF, b -> b
            .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.MELEE_WEAPON, LootCategory.TRIDENT)
            .stacking()
            .value(uncommon, 400, 600, 0, 1200)
            .value(rare, 400, 800, 0, 1200)
            .value(epic, 400, 800, StepFunction.fromBounds(0, 1, 0.25F), 1200)
            .value(mythic, 400, 1000, StepFunction.fromBounds(0, 2, 0.25F), 1200));

        this.addMobEffect("melee", "omniscient", ALObjects.MobEffects.KNOWLEDGE, Target.ATTACK_SELF, b -> b
            .definition(AffixType.BASIC_EFFECT, d -> d
                .weights(TieredWeights.onlyFor(WorldTier.PINNACLE, 20, 5))
                .exclusiveWith(afx("melee/mob_effect/sophisticated")))
            .categories(LootCategory.MELEE_WEAPON, LootCategory.TRIDENT)
            .stacking()
            .value(mythic, 100, 160, StepFunction.fromBounds(0, 1, 0.125F), 80));

        this.addMobEffect("melee", "weakening", MobEffects.WEAKNESS, Target.ATTACK_TARGET, b -> b
            .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.MELEE_WEAPON, LootCategory.TRIDENT)
            .value(uncommon, 80, 140, 0, 300)
            .value(rare, 80, 160, 0, 300)
            .value(epic, 80, 180, StepFunction.fromBounds(0, 1, 0.25F), 300)
            .value(mythic, 80, 200, StepFunction.fromBounds(0, 2, 0.125F), 300));

        this.addMobEffect("melee", "elusive", MobEffects.MOVEMENT_SPEED, Target.ATTACK_SELF, b -> b
            .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.MELEE_WEAPON, LootCategory.TRIDENT)
            .stacking()
            .value(uncommon, 200, 400, 0, 600)
            .value(rare, 200, 600, 0, 600)
            .value(epic, 200, 600, StepFunction.fromBounds(0, 1, 0.25F), 600)
            .value(mythic, 200, 800, StepFunction.fromBounds(0, 2, 0.25F), 600));

        // Shield basic effects

        this.addMobEffect("shield", "devilish", ALObjects.MobEffects.BLEEDING, Target.BLOCK_ATTACKER, b -> b
            .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.SHIELD)
            .stacking()
            .value(uncommon, 100, 100, 0, 60)
            .value(rare, 100, 120, 0, 60)
            .value(epic, 100, 140, StepFunction.fromBounds(0, 1, 0.125F), 60)
            .value(mythic, 100, 140, StepFunction.fromBounds(0, 1, 0.25F), 60));

        this.addMobEffect("shield", "venomous", MobEffects.POISON, Target.BLOCK_ATTACKER, b -> b
            .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.SHIELD)
            .stacking()
            .value(uncommon, 120, 120, 0, 300)
            .value(rare, 120, 180, 0, 300)
            .value(epic, 120, 200, StepFunction.fromBounds(0, 1, 0.25F), 300)
            .value(mythic, 160, 240, StepFunction.fromBounds(0, 1, 0.5F), 300));

        this.addMobEffect("shield", "withering", MobEffects.WITHER, Target.BLOCK_ATTACKER, b -> b
            .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.SHIELD)
            .value(epic, 40, 100, StepFunction.fromBounds(0, 1, 0.5F), 0)
            .value(mythic, 60, 160, StepFunction.fromBounds(0, 2, 0.25F), 0));

        this.addMobEffect("shield", "reinforcing", MobEffects.DAMAGE_RESISTANCE, Target.BLOCK_SELF, b -> b
            .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.SHIELD)
            .value(rare, 120, 180, 0, 200)
            .value(epic, 120, 200, StepFunction.fromBounds(0, 1, 0.25F), 200)
            .value(mythic, 160, 240, StepFunction.fromBounds(0, 1, 0.5F), 200));

        this.addMobEffect("shield", "galvanizing", MobEffects.DAMAGE_RESISTANCE, Target.BLOCK_SELF, b -> b
            .definition(AffixType.BASIC_EFFECT, d -> d
                .weights(TieredWeights.onlyFor(WorldTier.PINNACLE, 20, 5))
                .exclusiveWith(afx("shield/mob_effect/reinforcing")))
            .categories(LootCategory.SHIELD)
            .stacking()
            .value(mythic, 100, 160, StepFunction.fromBounds(0, 1, 0.125F), 80));

        // Breaker Abilities

        this.add(Apotheosis.loc("breaker/ability/enlightened"),
            AffixBuilder.simple(EnlightenedAffix::new)
                .definition(AffixType.ABILITY, DEFAULT_WEIGHT, DEFAULT_QUALITY)
                .step(-1)
                .value(rare, 12, 8)
                .value(epic, 10, 5)
                .value(mythic, 5, 0)
                .build());

        this.add(Apotheosis.loc("breaker/ability/supermassive"),
            new RadialAffix.Builder()
                .definition(AffixType.ABILITY, c -> c
                    .weights(TieredWeights.onlyFor(WorldTier.PINNACLE, 20, 5))
                    .exclusiveWith(afx("breaker/effect/radial")))
                .value(mythic, c -> c
                    .radii(7, 7))
                .build());

        this.add(Apotheosis.loc("breaker/ability/stoneforming"), new StoneformingAffix(
            AffixDefinition.builder(AffixType.ABILITY)
                .weights(TieredWeights.forAllTiers(DEFAULT_WEIGHT, DEFAULT_QUALITY))
                .exclusiveWith(afx("breaker/ability/sandforming"))
                .build(),
            blockSet(Apoth.Tags.STONEFORMING_CANDIDATES)));

        this.add(Apotheosis.loc("breaker/ability/sandforming"), new StoneformingAffix(
            AffixDefinition.builder(AffixType.ABILITY)
                .weights(TieredWeights.forAllTiers(DEFAULT_WEIGHT, DEFAULT_QUALITY))
                .exclusiveWith(afx("breaker/ability/stoneforming"))
                .build(),
            blockSet(Apoth.Tags.SANDFORMING_CANDIDATES)));

        // Ranged Abilities

        this.add(Apotheosis.loc("ranged/magical"), new MagicalArrowAffix(
            AffixDefinition.builder(AffixType.ABILITY)
                .weights(TieredWeights.forAllTiers(DEFAULT_WEIGHT, DEFAULT_QUALITY))
                .build(),
            linkedSet(epic, mythic)));

        this.add(Apotheosis.loc("ranged/spectral"),
            AffixBuilder.simple(SpectralShotAffix::new)
                .definition(AffixType.ABILITY, DEFAULT_WEIGHT, DEFAULT_QUALITY)
                .value(epic, 0.2F, 0.5F)
                .value(mythic, 0.3F, 0.7F)
                .build());

        Holder<Enchantment> looting = enchants.getOrThrow(Enchantments.LOOTING);
        this.addEnchantment("ranged", "prosperous", looting, Mode.SINGLE, b -> b
            .definition(AffixType.ABILITY, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BOW)
            .step(0.25F)
            .value(epic, 6, 8)
            .value(mythic, 8, 10));

        // Melee Abilities

        this.add(Apotheosis.loc("melee/festive"),
            AffixBuilder.simple(FestiveAffix::new)
                .definition(AffixType.BASIC_EFFECT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
                .step(0.005F)
                .value(epic, 0.02F, 0.05F)
                .value(mythic, 0.03F, 0.06F)
                .build());

        this.add(Apotheosis.loc("melee/thunderstruck"),
            AffixBuilder.simple(ThunderstruckAffix::new)
                .definition(AffixType.ABILITY, DEFAULT_WEIGHT, DEFAULT_QUALITY)
                .step(1)
                .value(epic, 3, 6)
                .value(mythic, 4, 8)
                .build());

        this.add(Apotheosis.loc("melee/cleaving"),
            new CleavingAffix.Builder()
                .definition(AffixType.ABILITY, DEFAULT_WEIGHT, DEFAULT_QUALITY)
                .value(epic, 0.3F, 0.5F, 2, 5)
                .value(mythic, 0.4F, 0.6F, 3, 6)
                .build());

        this.add(Apotheosis.loc("melee/executing"),
            AffixBuilder.simple(ExecutingAffix::new)
                .definition(AffixType.ABILITY, DEFAULT_WEIGHT, DEFAULT_QUALITY)
                .value(epic, 0.10F, 0.20F)
                .value(mythic, 0.15F, 0.25F)
                .build());

        // Shield Abilities

        this.add(Apotheosis.loc("shield/retreating"), new RetreatingAffix(
            AffixDefinition.builder(AffixType.ABILITY).weights(TieredWeights.forAllTiers(DEFAULT_WEIGHT, DEFAULT_QUALITY)).build(),
            linkedSet(epic, mythic)));

        this.add(Apotheosis.loc("shield/psychic"), AffixBuilder.simple(PsychicAffix::new)
            .definition(AffixType.ABILITY, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .value(epic, 0.5F, 0.9F)
            .value(mythic, 0.6F, 1.2F)
            .build());

        this.add(Apotheosis.loc("shield/catalyzing"), AffixBuilder.simple(CatalyzingAffix::new)
            .definition(AffixType.ABILITY, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .step(20)
            .value(epic, 200, 400)
            .value(mythic, 300, 600)
            .build());

        this.futures.add(CompletableFuture.runAsync(RarityRegistry.INSTANCE::validateExistingHolders));
        this.futures.add(CompletableFuture.runAsync(AffixRegistry.INSTANCE::validateExistingHolders));
    }

    private HolderSet<Block> blockSet(TagKey<Block> tag) {
        return BuiltInRegistries.BLOCK.getOrCreateTag(tag);
    }

    private void addEnchantment(String type, String name, Holder<Enchantment> enchantment, EnchantmentAffix.Mode mode, UnaryOperator<EnchantmentAffix.Builder> config) {
        var builder = new EnchantmentAffix.Builder(enchantment, mode);
        config.apply(builder);
        this.add(Apotheosis.loc(type + "/enchantment/" + name), builder.build());
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

    private static Set<LootRarity> linkedSet(LootRarity... rarities) {
        return ApothMiscUtil.linkedSet(rarities);
    }

}
