package dev.shadowsoffire.apotheosis.boss;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.apotheosis.AdventureModule;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.tiers.Constraints;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.TieredDynamicRegistry;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class MinibossRegistry extends TieredDynamicRegistry<ApothMiniboss> {

    public static final MinibossRegistry INSTANCE = new MinibossRegistry();

    public MinibossRegistry() {
        super(Apotheosis.LOGGER, "minibosses", false, false);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(Apotheosis.loc("miniboss"), ApothMiniboss.CODEC);
    }

    @Nullable
    public ApothMiniboss getRandomItem(GenContext ctx, Entity target) {
        return getRandomItem(ctx, Constraints.eval(ctx), IEntityMatch.matches(target));
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
