package dev.shadowsoffire.apotheosis.tiers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

/**
 * Generic constraints that can be applied to various object types (affix loot entries, bosses, gems, etc).
 * 
 * @param dimensions A set of dimensions that this object is applicable in.
 * @param biomes     A set of biomes that this object is applicable in.
 * @param gameStages A set of gamestages that this object is applicable in.
 */
public record Constraints(Set<ResourceKey<Level>> dimensions, HolderSet<Biome> biomes, Set<String> gameStages) {

    public static final Constraints EMPTY = new Constraints(Set.of(), HolderSet.empty(), Set.of());

    public static final Codec<Constraints> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        PlaceboCodecs.setOf(Level.RESOURCE_KEY_CODEC).optionalFieldOf("dimensions", Collections.emptySet()).forGetter(Constraints::dimensions),
        RegistryCodecs.homogeneousList(Registries.BIOME).optionalFieldOf("biomes", HolderSet.empty()).forGetter(Constraints::biomes),
        PlaceboCodecs.setOf(Codec.string(1, 256)).optionalFieldOf("dimensions", Collections.emptySet()).forGetter(Constraints::gameStages))
        .apply(inst, Constraints::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Constraints> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.collection(HashSet::new, ResourceKey.streamCodec(Registries.DIMENSION)), Constraints::dimensions,
        ByteBufCodecs.holderSet(Registries.BIOME), Constraints::biomes,
        ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.stringUtf8(256)), Constraints::gameStages,
        Constraints::new);

    public boolean test(GenContext ctx) {
        if (!dimensions.isEmpty() && !dimensions.contains(ctx.dimension())) {
            return false;
        }
        if (biomes.size() != 0 && !biomes.contains(ctx.biome())) {
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
