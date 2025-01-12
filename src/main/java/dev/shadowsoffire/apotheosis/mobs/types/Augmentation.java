package dev.shadowsoffire.apotheosis.mobs.types;

import java.util.Collections;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.mobs.util.EntityModifier;
import dev.shadowsoffire.apotheosis.mobs.util.SpawnCondition;
import dev.shadowsoffire.apotheosis.tiers.Constraints;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * An Augmentation is a non-exclusive modifier that may apply to any naturally spawned mob.
 * <p>
 * Augmentations are applied very early in the mob spawn pipeline, immediately after world tier modifiers.
 * <p>
 * Mobs have a chance to be selected for augmenting. If they are selected, every loaded augmentation will attempt to apply.
 *
 * @param chance      The chance that this augmentation is selected.
 * @param constraints Any context-based restrictions on the application of this augmentation.
 * @param exclusions  Any entity-based restrictions on the application of this augmentation.
 * @param modifiers   The list of modifiers that will be applied to the target entity.
 */
public record Augmentation(float chance, Constraints constraints, List<SpawnCondition> exclusions, List<EntityModifier> modifiers) implements CodecProvider<Augmentation> {

    public static final Codec<Augmentation> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            Codec.floatRange(0, 1).fieldOf("application_chance").forGetter(Augmentation::chance),
            Constraints.CODEC.fieldOf("constraints").forGetter(Augmentation::constraints),
            SpawnCondition.CODEC.listOf().optionalFieldOf("exclusions", Collections.emptyList()).forGetter(Augmentation::exclusions),
            EntityModifier.CODEC.listOf().fieldOf("modifiers").forGetter(Augmentation::modifiers))
        .apply(inst, Augmentation::new));

    @Override
    public Codec<? extends Augmentation> getCodec() {
        return CODEC;
    }

    public boolean canApply(ServerLevelAccessor level, Mob mob, MobSpawnType type, GenContext ctx) {
        if (!this.constraints.test(ctx)) {
            return false;
        }

        return SpawnCondition.checkAll(this.exclusions, mob, level, type);
    }

    public void apply(Mob mob, GenContext ctx) {
        if (ctx.rand().nextFloat() <= this.chance) {
            for (EntityModifier em : this.modifiers) {
                em.apply(mob, ctx);
            }
        }
    }

}
