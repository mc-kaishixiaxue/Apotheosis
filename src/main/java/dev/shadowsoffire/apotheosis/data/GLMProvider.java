package dev.shadowsoffire.apotheosis.data;

import java.util.concurrent.CompletableFuture;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.loot.modifiers.AffixHookLootModifier;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;

public class GLMProvider extends GlobalLootModifierProvider {

    public GLMProvider(PackOutput output, CompletableFuture<Provider> registries) {
        super(output, registries, Apotheosis.MODID);
    }

    @Override
    protected void start() {
        this.add("affix_hook", new AffixHookLootModifier());
    }

}
