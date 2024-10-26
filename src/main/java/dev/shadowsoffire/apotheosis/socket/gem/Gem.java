package dev.shadowsoffire.apotheosis.socket.gem;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.compat.GameStagesCompat.IStaged;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry.IDimensional;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry.ILuckyWeighted;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class Gem implements CodecProvider<Gem>, ILuckyWeighted, IDimensional, IStaged {

    public static final Codec<Gem> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        Codec.intRange(0, Integer.MAX_VALUE).fieldOf("weight").forGetter(ILuckyWeighted::getWeight),
        Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("quality", 0F).forGetter(ILuckyWeighted::getQuality),
        Purity.CODEC.optionalFieldOf("min_purity", Purity.CRACKED).forGetter(Gem::getMinPurity),
        PlaceboCodecs.setOf(ResourceLocation.CODEC).optionalFieldOf("dimensions", Collections.emptySet()).forGetter(IDimensional::getDimensions),
        GemBonus.CODEC.listOf().fieldOf("bonuses").forGetter(Gem::getBonuses),
        Codec.BOOL.optionalFieldOf("unique", false).forGetter(Gem::isUnique),
        PlaceboCodecs.setOf(Codec.STRING).optionalFieldOf("stages").forGetter(gem -> Optional.ofNullable(gem.getStages())))
        .apply(inst, Gem::new));

    protected final int weight;
    protected final float quality;
    protected final Purity minPurity;
    protected final Set<ResourceLocation> dimensions;
    protected final List<GemBonus> bonuses;
    protected final boolean unique;
    protected final @Nullable Set<String> stages;

    protected transient final Map<LootCategory, GemBonus> bonusMap;

    public Gem(int weight, float quality, Purity minPurity, Set<ResourceLocation> dimensions, List<GemBonus> bonuses, boolean unique, Optional<Set<String>> stages) {
        this.weight = weight;
        this.quality = quality;
        this.minPurity = minPurity;
        this.dimensions = dimensions;
        this.bonuses = bonuses;
        this.unique = unique;
        this.stages = stages.orElse(null);
        Preconditions.checkArgument(!bonuses.isEmpty(), "No bonuses were provided.");
        this.bonusMap = bonuses.stream().<Pair<LootCategory, GemBonus>>mapMulti((gemData, mapper) -> {
            for (LootCategory c : gemData.getGemClass().types()) {
                mapper.accept(Pair.of(c, gemData));
            }
        }).collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    /**
     * Adds all tooltip data from this gem to the gem stack.
     *
     * @param gem      The gem stack.
     * @param purity   The purity of this gem.
     * @param tooltips The destination for tooltips.
     */
    public void addInformation(GemInstance gem, Consumer<Component> list, AttributeTooltipContext ctx) {
        if (this.isUnique()) {
            list.accept(Component.translatable("text.apotheosis.unique").withStyle(Style.EMPTY.withColor(0xC73912)));
            list.accept(CommonComponents.EMPTY);
        }
        Style style = Style.EMPTY.withColor(0x0AFF0A);
        list.accept(Component.translatable("text.apotheosis.socketable_into").withStyle(style));
        addTypeInfo(list, this.bonusMap.keySet().toArray());
        list.accept(CommonComponents.EMPTY);

        list.accept(Component.translatable("text.apotheosis.when_socketed_in").withStyle(ChatFormatting.GOLD));
        for (GemBonus bonus : this.bonuses) {
            if (!bonus.supports(gem.purity())) {
                continue;
            }
            Component modifComp = bonus.getSocketBonusTooltip(gem, ctx);
            Component sum = Component.translatable("text.apotheosis.dot_prefix", Component.translatable("%s: %s", Component.translatable("gem_class." + bonus.getGemClass().key()), modifComp)).withStyle(ChatFormatting.GOLD);
            list.accept(sum);
        }
    }

    /**
     * Checks if this gem can be applied to an item, preventing more than one unique.
     *
     * @param socketed The target item.
     * @param rarity   The rarity of the gem.
     * @param gem      The gem
     * @return If this gem can be socketed into the item.
     */
    public boolean canApplyTo(ItemStack socketed, ItemStack gem, Purity purity) {
        if (this.isUnique()) {
            List<Gem> gems = SocketHelper.getGems(socketed).streamValidGems().map(GemInstance::gem).map(DynamicHolder::get).toList();
            if (gems.contains(this)) return false;
        }
        return this.isValidIn(socketed, gem, purity);
    }

    /**
     * Checks if this gem is legally socketed into an item. Does not validate uniques
     *
     * @param socketed The target item.
     * @param rarity   The rarity of the gem.
     * @param gem      The gem
     * @return If this gem can be socketed into the item.
     */
    public boolean isValidIn(ItemStack socketed, ItemStack gem, Purity purity) {
        LootCategory cat = LootCategory.forItem(socketed);
        return !cat.isNone() && this.bonusMap.containsKey(cat) && this.bonusMap.get(cat).supports(purity);
    }

    /**
     * Optionally returns this gem's bonus for the given category/rarity combination.
     *
     * @param cat    The category
     * @param rarity The rarity
     * @return If a bonus exists for the inputs, an {@link Optional} holding it, otherwise {@link Optional#empty()}.
     */
    public Optional<GemBonus> getBonus(LootCategory cat, Purity purity) {
        return Optional.ofNullable(this.bonusMap.get(cat)).filter(b -> b.supports(purity));
    }

    @Override
    public String toString() {
        return String.format("Gem: %s", this.getId());
    }

    public static String fmt(float f) {
        return Affix.fmt(f);
    }

    @Override
    public float getQuality() {
        return this.quality;
    }

    @Override
    public int getWeight() {
        return this.weight;
    }

    @Override
    public Set<ResourceLocation> getDimensions() {
        return this.dimensions;
    }

    public Purity getMinPurity() {
        return this.minPurity;
    }

    public List<GemBonus> getBonuses() {
        return this.bonuses;
    }

    public boolean isUnique() {
        return this.unique;
    }

    public Gem validate(ResourceLocation key) {
        Preconditions.checkNotNull(this.dimensions);
        return this;
    }

    @Override
    public Set<String> getStages() {
        return this.stages;
    }

    @Override
    public Codec<? extends Gem> getCodec() {
        return CODEC;
    }

    public static void addTypeInfo(Consumer<Component> list, Object... types) {
        Arrays.sort(types, (c1, c2) -> ((LootCategory) c1).getName().compareTo(((LootCategory) c2).getName()));
        Style style = Style.EMPTY.withColor(0x0AFF0A);
        if (types.length != LootCategory.BY_ID.size() - 1) {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            while (i < types.length) {
                int rem = Math.min(3, types.length - i);
                Object[] args = new Object[rem];
                for (int r = 0; r < rem; r++) {
                    sb.append("%s, ");
                    args[r] = Component.translatable(((LootCategory) types[i + r]).getDescIdPlural());
                }
                list.accept(Component.translatable("text.apotheosis.dot_prefix", Component.translatable(sb.substring(0, sb.length() - 2), args)).withStyle(style));
                sb.setLength(0);
                i += rem;
            }
        }
        else {
            list.accept(Component.translatable("text.apotheosis.dot_prefix", Component.translatable("text.apotheosis.anything")).withStyle(style));
        }
    }

    public final ResourceLocation getId() {
        return GemRegistry.INSTANCE.getKey(this);
    }

}
