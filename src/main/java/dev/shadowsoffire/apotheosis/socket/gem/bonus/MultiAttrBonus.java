package dev.shadowsoffire.apotheosis.socket.gem.bonus;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

public class MultiAttrBonus extends GemBonus {

    public static Codec<MultiAttrBonus> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            gemClass(),
            ModifierInst.CODEC.listOf().fieldOf("modifiers").forGetter(a -> a.modifiers),
            Codec.STRING.fieldOf("desc").forGetter(a -> a.desc))
        .apply(inst, MultiAttrBonus::new));

    protected final List<ModifierInst> modifiers;
    protected final String desc;

    public MultiAttrBonus(GemClass gemClass, List<ModifierInst> modifiers, String desc) {
        super(Apotheosis.loc("multi_attribute"), gemClass);
        this.modifiers = modifiers;
        this.desc = desc;
    }

    @Override
    public void addModifiers(GemInstance gem, ItemAttributeModifierEvent event) {
        int i = 0;
        for (ModifierInst modifier : this.modifiers) {
            event.addModifier(modifier.attr, modifier.build(makeUniqueId(gem, "" + i), gem.purity()), gem.category().getSlots());
        }
    }

    @Override
    public Component getSocketBonusTooltip(GemInstance inst, AttributeTooltipContext ctx) {
        Object[] values = new Object[this.modifiers.size() * 2];
        int i = 0;
        for (ModifierInst modifier : this.modifiers) {
            values[i] = modifier.attr.value().toComponent(modifier.build(makeUniqueId(inst, "" + i), inst.purity()), ctx.flag());
            values[this.modifiers.size() + i] = modifier.attr.value().toValueComponent(modifier.op, i, ctx.flag());
            i++;
        }
        return Component.translatable(this.desc, values).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public MultiAttrBonus validate() {
        Set<Purity> supported = this.modifiers.get(0).values.keySet();
        boolean allMatch = this.modifiers.stream().map(inst -> inst.values.keySet()).allMatch(supported::equals);
        Preconditions.checkArgument(allMatch, "Each modifier in a multi_attribute bonus must support the same purity levels.");
        return this;
    }

    @Override
    public boolean supports(Purity purity) {
        return this.modifiers.get(0).values.containsKey(purity);
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

    protected static record ModifierInst(Holder<Attribute> attr, Operation op, Map<Purity, Float> values) {

        public static Codec<ModifierInst> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(ModifierInst::attr),
                PlaceboCodecs.enumCodec(Operation.class).fieldOf("operation").forGetter(ModifierInst::op),
                Purity.mapCodec(Codec.FLOAT).fieldOf("values").forGetter(ModifierInst::values))
            .apply(inst, ModifierInst::new));

        public AttributeModifier build(ResourceLocation id, Purity purity) {
            return new AttributeModifier(id, this.values.get(purity), this.op);
        }

    }

}
