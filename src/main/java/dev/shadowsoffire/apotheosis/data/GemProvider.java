package dev.shadowsoffire.apotheosis.data;

import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.effect.DamageReductionAffix.DamageType;
import dev.shadowsoffire.apotheosis.affix.effect.MobEffectAffix.Target;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.conditions.MatchesBlockCondition;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.AttributeBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.DamageReductionBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.DurabilityBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.EnchantmentBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.EnchantmentBonus.Mode;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.MobEffectBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.MultiAttrBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.special.AllStatsBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.special.DropTransformBonus;
import dev.shadowsoffire.apotheosis.tiers.Constraints;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import dev.shadowsoffire.placebo.util.data.DynamicRegistryProvider;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.Tags;

public class GemProvider extends DynamicRegistryProvider<Gem> {

    public static final int DEFAULT_WEIGHT = 10;
    public static final int DEFAULT_QUALITY = 0;

    public static final GemClass LIGHT_WEAPON = new GemClass("light_weapon", LootCategory.MELEE_WEAPON, LootCategory.TRIDENT);
    public static final GemClass CORE_ARMOR = new GemClass("core_armor", LootCategory.CHESTPLATE, LootCategory.LEGGINGS);
    public static final GemClass RANGED_WEAPON = new GemClass("ranged_weapon", LootCategory.BOW, LootCategory.TRIDENT);
    public static final GemClass LOWER_ARMOR = new GemClass("lower_armor", LootCategory.LEGGINGS, LootCategory.BOOTS);
    public static final GemClass WEAPONS = new GemClass("weapons", LootCategory.MELEE_WEAPON, LootCategory.TRIDENT, LootCategory.BOW);
    public static final GemClass WEAPON_OR_TOOL = new GemClass("weapon_or_tool", LootCategory.MELEE_WEAPON, LootCategory.TRIDENT, LootCategory.BOW, LootCategory.BREAKER);
    public static final GemClass NON_TRIDENT_WEAPONS = new GemClass("weapons", LootCategory.MELEE_WEAPON, LootCategory.BOW);

    public GemProvider(PackOutput output, CompletableFuture<Provider> registries) {
        super(output, registries, GemRegistry.INSTANCE);
    }

    @Override
    public String getName() {
        return "Gems";
    }

