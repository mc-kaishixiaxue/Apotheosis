package dev.shadowsoffire.apotheosis.data;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apothic_enchanting.ApothicEnchanting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class ApothLootProvider extends LootTableProvider {

    private ApothLootProvider(PackOutput output, Set<ResourceKey<LootTable>> requiredTables, List<SubProviderEntry> subProviders, CompletableFuture<Provider> registries) {
        super(output, requiredTables, subProviders, registries);
    }

    public static ApothLootProvider create(PackOutput output, CompletableFuture<Provider> registries) {
        return new ApothLootProvider(
            output,
            Set.of(),
            List.of(
                new LootTableProvider.SubProviderEntry(BlockLoot::new, LootContextParamSets.BLOCK)),
            registries);
    }

    public static class BlockLoot extends BlockLootSubProvider {

        public static final Set<Item> EXPLOSION_RESISTANT = Set.of(
            Apoth.Items.REFORGING_TABLE.value(),
            Apoth.Items.AUGMENTING_TABLE.value());

        protected BlockLoot(Provider registries) {
            super(EXPLOSION_RESISTANT, FeatureFlags.REGISTRY.allFlags(), registries);
        }

        @Override
        protected void generate() {
            this.dropSelf(Apoth.Blocks.SIMPLE_REFORGING_TABLE);
            this.dropSelf(Apoth.Blocks.REFORGING_TABLE);
            this.dropSelf(Apoth.Blocks.SALVAGING_TABLE);
            this.dropSelf(Apoth.Blocks.GEM_CUTTING_TABLE);
            this.dropSelf(Apoth.Blocks.AUGMENTING_TABLE);
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return BuiltInRegistries.BLOCK.holders().filter(h -> h.getKey().location().getNamespace().equals(ApothicEnchanting.MODID)).map(Holder::value).toList();
        }

        protected void dropSelf(Holder<Block> block) {
            this.dropSelf(block.value());
        }

    }
}
