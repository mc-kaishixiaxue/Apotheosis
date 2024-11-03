package dev.shadowsoffire.apotheosis.util;

import java.util.regex.Pattern;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;

public record LootPatternMatcher(@Nullable String domain, Pattern pathRegex, float chance) {

    public boolean matches(ResourceLocation id) {
        return (this.domain == null || this.domain.equals(id.getNamespace())) && this.pathRegex.matcher(id.getPath()).matches();
    }

    public static LootPatternMatcher parse(String s) throws Exception {
        int pipe = s.lastIndexOf('|');
        int colon = s.indexOf(':');
        float chance = Float.parseFloat(s.substring(pipe + 1));
        String domain = colon == -1 ? null : s.substring(0, colon);
        Pattern pattern = Pattern.compile(s.substring(colon + 1, pipe));
        return new LootPatternMatcher(domain, pattern, chance);
    }
}
