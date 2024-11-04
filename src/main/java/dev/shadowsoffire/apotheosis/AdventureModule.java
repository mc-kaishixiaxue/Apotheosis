package dev.shadowsoffire.apotheosis;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.shadowsoffire.apotheosis.Apoth.Blocks;
import dev.shadowsoffire.apotheosis.Apoth.Items;
import dev.shadowsoffire.apotheosis.Apotheosis.ApotheosisConstruction;
import dev.shadowsoffire.apotheosis.Apotheosis.ApotheosisReloadEvent;
import dev.shadowsoffire.apotheosis.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.affix.UnnamingRecipe;
import dev.shadowsoffire.apotheosis.affix.reforging.ReforgingRecipe;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingRecipe;
import dev.shadowsoffire.apotheosis.boss.BossEvents;
import dev.shadowsoffire.apotheosis.boss.BossRegistry;
import dev.shadowsoffire.apotheosis.boss.Exclusion;
import dev.shadowsoffire.apotheosis.boss.MinibossRegistry;
import dev.shadowsoffire.apotheosis.client.AdventureModuleClient;
import dev.shadowsoffire.apotheosis.compat.AdventureTOPPlugin;
import dev.shadowsoffire.apotheosis.compat.AdventureTwilightCompat;
import dev.shadowsoffire.apotheosis.compat.GatewaysCompat;
import dev.shadowsoffire.apotheosis.gen.BlacklistModifier;
import dev.shadowsoffire.apotheosis.loot.AffixConvertLootModifier;
import dev.shadowsoffire.apotheosis.loot.AffixHookLootModifier;
import dev.shadowsoffire.apotheosis.loot.AffixLootModifier;
import dev.shadowsoffire.apotheosis.loot.AffixLootPoolEntry;
import dev.shadowsoffire.apotheosis.loot.AffixLootRegistry;
import dev.shadowsoffire.apotheosis.loot.GemLootModifier;
import dev.shadowsoffire.apotheosis.loot.GemLootPoolEntry;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.socket.AddSocketsRecipe;
import dev.shadowsoffire.apotheosis.socket.SocketingRecipe;
import dev.shadowsoffire.apotheosis.socket.WithdrawalRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.apotheosis.spawner.RogueSpawnerRegistry;
import dev.shadowsoffire.apotheosis.util.AffixItemIngredient;
import dev.shadowsoffire.apotheosis.util.GemIngredient;
import dev.shadowsoffire.apotheosis.util.NameHelper;
import dev.shadowsoffire.placebo.config.Configuration;
import dev.shadowsoffire.placebo.loot.LootSystem;
import dev.shadowsoffire.placebo.registry.RegistryEvent.Register;
import dev.shadowsoffire.placebo.tabs.TabFillingRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.crafting.CraftingHelper;
import net.neoforged.neoforge.registries.RegisterEvent;

public class AdventureModule {

    public static final Logger LOGGER = LogManager.getLogger("Apotheosis : Adventure");
    public static final boolean STAGES_LOADED = ModList.get().isLoaded("gamestages");
    static final Map<ResourceLocation, LootCategory> IMC_TYPE_OVERRIDES = new HashMap<>();

    public AdventureModule() {
        Adventure.bootstrap();
    }

    @SubscribeEvent
    public void preInit(ApotheosisConstruction e) {
        ObfuscationReflectionHelper.setPrivateValue(RangedAttribute.class, (RangedAttribute) Attributes.ARMOR, 200D, "f_22308_");
        ObfuscationReflectionHelper.setPrivateValue(RangedAttribute.class, (RangedAttribute) Attributes.ARMOR_TOUGHNESS, 100D, "f_22308_");
    }

