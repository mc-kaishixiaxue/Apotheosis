package dev.shadowsoffire.apotheosis.mobs;

import java.util.Arrays;
import java.util.UUID;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.Apoth.Attachments;
import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootController;
import dev.shadowsoffire.apotheosis.mobs.registries.AugmentRegistry;
import dev.shadowsoffire.apotheosis.mobs.registries.EliteRegistry;
import dev.shadowsoffire.apotheosis.mobs.registries.InvaderRegistry;
import dev.shadowsoffire.apotheosis.mobs.types.Augmentation;
import dev.shadowsoffire.apotheosis.mobs.types.Elite;
import dev.shadowsoffire.apotheosis.mobs.types.Invader;
import dev.shadowsoffire.apotheosis.mobs.util.SurfaceType;
import dev.shadowsoffire.apotheosis.mobs.util.SpawnCooldownSavedData;
import dev.shadowsoffire.apotheosis.net.BossSpawnPayload;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment.Target;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugmentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * This file contains Apotheosis's mob processing events.
 * <p>
 * These events execute the following steps in sequential order:
 * <ol>
 * <li>Checks if the mob spawn can be consumed to spawn an {@link Invader}, aborting the mob spawn.</li>
 * <li>Checks if a mob should be {@linkplain Augmentation augmented}, and attempts to run the augments.</li>
 * <li>Attempts to transform the entity into an {@link Elite}, cancelling future events if successful.</li>
 * <li>Attmpts to grant the entity a random affix item.</li>
 * </ol>
 */
public class ApothMobEvents {

    public static final String APOTH_MINIBOSS = "apoth.miniboss";
    public static final String APOTH_MINIBOSS_PLAYER = APOTH_MINIBOSS + ".player";

    protected SpawnCooldownSavedData cooldownData = new SpawnCooldownSavedData();

    @SubscribeEvent(priority = EventPriority.LOW)
    public void finalizeMobSpawns(FinalizeSpawnEvent e) {
        if (e.isCanceled() || e.isSpawnCancelled()) {
            return;
        }

        Player player = e.getLevel().getNearestPlayer(e.getX(), e.getY(), e.getZ(), -1, false);
        if (player == null) return; // Spawns require player context

        Mob mob = e.getEntity();
        RandomSource rand = e.getLevel().getRandom();
        GenContext ctx = GenContext.forPlayerAtPos(rand, player, mob.blockPosition());

        if (this.trySpawnInvader(e, mob, ctx, player)) {
            return;
        }

        this.tryAugmentations(e.getLevel(), mob, e.getSpawnType(), ctx);

        if (this.trySpawnElite(e, mob, ctx, player)) {
            return;
        }

        this.tryRandomAffixItems(e, mob, ctx);
    }

