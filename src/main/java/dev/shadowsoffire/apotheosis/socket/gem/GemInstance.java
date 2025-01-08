package dev.shadowsoffire.apotheosis.socket.gem;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;

/**
 * A Gem Instance is a live copy of a Gem with all context needed to call Gem methods.
 * <p>
 * This is the Gem counterpart of {@link AffixInstance}.
 * <p>
 * The major difference between them is that most methods do not live on {@link Gem} but rather on {@link GemBonus}.
 *
 * @param gem      The socketed Gem.
 * @param category The LootCategory of the item the Gem is socketed into.
 * @param purity   The purity of the socketed Gem.
 * @param gemStack The itemstack form of the sockted Gem.
 * @param slot     The slot index of this gem in the socketed parent item.
 */
public record GemInstance(DynamicHolder<Gem> gem, LootCategory category, Purity purity, ItemStack gemStack, int slot) implements GemView {

    public static GemInstance EMPTY = new GemInstance(GemRegistry.INSTANCE.emptyHolder(), LootCategory.NONE, Purity.CHIPPED, ItemStack.EMPTY, -1);

    /**
     * Creates a {@link GemInstance} for a socketed gem.
     *
     * @param socketed The item the gem is socketed in.
     * @param gemStack The stack representing the gem.
     */
    public static GemInstance socketed(ItemStack socketed, ItemStack gemStack, int slot) {
        return socketed(LootCategory.forItem(socketed), gemStack, slot);
    }

    /**
     * Creates a {@link GemInstance} for a socketed gem.
     *
     * @param category The category of the object the gem is socketed in.
     * @param gemStack The stack representing the gem.
     */
    public static GemInstance socketed(LootCategory category, ItemStack gemStack, int slot) {
        DynamicHolder<Gem> gem = GemItem.getGem(gemStack);
        Purity purity = GemItem.getPurity(gemStack);

        if (gem.isBound()) {
            purity = Purity.max(gem.get().getMinPurity(), purity);
        }

        return new GemInstance(gem, category, purity, gemStack, slot);
    }

    /**
     * Creates a {@link GemInstance} with {@link LootCategory#NONE} and an unknown slot index (-1).
     * This instance will be unable to invoke bonus methods, but may be used to easily retrieve the gem properties.
     * 
     * @deprecated See {@link UnsocketedGem}.
     */
    @Deprecated(forRemoval = true, since = "8.1.0")
    public static GemInstance unsocketed(ItemStack gemStack) {
        return socketed(LootCategory.NONE, gemStack, -1);
    }

    /**
     * @deprecated See {@link UnsocketedGem}.
     */
    @Deprecated(forRemoval = true, since = "8.1.0")
    public boolean equalsUnsocketed(GemInstance other) {
        return this.isValid() && this.gem.equals(other.gem) && this.purity == other.purity;
    }

    /**
     * Checks if the underlying {@link #gem} is bound, but does not validate that the {@link #category} is correct.
     * <p>
     * This should only be used in conjunction with {@link #unsocketed(ItemStack)}. Otherwise, use {@link #isValid()}.
     * 
     * @deprecated See {@link UnsocketedGem}.
     */
    @Deprecated(forRemoval = true, since = "8.1.0")
    public boolean isValidUnsocketed() {
        return this.gem.isBound();
    }

    /**
     * If this gem instance {@linkplain #isValidUnsocketed() is valid}, returns the stored {@link Gem}.
     *
     * @throws NullPointerException if this gem instance is invalid.
     */
    public Gem getGem() {
        return this.gem.get();
    }

    /**
     * Checks if the gem and rarity are not null, and there is a valid bonus for the socketed category.
     */
    public boolean isValid() {
        return this.gem.isBound() && this.getGem().getBonus(this.category, this.purity).isPresent() && this.slot != -1;
    }

    /**
     * Checks if this gem is a {@link Purity#PERFECT perfect} gem, which can no longer be upgraded.
     */
    public boolean isPerfect() {
        return this.purity == Purity.PERFECT;
    }

    /**
     * @see Gem#addInformation(GemInstance, Consumer, AttributeTooltipContext)
     */
    public void addInformation(Consumer<Component> list, AttributeTooltipContext ctx) {
        this.getGem().addInformation(this, list, ctx);
    }

    /**
     * @see Gem#canApplyTo(ItemStack, ItemStack, LootRarity)
     */
    public boolean canApplyTo(ItemStack stack) {
        return this.gem.get().canApplyTo(stack, this.gemStack, this.purity);
    }

    /**
     * @see GemBonus#addModifiers(ItemStack, LootRarity, BiConsumer)
     */
    public void addModifiers(ItemAttributeModifierEvent event) {
        this.ifPresent(b -> b.addModifiers(this, event));
    }

