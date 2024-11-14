package dev.shadowsoffire.apotheosis.gen;

import com.mojang.serialization.MapCodec;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureEntityInfo;

public class ItemFrameGemsProcessor extends StructureProcessor {

    public static final MapCodec<StructureProcessor> CODEC = MapCodec.unit(new ItemFrameGemsProcessor(null));

    // public static final Codec<ItemFrameGemsProcessor> CODEC = RecordCodecBuilder
    // .create(instance -> instance.group(ResourceLocation.CODEC.fieldOf("loot_table").forGetter(ItemFrameGemsProcessor::getLootTable)).apply(instance,
    // ItemFrameGemsProcessor::new));

    protected final ResourceLocation lootTable;

    public ItemFrameGemsProcessor(ResourceLocation lootTable) {
        this.lootTable = lootTable;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return Apoth.Features.ITEM_FRAME_GEMS.value();
    }

    @Override
    public StructureEntityInfo processEntity(LevelReader world, BlockPos seedPos, StructureEntityInfo rawEntityInfo, StructureEntityInfo entityInfo, StructurePlaceSettings placementSettings, StructureTemplate template) {
        CompoundTag entityNBT = entityInfo.nbt;

        String id = entityNBT.getString("id"); // entity type ID
        if (world instanceof ServerLevelAccessor sla && "minecraft:item_frame".equals(id)) {
            this.writeEntityNBT(sla.getLevel(), entityInfo.blockPos, placementSettings.getRandom(entityInfo.blockPos), entityNBT, placementSettings);
        }

        return entityInfo;
    }

    protected void writeEntityNBT(ServerLevel level, BlockPos pos, RandomSource rand, CompoundTag nbt, StructurePlaceSettings settings) {
        Player player = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), -1, true);
        GenContext ctx = player != null ? GenContext.forPlayerAtPos(rand, player, pos) : GenContext.standalone(rand, WorldTier.HAVEN, 0, level, pos);
        ItemStack stack = GemRegistry.createRandomGemStack(ctx);
        if (!stack.isEmpty()) {
            nbt.put("Item", stack.save(level.registryAccess()));
        }
        nbt.putInt("TileX", pos.getX());
        nbt.putInt("TileY", pos.getY());
        nbt.putInt("TileZ", pos.getZ());
    }
}
