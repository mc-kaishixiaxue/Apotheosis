package dev.shadowsoffire.apotheosis.socket.gem.bonus;

import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class DurabilityBonus extends GemBonus {

    public static Codec<DurabilityBonus> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            gemClass(),
            Purity.mapCodec(Codec.floatRange(0, 1)).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, DurabilityBonus::new));

    protected final Map<Purity, Float> values;

    public DurabilityBonus(GemClass gemClass, Map<Purity, Float> values) {
        super(Apotheosis.loc("durability"), gemClass);
        this.values = values;
    }

    @Override
    public Component getSocketBonusTooltip(GemInstance gem, AttributeTooltipContext ctx) {
        float level = this.values.get(gem.purity());
        return Component.translatable("bonus." + this.getId() + ".desc", Affix.fmt(100 * level)).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public float getDurabilityBonusPercentage(GemInstance gem) {
        return this.values.get(gem.purity());
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
