package dev.shadowsoffire.apotheosis;

import java.io.File;
import java.util.Locale;
import java.util.Map;

import dev.shadowsoffire.apotheosis.Apotheosis.ApotheosisReloadEvent;
import dev.shadowsoffire.apotheosis.client.AdventureModuleClient;
import dev.shadowsoffire.apotheosis.gen.BlacklistModifier;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.util.NameHelper;
import dev.shadowsoffire.placebo.config.Configuration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.RegisterEvent;

public class AdventureModule {

    @SubscribeEvent
    public void init(FMLCommonSetupEvent e) {
        this.reload(null);
        NeoForge.EVENT_BUS.addListener(this::reload);
    }

    @SubscribeEvent
    public void miscRegistration(RegisterEvent e) {
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
        if (DEBUG) Apotheosis.LOGGER.info("Generated a {} at {} {} {}", name, pos.getX(), pos.getY(), pos.getZ());
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
