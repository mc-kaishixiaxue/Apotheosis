package dev.shadowsoffire.apotheosis.boss;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.apotheosis.AdventureModule;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.tiers.Constraints;
import dev.shadowsoffire.apotheosis.tiers.TieredDynamicRegistry;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

public class MinibossRegistry extends TieredDynamicRegistry<ApothMiniboss> {

    public static final MinibossRegistry INSTANCE = new MinibossRegistry();

    public MinibossRegistry() {
        super(AdventureModule.LOGGER, "minibosses", false, false);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(Apotheosis.loc("miniboss"), ApothMiniboss.CODEC);
    }

    @Nullable
    public ApothMiniboss getRandomItem(RandomSource rand, Player player) {
        return getRandomItem(rand, WorldTier.getTier(player), player.getLuck(), Constraints.eval(player));
    }

    /**
     * An item that is limited on a per-entity basis.
     */
    public static interface IEntityMatch {

        /**
         * An empty set means "all entities". To make an item invalid, return 0 weight.
         *
         * @return A set of all entities that this item can be applied to.
         */
        HolderSet<EntityType<?>> getEntities();

        public static <T extends IEntityMatch> Predicate<T> matches(EntityType<?> type) {
            return obj -> {
                var types = obj.getEntities();
                return types == null || types.size() == 0 || types.contains(BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type));
            };
        }

        public static <T extends IEntityMatch> Predicate<T> matches(Entity entity) {
            return matches(entity.getType());
        }
    }

}
