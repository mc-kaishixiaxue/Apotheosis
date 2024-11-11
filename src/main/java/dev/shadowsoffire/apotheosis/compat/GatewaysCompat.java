package dev.shadowsoffire.apotheosis.compat;

import java.util.Optional;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.boss.ApothBoss;
import dev.shadowsoffire.apotheosis.boss.BossRegistry;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.WaveEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ServerLevelAccessor;

public class GatewaysCompat {

    public static void register() {
        WaveEntity.CODEC.register(Apotheosis.loc("boss"), BossWaveEntity.CODEC);
    }

    public static class BossWaveEntity implements WaveEntity {

        public static Codec<BossWaveEntity> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                ResourceLocation.CODEC.optionalFieldOf("boss").forGetter(b -> b.bossId))
            .apply(inst, BossWaveEntity::new));

        private final Optional<ResourceLocation> bossId;
        private final Supplier<ApothBoss> boss;

        public BossWaveEntity(Optional<ResourceLocation> bossId) {
            this.bossId = bossId;
            this.boss = Suppliers.memoize(() -> bossId.map(BossRegistry.INSTANCE::getValue).orElse(null));
        }

        @Override
        public LivingEntity createEntity(ServerLevel level, GatewayEntity gate) {
            GenContext ctx = GenContext.forPlayer(gate.summonerOrClosest());
            ApothBoss realBoss = this.bossId.isEmpty() ? BossRegistry.INSTANCE.getRandomItem(ctx) : this.boss.get();
            if (realBoss == null) return null; // error condition
            return realBoss.createBoss((ServerLevelAccessor) level, BlockPos.ZERO, ctx);
        }

        @Override
        public MutableComponent getDescription() {
            return Component.translatable("misc.apotheosis.boss", Component.translatable(this.bossId.isEmpty() ? "misc.apotheosis.random" : this.boss.get().getEntity().getDescriptionId()));
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
    }

}
