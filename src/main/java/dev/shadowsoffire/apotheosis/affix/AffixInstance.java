package dev.shadowsoffire.apotheosis.affix;

import java.util.Map;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

/**
 * An Affix Instance is a wrapper around the necessary parameters for all affix methods.
 * Prefer using this over directly invoking methods on {@link Affix}.
 * 
 * @param affix  The affix this instance is for.
 * @param level  The level of the affix.
 * @param rarity The rarity of the source item.
 * @param stack  The source item stack.
 */
public record AffixInstance(DynamicHolder<Affix> affix, float level, DynamicHolder<LootRarity> rarity, ItemStack stack) {

    public LootCategory category() {
        return LootCategory.forItem(stack);
    }

    public boolean isValid() {
        return this.affix.isBound() && this.rarity.isBound();
    }

    /**
     * Resolves the underlying {@link #rarity}. Throws if unbound.
     */
    public LootRarity getRarity() {
        return this.rarity.get();
    }

    /**
     * @see Affix#addModifiers(ItemStack, LootRarity, float, EquipmentSlot, BiConsumer)
     */
    public void addModifiers(ItemAttributeModifierEvent event) {
        this.afx().addModifiers(this, event);
    }

    /**
     * @see Affix#getDescription(ItemStack, LootRarity, float)
     */
    public MutableComponent getDescription(AttributeTooltipContext ctx) {
        return this.afx().getDescription(this, ctx);
    }

    /**
     * @see Affix#getAugmentingText(ItemStack, LootRarity, float)
     */
    public Component getAugmentingText(AttributeTooltipContext ctx) {
        return this.afx().getAugmentingText(this, ctx);
    }

    /**
     * @see Affix#getName(boolean)
     */
    public Component getName(boolean prefix) {
        return this.afx().getName(prefix);
    }

    /**
     * @see Affix#getDamageProtection(ItemStack, LootRarity, float, DamageSource)
     */
    public int getDamageProtection(DamageSource source) {
        return this.afx().getDamageProtection(this, source);
    }

    /**
     * @see Affix#getDamageBonus(ItemStack, LootRarity, float, MobType)
     */
    public float getDamageBonus(Entity entity) {
        return this.afx().getDamageBonus(this, entity);
    }

    /**
     * @see Affix#doPostAttack(ItemStack, LootRarity, float, LivingEntity, Entity)
     */
    public void doPostAttack(LivingEntity user, @Nullable Entity target) {
        this.afx().doPostAttack(this, user, target);
    }

    /**
     * @see Affix#doPostHurt(ItemStack, LootRarity, float, LivingEntity, Entity)
     */
    public void doPostHurt(LivingEntity user, @Nullable Entity attacker) {
        this.afx().doPostHurt(this, user, attacker);
    }

    /**
     * @see Affix#onArrowFired(ItemStack, LootRarity, float, LivingEntity, AbstractArrow)
     */
    public void onArrowFired(LivingEntity user, AbstractArrow arrow) {
        this.afx().onArrowFired(this, user, arrow);
    }

    /**
     * @see Affix#onItemUse(ItemStack, LootRarity, float, UseOnContext)
     */
    @Nullable
    public InteractionResult onItemUse(UseOnContext ctx) {
        return this.afx().onItemUse(this, ctx);
    }

    /**
     * @see Affix#onShieldBlock(ItemStack, LootRarity, float, LivingEntity, DamageSource, float)
     */
    public float onShieldBlock(LivingEntity entity, DamageSource source, float amount) {
        return this.afx().onShieldBlock(this, entity, source, amount);
    }

    /**
     * @see Affix#onBlockBreak(ItemStack, LootRarity, float, Player, LevelAccessor, BlockPos, BlockState)
     */
    public void onBlockBreak(Player player, LevelAccessor world, BlockPos pos, BlockState state) {
        this.afx().onBlockBreak(this, player, world, pos, state);
    }

    /**
     * @see Affix#getDurabilityBonusPercentage(ItemStack, LootRarity, float, ServerPlayer)
     */
    public float getDurabilityBonusPercentage(@Nullable ServerPlayer user) {
        return this.afx().getDurabilityBonusPercentage(this, user);
    }

    /**
     * @see Affix#onArrowImpact(AbstractArrow, LootRarity, float, HitResult, net.minecraft.world.phys.HitResult.Type)
     */
    public void onArrowImpact(AbstractArrow arrow, HitResult res, HitResult.Type type) {
        this.afx().onArrowImpact(this.level, this.getRarity(), arrow, res, type);
    }

    /**
     * @see Affix#enablesTelepathy()
     */
    public boolean enablesTelepathy() {
        return this.afx().enablesTelepathy();
    }

    /**
     * @see Affix#onHurt(ItemStack, LootRarity, float, DamageSource, LivingEntity, float)
     */
    public float onHurt(DamageSource src, LivingEntity ent, float amount) {
        return this.afx().onHurt(this, src, ent, amount);
    }

    /**
     * @see Affix#getEnchantmentLevels(ItemStack, LootRarity, float, Map)
     */
    public void getEnchantmentLevels(Map<Enchantment, Integer> enchantments) {
        this.afx().getEnchantmentLevels(this, enchantments);
    }

    /**
     * @see Affix#modifyLoot(ItemStack, LootRarity, float, ObjectArrayList, LootContext)
     */
    public void modifyLoot(ObjectArrayList<ItemStack> loot, LootContext ctx) {
        this.afx().modifyLoot(this, loot, ctx);
    }

    public AffixInstance withNewLevel(float level) {
        return new AffixInstance(this.affix, Mth.clamp(level, 0, 1), this.rarity, this.stack);
    }

    public ResourceLocation makeUniqueId(String salt) {
        return Affix.makeUniqueId(this, salt);
    }

    public ResourceLocation makeUniqueId() {
        return Affix.makeUniqueId(this);
    }

    private Affix afx() {
        return this.affix.get();
    }
}
