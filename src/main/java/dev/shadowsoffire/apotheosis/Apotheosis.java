package dev.shadowsoffire.apotheosis;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.shadowsoffire.apotheosis.Apoth.Items;
import dev.shadowsoffire.apotheosis.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.boss.BossEvents;
import dev.shadowsoffire.apotheosis.boss.BossRegistry;
import dev.shadowsoffire.apotheosis.boss.Exclusion;
import dev.shadowsoffire.apotheosis.boss.MinibossRegistry;
import dev.shadowsoffire.apotheosis.compat.AdventureTwilightCompat;
import dev.shadowsoffire.apotheosis.compat.GatewaysCompat;
import dev.shadowsoffire.apotheosis.data.AffixLootEntryProvider;
import dev.shadowsoffire.apotheosis.data.AffixProvider;
import dev.shadowsoffire.apotheosis.data.ApothLootProvider;
import dev.shadowsoffire.apotheosis.data.ApothRecipeProvider;
import dev.shadowsoffire.apotheosis.data.ApothTagsProvider;
import dev.shadowsoffire.apotheosis.data.RarityProvider;
import dev.shadowsoffire.apotheosis.loot.AffixLootRegistry;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRule;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.net.BossSpawnPayload;
import dev.shadowsoffire.apotheosis.net.RadialStateChangePayload;
import dev.shadowsoffire.apotheosis.net.RerollResultPayload;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.apotheosis.spawner.RogueSpawnerRegistry;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apotheosis.util.NameHelper;
import dev.shadowsoffire.apothic_attributes.ApothicAttributes;
import dev.shadowsoffire.placebo.config.Configuration;
import dev.shadowsoffire.placebo.datagen.DataGenBuilder;
import dev.shadowsoffire.placebo.network.PayloadHelper;
import dev.shadowsoffire.placebo.tabs.TabFillingRegistry;
import dev.shadowsoffire.placebo.util.RunnableReloader;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@Mod(Apotheosis.MODID)
public class Apotheosis {

    public static final String MODID = "apotheosis";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static final boolean DEBUG_WORLDGEN = "on".equalsIgnoreCase(System.getenv("apotheosis.debug_worldgen"));

    public static final boolean STAGES_LOADED = ModList.get().isLoaded("gamestages");
    static final Map<ResourceLocation, LootCategory> IMC_TYPE_OVERRIDES = new HashMap<>();

    public Apotheosis(IEventBus bus) {
        Apoth.bootstrap(bus);
        bus.register(this);
        ObfuscationReflectionHelper.setPrivateValue(RangedAttribute.class, (RangedAttribute) Attributes.ARMOR.value(), 200D, "minValue");
        ObfuscationReflectionHelper.setPrivateValue(RangedAttribute.class, (RangedAttribute) Attributes.ARMOR_TOUGHNESS.value(), 100D, "maxValue");
        LootRule.initCodecs();
        Exclusion.initCodecs();
        GemBonus.initCodecs();
        if (ModList.get().isLoaded("gateways")) {
            GatewaysCompat.register();
        }
        if (ModList.get().isLoaded("twilightforest")) {
            AdventureTwilightCompat.register();
        }
    }

    @SubscribeEvent
    public void setup(FMLCommonSetupEvent e) {
        e.enqueueWork(() -> {
            TabFillingRegistry.register(Apoth.Tabs.ADVENTURE.getKey(), Items.COMMON_MATERIAL, Items.UNCOMMON_MATERIAL, Items.RARE_MATERIAL, Items.EPIC_MATERIAL, Items.MYTHIC_MATERIAL, Items.GEM_DUST,
                Items.GEM_FUSED_SLATE, Items.SIGIL_OF_SOCKETING, Items.SIGIL_OF_WITHDRAWAL, Items.SIGIL_OF_REBIRTH, Items.SIGIL_OF_ENHANCEMENT, Items.SIGIL_OF_UNNAMING, Items.BOSS_SUMMONER,
                Items.SALVAGING_TABLE, Items.GEM_CUTTING_TABLE, Items.SIMPLE_REFORGING_TABLE, Items.REFORGING_TABLE, Items.AUGMENTING_TABLE, Items.GEM);
        });
        PayloadHelper.registerPayload(new BossSpawnPayload.Provider());
        PayloadHelper.registerPayload(new RerollResultPayload.Provider());
        PayloadHelper.registerPayload(new RadialStateChangePayload.Provider());
        NeoForge.EVENT_BUS.register(new AdventureEvents());
        NeoForge.EVENT_BUS.register(new BossEvents());
        RarityRegistry.INSTANCE.registerToBus();
        AffixRegistry.INSTANCE.registerToBus();
        GemRegistry.INSTANCE.registerToBus();
        AffixLootRegistry.INSTANCE.registerToBus();
        BossRegistry.INSTANCE.registerToBus();
        RogueSpawnerRegistry.INSTANCE.registerToBus();
        MinibossRegistry.INSTANCE.registerToBus();
        loadConfig(true);
        NeoForge.EVENT_BUS.addListener(AddReloadListenerEvent.class, event -> event.addListener(RunnableReloader.of(() -> loadConfig(false))));
    }

