package dev.shadowsoffire.apotheosis.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;

@Mixin(BaseSpawner.class)
public interface BaseSpawnerAccessor {

    @Invoker("setNextSpawnData")
    void setNextSpawnData(@Nullable Level level, BlockPos pos, SpawnData data);

}
