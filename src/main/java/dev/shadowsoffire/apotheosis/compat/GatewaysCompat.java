package dev.shadowsoffire.apotheosis.compat;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.mobs.registries.InvaderRegistry;
import dev.shadowsoffire.apotheosis.mobs.types.Invader;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.WaveEntity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public class GatewaysCompat {

    public static void register() {
        WaveEntity.CODEC.register(Apotheosis.loc("invader"), InvaderWaveEntity.CODEC);
    }

    public static record InvaderWaveEntity(DynamicHolder<Invader> invader, String desc) implements WaveEntity {

        public static Codec<InvaderWaveEntity> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                InvaderRegistry.INSTANCE.holderCodec().optionalFieldOf("invader", InvaderRegistry.INSTANCE.emptyHolder()).forGetter(InvaderWaveEntity::invader),
                Codec.STRING.optionalFieldOf("desc").forGetter(b -> Optional.of(b.desc)))
            .apply(inst, InvaderWaveEntity::new));

        public InvaderWaveEntity(DynamicHolder<Invader> invader, Optional<String> desc) {
            this(invader, desc.orElse(resolveInvaderDesc(invader)));
        }

        @Override
        public LivingEntity createEntity(ServerLevel level, GatewayEntity gate) {
            GenContext ctx = GenContext.forPlayer(gate.summonerOrClosest());
            Invader realBoss = resolveInvader(ctx);
            if (realBoss == null) {
                if (usingRandomInvader()) {
                    Apotheosis.LOGGER.error("Failed to resolve a random invader when generating a InvaderWaveEntity!");
                }
                else {
                    String type = this.invader.getId().toString();
                    Apotheosis.LOGGER.error("Failed to resolve the invader '{}' when generating a InvaderWaveEntity!", type);
                }
                return null;
            }
            return realBoss.createBoss(level, BlockPos.ZERO, ctx);
        }

        @Override
        public MutableComponent getDescription() {
            return Component.translatable("misc.apotheosis.invader", Component.translatable(this.desc));
        }

        @Override
        public boolean shouldFinalizeSpawn() {
            return false;
        }

        @Override
        public Codec<? extends WaveEntity> getCodec() {
            return CODEC;
        }

        @Override
        public int getCount() {
            return 1;
        }

        protected boolean usingRandomInvader() {
            return this.invader.equals(InvaderRegistry.INSTANCE.emptyHolder());
        }

        @Nullable
        protected Invader resolveInvader(GenContext ctx) {
            if (this.usingRandomInvader()) {
                return InvaderRegistry.INSTANCE.getRandomItem(ctx);
            }

            return this.invader().getOptional().orElse(null);
        }

        public static String resolveInvaderDesc(DynamicHolder<Invader> invader) {
            return invader.isBound() ? invader.get().entity().getDescriptionId() : "misc.apotheosis.random";
        }
    }

}
