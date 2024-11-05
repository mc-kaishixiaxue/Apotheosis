package dev.shadowsoffire.apotheosis;

import dev.shadowsoffire.apotheosis.loot.LootRule;
import dev.shadowsoffire.apotheosis.net.BossSpawnPayload;
import dev.shadowsoffire.apotheosis.net.RadialStateChangePayload;
import dev.shadowsoffire.apotheosis.net.RerollResultPayload;
import dev.shadowsoffire.placebo.network.PayloadHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(Apotheosis.MODID)
public class Apotheosis {

    public static final String MODID = "apotheosis";

    public Apotheosis(IEventBus bus) {
        Apoth.bootstrap();
        bus.register(this);
    }

    @SubscribeEvent
    public void setup(FMLCommonSetupEvent e) {
        e.enqueueWork(() -> {
            LootRule.initCodecs();
        });
        PayloadHelper.registerPayload(new BossSpawnPayload.Provider());
        PayloadHelper.registerPayload(new RerollResultPayload.Provider());
        PayloadHelper.registerPayload(new RadialStateChangePayload.Provider());
    }

    /**
     * Constructs a resource location using the {@link Apotheosis#MODID} as the namespace.
     */
    public static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    /**
     * Constructs a mutable component with a lang key of the form "type.modid.path", using {@link Apotheosis#MODID}.
     * 
     * @param type The type of language key, "misc", "info", "title", etc...
     * @param path The path of the language key.
     * @param args Translation arguments passed to the created translatable component.
     */
    public static MutableComponent lang(String type, String path, Object... args) {
        return Component.translatable(type + "." + MODID + "." + path, args);
    }

    public static MutableComponent sysMessageHeader() {
        return Component.translatable("[%s] ", Component.literal("Apoth").withStyle(ChatFormatting.GOLD));
    }

}