    /**
     * @see GemBonus#getSocketBonusTooltip(ItemStack, LootRarity)
     */
    public Component getSocketBonusTooltip(AttributeTooltipContext ctx) {
        return this.map(b -> b.getSocketBonusTooltip(this, ctx)).orElse(Component.literal("Invalid Gem Category"));
    }

    /**
     * @see GemBonus#getDamageProtection(ItemStack, LootRarity, DamageSource)
     */
    public float getDamageProtection(DamageSource source) {
        return this.map(b -> b.getDamageProtection(this, source)).orElse(0F);
    }

    /**
     * @see GemBonus#getDamageBonus(ItemStack, LootRarity, MobType)
     */
    public float getDamageBonus(Entity target) {
        return this.map(b -> b.getDamageBonus(this, target)).orElse(0F);
    }

    /**
     * @see GemBonus#doPostAttack(ItemStack, LootRarity, LivingEntity, Entity)
     */
    public void doPostAttack(LivingEntity user, @Nullable Entity target) {
        this.ifPresent(b -> b.doPostAttack(this, user, target));
    }

    /**
     * @see GemBonus#doPostHurt(ItemStack, LootRarity, LivingEntity, Entity)
     */
    public void doPostHurt(LivingEntity user, DamageSource source) {
        this.ifPresent(b -> b.doPostHurt(this, user, source));
    }

    /**
     * @see GemBonus#onArrowFired(ItemStack, LootRarity, LivingEntity, AbstractArrow)
     */
    public void onArrowFired(LivingEntity user, AbstractArrow arrow) {
        this.ifPresent(b -> b.onArrowFired(this, user, arrow));
    }

    /**
     * @see GemBonus#onItemUse(ItemStack, LootRarity, UseOnContext)
     */
    @Nullable
    public InteractionResult onItemUse(UseOnContext ctx) {
        return this.map(b -> b.onItemUse(this, ctx)).orElse(null);
    }

    /**
     * @see {@link GemBonus#onArrowImpact(AbstractArrow, LootRarity, HitResult, HitResult.Type)}
     */
    public void onArrowImpact(AbstractArrow arrow, HitResult res) {
        this.ifPresent(b -> b.onArrowImpact(this, arrow, res));
    }

    /**
     * @see GemBonus#onShieldBlock(ItemStack, LootRarity, LivingEntity, DamageSource, float)
     */
    public float onShieldBlock(LivingEntity entity, DamageSource source, float amount) {
        return this.map(b -> b.onShieldBlock(this, entity, source, amount)).orElse(amount);
    }

    /**
     * @see GemBonus#onBlockBreak(ItemStack, LootRarity, Player, LevelAccessor, BlockPos, BlockState)
     */
    public void onBlockBreak(Player player, LevelAccessor world, BlockPos pos, BlockState state) {
        this.ifPresent(b -> b.onBlockBreak(this, player, world, pos, state));
    }

    /**
     * @see GemBonus#getDurabilityBonusPercentage(ItemStack, LootRarity, ServerPlayer)
     */
    public float getDurabilityBonusPercentage() {
        return this.map(b -> b.getDurabilityBonusPercentage(this)).orElse(0F);
    }

    /**
     * @see GemBonus#onHurt(ItemStack, LootRarity, DamageSource, LivingEntity, float)
     */
    public float onHurt(DamageSource src, LivingEntity ent, float amount) {
        return this.map(b -> b.onHurt(this, src, ent, amount)).orElse(amount);
    }

    /**
     * @see GemBonus#getEnchantmentLevels(ItemStack, LootRarity, Map)
     */
    public void getEnchantmentLevels(GetEnchantmentLevelEvent event) {
        this.ifPresent(b -> b.getEnchantmentLevels(this, event));
    }

    /**
     * @see GemBonus#modifyLoot(ItemStack, LootRarity, ObjectArrayList, LootContext)
     */
    public void modifyLoot(ObjectArrayList<ItemStack> loot, LootContext ctx) {
        this.ifPresent(b -> b.modifyLoot(this, loot, ctx));
    }

    /**
     * @see GemBonus#skipModifierIds(GemInstance, Consumer)
     */
    public void skipModifierIds(Consumer<ResourceLocation> skip) {
        this.ifPresent(b -> b.skipModifierIds(this, skip));
    }

    /**
     * Resolves a gem bonus using {@link Optional#map(Function)}.
     *
     * @throws UnsupportedOperationException if this instance is not {@link #isValid()}.
     */
    private <T> Optional<T> map(Function<GemBonus, T> function) {
        return this.gem.get().getBonus(this.category, this.purity).map(function);
    }

    /**
     * Resolves a gem bonus using {@link Optional#ifPresent(Consumer)}.
     *
     * @throws UnsupportedOperationException if this instance is not {@link #isValid()}.
     */
    private void ifPresent(Consumer<GemBonus> function) {
        this.gem.get().getBonus(this.category, this.purity).ifPresent(function);
    }
}
