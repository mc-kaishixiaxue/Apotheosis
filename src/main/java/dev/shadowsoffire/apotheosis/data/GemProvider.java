package dev.shadowsoffire.apotheosis.data;

import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.AttributeBonus;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.DurabilityBonus;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import dev.shadowsoffire.placebo.util.data.DynamicRegistryProvider;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class GemProvider extends DynamicRegistryProvider<Gem> {

    public static final int DEFAULT_WEIGHT = 10;
    public static final int DEFAULT_QUALITY = 0;

    public static final GemClass MELEE_WEAPON = new GemClass("melee_weapon", LootCategory.MELEE_WEAPON, LootCategory.TRIDENT);
    public static final GemClass BREAKER = new GemClass("breaker", LootCategory.BREAKER);
    public static final GemClass CORE_ARMOR = new GemClass("core_armor", LootCategory.CHESTPLATE, LootCategory.LEGGINGS);
    public static final GemClass SHIELD = new GemClass("shield", LootCategory.SHIELD);
    public static final GemClass BOW = new GemClass("bow", LootCategory.BOW);

    public GemProvider(PackOutput output, CompletableFuture<Provider> registries) {
        super(output, registries, GemRegistry.INSTANCE);
    }

    @Override
    public String getName() {
        return "Gems";
    }

    @Override
    public void generate() {
        addGem("core/ballast", c -> c
            .bonus(MELEE_WEAPON, AttributeBonus.builder()
                .attr(Attributes.ATTACK_DAMAGE)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 1)
                .value(Purity.CHIPPED, 2)
                .value(Purity.FLAWED, 3.5F)
                .value(Purity.NORMAL, 5)
                .value(Purity.FLAWLESS, 7)
                .value(Purity.PERFECT, 10))
            .bonus(BREAKER, DurabilityBonus.builder()
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
            .bonus(MELEE_WEAPON, AttributeBonus.builder()
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
            .bonus(SHIELD, AttributeBonus.builder()
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
            .bonus(MELEE_WEAPON, AttributeBonus.builder()
                .attr(ALObjects.Attributes.ARMOR_PIERCE)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 2)
                .value(Purity.CHIPPED, 3)
                .value(Purity.FLAWED, 5)
                .value(Purity.NORMAL, 7)
                .value(Purity.FLAWLESS, 9)
                .value(Purity.PERFECT, 12))
            .bonus(BREAKER, AttributeBonus.builder()
                .attr(Attributes.BLOCK_INTERACTION_RANGE)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 0.5)
                .value(Purity.CHIPPED, 1)
                .value(Purity.FLAWED, 1.5)
                .value(Purity.NORMAL, 2)
                .value(Purity.FLAWLESS, 2.5)
                .value(Purity.PERFECT, 3))
            .bonus(BOW, AttributeBonus.builder()
                .attr(ALObjects.Attributes.PROT_PIERCE)
                .op(Operation.ADD_VALUE)
                .value(Purity.CRACKED, 4)
                .value(Purity.CHIPPED, 5)
                .value(Purity.FLAWED, 7)
                .value(Purity.NORMAL, 8)
                .value(Purity.FLAWLESS, 10)
                .value(Purity.PERFECT, 15)));
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
