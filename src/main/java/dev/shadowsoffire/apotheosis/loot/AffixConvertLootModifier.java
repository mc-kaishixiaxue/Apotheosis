package dev.shadowsoffire.apotheosis.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apotheosis.util.LootPatternMatcher;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class AffixConvertLootModifier extends LootModifier {

    public static final MapCodec<AffixConvertLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, AffixConvertLootModifier::new));

    protected AffixConvertLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        Player player = context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof Player p ? p : null;
        if (player == null) {
            return generatedLoot;
        }

        // TODO: Move convert loot rules into this loot modifier as a codec parameter.
        for (LootPatternMatcher m : AdventureConfig.AFFIX_CONVERT_LOOT_RULES) {
            if (m.matches(context.getQueriedLootTableId())) {
                RandomSource rand = context.getRandom();
                float luck = context.getLuck();
                for (ItemStack s : generatedLoot) {
                    if (!LootCategory.forItem(s).isNone() && AffixHelper.getAffixes(s).isEmpty() && rand.nextFloat() <= m.chance()) {
                        WorldTier tier = WorldTier.getTier(player);
                        LootController.createLootItem(s, LootRarity.random(rand, tier, luck), rand, tier, luck);
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
