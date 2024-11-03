package dev.shadowsoffire.apotheosis.boss;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;

import dev.shadowsoffire.apotheosis.AdventureModule;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.tiers.Constraints;
import dev.shadowsoffire.apotheosis.tiers.TieredDynamicRegistry;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;

public class BossRegistry extends TieredDynamicRegistry<ApothBoss> {

    public static final BossRegistry INSTANCE = new BossRegistry();

    public BossRegistry() {
        super(AdventureModule.LOGGER, "bosses", false, false);
    }

    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        var map = super.prepare(pResourceManager, pProfiler);
        // The author of Brutal Bosses continues to use my subkey, so, here we go doing stupid shit to work around it.
        map.keySet().removeIf(r -> "brutalbosses".equals(r.getNamespace()));
        return map;
    }

    @Override
    protected void validateItem(ResourceLocation key, ApothBoss item) {
        super.validateItem(key, item);
        item.validate(key);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(Apotheosis.loc("boss"), ApothBoss.CODEC);
    }

    @Nullable
    public ApothBoss getRandomItem(RandomSource rand, Player player) {
        return getRandomItem(rand, WorldTier.getTier(player), player.getLuck(), Constraints.eval(player));
    }

}
