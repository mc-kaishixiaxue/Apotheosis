package dev.shadowsoffire.apotheosis.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.util.LootPatternMatcher;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;

public class AffixConvertLootModifier extends ContextualLootModifier {

    public static final MapCodec<AffixConvertLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, AffixConvertLootModifier::new));

    protected AffixConvertLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context, GenContext gCtx) {
        // TODO: Move convert loot rules into this loot modifier as a codec parameter.
        for (LootPatternMatcher m : AdventureConfig.AFFIX_CONVERT_LOOT_RULES) {
            if (m.matches(context.getQueriedLootTableId())) {
                RandomSource rand = context.getRandom();
                for (ItemStack s : generatedLoot) {
                    if (!LootCategory.forItem(s).isNone() && AffixHelper.getAffixes(s).isEmpty() && rand.nextFloat() <= m.chance()) {
                        LootController.createLootItem(s, LootRarity.random(gCtx), gCtx);
                    }
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
