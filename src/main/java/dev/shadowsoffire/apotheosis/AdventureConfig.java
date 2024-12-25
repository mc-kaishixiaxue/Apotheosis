package dev.shadowsoffire.apotheosis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.mobs.util.BossSpawnRules;
import dev.shadowsoffire.placebo.config.Configuration;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;

public class AdventureConfig {

    public static final List<ResourceLocation> DIM_WHITELIST = new ArrayList<>();
    public static final Map<ResourceLocation, LootCategory> TYPE_OVERRIDES = new HashMap<>(); // needs sync
    public static final Map<ResourceLocation, Pair<Float, BossSpawnRules>> BOSS_SPAWN_RULES = new HashMap<>();

    public static float augmentedMobChance = 0.075F;

    // Boss Stats
    public static boolean curseBossItems = false;
    public static float bossAnnounceRange = 96;
    public static float bossAnnounceVolume = 0.75F;
    public static boolean bossAnnounceIgnoreY = false;
    public static int bossSpawnCooldown = 3600;
    public static boolean bossAutoAggro = false;
    public static boolean bossGlowOnSpawn = true;

    // Generation
    public static float spawnerValueChance = 0.11F;

    // Affix
    public static float randomAffixItem = 0.075F;
    public static boolean disableQuarkOnAffixItems = true; // needs sync - maybe client only?
    public static Supplier<Item> torchItem = () -> Items.TORCH; // needs sync
    public static boolean cleaveHitsPlayers = false; // needs sync

    // Wandering Trader
    public static boolean undergroundTrader = true;

    public static boolean charmsInCuriosOnly = false;

    public static void load(Configuration c) {
        c.setTitle("Apotheosis Adventure Module Config");

        TYPE_OVERRIDES.clear();
        TYPE_OVERRIDES.putAll(Apotheosis.IMC_TYPE_OVERRIDES);
        String[] overrides = c.getStringList("Equipment Type Overrides", "affixes", new String[] { "minecraft:iron_sword|melee_weapon", "minecraft:shulker_shell|none" },
            "A list of type overrides for the affix loot system.  Format is <itemname>|chance|<type>.\nValid types are: none, sword, trident, shield, heavy_weapon, pickaxe, shovel, crossbow, bow");
        for (String s : overrides) {
            String[] split = s.split("\\|");
            try {
                LootCategory type = LootCategory.byId(split[1].toLowerCase(Locale.ROOT));
                if (type.isArmor()) throw new UnsupportedOperationException("Cannot override an item to an armor type.");
                TYPE_OVERRIDES.put(ResourceLocation.parse(split[0]), type);
            }
            catch (Exception e) {
                Apotheosis.LOGGER.error("Invalid type override entry: " + s + " will be ignored!");
                e.printStackTrace();
            }
        }

        randomAffixItem = c.getFloat("Random Affix Chance", "affixes", randomAffixItem, 0, 1, "The chance that a naturally spawned mob will be granted an affix item. 0 = 0%, 1 = 100%");
        cleaveHitsPlayers = c.getBoolean("Cleave Players", "affixes", cleaveHitsPlayers, "If affixes that cleave can hit players (excluding the user).");

        disableQuarkOnAffixItems = c.getBoolean("Disable Quark Tooltips for Affix Items", "affixes", true, "If Quark's Attribute Tooltip handling is disabled for affix items");

        String torch = c.getString("Torch Placement Item", "affixes", "minecraft:torch",
            "The item that will be used when attempting to place torches with the torch placer affix.  Must be a valid item that places a block on right click.");
        torchItem = () -> {
            try {
                Item i = BuiltInRegistries.ITEM.get(ResourceLocation.parse(torch));
                return i == Items.AIR ? Items.TORCH : i;
            }
            catch (Exception ex) {
                Apotheosis.LOGGER.error("Invalid torch item {}", torch);
                return Items.TORCH;
            }
        };

        curseBossItems = c.getBoolean("Curse Boss Items", "bosses", curseBossItems, "If boss items are always cursed.  Enable this if you want bosses to be less overpowered by always giving them a negative effect.");
        bossAnnounceRange = c.getFloat("Boss Announce Range", "bosses", bossAnnounceRange, 0, 1024,
            "The range at which boss spawns will be announced.  If you are closer than this number of blocks (ignoring y-level), you will receive the announcement.");
        bossAnnounceVolume = c.getFloat("Boss Announce Volume", "bosses", bossAnnounceVolume, 0, 1, "The volume of the boss announcement sound. 0 to disable. This control is clientside.");
        bossAnnounceIgnoreY = c.getBoolean("Boss Announce Ignore Y", "bosses", bossAnnounceIgnoreY, "If the boss announcement range ignores y-level.");
        bossSpawnCooldown = c.getInt("Boss Spawn Cooldown", "bosses", bossSpawnCooldown, 0, 720000, "The time, in ticks, that must pass between any two natural boss spawns in a single dimension.");
        bossAutoAggro = c.getBoolean("Boss Auto-Aggro", "bosses", bossAutoAggro, "If true, invading bosses will automatically target the closest player.");
        bossGlowOnSpawn = c.getBoolean("Boss Glowing On Spawn", "bosses", bossGlowOnSpawn, "If true, bosses will glow when they spawn.");

        String[] dims = c.getStringList("Boss Spawn Dimensions", "bosses",
            new String[] {
                "minecraft:overworld|0.018|NEEDS_SKY",
                "minecraft:the_nether|0.025|ANY",
                "minecraft:the_end|0.018|SURFACE_OUTER_END",
                "twilightforest:twilight_forest|0.05|NEEDS_SURFACE"
            },
            "Dimensions where bosses can spawn naturally, spawn chance, and spawn rules.\nFormat is dimname|chance|rule, chance is a float from 0..1."
                + "\nValid rules are visible here https://github.com/Shadows-of-Fire/Apotheosis/blob/1.19/src/main/java/shadows/apotheosis/adventure/boss/BossEvents.java#L174C27-L174C27");

        BOSS_SPAWN_RULES.clear();
        for (String s : dims) {
            try {
                String[] split = s.split("\\|");
                BOSS_SPAWN_RULES.put(ResourceLocation.parse(split[0]), Pair.of(Float.parseFloat(split[1]), BossSpawnRules.valueOf(split[2].toUpperCase(Locale.ROOT))));
            }
            catch (Exception e) {
                Apotheosis.LOGGER.error("Invalid boss spawn rules: " + s + " will be ignored");
                e.printStackTrace();
            }
        }

        dims = c.getStringList("Generation Dimension Whitelist", "worldgen", new String[] { "overworld" }, "The dimensions that the deadly module will generate in.");
        DIM_WHITELIST.clear();
        for (String s : dims) {
            try {
                DIM_WHITELIST.add(ResourceLocation.parse(s.trim()));
            }
            catch (ResourceLocationException e) {
                Apotheosis.LOGGER.error("Invalid dim whitelist entry: " + s + " will be ignored");
            }
        }

        spawnerValueChance = c.getFloat("Spawner Value Chance", "spawners", spawnerValueChance, 0, 1, "The chance that a Rogue Spawner has a \"valuable\" chest instead of a standard one. 0 = 0%, 1 = 100%");

        undergroundTrader = c.getBoolean("Underground Trader", "wanderer", undergroundTrader, "If the Wandering Trader can attempt to spawn underground.\nServer-authoritative.");
    }

    public static boolean canGenerateIn(WorldGenLevel world) {
        ResourceKey<Level> key = world.getLevel().dimension();
        return DIM_WHITELIST.contains(key.location());
    }

}
