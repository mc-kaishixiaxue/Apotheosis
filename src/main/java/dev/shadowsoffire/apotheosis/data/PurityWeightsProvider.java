package dev.shadowsoffire.apotheosis.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.PurityWeightsRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.PurityWeightsRegistry.PurityWeights;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights.Weight;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.placebo.util.data.DynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

public class PurityWeightsProvider extends DynamicRegistryProvider<PurityWeights> {

    public PurityWeightsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, PurityWeightsRegistry.INSTANCE);
    }

    @Override
    public String getName() {
        return "Purity Weights";
    }

    @Override
    public void generate() {
        // The total base weight for each tier is 100

        this.add(b -> b
            .tier(WorldTier.HAVEN, c -> c
                .with(Purity.CRACKED, 600, 0)
                .with(Purity.CHIPPED, 360, 2.5F)
                .with(Purity.FLAWED, 40, 5F)
                .with(Purity.NORMAL, 0, 0)
                .with(Purity.FLAWLESS, 0, 0)
                .with(Purity.PERFECT, 0, 0))
            .tier(WorldTier.FRONTIER, c -> c
                .with(Purity.CRACKED, 290, 0)
                .with(Purity.CHIPPED, 600, 0)
                .with(Purity.FLAWED, 100, 2.5F)
                .with(Purity.NORMAL, 10, 1F)
                .with(Purity.FLAWLESS, 0, 0)
                .with(Purity.PERFECT, 0, 0))
            .tier(WorldTier.ASCENT, c -> c
                .with(Purity.CRACKED, 100, 0)
                .with(Purity.CHIPPED, 550, 0)
                .with(Purity.FLAWED, 330, 2.5F)
                .with(Purity.NORMAL, 20, 5F)
                .with(Purity.FLAWLESS, 0, 1F)
                .with(Purity.PERFECT, 0, 0))
            .tier(WorldTier.SUMMIT, c -> c
                .with(Purity.CRACKED, 0, 0)
                .with(Purity.CHIPPED, 350, 0)
                .with(Purity.FLAWED, 480, 2.5F)
                .with(Purity.NORMAL, 150, 5F)
                .with(Purity.FLAWLESS, 20, 5F)
                .with(Purity.PERFECT, 0, 0))
            .tier(WorldTier.APOTHEOSIS, c -> c
                .with(Purity.CRACKED, 0, 0)
                .with(Purity.CHIPPED, 0, 0)
                .with(Purity.FLAWED, 100, 0)
                .with(Purity.NORMAL, 250, 0)
                .with(Purity.FLAWLESS, 500, 0)
                .with(Purity.PERFECT, 150, 5F)));

    }

    private void add(UnaryOperator<Builder> config) {
        this.add(PurityWeightsRegistry.TARGET_FILE, config.apply(new Builder()).build());
    }

    private static class Builder {
        Map<WorldTier, Map<Purity, Weight>> weightsMap = new HashMap<>();

        public Builder tier(WorldTier tier, UnaryOperator<InnerBuilder> config) {
            weightsMap.put(tier, config.apply(new InnerBuilder()).innerMap);
            return this;
        }

        public PurityWeights build() {
            return new PurityWeights(this.weightsMap);
        }

    }

    private static class InnerBuilder {
        Map<Purity, Weight> innerMap = new HashMap<>();

        InnerBuilder with(Purity purity, int weight, float quality) {
            innerMap.put(purity, new Weight(weight, quality));
            return this;
        }
    }
}
