package dev.shadowsoffire.apotheosis.affix;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.AdventureModule;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

/**
 * Helper class for affixes that modify attributes, as the apply method is the same for most of those.
 */
public class AttributeAffix extends Affix {

    public static final Codec<AttributeAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            affixDef(),
            BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(a -> a.attribute),
            PlaceboCodecs.enumCodec(Operation.class).fieldOf("operation").forGetter(a -> a.operation),
            LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values),
            LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types))
        .apply(inst, AttributeAffix::new));

    protected final Holder<Attribute> attribute;
    protected final Operation operation;
    protected final Map<LootRarity, StepFunction> values;
    protected final Set<LootCategory> types;

    protected transient final Map<LootRarity, ModifierInst> modifiers;

    public AttributeAffix(AffixDefinition def, Holder<Attribute> attr, Operation op, Map<LootRarity, StepFunction> values, Set<LootCategory> types) {
        super(def);
        this.attribute = attr;
        this.operation = op;
        this.values = values;
        this.types = types;
        this.modifiers = values.entrySet().stream().map(entry -> Pair.of(entry.getKey(), new ModifierInst(attr, op, entry.getValue()))).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        return Component.empty();
    }

    @Override
    public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
        ModifierInst modif = this.modifiers.get(inst.getRarity());
        double value = modif.valueFactory.get(inst.level());
        Attribute attr = this.attribute.value();

        MutableComponent comp;
        MutableComponent valueComp = attr.toValueComponent(this.operation, value < 0 ? -value : value, ctx.flag());

        if (value > 0.0D) {
            comp = Component.translatable("attributeslib.modifier.plus", valueComp, Component.translatable(attr.getDescriptionId())).withStyle(ChatFormatting.BLUE);
        }
        else {
            comp = Component.translatable("attributeslib.modifier.take", valueComp, Component.translatable(attr.getDescriptionId())).withStyle(ChatFormatting.RED);
        }

        if (modif.valueFactory.get(0) != modif.valueFactory.get(1)) {
            Component minComp = attr.toValueComponent(this.operation, modif.valueFactory.get(0), ctx.flag());
            Component maxComp = attr.toValueComponent(this.operation, modif.valueFactory.get(1), ctx.flag());
            comp.append(valueBounds(minComp, maxComp));
        }

        return comp;
    }

    @Override
    public void addModifiers(AffixInstance inst, ItemAttributeModifierEvent event) {
        LootCategory cat = inst.category();
        if (cat.isNone()) {
            AdventureModule.LOGGER.debug("Attempted to apply the attributes of affix {} on item {}, but it is not an affix-compatible item!", this.id(), inst.stack().getHoverName().getString());
            return;
        }
        ModifierInst modif = this.modifiers.get(inst.getRarity());
        if (modif.attr == null) {
            AdventureModule.LOGGER.debug("The affix {} has attempted to apply a null attribute modifier to {}!", this.id(), inst.stack().getHoverName().getString());
            return;
        }
        event.addModifier(this.attribute, modif.build(inst), cat.getSlots());
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (cat.isNone()) return false;
        return (this.types.isEmpty() || this.types.contains(cat)) && this.modifiers.containsKey(rarity);
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }

    public record ModifierInst(Holder<Attribute> attr, Operation op, StepFunction valueFactory) {

        public AttributeModifier build(AffixInstance inst) {
            return new AttributeModifier(inst.makeUniqueId(), this.valueFactory.get(inst.level()), this.op);
        }
    }

}