    private boolean trySpawnInvader(FinalizeSpawnEvent e, Mob mob, GenContext ctx, Player player) {
        // Invaders can only trigger off of natural spawns (chunk generation is considered "natural")
        if (e.getSpawnType() != MobSpawnType.NATURAL && e.getSpawnType() != MobSpawnType.CHUNK_GENERATION) {
            return false;
        }

        if (this.cooldownData.isOnCooldown(mob.level()) || !(mob instanceof Monster)) {
            return false;
        }

        ServerLevelAccessor sLevel = e.getLevel();
        ResourceLocation dimId = sLevel.getLevel().dimension().location();

        Pair<Float, SurfaceType> rules = AdventureConfig.BOSS_SPAWN_RULES.get(dimId);
        if (rules == null) {
            return false;
        }

        if (ctx.rand().nextFloat() <= rules.getLeft() && rules.getRight().test(sLevel, BlockPos.containing(e.getX(), e.getY(), e.getZ()))) {

            Invader item = InvaderRegistry.INSTANCE.getRandomItem(ctx);
            if (item == null) {
                Apotheosis.LOGGER.error("Attempted to spawn a boss in dimension {} using configured boss spawn rule {}/{} but no bosses were made available.", dimId, rules.getRight(), rules.getLeft());
                return false;
            }

            if (!item.basicData().canSpawn(mob, sLevel, e.getSpawnType())) {
                return false;
            }

            Mob boss = item.createBoss(sLevel, BlockPos.containing(e.getX() - 0.5, e.getY(), e.getZ() - 0.5), ctx);
            if (AdventureConfig.bossAutoAggro && !player.isCreative()) {
                boss.setTarget(player);
            }

            if (canSpawn(sLevel, boss, player.distanceToSqr(boss))) {
                sLevel.addFreshEntityWithPassengers(boss);
                e.setCanceled(true);
                e.setSpawnCancelled(true);
                Apotheosis.debugLog(boss.blockPosition(), "Surface Boss - " + boss.getName().getString());
                Component name = getName(boss);
                if (name == null || name.getStyle().getColor() == null) {
                    Apotheosis.LOGGER.warn("A Boss {} ({}) has spawned without a custom name!", boss.getName().getString(), EntityType.getKey(boss.getType()));
                }
                else {
                    sLevel.players().forEach(p -> {
                        Vec3 tPos = new Vec3(boss.getX(), AdventureConfig.bossAnnounceIgnoreY ? p.getY() : boss.getY(), boss.getZ());
                        if (p.distanceToSqr(tPos) <= AdventureConfig.bossAnnounceRange * AdventureConfig.bossAnnounceRange) {
                            ((ServerPlayer) p).connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("info.apotheosis.boss_spawn", name, (int) boss.getX(), (int) boss.getY())));
                            TextColor color = name.getStyle().getColor();
                            PacketDistributor.sendToPlayer((ServerPlayer) player, new BossSpawnPayload(boss.blockPosition(), color == null ? 0xFFFFFF : color.getValue()));
                        }
                    });
                }

                this.cooldownData.startCooldown(mob.level(), AdventureConfig.bossSpawnCooldown);
                return true;
            }
        }

        return false;
    }

    /**
     * Applies all active {@link TierAugment}s to the mob, then rolls the {@link AdventureConfig#augmentedMobChance} to apply {@link Augmentation}s.
     */
    private void tryAugmentations(ServerLevelAccessor level, Mob mob, MobSpawnType type, GenContext ctx) {
        for (TierAugment aug : TierAugmentRegistry.getAugments(ctx.tier(), Target.MONSTERS)) {
            aug.apply(level, mob);
        }
        mob.setData(Attachments.TIER_AUGMENTS_APPLIED, true);

        if (ctx.rand().nextFloat() <= AdventureConfig.augmentedMobChance) {
            for (Augmentation aug : AugmentRegistry.getAll()) {
                if (aug.canApply(level, mob, type, ctx)) {
                    aug.apply(mob, ctx);
                }
            }
        }
    }

    private boolean trySpawnElite(FinalizeSpawnEvent e, Mob mob, GenContext ctx, Player player) {
        ServerLevelAccessor sLevel = e.getLevel();

        Elite item = EliteRegistry.INSTANCE.getRandomItem(ctx, mob);
        if (item == null || !item.basicData().canSpawn(mob, sLevel, e.getSpawnType())) {
            return false;
        }

        if (ctx.rand().nextFloat() <= item.getChance()) {
            mob.getPersistentData().putString(Elite.MINIBOSS_KEY, EliteRegistry.INSTANCE.getKey(item).toString());
            mob.getPersistentData().putString(Elite.PLAYER_KEY, player.getUUID().toString());
            if (!item.basicData().finalizeSpawn()) {
                e.setCanceled(true);
            }
            return true;
        }

        return false;
    }

    private void tryRandomAffixItems(FinalizeSpawnEvent e, Mob mob, GenContext ctx) {
        if (e.getSpawnType() != MobSpawnType.NATURAL && e.getSpawnType() != MobSpawnType.CHUNK_GENERATION) {
            return;
        }

        if (ctx.rand().nextFloat() <= AdventureConfig.randomAffixItem && e.getEntity() instanceof Monster) {
            ItemStack affixItem = LootController.createRandomLootItem(ctx, null);
            if (affixItem.isEmpty()) {
                return;
            }

            affixItem.set(Components.FROM_MOB, true);
            LootCategory cat = LootCategory.forItem(affixItem);
            EquipmentSlot slot = Arrays.stream(EquipmentSlot.values()).filter(cat.getSlots()::test).findAny().orElse(EquipmentSlot.MAINHAND);
            e.getEntity().setItemSlot(slot, affixItem);
            e.getEntity().setGuaranteedDrop(slot);
        }
    }

    /**
     * The {@link FinalizeSpawnEvent} happens when the entity spawns, but is still before the mob is added to the world.
     * <p>
     * Since applying an {@link Elite} can have side effects that spawn mobs into the world, we need to delay the application
     * until the target entity joins the world.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void delayedEliteMobs(EntityJoinLevelEvent e) {
        if (!e.getLevel().isClientSide && e.getEntity() instanceof Mob mob) {
            CompoundTag data = mob.getPersistentData();
            if (data.contains(Elite.MINIBOSS_KEY) && data.contains(Elite.PLAYER_KEY)) {
                String key = data.getString(Elite.MINIBOSS_KEY);
                try {
                    UUID playerId = UUID.fromString(data.getString(Elite.PLAYER_KEY));
                    Player player = e.getLevel().getPlayerByUUID(playerId);
                    if (player == null) {
                        player = e.getLevel().getNearestPlayer(mob, -1);
                    }

                    if (player != null) {
                        GenContext ctx = GenContext.forPlayerAtPos(e.getLevel().random, player, mob.blockPosition());
                        Elite item = EliteRegistry.INSTANCE.getValue(ResourceLocation.tryParse(key));
                        if (item != null) {
                            item.transformMiniboss((ServerLevel) e.getLevel(), mob, ctx);
                        }
                    }
                }
                catch (Exception ex) {
                    Apotheosis.LOGGER.error("Failure while initializing the Apothic Elite " + key, ex);
                }
            }
        }
    }

    @SubscribeEvent
    public void tick(LevelTickEvent.Post e) {
        this.cooldownData.tick(e.getLevel().dimension().location());
    }

    @SubscribeEvent
    public void load(ServerStartedEvent e) {
        this.cooldownData = e.getServer().getLevel(Level.OVERWORLD).getDataStorage()
            .computeIfAbsent(new SavedData.Factory<>(SpawnCooldownSavedData::new, SpawnCooldownSavedData::loadTimes, null), "apotheosis_boss_times");
    }

    private static boolean canSpawn(LevelAccessor world, Mob entity, double playerDist) {
        if (playerDist > entity.getType().getCategory().getDespawnDistance() * entity.getType().getCategory().getDespawnDistance() && entity.removeWhenFarAway(playerDist)) {
            return false;
        }
        else {
            return entity.checkSpawnRules(world, MobSpawnType.NATURAL) && entity.checkSpawnObstruction(world);
        }
    }

    @Nullable
    private static Component getName(Mob boss) {
        return boss.getSelfAndPassengers().filter(e -> e.getPersistentData().contains(Invader.BOSS_KEY)).findFirst().map(Entity::getCustomName).orElse(null);
    }

}