    @Override
    public void generate() {
        RegistryLookup<Enchantment> enchants = this.lookupProvider.join().lookup(Registries.ENCHANTMENT).get();

        addGem("core/ballast", c -> c
            .bonus(LIGHT_WEAPON, AttributeBonus.builder()
                .attr(Attributes.ATTACK_DAMAGE)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 1)
                .value(Purity.CHIPPED, 2)
                .value(Purity.FLAWED, 3.5F)
                .value(Purity.NORMAL, 5)
                .value(Purity.FLAWLESS, 7)
                .value(Purity.PERFECT, 10))
            .bonus(LootCategory.BREAKER, DurabilityBonus.builder()
                .value(Purity.CRACKED, 0.10F)
                .value(Purity.CHIPPED, 0.15F)
                .value(Purity.FLAWED, 0.25F)
                .value(Purity.NORMAL, 0.35F)
                .value(Purity.FLAWLESS, 0.45F)
                .value(Purity.PERFECT, 0.60F))
            .bonus(CORE_ARMOR, AttributeBonus.builder()
                .attr(Attributes.KNOCKBACK_RESISTANCE)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 0.1)
                .value(Purity.CHIPPED, 0.2)
                .value(Purity.FLAWED, 0.3)
                .value(Purity.NORMAL, 0.4)
                .value(Purity.FLAWLESS, 0.5)
                .value(Purity.PERFECT, 0.7)));

        addGem("core/brawlers", c -> c
            .bonus(LIGHT_WEAPON, AttributeBonus.builder()
                .attr(Attributes.ATTACK_SPEED)
                .op(Operation.ADD_MULTIPLIED_BASE)
                .value(Purity.CRACKED, 0.10)
                .value(Purity.CHIPPED, 0.15)
                .value(Purity.FLAWED, 0.20)
                .value(Purity.NORMAL, 0.30)
                .value(Purity.FLAWLESS, 0.35)
                .value(Purity.PERFECT, 0.50))
            .bonus(CORE_ARMOR, AttributeBonus.builder()
                .attr(Attributes.MAX_HEALTH)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 1)
                .value(Purity.CHIPPED, 2)
                .value(Purity.FLAWED, 4)
                .value(Purity.NORMAL, 6)
                .value(Purity.FLAWLESS, 8)
                .value(Purity.PERFECT, 12))
            .bonus(LootCategory.SHIELD, AttributeBonus.builder()
                .attr(Attributes.MAX_HEALTH)
                .op(Operation.ADD_MULTIPLIED_TOTAL)
                .value(Purity.CRACKED, 0.05)
                .value(Purity.CHIPPED, 0.10)
                .value(Purity.FLAWED, 0.15)
                .value(Purity.NORMAL, 0.20)
                .value(Purity.FLAWLESS, 0.25)
                .value(Purity.PERFECT, 0.30)));

        addGem("core/breach", c -> c
            .unique()
            .bonus(LIGHT_WEAPON, AttributeBonus.builder()
                .attr(ALObjects.Attributes.ARMOR_PIERCE)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 2)
                .value(Purity.CHIPPED, 3)
                .value(Purity.FLAWED, 5)
                .value(Purity.NORMAL, 7)
                .value(Purity.FLAWLESS, 9)
                .value(Purity.PERFECT, 12))
            .bonus(LootCategory.BREAKER, AttributeBonus.builder()
                .attr(Attributes.BLOCK_INTERACTION_RANGE)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 0.5)
                .value(Purity.CHIPPED, 1)
                .value(Purity.FLAWED, 1.5)
                .value(Purity.NORMAL, 2)
                .value(Purity.FLAWLESS, 2.5)
                .value(Purity.PERFECT, 3))
            .bonus(LootCategory.BOW, AttributeBonus.builder()
                .attr(ALObjects.Attributes.PROT_PIERCE)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 4)
                .value(Purity.CHIPPED, 5)
                .value(Purity.FLAWED, 7)
                .value(Purity.NORMAL, 8)
                .value(Purity.FLAWLESS, 10)
                .value(Purity.PERFECT, 15)));

        addGem("core/combatant", c -> c
            .unique()
            .bonus(RANGED_WEAPON, AttributeBonus.builder()
                .attr(ALObjects.Attributes.ARROW_DAMAGE)
                .op(Operation.ADD_MULTIPLIED_BASE)
                .value(Purity.CRACKED, 0.05)
                .value(Purity.CHIPPED, 0.15)
                .value(Purity.FLAWED, 0.20)
                .value(Purity.NORMAL, 0.30)
                .value(Purity.FLAWLESS, 0.40)
                .value(Purity.PERFECT, 0.55))
            .bonus(CORE_ARMOR, DamageReductionBonus.builder()
                .damageType(DamageType.PHYSICAL)
                .value(Purity.CRACKED, 0.05F)
                .value(Purity.CHIPPED, 0.075F)
                .value(Purity.FLAWED, 0.125F)
                .value(Purity.NORMAL, 0.175F)
                .value(Purity.FLAWLESS, 0.225F)
                .value(Purity.PERFECT, 0.275F))
            .bonus(LootCategory.MELEE_WEAPON, DurabilityBonus.builder()
                .value(Purity.CRACKED, 0.05F)
                .value(Purity.CHIPPED, 0.10F)
                .value(Purity.FLAWED, 0.15F)
                .value(Purity.NORMAL, 0.225F)
                .value(Purity.FLAWLESS, 0.30F)
                .value(Purity.PERFECT, 0.40F)));

        addGem("core/guardian", c -> c
            .bonus(CORE_ARMOR, AttributeBonus.builder()
                .attr(Attributes.ARMOR)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 0.5)
                .value(Purity.CHIPPED, 1)
                .value(Purity.FLAWED, 2.5)
                .value(Purity.NORMAL, 4)
                .value(Purity.FLAWLESS, 6)
                .value(Purity.PERFECT, 8))
            .bonus(LIGHT_WEAPON, AttributeBonus.builder()
                .attr(ALObjects.Attributes.LIFE_STEAL)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 0.05)
                .value(Purity.CHIPPED, 0.10)
                .value(Purity.FLAWED, 0.15)
                .value(Purity.NORMAL, 0.20)
                .value(Purity.FLAWLESS, 0.25)
                .value(Purity.PERFECT, 0.30))
            .bonus(LootCategory.SHIELD, AttributeBonus.builder()
                .attr(Attributes.ARMOR)
                .op(Operation.ADD_MULTIPLIED_TOTAL)
                .value(Purity.CRACKED, 0.05)
                .value(Purity.CHIPPED, 0.10)
                .value(Purity.FLAWED, 0.20)
                .value(Purity.NORMAL, 0.25)
                .value(Purity.FLAWLESS, 0.30)
                .value(Purity.PERFECT, 0.45)));

        addGem("core/lightning", c -> c
            .bonus(LootCategory.BOW, AttributeBonus.builder()
                .attr(ALObjects.Attributes.ARROW_VELOCITY)
                .op(Operation.ADD_MULTIPLIED_BASE)
                .value(Purity.CRACKED, 0.05)
                .value(Purity.CHIPPED, 0.15)
                .value(Purity.FLAWED, 0.25)
                .value(Purity.NORMAL, 0.35)
                .value(Purity.FLAWLESS, 0.425)
                .value(Purity.PERFECT, 0.55))
            .bonus(LootCategory.BREAKER, AttributeBonus.builder()
                .attr(ALObjects.Attributes.MINING_SPEED)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 0.05)
                .value(Purity.CHIPPED, 0.15)
                .value(Purity.FLAWED, 0.25)
                .value(Purity.NORMAL, 0.35)
                .value(Purity.FLAWLESS, 0.425)
                .value(Purity.PERFECT, 0.50))
            .bonus(LOWER_ARMOR, AttributeBonus.builder()
                .attr(Attributes.MOVEMENT_SPEED)
                .op(Operation.ADD_MULTIPLIED_TOTAL)
                .value(Purity.CRACKED, 0.10)
                .value(Purity.CHIPPED, 0.225)
                .value(Purity.FLAWED, 0.375)
                .value(Purity.NORMAL, 0.45)
                .value(Purity.FLAWLESS, 0.55)
                .value(Purity.PERFECT, 0.70)));

        addGem("core/lunar", c -> c
            .bonus(LIGHT_WEAPON, AttributeBonus.builder()
                .attr(ALObjects.Attributes.COLD_DAMAGE)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 1)
                .value(Purity.CHIPPED, 1.5)
                .value(Purity.FLAWED, 2.5)
                .value(Purity.NORMAL, 4)
                .value(Purity.FLAWLESS, 6)
                .value(Purity.PERFECT, 8))
            .bonus(CORE_ARMOR, AttributeBonus.builder()
                .attr(Attributes.GRAVITY)
                .op(Operation.ADD_MULTIPLIED_TOTAL)
                .value(Purity.CRACKED, -0.10)
                .value(Purity.CHIPPED, -0.25)
                .value(Purity.FLAWED, -0.45)
                .value(Purity.NORMAL, -0.65)
                .value(Purity.FLAWLESS, -0.85)
                .value(Purity.PERFECT, -1.04))
            .bonus(LootCategory.BOOTS, AttributeBonus.builder()
                .attr(NeoForgeMod.SWIM_SPEED)
                .op(Operation.ADD_MULTIPLIED_BASE)
                .value(Purity.CRACKED, 0.10)
                .value(Purity.CHIPPED, 0.20)
                .value(Purity.FLAWED, 0.30)
                .value(Purity.NORMAL, 0.45)
                .value(Purity.FLAWLESS, 0.60)
                .value(Purity.PERFECT, 0.80)));

        addGem("core/samurai", c -> c
            .unique()
            .bonus(WEAPONS, AttributeBonus.builder()
                .attr(ALObjects.Attributes.CRIT_CHANCE)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 0.05)
                .value(Purity.CHIPPED, 0.15)
                .value(Purity.FLAWED, 0.25)
                .value(Purity.NORMAL, 0.35)
                .value(Purity.FLAWLESS, 0.4)
                .value(Purity.PERFECT, 0.5))
            .bonus(LOWER_ARMOR, AttributeBonus.builder()
                .attr(Attributes.MOVEMENT_SPEED)
                .op(Operation.ADD_MULTIPLIED_TOTAL)
                .value(Purity.CRACKED, 0.10)
                .value(Purity.CHIPPED, 0.15)
                .value(Purity.FLAWED, 0.25)
                .value(Purity.NORMAL, 0.35)
                .value(Purity.FLAWLESS, 0.45)
                .value(Purity.PERFECT, 0.65))
            .bonus(LootCategory.HELMET, AttributeBonus.builder()
                .attr(ALObjects.Attributes.ARROW_VELOCITY)
                .op(Operation.ADD_MULTIPLIED_TOTAL)
                .value(Purity.CRACKED, 0.075)
                .value(Purity.CHIPPED, 0.15)
                .value(Purity.FLAWED, 0.25)
                .value(Purity.NORMAL, 0.375)
                .value(Purity.FLAWLESS, 0.425)
                .value(Purity.PERFECT, 0.50))
            .bonus(LootCategory.SHIELD, AttributeBonus.builder()
                .attr(ALObjects.Attributes.DODGE_CHANCE)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 0.01)
                .value(Purity.CHIPPED, 0.02)
                .value(Purity.FLAWED, 0.04)
                .value(Purity.NORMAL, 0.06)
                .value(Purity.FLAWLESS, 0.08)
                .value(Purity.PERFECT, 0.10)));

        addGem("core/slipstream", c -> c
            .unique()
            .bonus(LootCategory.BOW, AttributeBonus.builder()
                .attr(ALObjects.Attributes.DRAW_SPEED)
                .op(Operation.ADD_MULTIPLIED_BASE)
                .value(Purity.CRACKED, 0.10)
                .value(Purity.CHIPPED, 0.25)
                .value(Purity.FLAWED, 0.35)
                .value(Purity.NORMAL, 0.45)
                .value(Purity.FLAWLESS, 0.5)
                .value(Purity.PERFECT, 0.60))
            .bonus(LootCategory.BREAKER, AttributeBonus.builder()
                .attr(ALObjects.Attributes.MINING_SPEED)
                .op(Operation.ADD_MULTIPLIED_BASE)
                .value(Purity.CRACKED, 0.10)
                .value(Purity.CHIPPED, 0.15)
                .value(Purity.FLAWED, 0.225)
                .value(Purity.NORMAL, 0.30)
                .value(Purity.FLAWLESS, 0.375)
                .value(Purity.PERFECT, 0.45))
            .bonus(LootCategory.BOOTS, AttributeBonus.builder()
                .attr(ALObjects.Attributes.DODGE_CHANCE)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 0.025)
                .value(Purity.CHIPPED, 0.05)
                .value(Purity.FLAWED, 0.075)
                .value(Purity.NORMAL, 0.10)
                .value(Purity.FLAWLESS, 0.125)
                .value(Purity.PERFECT, 0.15)));

        addGem("core/solar", c -> c
            .bonus(LIGHT_WEAPON, AttributeBonus.builder()
                .attr(ALObjects.Attributes.FIRE_DAMAGE)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 1)
                .value(Purity.CHIPPED, 1.5)
                .value(Purity.FLAWED, 2.5)
                .value(Purity.NORMAL, 4)
                .value(Purity.FLAWLESS, 6)
                .value(Purity.PERFECT, 8))
            .bonus(CORE_ARMOR, AttributeBonus.builder()
                .attr(Attributes.GRAVITY)
                .op(Operation.ADD_MULTIPLIED_TOTAL)
                .value(Purity.CRACKED, 0.10)
                .value(Purity.CHIPPED, 0.25)
                .value(Purity.FLAWED, 0.45)
                .value(Purity.NORMAL, 0.65)
                .value(Purity.FLAWLESS, 0.85)
                .value(Purity.PERFECT, 1.04))
            .bonus(LootCategory.BOOTS, AttributeBonus.builder()
                .attr(Attributes.STEP_HEIGHT)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 0.25)
                .value(Purity.CHIPPED, 0.5)
                .value(Purity.FLAWED, 1)
                .value(Purity.NORMAL, 1.5)
                .value(Purity.FLAWLESS, 2)
                .value(Purity.PERFECT, 3)));

        addGem("core/splendor", c -> c
            .bonus(CORE_ARMOR, AttributeBonus.builder()
                .attr(Attributes.LUCK)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 0.5)
                .value(Purity.CHIPPED, 1.5)
                .value(Purity.FLAWED, 2.5)
                .value(Purity.NORMAL, 3.5)
                .value(Purity.FLAWLESS, 4.5)
                .value(Purity.PERFECT, 6))
            .bonus(WEAPON_OR_TOOL, AttributeBonus.builder()
                .attr(ALObjects.Attributes.EXPERIENCE_GAINED)
                .op(Operation.ADD_MULTIPLIED_BASE)
                .value(Purity.CRACKED, 0.075)
                .value(Purity.CHIPPED, 0.15)
                .value(Purity.FLAWED, 0.225)
                .value(Purity.NORMAL, 0.3)
                .value(Purity.FLAWLESS, 0.40)
                .value(Purity.PERFECT, 0.60))
            .bonus(LootCategory.BOOTS, AttributeBonus.builder()
                .attr(Attributes.MOVEMENT_SPEED)
                .op(Operation.ADD_MULTIPLIED_TOTAL)
                .value(Purity.CRACKED, 0.10)
                .value(Purity.CHIPPED, 0.25)
                .value(Purity.FLAWED, 0.35)
                .value(Purity.NORMAL, 0.45)
                .value(Purity.FLAWLESS, 0.55)
                .value(Purity.PERFECT, 0.70)));

        addGem("core/tyrannical", c -> c
            .unique()
            .bonus(LIGHT_WEAPON, AttributeBonus.builder()
                .attr(Attributes.KNOCKBACK_RESISTANCE)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 0.5)
                .value(Purity.CHIPPED, 1)
                .value(Purity.FLAWED, 1.5)
                .value(Purity.NORMAL, 2)
                .value(Purity.FLAWLESS, 3)
                .value(Purity.PERFECT, 3.5))
            .bonus(CORE_ARMOR, AttributeBonus.builder()
                .attr(Attributes.ARMOR_TOUGHNESS)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 0.5)
                .value(Purity.CHIPPED, 1)
                .value(Purity.FLAWED, 2)
                .value(Purity.NORMAL, 3)
                .value(Purity.FLAWLESS, 4)
                .value(Purity.PERFECT, 6))
            .bonus(LootCategory.SHIELD, AttributeBonus.builder()
                .attr(Attributes.ARMOR_TOUGHNESS)
                .op(Operation.ADD_MULTIPLIED_BASE)
                .value(Purity.CRACKED, 0.05)
                .value(Purity.CHIPPED, 0.15)
                .value(Purity.FLAWED, 0.225)
                .value(Purity.NORMAL, 0.30)
                .value(Purity.FLAWLESS, 0.375)
                .value(Purity.PERFECT, 0.5))
            .bonus(LootCategory.BOW, MobEffectBonus.builder()
                .effect(ALObjects.MobEffects.BLEEDING)
                .target(Target.ARROW_TARGET)
                .stacking()
                .value(Purity.FLAWLESS, 160, 0, 40)
                .value(Purity.PERFECT, 160, 1, 40)));

        addGem("core/warlord", c -> c
            .bonus(NON_TRIDENT_WEAPONS, AttributeBonus.builder()
                .attr(ALObjects.Attributes.CRIT_DAMAGE)
                .op(Operation.ADD_MULTIPLIED_BASE)
                .value(Purity.CRACKED, 0.10)
                .value(Purity.CHIPPED, 0.20)
                .value(Purity.FLAWED, 0.35)
                .value(Purity.NORMAL, 0.45)
                .value(Purity.FLAWLESS, 0.55)
                .value(Purity.PERFECT, 0.70))
            .bonus(LootCategory.CHESTPLATE, AttributeBonus.builder()
                .attr(Attributes.MAX_HEALTH)
                .op(Operation.ADD_MULTIPLIED_BASE)
                .value(Purity.CRACKED, 0.05)
                .value(Purity.CHIPPED, 0.10)
                .value(Purity.FLAWED, 0.15)
                .value(Purity.NORMAL, 0.20)
                .value(Purity.FLAWLESS, 0.25)
                .value(Purity.PERFECT, 0.35))
            .bonus(LootCategory.HELMET, AttributeBonus.builder()
                .attr(Attributes.ATTACK_DAMAGE)
                .op(Operation.ADD_MULTIPLIED_TOTAL)
                .value(Purity.CRACKED, 0.05)
                .value(Purity.CHIPPED, 0.10)
                .value(Purity.FLAWED, 0.125)
                .value(Purity.NORMAL, 0.15)
                .value(Purity.FLAWLESS, 0.175)
                .value(Purity.PERFECT, 0.225))
            .bonus(LootCategory.TRIDENT, MobEffectBonus.builder()
                .effect(MobEffects.DAMAGE_BOOST)
                .target(Target.ARROW_TARGET)
                .stacking()
                .value(Purity.FLAWLESS, 200, 0, 40)
                .value(Purity.PERFECT, 200, 1, 40)));

        addGem("overworld/earth", TieredWeights.forAllTiers(5, 1.5F), c -> c
            .unique()
            .minPurity(Purity.FLAWED)
            .contstraints(Constraints.forDimension(Level.OVERWORLD))
            .bonus(LIGHT_WEAPON, EnchantmentBonus.builder()
                .enchantment(enchants.getOrThrow(Enchantments.SHARPNESS))
                .mode(Mode.EXISTING)
                .value(Purity.FLAWED, 1)
                .value(Purity.NORMAL, 2)
                .value(Purity.FLAWLESS, 3)
                .value(Purity.PERFECT, 4))
            .bonus(CORE_ARMOR, EnchantmentBonus.builder()
                .enchantment(enchants.getOrThrow(Enchantments.PROTECTION))
                .mode(Mode.EXISTING)
                .value(Purity.FLAWED, 1)
                .value(Purity.NORMAL, 2)
                .value(Purity.FLAWLESS, 3)
                .value(Purity.PERFECT, 4))
            .bonus(LootCategory.BREAKER, EnchantmentBonus.builder()
                .enchantment(enchants.getOrThrow(Enchantments.FORTUNE))
                .mode(Mode.EXISTING)
                .value(Purity.FLAWED, 1)
                .value(Purity.NORMAL, 2)
                .value(Purity.FLAWLESS, 3)
                .value(Purity.PERFECT, 4)));

        addGem("overworld/royalty", TieredWeights.forAllTiers(5, 1.5F), c -> c
            .unique()
            .minPurity(Purity.FLAWED)
            .contstraints(Constraints.forDimension(Level.OVERWORLD))
            .bonus(LootCategory.HELMET, AllStatsBonus.builder()
                .op(Operation.ADD_MULTIPLIED_TOTAL)
                .value(Purity.FLAWED, 0.05F)
                .value(Purity.NORMAL, 0.075F)
                .value(Purity.FLAWLESS, 0.10F)
                .value(Purity.PERFECT, 0.15F)
                .attributes(
                    Attributes.MAX_HEALTH,
                    Attributes.KNOCKBACK_RESISTANCE,
                    Attributes.MOVEMENT_SPEED,
                    Attributes.ATTACK_DAMAGE,
                    Attributes.ATTACK_KNOCKBACK,
                    Attributes.ATTACK_SPEED,
                    Attributes.ARMOR,
                    Attributes.ARMOR_TOUGHNESS,
                    Attributes.LUCK,
                    Attributes.STEP_HEIGHT,
                    Attributes.BLOCK_INTERACTION_RANGE,
                    Attributes.ENTITY_INTERACTION_RANGE,
                    ALObjects.Attributes.ARMOR_PIERCE,
                    ALObjects.Attributes.ARMOR_SHRED,
                    ALObjects.Attributes.ARROW_DAMAGE,
                    ALObjects.Attributes.ARROW_VELOCITY,
                    ALObjects.Attributes.COLD_DAMAGE,
                    ALObjects.Attributes.CRIT_CHANCE,
                    ALObjects.Attributes.CRIT_DAMAGE,
                    ALObjects.Attributes.CURRENT_HP_DAMAGE,
                    ALObjects.Attributes.DODGE_CHANCE,
                    ALObjects.Attributes.EXPERIENCE_GAINED,
                    ALObjects.Attributes.FIRE_DAMAGE,
                    ALObjects.Attributes.GHOST_HEALTH,
                    ALObjects.Attributes.HEALING_RECEIVED,
                    ALObjects.Attributes.LIFE_STEAL,
                    ALObjects.Attributes.MINING_SPEED,
                    ALObjects.Attributes.OVERHEAL,
                    ALObjects.Attributes.PROT_PIERCE,
                    ALObjects.Attributes.PROT_SHRED,
                    NeoForgeMod.SWIM_SPEED))
            .bonus(LootCategory.BREAKER, DropTransformBonus.builder()
                .condition(new MatchesBlockCondition(BuiltInRegistries.BLOCK.getOrCreateTag(Tags.Blocks.ORES_COPPER)))
                .inputs(Ingredient.of(Tags.Items.RAW_MATERIALS_COPPER))
                .desc("gem.apotheosis:overworld/royalty.bonus.pickaxe")
                .output(new ItemStack(Items.RAW_GOLD))
                .value(Purity.FLAWED, 0.15F)
                .value(Purity.NORMAL, 0.20F)
                .value(Purity.FLAWLESS, 0.25F)
                .value(Purity.PERFECT, 0.40F))
            .bonus(LootCategory.BOW, MultiAttrBonus.builder()
                .desc("bonus.apotheosis:multi_attr.desc.and")
                .modifier(b -> b
                    .attr(ALObjects.Attributes.PROT_SHRED)
                    .op(Operation.ADD_VALUE)
                    .value(Purity.FLAWED, 0.25F)
                    .value(Purity.NORMAL, 0.30F)
                    .value(Purity.FLAWLESS, 0.35F)
                    .value(Purity.PERFECT, 0.40F))
                .modifier(b -> b
                    .attr(ALObjects.Attributes.DRAW_SPEED)
                    .op(Operation.ADD_MULTIPLIED_TOTAL)
                    .value(Purity.FLAWED, -0.35F)
                    .value(Purity.NORMAL, -0.45F)
                    .value(Purity.FLAWLESS, -0.55F)
                    .value(Purity.PERFECT, -0.65F)))
            .bonus(LootCategory.SHIELD, MultiAttrBonus.builder()
                .desc("bonus.apotheosis:multi_attr.desc.and_but")
                .modifier(b -> b
                    .attr(Attributes.ARMOR)
                    .op(Operation.ADD_MULTIPLIED_BASE)
                    .value(Purity.FLAWED, 0.15F)
                    .value(Purity.NORMAL, 0.25F)
                    .value(Purity.FLAWLESS, 0.35F)
                    .value(Purity.PERFECT, 0.50F))
                .modifier(b -> b
                    .attr(Attributes.ARMOR_TOUGHNESS)
                    .op(Operation.ADD_MULTIPLIED_BASE)
                    .value(Purity.FLAWED, 0.075F)
                    .value(Purity.NORMAL, 0.125F)
                    .value(Purity.FLAWLESS, 0.225F)
                    .value(Purity.PERFECT, 0.30F))
                .modifier(b -> b
                    .attr(Attributes.MOVEMENT_SPEED)
                    .op(Operation.ADD_MULTIPLIED_TOTAL)
                    .value(Purity.FLAWED, -0.25F)
                    .value(Purity.NORMAL, -0.30F)
                    .value(Purity.FLAWLESS, -0.35F)
                    .value(Purity.PERFECT, -0.40F))));

    }

    private void addGem(String name, UnaryOperator<Gem.Builder> config) {
        addGem(name, TieredWeights.forAllTiers(DEFAULT_WEIGHT, DEFAULT_QUALITY), config);
    }

    private void addGem(String name, TieredWeights weights, UnaryOperator<Gem.Builder> config) {
        var builder = new Gem.Builder(weights);
        config.apply(builder);
        this.add(Apotheosis.loc(name), builder.build());
    }

}
