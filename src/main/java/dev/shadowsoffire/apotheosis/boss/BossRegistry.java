package dev.shadowsoffire.apotheosis.boss;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;

import dev.shadowsoffire.apotheosis.AdventureModule;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.tiers.Constraints;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.TieredDynamicRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

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
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(Apotheosis.loc("boss"), ApothBoss.CODEC);
    }

    @Nullable
    public ApothBoss getRandomItem(GenContext ctx) {
        return getRandomItem(ctx, Constraints.eval(ctx));
    }

}
