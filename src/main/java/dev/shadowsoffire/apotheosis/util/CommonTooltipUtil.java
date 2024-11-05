package dev.shadowsoffire.apotheosis.util;

import java.util.function.Consumer;

import com.google.common.base.Predicates;

import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apothic_attributes.ApothicAttributes;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLEnvironment;

public class CommonTooltipUtil {

    public static void appendBossData(Level level, LivingEntity entity, Consumer<Component> tooltip) {
        DynamicHolder<LootRarity> rarity = RarityRegistry.byLegacyId(entity.getPersistentData().getString("apoth.rarity"));
        if (!rarity.isBound()) return;
        tooltip.accept(Component.translatable("info.apotheosis.boss", rarity.get().toComponent()).withStyle(ChatFormatting.GRAY));
        if (FMLEnvironment.production) return;
        tooltip.accept(CommonComponents.EMPTY);
        tooltip.accept(Component.translatable("info.apotheosis.boss_modifiers").withStyle(ChatFormatting.GRAY));
        AttributeMap map = entity.getAttributes();
        BuiltInRegistries.ATTRIBUTE.holders().map(map::getInstance).filter(Predicates.notNull()).forEach(inst -> {
            for (AttributeModifier modif : inst.getModifiers()) {
                // TODO: Figure out how to identify boss modifiers after the fact.
                if (modif.getName().startsWith("placebo_random_modifier_")) {
                    tooltip.accept(inst.getAttribute().value().toComponent(modif, ApothicAttributes.getTooltipFlag()));
                }
            }
        });
    }

}