    @SubscribeEvent
    public void data(GatherDataEvent e) {
        DataGenBuilder.create(Apotheosis.MODID)
            .provider(ApothLootProvider::create)
            .provider(ApothRecipeProvider::new)
            .provider(ApothTagsProvider::new)
            .provider(RarityProvider::new)
            .provider(AffixLootEntryProvider::new)
            .provider(AffixProvider::new)
            .build(e);

        /*
         * TODO: Loot Modifier Provider, with the following entries:
         * 1. Affix conversion modifier
         * 2. Affix hook modifier (no config)
         * 3. Gem Loot Modifier (chest config)
         * 4. Gem Loot Modifier (player-only condition, entity config)
         * 5. Affix loot modifier (chest config)
         */

        Object2IntOpenHashMap<String> map = (Object2IntOpenHashMap<String>) DataProvider.FIXED_ORDER_FIELDS;
        // Keep enums in ordinal order
        for (Purity p : Purity.values()) {
            map.put(p.getSerializedName(), p.ordinal() + 1);
        }
        for (WorldTier t : WorldTier.values()) {
            map.put(t.getSerializedName(), t.ordinal() + 1);
        }

        // Place min/max in order
        map.put("min", 1);
        map.put("max", 2);

        // Rarity values
        map.put("apotheosis:common", 1);
        map.put("apotheosis:uncommon", 2);
        map.put("apotheosis:rare", 3);
        map.put("apotheosis:epic", 4);
        map.put("apotheosis:mythic", 5);

    }

    @SubscribeEvent
    @SuppressWarnings({ "unchecked", "deprecation" })
    public void imc(InterModProcessEvent e) {
        e.getIMCStream().forEach(msg -> {
            switch (msg.method().toLowerCase(Locale.ROOT)) {
                // Payload: Map.Entry<Item, String> where the string is a LootCategory ID.
                case "loot_category_override" -> {
                    try {
                        var categoryOverride = (Map.Entry<Item, String>) msg.messageSupplier().get();
                        ResourceLocation item = BuiltInRegistries.ITEM.getKey(categoryOverride.getKey());
                        LootCategory cat = LootCategory.byId(categoryOverride.getValue());
                        if (cat == null) throw new NullPointerException("Invalid loot category ID: " + categoryOverride.getValue());
                        Apotheosis.IMC_TYPE_OVERRIDES.put(item, cat);
                        Apotheosis.LOGGER.info("Mod {} has overriden the loot category of {} to {}.", msg.senderModId(), item, cat.getName());
                        break;
                    }
                    catch (Exception ex) {
                        Apotheosis.LOGGER.error(ex.getMessage());
                        ex.printStackTrace();
                    }
                }
                default -> {
                    Apotheosis.LOGGER.error("Unknown or invalid IMC Message: {}", msg);
                }
            }
        });
    }

    public static void loadConfig(boolean firstLoad) {
        Configuration mainConfig = new Configuration(ApothicAttributes.getConfigFile(MODID));
        Configuration nameConfig = new Configuration(ApothicAttributes.getConfigFile("name_generation"));
        AdventureConfig.load(mainConfig);
        NameHelper.load(nameConfig);
        if (firstLoad && mainConfig.hasChanged()) mainConfig.save();
        if (firstLoad && nameConfig.hasChanged()) nameConfig.save();
        // TODO: Sync what needs to sync
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

    public static void debugLog(BlockPos pos, String name) {
        if (DEBUG_WORLDGEN) {
            Apotheosis.LOGGER.info("Generated a {} at {} {} {}", name, pos.getX(), pos.getY(), pos.getZ());
        }
    }

}
