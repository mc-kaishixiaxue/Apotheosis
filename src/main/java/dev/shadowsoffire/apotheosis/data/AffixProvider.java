package dev.shadowsoffire.apotheosis.data;

import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import org.spongepowered.include.com.google.common.base.Preconditions;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.affix.AffixType;
import dev.shadowsoffire.apotheosis.affix.AttributeAffix;
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
            .value(uncommon, StepFunction.fromBounds(1F, 1.5F))
            .value(rare, StepFunction.fromBounds(2F, 3F))
            .value(epic, StepFunction.fromBounds(2.5F, 4F))
            .value(mythic, StepFunction.fromBounds(3F, 6F)));

        // Armor Attributes
        addAttribute("armor", "aquatic", NeoForgeMod.SWIM_SPEED, Operation.ADD_MULTIPLIED_TOTAL, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BOOTS)
            .value(common, StepFunction.fromBounds(0.2F, 0.3F, 0.05F))
            .value(uncommon, StepFunction.fromBounds(0.2F, 0.3F, 0.05F))
            .value(rare, StepFunction.fromBounds(0.3F, 0.5F, 0.05F))
            .value(epic, StepFunction.fromBounds(0.3F, 0.5F, 0.05F))
            .value(mythic, StepFunction.fromBounds(0.4F, 0.7F, 0.05F)));

        addAttribute("armor", "blessed", Attributes.MAX_HEALTH, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(ARMOR)
            .value(common, StepFunction.fromBounds(2, 4))
            .value(uncommon, StepFunction.fromBounds(2, 5))
            .value(rare, StepFunction.fromBounds(3, 6))
            .value(epic, StepFunction.fromBounds(3, 8))
            .value(mythic, StepFunction.fromBounds(5, 8)));

        addAttribute("armor", "elastic", Attributes.STEP_HEIGHT, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BOOTS)
            .value(common, StepFunction.constant(0.5F))
            .value(uncommon, StepFunction.constant(0.5F))
            .value(rare, StepFunction.fromBounds(0.5F, 1.5F, 0.25F))
            .value(epic, StepFunction.fromBounds(0.5F, 1.5F, 0.25F))
            .value(mythic, StepFunction.fromBounds(1, 2, 0.25F)));

        addAttribute("armor", "fortunate", Attributes.LUCK, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(ARMOR)
            .value(common, StepFunction.fromBounds(0.5F, 1.5F))
            .value(uncommon, StepFunction.fromBounds(0.5F, 1.5F))
            .value(rare, StepFunction.fromBounds(1.5F, 3))
            .value(epic, StepFunction.fromBounds(1.5F, 3.5F))
            .value(mythic, StepFunction.fromBounds(3, 5)));

        addAttribute("armor", "gravitational", Attributes.GRAVITY, Operation.ADD_MULTIPLIED_TOTAL, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.CHESTPLATE)
            .value(common, StepFunction.fromBounds(-0.1F, -0.2F, -0.05F))
            .value(uncommon, StepFunction.fromBounds(-0.1F, -0.25F, -0.05F))
            .value(rare, StepFunction.fromBounds(-0.15F, -0.30F, -0.05F))
            .value(epic, StepFunction.fromBounds(-0.15F, -0.35F, -0.05F))
            .value(mythic, StepFunction.fromBounds(-0.20F, -0.5F, -0.05F)));

        addAttribute("armor", "ironforged", Attributes.ARMOR, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(ARMOR)
            .value(common, StepFunction.fromBounds(1, 1.5F))
            .value(uncommon, StepFunction.fromBounds(1, 1.5F))
            .value(rare, StepFunction.fromBounds(1.5F, 3))
            .value(epic, StepFunction.fromBounds(2, 5))
            .value(mythic, StepFunction.fromBounds(4, 8)));

        addAttribute("armor", "adamantine", Attributes.ARMOR, Operation.ADD_MULTIPLIED_BASE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.CHESTPLATE, LootCategory.LEGGINGS)
            .value(rare, StepFunction.fromBounds(0.15F, 0.3F, 0.05F))
            .value(epic, StepFunction.fromBounds(0.15F, 0.3F, 0.05F))
            .value(mythic, StepFunction.fromBounds(0.25F, 0.4F, 0.05F)));

        addAttribute("armor", "spiritual", ALObjects.Attributes.HEALING_RECEIVED, Operation.ADD_MULTIPLIED_BASE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.CHESTPLATE, LootCategory.LEGGINGS)
            .value(rare, StepFunction.fromBounds(0.10F, 0.25F, 0.05F))
            .value(epic, StepFunction.fromBounds(0.15F, 0.30F, 0.05F))
            .value(mythic, StepFunction.fromBounds(0.20F, 0.40F, 0.05F)));

        addAttribute("armor", "stalwart", Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(ARMOR)
            .value(uncommon, StepFunction.fromBounds(0.05F, 0.1F, 0.01F))
            .value(rare, StepFunction.fromBounds(0.05F, 0.1F, 0.01F))
            .value(epic, StepFunction.fromBounds(0.15F, 0.20F, 0.01F))
            .value(mythic, StepFunction.fromBounds(0.25F, 0.35F, 0.01F)));

        addAttribute("armor", "steel_touched", Attributes.ARMOR_TOUGHNESS, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(ARMOR)
            .value(rare, StepFunction.constant(1))
            .value(epic, StepFunction.fromBounds(1.5F, 3F))
            .value(mythic, StepFunction.fromBounds(2F, 6F)));

        addAttribute("armor", "windswept", Attributes.MOVEMENT_SPEED, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.LEGGINGS, LootCategory.BOOTS)
            .value(common, StepFunction.fromBounds(0.1F, 0.2F, 0.05F))
            .value(uncommon, StepFunction.fromBounds(0.1F, 0.25F, 0.05F))
            .value(rare, StepFunction.fromBounds(0.15F, 0.3F, 0.05F))
            .value(epic, StepFunction.fromBounds(0.15F, 0.4F, 0.05F))
            .value(mythic, StepFunction.fromBounds(0.2F, 0.45F, 0.05F)));

        addAttribute("armor", "winged", ALObjects.Attributes.ELYTRA_FLIGHT, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.CHESTPLATE)
            .value(epic, StepFunction.constant(1))
            .value(mythic, StepFunction.constant(1)));

        addAttribute("armor", "unbound", NeoForgeMod.CREATIVE_FLIGHT, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, d -> d
                .weights(TieredWeights.onlyFor(WorldTier.APOTHEOSIS, 20, 5))
                .exclusiveWith(afx("armor/attribute/winged")))
            .categories(LootCategory.CHESTPLATE)
            .value(mythic, StepFunction.constant(1)));

        addAttribute("armor", "fireproof", Attributes.BURNING_TIME, Operation.ADD_MULTIPLIED_BASE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.HELMET)
            .value(uncommon, StepFunction.fromBounds(-0.15F, -0.25F, -0.05F))
            .value(rare, StepFunction.fromBounds(-0.20F, -0.30F, -0.05F))
            .value(epic, StepFunction.fromBounds(-0.25F, -0.35F, -0.05F))
            .value(mythic, StepFunction.fromBounds(-0.30F, -0.45F, -0.05F)));

        addAttribute("armor", "oxygenated", Attributes.OXYGEN_BONUS, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.HELMET)
            .value(uncommon, StepFunction.fromBounds(0.2F, 0.3F, 0.05F))
            .value(rare, StepFunction.fromBounds(0.35F, 0.5F, 0.05F))
            .value(epic, StepFunction.fromBounds(0.55F, 0.7F, 0.05F))
            .value(mythic, StepFunction.fromBounds(0.85F, 1.25F, 0.05F)));

        // TODO: Potentially add offensive stats as armor affixes?

        // Breaker Attributes

        addAttribute("breaker", "destructive", ALObjects.Attributes.MINING_SPEED, Operation.ADD_MULTIPLIED_TOTAL, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BREAKER)
            .value(common, StepFunction.fromBounds(0.15F, 0.3F, 0.01F))
            .value(uncommon, StepFunction.fromBounds(0.15F, 0.3F, 0.01F))
            .value(rare, StepFunction.fromBounds(0.25F, 0.5F, 0.01F))
            .value(epic, StepFunction.fromBounds(0.25F, 0.7F, 0.01F))
            .value(mythic, StepFunction.fromBounds(0.55F, 0.85F, 0.01F)));

        addAttribute("breaker", "experienced", ALObjects.Attributes.EXPERIENCE_GAINED, Operation.ADD_MULTIPLIED_TOTAL, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BREAKER)
            .value(common, StepFunction.fromBounds(0.25F, 0.4F, 0.01F))
            .value(uncommon, StepFunction.fromBounds(0.25F, 0.4F, 0.01F))
            .value(rare, StepFunction.fromBounds(0.35F, 0.5F, 0.01F))
            .value(epic, StepFunction.fromBounds(0.35F, 0.6F, 0.01F))
            .value(mythic, StepFunction.fromBounds(0.55F, 0.65F, 0.01F)));

        addAttribute("breaker", "lengthy", Attributes.BLOCK_INTERACTION_RANGE, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BREAKER)
            .value(common, StepFunction.fromBounds(0.5F, 1.5F, 0.25F))
            .value(uncommon, StepFunction.fromBounds(0.5F, 1.5F, 0.25F))
            .value(rare, StepFunction.fromBounds(1F, 2.5F, 0.25F))
            .value(epic, StepFunction.fromBounds(1F, 2.5F, 0.25F))
            .value(mythic, StepFunction.fromBounds(1.5F, 4, 0.25F)));

        addAttribute("breaker", "submerged", Attributes.SUBMERGED_MINING_SPEED, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BREAKER)
            .value(common, StepFunction.fromBounds(0.1F, 0.3F, 0.025F))
            .value(uncommon, StepFunction.fromBounds(0.2F, 0.3F, 0.025F))
            .value(rare, StepFunction.fromBounds(0.3F, 0.5F, 0.025F))
            .value(epic, StepFunction.fromBounds(0.4F, 0.55F, 0.025F))
            .value(mythic, StepFunction.fromBounds(0.5F, 0.8F, 0.025F)));

        // TODO: We need more of these, the pool here is too limited.

        // Ranged Attributes

        addAttribute("ranged", "agile", ALObjects.Attributes.DRAW_SPEED, Operation.ADD_MULTIPLIED_TOTAL, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BOW)
            .value(common, StepFunction.fromBounds(0.2F, 0.4F, 0.05F))
            .value(uncommon, StepFunction.fromBounds(0.2F, 0.4F, 0.05F))
            .value(rare, StepFunction.fromBounds(0.3F, 0.5F, 0.05F))
            .value(epic, StepFunction.fromBounds(0.5F, 0.6F, 0.05F))
            .value(mythic, StepFunction.fromBounds(0.5F, 0.65F, 0.05F)));

        addAttribute("ranged", "elven", ALObjects.Attributes.ARROW_DAMAGE, Operation.ADD_MULTIPLIED_TOTAL, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BOW, LootCategory.TRIDENT)
            .value(common, StepFunction.fromBounds(0.15F, 0.20F, 0.01F))
            .value(uncommon, StepFunction.fromBounds(0.15F, 0.25F, 0.01F))
            .value(rare, StepFunction.fromBounds(0.20F, 0.30F, 0.01F))
            .value(epic, StepFunction.fromBounds(0.25F, 0.35F, 0.01F))
            .value(mythic, StepFunction.fromBounds(0.25F, 0.40F, 0.01F)));

        addAttribute("ranged", "streamlined", ALObjects.Attributes.ARROW_VELOCITY, Operation.ADD_MULTIPLIED_TOTAL, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BOW, LootCategory.TRIDENT)
            .value(common, StepFunction.fromBounds(0.15F, 0.20F, 0.01F))
            .value(uncommon, StepFunction.fromBounds(0.15F, 0.20F, 0.01F))
            .value(rare, StepFunction.fromBounds(0.15F, 0.25F, 0.01F))
            .value(epic, StepFunction.fromBounds(0.15F, 0.30F, 0.01F))
            .value(mythic, StepFunction.fromBounds(0.15F, 0.35F, 0.01F)));

        addAttribute("ranged", "windswept", Attributes.MOVEMENT_SPEED, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.BOW, LootCategory.TRIDENT)
            .value(common, StepFunction.fromBounds(0.15F, 0.25F, 0.05F))    
            .value(uncommon, StepFunction.fromBounds(0.15F, 0.3F, 0.05F))
            .value(rare, StepFunction.fromBounds(0.15F, 0.3F, 0.05F))
            .value(epic, StepFunction.fromBounds(0.15F, 0.35F, 0.05F))
            .value(mythic, StepFunction.fromBounds(0.2F, 0.4F, 0.05F)));

        // Shield Attributes

        addAttribute("shield", "ironforged", Attributes.ARMOR, Operation.ADD_MULTIPLIED_BASE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.SHIELD)
            .value(common, StepFunction.fromBounds(0.10F, 0.15F, 0.01F))
            .value(uncommon, StepFunction.fromBounds(0.10F, 0.15F, 0.01F))
            .value(rare, StepFunction.fromBounds(0.15F, 0.20F, 0.01F))
            .value(epic, StepFunction.fromBounds(0.15F, 0.25F, 0.01F))
            .value(mythic, StepFunction.fromBounds(0.20F, 0.30F, 0.01F)));

        addAttribute("shield", "stalwart", Attributes.KNOCKBACK_RESISTANCE, Operation.ADD_VALUE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.SHIELD)
            .value(common, StepFunction.fromBounds(0.10F, 0.20F, 0.01F))
            .value(uncommon, StepFunction.fromBounds(0.10F, 0.20F, 0.01F))
            .value(rare, StepFunction.fromBounds(0.20F, 0.30F, 0.01F))
            .value(epic, StepFunction.fromBounds(0.20F, 0.30F, 0.01F))
            .value(mythic, StepFunction.fromBounds(0.25F, 0.35F, 0.01F)));

        addAttribute("shield", "steel_touched", Attributes.ARMOR_TOUGHNESS, Operation.ADD_MULTIPLIED_BASE, b -> b
            .definition(AffixType.STAT, DEFAULT_WEIGHT, DEFAULT_QUALITY)
            .categories(LootCategory.SHIELD)
            .value(rare, StepFunction.fromBounds(0.15F, 0.20F, 0.01F))
            .value(epic, StepFunction.fromBounds(0.15F, 0.20F, 0.01F))
            .value(mythic, StepFunction.fromBounds(0.20F, 0.30F, 0.01F)));
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
