package dev.shadowsoffire.apotheosis.util;

import java.util.Optional;
import java.util.regex.Pattern;

import org.spongepowered.include.com.google.common.base.Preconditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;

public record LootPatternMatcher(Optional<String> domain, Pattern pathRegex) {

    public static final Codec<LootPatternMatcher> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        Codec.STRING.optionalFieldOf("domain").forGetter(LootPatternMatcher::domain),
        Codec.STRING.xmap(Pattern::compile, Pattern::toString).fieldOf("path_regex").forGetter(LootPatternMatcher::pathRegex))
        .apply(inst, LootPatternMatcher::new));

    public static LootPatternMatcher of(String domain, String regex) {
        Preconditions.checkArgument(!domain.isBlank());
        return new LootPatternMatcher(Optional.of(domain), Pattern.compile(regex));
    }

    public static LootPatternMatcher of(String regex) {
        return new LootPatternMatcher(Optional.empty(), Pattern.compile(regex));
    }

    public boolean matches(ResourceLocation id) {
        return (this.domain.isEmpty() || this.domain.get().equals(id.getNamespace())) && this.pathRegex.matcher(id.getPath()).matches();
    }
}
