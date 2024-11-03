package dev.shadowsoffire.apotheosis.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.util.LootPatternMatcher;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class GemLootModifier extends LootModifier {

    public static final MapCodec<GemLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, GemLootModifier::new));

    protected GemLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        var player = GemLootPoolEntry.findPlayer(context);
        if (player == null) return generatedLoot;

        for (LootPatternMatcher m : AdventureConfig.GEM_LOOT_RULES) {
            if (m.matches(context.getQueriedLootTableId())) {
                if (context.getRandom().nextFloat() <= m.chance()) {
                    ItemStack gem = GemRegistry.createRandomGemStack(context.getRandom(), context.getLevel(), player);
                    generatedLoot.add(gem);
                }
                break;
            }
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

}
