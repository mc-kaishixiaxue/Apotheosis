package dev.shadowsoffire.apotheosis.tiers;

import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

/**
 * Generic constraints that can be applied to various object types (affix loot entries, bosses, gems, etc).
 * <p>
 * For each of the parameters, an empty set means "accept all".
 *
 * @param tiers      The set of world tiers this object is applicable in.
 * @param dimensions The set of dimensions that this object is applicable in.
 * @param biomes     The set of biomes that this object is applicable in.
 * @param gameStages The set of gamestages that this object is applicable in.
 */
public record Constraints(Set<WorldTier> tiers, Set<ResourceKey<Level>> dimensions, HolderSet<Biome> biomes, Set<String> gameStages) {

    public static final Constraints EMPTY = new Constraints(Set.of(), Set.of(), HolderSet.empty(), Set.of());

    public static final Codec<Constraints> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        PlaceboCodecs.setOf(WorldTier.CODEC).optionalFieldOf("tiers", Collections.emptySet()).forGetter(Constraints::tiers),
        PlaceboCodecs.setOf(Level.RESOURCE_KEY_CODEC).optionalFieldOf("dimensions", Collections.emptySet()).forGetter(Constraints::dimensions),
        RegistryCodecs.homogeneousList(Registries.BIOME).optionalFieldOf("biomes", HolderSet.empty()).forGetter(Constraints::biomes),
        PlaceboCodecs.setOf(Codec.string(1, 256)).optionalFieldOf("stages", Collections.emptySet()).forGetter(Constraints::gameStages))
        .apply(inst, Constraints::new));

    public static Constraints forDimension(ResourceKey<Level> key) {
        return new Constraints(Set.of(), Set.of(key), HolderSet.empty(), Set.of());
    }

    public boolean test(GenContext ctx) {
        if (!this.tiers.isEmpty() && !this.tiers.contains(ctx.tier())) {
            return false;
        }

        if (!this.dimensions.isEmpty() && !this.dimensions.contains(ctx.dimension())) {
            return false;
        }

        if (this.biomes.size() != 0 && !this.biomes.contains(ctx.biome())) {
            return false;
        }

        return this.gameStages.isEmpty() || this.gameStages.stream().anyMatch(ctx.stages()::contains);
    }

    public static <T extends Constrained> Predicate<T> eval(GenContext ctx) {
        return t -> t.constraints().test(ctx);
    }

    public static interface Constrained {
        Constraints constraints();
    }
}
