package dev.shadowsoffire.apotheosis.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    /**
     * Injected at the return of getDamageProtection to call the equivalent affix and gem hooks: {@link GemBonus#getDamageProtection(GemInstance, DamageSource)} and
     * {@link Affix#getDamageProtection(AffixInstance, DamageSource)}.
     */
    @Inject(at = @At("RETURN"), method = "getDamageProtection(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;)F", cancellable = true)
    private static void apoth_getDamageProtection(ServerLevel level, LivingEntity entity, DamageSource source, CallbackInfoReturnable<Float> cir) {
        float prot = cir.getReturnValueF();
        for (ItemStack s : entity.getArmorAndBodyArmorSlots()) {
            prot += SocketHelper.getGems(s).getDamageProtection(source);

            var affixes = AffixHelper.getAffixes(s);
            for (AffixInstance inst : affixes.values()) {
                prot += inst.getDamageProtection(source);
            }
        }
        cir.setReturnValue(prot);
    }

    /**
     * Injected at the return of modifyDamage to call the equivalent affix and gem hooks: {@link GemBonus#getDamageBonus(GemInstance, Entity)} and
     * {@link Affix#getDamageBonus(AffixInstance, Entity)}.
     */
    @Inject(at = @At("RETURN"), method = "modifyDamage(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)F", cancellable = true)
    private static void apoth_modifyDamage(ServerLevel level, ItemStack tool, Entity entity, DamageSource damageSource, float damage, CallbackInfoReturnable<Float> cir) {
        float dmg = cir.getReturnValueF();

        dmg += SocketHelper.getGems(tool).getDamageBonus(entity);

        var affixes = AffixHelper.getAffixes(tool);
        for (AffixInstance inst : affixes.values()) {
            dmg += inst.getDamageBonus(entity);
        }

        cir.setReturnValue(dmg);
    }

    /**
     * Injects at the end of doPostAttackEffectsWithItemSource to handle the equivalent affix and gem hooks.
     * <p>
     * These hooks retain the prior semantics from before this type of hook was flattened into one call.
     */
    @Inject(at = @At("TAIL"), method = "doPostAttackEffectsWithItemSource(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/item/ItemStack;)V")
    private static void apoth_doPostAttackEffectsWithItemSource(ServerLevel level, Entity target, DamageSource damageSource, @Nullable ItemStack itemSource, CallbackInfo ci) {
        if (damageSource.getEntity() instanceof LivingEntity user) {
            for (ItemStack s : user.getAllSlots()) {
                SocketHelper.getGems(s).doPostAttack(user, target);

                var affixes = AffixHelper.getAffixes(s);
                for (AffixInstance inst : affixes.values()) {
                    int old = target.invulnerableTime;
                    target.invulnerableTime = 0;
                    inst.doPostAttack(user, target);
                    target.invulnerableTime = old;
                }
            }
        }

        if (target instanceof LivingEntity livingTarget) {
            for (ItemStack s : livingTarget.getAllSlots()) {
                SocketHelper.getGems(s).doPostHurt(livingTarget, damageSource);

                var affixes = AffixHelper.getAffixes(s);
                for (AffixInstance inst : affixes.values()) {
                    inst.doPostHurt(livingTarget, damageSource);
                }
            }
        }
    }

}
