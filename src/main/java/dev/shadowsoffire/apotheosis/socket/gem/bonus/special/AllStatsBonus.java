package dev.shadowsoffire.apotheosis.socket.gem.bonus.special;

import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

@SuppressWarnings("deprecation")
public class AllStatsBonus extends GemBonus {

    public static Codec<AllStatsBonus> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            gemClass(),
            PlaceboCodecs.enumCodec(Operation.class).fieldOf("operation").forGetter(a -> a.operation),
            Purity.mapCodec(Codec.FLOAT).fieldOf("values").forGetter(a -> a.values),
            RegistryCodecs.homogeneousList(Registries.ATTRIBUTE).fieldOf("attributes").forGetter(a -> a.attributes))
        .apply(inst, AllStatsBonus::new));

    protected final Operation operation;
    protected final Map<Purity, Float> values;
    protected final HolderSet<Attribute> attributes;

    public AllStatsBonus(GemClass gemClass, Operation op, Map<Purity, Float> values, HolderSet<Attribute> attributes) {
        super(gemClass);
        this.operation = op;
        this.values = values;
        this.attributes = attributes;
    }

    @Override
    public void addModifiers(GemInstance inst, ItemAttributeModifierEvent event) {
        int idx = 0;
        for (Holder<Attribute> attr : this.attributes) {
            ResourceLocation id = makeUniqueId(inst, "" + idx++);
            var modif = new AttributeModifier(id, this.values.get(inst.purity()), this.operation);
            event.addModifier(attr, modif, inst.category().getSlots());
        }
    }

    @Override
    public Component getSocketBonusTooltip(GemInstance inst, AttributeTooltipContext ctx) {
        float value = this.values.get(inst.purity());
        return Component.translatable("bonus." + this.getTypeKey() + ".desc", Affix.fmt(value * 100)).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public boolean supports(Purity purity) {
        return this.values.containsKey(purity);
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

}
