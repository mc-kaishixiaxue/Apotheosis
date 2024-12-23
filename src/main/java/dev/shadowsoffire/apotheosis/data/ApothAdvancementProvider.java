package dev.shadowsoffire.apotheosis.data;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.advancements.predicates.AffixItemPredicate;
import dev.shadowsoffire.apotheosis.advancements.predicates.TypeAwareISP;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ApothAdvancementProvider extends AdvancementProvider {

    private ApothAdvancementProvider(PackOutput output, CompletableFuture<Provider> registries, ExistingFileHelper existingFileHelper, List<AdvancementGenerator> subProviders) {
        super(output, registries, existingFileHelper, subProviders);
    }

    public static ApothAdvancementProvider create(PackOutput output, CompletableFuture<Provider> registries, ExistingFileHelper existingFileHelper) {
        return new ApothAdvancementProvider(
            output,
            registries,
            existingFileHelper,
            List.of(
                new ProgressionGenerator()

            ));
    }

    private static class ProgressionGenerator implements AdvancementGenerator {

        @Override
        public void generate(Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {
            AdvancementHolder root = Advancement.Builder.advancement()
                .display(
                    Items.MAP,
                    title("progression.root"),
                    desc("progression.root"),
                    Apotheosis.loc("textures/advancements/bg/apoth.png"),
                    AdvancementType.TASK,
                    false,
                    false,
                    false)
                .requirements(AdvancementRequirements.Strategy.OR)
                .addCriterion("affixed", InventoryChangeTrigger.TriggerInstance.hasItems(ip(new AffixItemPredicate())))
                .save(saver, loc("progression/root"));
        }

        private static Component title(String key) {
            return Apotheosis.lang("advancements", key + ".title");
        }

        private static Component desc(String key) {
            return Apotheosis.lang("advancements", key + ".description");
        }
    }

    private static String loc(String path) {
        return Apotheosis.loc(path).toString();
    }

    private static ItemPredicate ip(TypeAwareISP<?> sub) {
        return new ItemPredicate(Optional.empty(), MinMaxBounds.Ints.ANY, DataComponentPredicate.EMPTY, Map.of(sub.type(), sub));
    }
}
