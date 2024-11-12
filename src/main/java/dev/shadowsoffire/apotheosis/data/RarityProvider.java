package dev.shadowsoffire.apotheosis.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import org.spongepowered.include.com.google.common.base.Preconditions;

import dev.shadowsoffire.apotheosis.Apoth.Items;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.AffixType;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.LootRule;
import dev.shadowsoffire.apotheosis.loot.LootRule.AffixLootRule;
import dev.shadowsoffire.apotheosis.loot.LootRule.ChancedLootRule;
import dev.shadowsoffire.apotheosis.loot.LootRule.ComponentLootRule;
import dev.shadowsoffire.apotheosis.loot.LootRule.DurabilityLootRule;
import dev.shadowsoffire.apotheosis.loot.LootRule.SelectLootRule;
import dev.shadowsoffire.apotheosis.loot.LootRule.SocketLootRule;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.placebo.util.data.DynamicRegistryProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Unbreakable;

public class RarityProvider extends DynamicRegistryProvider<LootRarity> {

    public RarityProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, RarityRegistry.INSTANCE);
    }

    @Override
    public String getName() {
        return "Rarities";
    }

    @Override
    public void generate() {
        // Common, high rarity early, dropping to 0% later
        // One stat, with a low chance for a second.
        addRarity("common", TextColor.fromRgb(0x808080), Items.COMMON_MATERIAL, b -> b
            .weights(TieredWeights.builder()
                .with(WorldTier.HAVEN, 600, 0)
                .with(WorldTier.FRONTIER, 290, 0)
                .with(WorldTier.ASCENT, 100, 0)
                .with(WorldTier.SUMMIT, 50, 0)
                .with(WorldTier.APOTHEOSIS, 0, 0))
            .rule(new AffixLootRule(AffixType.STAT))
            .rule(new ChancedLootRule(0.25F, new AffixLootRule(AffixType.STAT))));

        // Uncommon, reasonable rarity until the final tier.
        // Two stats, with a chance for a potion effect. Opportunity for a socket.
        addRarity("uncommon", TextColor.fromRgb(0x33FF33), Items.UNCOMMON_MATERIAL, b -> b
            .weights(TieredWeights.builder()
                .with(WorldTier.HAVEN, 360, 2.5F)
                .with(WorldTier.FRONTIER, 600, 0)
                .with(WorldTier.ASCENT, 550, 0)
                .with(WorldTier.SUMMIT, 350, 0)
                .with(WorldTier.APOTHEOSIS, 0, 0))
            .rule(new AffixLootRule(AffixType.STAT))
            .rule(new AffixLootRule(AffixType.STAT))
            .rule(new ChancedLootRule(0.5F, new AffixLootRule(AffixType.POTION)))
            .rule(new SocketLootRule(0, 1)));

        // Rare - always available, but starts out with very low frequency.
        // Two stats, a potion, with the chance for an ability.
        // Chance for two sockets, and can receive durability bonuses.
        addRarity("rare", TextColor.fromRgb(0x5555FF), Items.RARE_MATERIAL, b -> b
            .weights(TieredWeights.builder()
                .with(WorldTier.HAVEN, 40, 5)
                .with(WorldTier.FRONTIER, 100, 5)
                .with(WorldTier.ASCENT, 330, 2.5F)
                .with(WorldTier.SUMMIT, 430, 2.5F)
                .with(WorldTier.APOTHEOSIS, 100, 0))
            .rule(new AffixLootRule(AffixType.STAT))
            .rule(new AffixLootRule(AffixType.STAT))
            .rule(new AffixLootRule(AffixType.POTION))
            .rule(new ChancedLootRule(0.35F, new AffixLootRule(AffixType.ABILITY)))
            .rule(new SocketLootRule(0, 2))
            .rule(new DurabilityLootRule(0.1F, 0.25F)));

        // Epic, not available in Haven, and exceptionally low chance on Frontier+Ascent.
        // Starts to really appear in Summit, and is the most common rarity in Apotheosis.
        // Three stats, a potion, an ability, and a chance for a second of each non-stat.
        // Guaranteed to receive a socket, and receives a good durability bonus.
        addRarity("epic", TextColor.fromRgb(0xBB00BB), Items.EPIC_MATERIAL, b -> b
            .weights(TieredWeights.builder()
                .with(WorldTier.HAVEN, 0, 0)
                .with(WorldTier.FRONTIER, 10, 0)
                .with(WorldTier.ASCENT, 20, 5)
                .with(WorldTier.SUMMIT, 150, 5)
                .with(WorldTier.APOTHEOSIS, 650, 2.5F))
            .rule(new AffixLootRule(AffixType.STAT))
            .rule(new AffixLootRule(AffixType.STAT))
            .rule(new AffixLootRule(AffixType.STAT))
            .rule(new AffixLootRule(AffixType.POTION))
            .rule(new ChancedLootRule(0.25F, new AffixLootRule(AffixType.POTION)))
            .rule(new AffixLootRule(AffixType.ABILITY))
            .rule(new ChancedLootRule(0.35F, new AffixLootRule(AffixType.ABILITY)))
            .rule(new SocketLootRule(1, 3))
            .rule(new DurabilityLootRule(0.25F, 0.55F)));

        // Mythic, the pinnacle rarity. Does not appear until summit, and has very low rarity until Apotheosis.
        // Four stats, two potions, two abilities, guaranteed sockets and high durability bonuses.
        // Has a very low chance to receive 4 sockets, and be Unbreakable.
        addRarity("mythic", TextColor.fromRgb(0xED7014), Items.MYTHIC_MATERIAL, b -> b
            .weights(TieredWeights.builder()
                .with(WorldTier.HAVEN, 0, 0)
                .with(WorldTier.FRONTIER, 0, 0)
                .with(WorldTier.ASCENT, 0, 0)
                .with(WorldTier.SUMMIT, 20, 5)
                .with(WorldTier.APOTHEOSIS, 250, 10))
            .rule(new AffixLootRule(AffixType.STAT))
            .rule(new AffixLootRule(AffixType.STAT))
            .rule(new AffixLootRule(AffixType.STAT))
            .rule(new AffixLootRule(AffixType.STAT))
            .rule(new AffixLootRule(AffixType.POTION))
            .rule(new AffixLootRule(AffixType.POTION))
            .rule(new AffixLootRule(AffixType.ABILITY))
            .rule(new AffixLootRule(AffixType.ABILITY))
            .rule(new SelectLootRule(0.95F, // 95% chance for 1-3 sockets, 5% chance for guaranteed 4 sockets.
                new SocketLootRule(1, 3),
                new SocketLootRule(4, 4)))
            .rule(new SelectLootRule(0.99F, // 99% chance to roll a durability bonus, 1% to be unbreakable.
                new DurabilityLootRule(0.45F, 0.75F),
                componentRule(DataComponents.UNBREAKABLE, new Unbreakable(true)))));
    }

    static <T> LootRule componentRule(DataComponentType<T> type, T value) {
        return new ComponentLootRule(DataComponentPatch.builder().set(type, value).build());
    }

    void addRarity(String id, TextColor color, Holder<Item> material, UnaryOperator<RarityBuilder> config) {
        this.add(Apotheosis.loc(id), config.apply(builder(color, material)).build());
    }

    static RarityBuilder builder(TextColor color, Holder<Item> material) {
        return new RarityBuilder(color, material);
    }

    private static class RarityBuilder {

        private final TextColor color;
        private final Holder<Item> material;
        private TieredWeights weights;
        private final List<LootRule> rules = new ArrayList<>();

        private RarityBuilder(TextColor color, Holder<Item> material) {
            this.color = color;
            this.material = material;
        }

        RarityBuilder weights(TieredWeights.Builder builder) {
            this.weights = builder.build();
            return this;
        }

        RarityBuilder rule(LootRule rule) {
            this.rules.add(rule);
            return this;
        }

        public LootRarity build() {
            Preconditions.checkNotNull(this.weights);
            Preconditions.checkArgument(this.rules.size() > 0);
            return new LootRarity(color, material, weights, rules);
        }

    }

}