    @SubscribeEvent
    public void init(FMLCommonSetupEvent e) {
        this.reload(null);
        NeoForge.EVENT_BUS.register(new AdventureEvents());
        NeoForge.EVENT_BUS.register(new BossEvents());
        NeoForge.EVENT_BUS.addListener(this::reload);
        RarityRegistry.INSTANCE.registerToBus();
        AffixRegistry.INSTANCE.registerToBus();
        GemRegistry.INSTANCE.registerToBus();
        AffixLootRegistry.INSTANCE.registerToBus();
        BossRegistry.INSTANCE.registerToBus();
        RogueSpawnerRegistry.INSTANCE.registerToBus();
        MinibossRegistry.INSTANCE.registerToBus();
        Apotheosis.HELPER.registerProvider(f -> {
            f.addRecipe(new SocketingRecipe());
            f.addRecipe(new WithdrawalRecipe());
            f.addRecipe(new UnnamingRecipe());
        });
        e.enqueueWork(() -> {
            if (ModList.get().isLoaded("gateways")) GatewaysCompat.register();
            if (ModList.get().isLoaded("theoneprobe")) AdventureTOPPlugin.register();
            if (ModList.get().isLoaded("twilightforest")) AdventureTwilightCompat.register();
            LootSystem.defaultBlockTable(Blocks.SIMPLE_REFORGING_TABLE.get());
            LootSystem.defaultBlockTable(Blocks.REFORGING_TABLE.get());
            LootSystem.defaultBlockTable(Blocks.SALVAGING_TABLE.get());
            LootSystem.defaultBlockTable(Blocks.GEM_CUTTING_TABLE.get());
            LootSystem.defaultBlockTable(Blocks.AUGMENTING_TABLE.get());
            Registry.register(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, Apotheosis.loc("random_affix_item"), AffixLootPoolEntry.TYPE);
            Registry.register(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, Apotheosis.loc("random_gem"), GemLootPoolEntry.TYPE);
            Exclusion.initSerializers();
            GemBonus.initCodecs();
            CraftingHelper.register(Apotheosis.loc("affix_item"), AffixItemIngredient.Serializer.INSTANCE);
            CraftingHelper.register(Apotheosis.loc("gem"), GemIngredient.Serializer.INSTANCE);

            TabFillingRegistry.register(Adventure.Tabs.ADVENTURE.getKey(), Items.COMMON_MATERIAL, Items.UNCOMMON_MATERIAL, Items.RARE_MATERIAL, Items.EPIC_MATERIAL, Items.MYTHIC_MATERIAL, Items.GEM_DUST,
                Items.GEM_FUSED_SLATE, Items.SIGIL_OF_SOCKETING, Items.SIGIL_OF_WITHDRAWAL, Items.SIGIL_OF_REBIRTH, Items.SIGIL_OF_ENHANCEMENT, Items.SIGIL_OF_UNNAMING, Items.BOSS_SUMMONER,
                Items.SALVAGING_TABLE, Items.GEM_CUTTING_TABLE, Items.SIMPLE_REFORGING_TABLE, Items.REFORGING_TABLE, Items.AUGMENTING_TABLE);
            TabFillingRegistry.register(Adventure.Tabs.ADVENTURE.getKey(), Items.GEM);
        });
    }

    @SubscribeEvent
    public void serializers(Register<RecipeSerializer<?>> e) {
        e.getRegistry().register(SocketingRecipe.Serializer.INSTANCE, "socketing");
        e.getRegistry().register(WithdrawalRecipe.Serializer.INSTANCE, "widthdrawal");
        e.getRegistry().register(UnnamingRecipe.Serializer.INSTANCE, "unnaming");
        e.getRegistry().register(AddSocketsRecipe.Serializer.INSTANCE, "add_sockets");
        e.getRegistry().register(SalvagingRecipe.Serializer.INSTANCE, "salvaging");
        e.getRegistry().register(ReforgingRecipe.Serializer.INSTANCE, "reforging");
    }

    @SubscribeEvent
    public void miscRegistration(RegisterEvent e) {
        if (e.getForgeRegistry() == (Object) ForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS.get()) {
            e.getForgeRegistry().register("gems", GemLootModifier.CODEC);
            e.getForgeRegistry().register("affix_loot", AffixLootModifier.CODEC);
            e.getForgeRegistry().register("affix_conversion", AffixConvertLootModifier.CODEC);
            e.getForgeRegistry().register("affix_hook", AffixHookLootModifier.CODEC);
        }
        if (e.getForgeRegistry() == (Object) ForgeRegistries.BIOME_MODIFIER_SERIALIZERS.get()) {
            e.getForgeRegistry().register("blacklist", BlacklistModifier.CODEC);
        }
    }

    @SubscribeEvent
    public void client(FMLClientSetupEvent e) {
        e.enqueueWork(AdventureModuleClient::init);
        FMLJavaModLoadingContext.get().getModEventBus().register(new AdventureModuleClient());
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
                        IMC_TYPE_OVERRIDES.put(item, cat);
                        AdventureModule.LOGGER.info("Mod {} has overriden the loot category of {} to {}.", msg.senderModId(), item, cat.getName());
                        break;
                    }
                    catch (Exception ex) {
                        AdventureModule.LOGGER.error(ex.getMessage());
                        ex.printStackTrace();
                    }
                }
                default -> {
                    AdventureModule.LOGGER.error("Unknown or invalid IMC Message: {}", msg);
                }
            }
        });
    }

    /**
     * Loads all configurable data for the deadly module.
     */
    public void reload(ApotheosisReloadEvent e) {
        Configuration mainConfig = new Configuration(new File(Apotheosis.configDir, "adventure.cfg"));
        Configuration nameConfig = new Configuration(new File(Apotheosis.configDir, "names.cfg"));
        AdventureConfig.load(mainConfig);
        NameHelper.load(nameConfig);
        if (e == null && mainConfig.hasChanged()) mainConfig.save();
        if (e == null && nameConfig.hasChanged()) nameConfig.save();
    }

    public static final boolean DEBUG = false;

    public static void debugLog(BlockPos pos, String name) {
        if (DEBUG) AdventureModule.LOGGER.info("Generated a {} at {} {} {}", name, pos.getX(), pos.getY(), pos.getZ());
    }

    public static class ApothSmithingRecipe extends SmithingTransformRecipe {

        public static final int TEMPLATE = 0, BASE = 1, ADDITION = 2;

        public ApothSmithingRecipe(Ingredient pBase, Ingredient pAddition, ItemStack pResult) {
            super(Ingredient.EMPTY, pBase, pAddition, pResult);
        }

        @Override
        public boolean isBaseIngredient(ItemStack pStack) {
            return !LootCategory.forItem(pStack).isNone();
        }
    }

}
