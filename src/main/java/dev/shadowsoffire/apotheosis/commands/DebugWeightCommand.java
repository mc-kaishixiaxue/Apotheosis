package dev.shadowsoffire.apotheosis.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.loot.AffixLootRegistry;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.mobs.registries.EliteRegistry;
import dev.shadowsoffire.apotheosis.mobs.registries.InvaderRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.tiers.Constraints.Constrained;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights.Weighted;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.util.random.WeightedRandom;

public class DebugWeightCommand {

    public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
        LiteralArgumentBuilder<CommandSourceStack> weights = Commands.literal("weights");

        weights.then(Commands.literal("affix_loot_entries").executes(c -> dumpWeights(c, AffixLootRegistry.INSTANCE)));
        weights.then(Commands.literal("affixes").executes(c -> dumpWeights(c, AffixRegistry.INSTANCE)));
        weights.then(Commands.literal("elites").executes(c -> dumpWeights(c, EliteRegistry.INSTANCE)));
        weights.then(Commands.literal("gems").executes(c -> dumpWeights(c, GemRegistry.INSTANCE)));
        weights.then(Commands.literal("invaders").executes(c -> dumpWeights(c, InvaderRegistry.INSTANCE)));
        weights.then(Commands.literal("rarities").executes(c -> dumpWeights(c, RarityRegistry.INSTANCE)));

        root.then(weights);
    }

    /**
     * Dumps the weights for all objects in the target registry.
     * <p>
     * If the registry objects are {@link Constrainted}, objects that fail their constraint check will be treated as having zero weight.
     */
    public static <T extends CodecProvider<? super T> & Weighted> void dumpWeightsFor(GenContext ctx, DynamicRegistry<T> registry) {
        Collection<T> values = registry.getValues();
        List<Wrapper<T>> list = new ArrayList<>(values.size());

        values.stream().map(t -> wrapWithConstraints(ctx, t)).forEach(list::add);

        float total = WeightedRandom.getTotalWeight(list);

        Apotheosis.LOGGER.info("Starting dump of all {} weights...", registry.getPath());
        Apotheosis.LOGGER.info("Current GenContext: {}", ctx);
        list.sort(Comparator.comparing(w -> -w.weight().asInt()));
        for (Wrapper<T> entry : list) {
            ResourceLocation key = registry.getKey(entry.data());
            float chance = entry.weight().asInt() / total;
            Apotheosis.LOGGER.info("{} : {}% ({} / {}}", key, Affix.fmt(chance * 100), entry.weight().asInt(), (int) total);
        }
    }

    public static <T extends CodecProvider<? super T> & Weighted> int dumpWeights(CommandContext<CommandSourceStack> c, DynamicRegistry<T> registry) throws CommandSyntaxException {
        GenContext ctx = GenContext.forPlayer(c.getSource().getPlayerOrException());
        dumpWeightsFor(ctx, registry);
        c.getSource().sendSuccess(() -> Component.literal("Weight values have been dumped to the log file."), true);
        return 0;
    }

    private static <T extends Weighted> Wrapper<T> wrapWithConstraints(GenContext ctx, T t) {
        if (t instanceof Constrained c && !c.constraints().test(ctx)) {
            return new WeightedEntry.Wrapper<>(t, Weighted.SAFE_ZERO);
        }
        return t.<T>wrap(ctx.tier(), ctx.luck());
    }

}
