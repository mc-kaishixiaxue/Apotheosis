package dev.shadowsoffire.apotheosis.loot.modifiers;

import com.mojang.serialization.MapCodec;

import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class AffixHookLootModifier extends LootModifier {

    public static final MapCodec<AffixHookLootModifier> CODEC = MapCodec.unit(AffixHookLootModifier::new);

    protected AffixHookLootModifier() {
        super(new LootItemCondition[0]);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext ctx) {
        if (ctx.hasParam(LootContextParams.TOOL)) {
            ItemStack tool = ctx.getParam(LootContextParams.TOOL);
            SocketHelper.getGems(tool).modifyLoot(generatedLoot, ctx);
            AffixHelper.streamAffixes(tool).forEach(inst -> inst.modifyLoot(generatedLoot, ctx));
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

}
