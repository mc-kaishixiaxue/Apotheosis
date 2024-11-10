package dev.shadowsoffire.apotheosis.spawner;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.mixin.BaseSpawnerAccessor;
import dev.shadowsoffire.apotheosis.util.PresetSpawnerStats;
import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry.ILuckyWeighted;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootTable;

public class RogueSpawner implements CodecProvider<RogueSpawner>, ILuckyWeighted {

    public static final Codec<RogueSpawner> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            Codec.INT.fieldOf("weight").forGetter(RogueSpawner::getWeight),
            PresetSpawnerStats.CODEC.fieldOf("stats").forGetter(RogueSpawner::getStats),
            ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("loot_table").forGetter(RogueSpawner::getLootTableId),
            SimpleWeightedRandomList.wrappedCodec(SpawnData.CODEC).fieldOf("spawn_potentials").forGetter(s -> s.spawnPotentials))
        .apply(inst, RogueSpawner::new));

    protected final int weight;
    protected final PresetSpawnerStats stats;
    protected final ResourceKey<LootTable> lootTable;
    protected final SimpleWeightedRandomList<SpawnData> spawnPotentials;

    public RogueSpawner(int weight, PresetSpawnerStats stats, ResourceKey<LootTable> lootTable, SimpleWeightedRandomList<SpawnData> potentials) {
        this.weight = weight;
        this.stats = stats;
        this.lootTable = lootTable;
        this.spawnPotentials = potentials;
    }

    @Override
    public int getWeight() {
        return this.weight;
    }

    @Override
    public float getQuality() {
        return 0;
    }

    public PresetSpawnerStats getStats() {
        return this.stats;
    }

    public ResourceKey<LootTable> getLootTableId() {
        return this.lootTable;
    }

    @SuppressWarnings("deprecation")
    public void place(WorldGenLevel level, BlockPos pos, RandomSource rand) {
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof ApothSpawnerTile spawner) {
            this.stats.apply(spawner);
            spawner.getSpawner().spawnPotentials = this.spawnPotentials;
            ((BaseSpawnerAccessor) spawner.getSpawner()).setNextSpawnData(null, pos, this.spawnPotentials.getRandomValue(rand).get());

            level.setBlock(pos.below(), Blocks.CHEST.defaultBlockState(), 2);
            ResourceKey<LootTable> realLootTable = rand.nextFloat() <= AdventureConfig.spawnerValueChance ? Apoth.LootTables.CHEST_VALUABLE : this.lootTable;
            RandomizableContainer.setBlockEntityLootTable(level, rand, pos.below(), realLootTable);

            Block cover = BuiltInRegistries.BLOCK.getRandomElementOf(Apoth.Tags.ROGUE_SPAWNER_COVERS, rand).map(Holder::value).orElse(Blocks.STONE);
            level.setBlock(pos.above(), cover.defaultBlockState(), 2);
            for (Direction f : Plane.HORIZONTAL) {
                if (level.getBlockState(pos.relative(f)).isAir()) {
                    BooleanProperty side = (BooleanProperty) Blocks.VINE.getStateDefinition().getProperty(f.getOpposite().getName());
                    level.setBlock(pos.relative(f), Blocks.VINE.defaultBlockState().setValue(side, true), 2);
                }
            }
        }
    }

    @Override
    public Codec<? extends RogueSpawner> getCodec() {
        return CODEC;
    }

}
