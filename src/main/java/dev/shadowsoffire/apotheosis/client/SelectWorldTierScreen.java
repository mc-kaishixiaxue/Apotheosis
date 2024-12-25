package dev.shadowsoffire.apotheosis.client;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.PurityWeightsRegistry;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment.Target;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugmentRegistry;
import dev.shadowsoffire.apothic_attributes.ApothicAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item.TooltipContext;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class SelectWorldTierScreen extends Screen {

    public static final ResourceLocation TEXTURE = Apotheosis.loc("textures/gui/select_world_tier.png");

    public static final int GUI_WIDTH = 480;
    public static final int GUI_HEIGHT = 270;
    public static final int IMAGE_WIDTH = 512;
    public static final int IMAGE_HEIGHT = 512;

    protected Button havenBtn, frontierBtn, ascentBtn, summitBtn, pinnacleBtn;

    protected WorldTier displayedTier = WorldTier.getTier(Minecraft.getInstance().player);

    public SelectWorldTierScreen() {
        super(Apotheosis.lang("title", "select_world_tier"));
    }

    @Override
    protected void init() {
        int leftPos = (this.width - GUI_WIDTH) / 2;
        int topPos = (this.height - GUI_HEIGHT) / 2;
        LocalPlayer player = Minecraft.getInstance().player;

        this.havenBtn = this.addRenderableWidget(
            Button.builder(Apotheosis.lang("button", "select_haven"), displayTier(WorldTier.HAVEN))
                .size(30, 30)
                .pos(leftPos + 25, topPos + 235)
                .build());
        this.havenBtn.active = WorldTier.isUnlocked(player, WorldTier.HAVEN);

        this.frontierBtn = this.addRenderableWidget(
            Button.builder(Apotheosis.lang("button", "select_frontier"), displayTier(WorldTier.FRONTIER))
                .size(30, 30)
                .pos(leftPos + 112, topPos + 225)
                .build());
        this.frontierBtn.active = WorldTier.isUnlocked(player, WorldTier.FRONTIER);

        this.ascentBtn = this.addRenderableWidget(
            Button.builder(Apotheosis.lang("button", "select_ascent"), displayTier(WorldTier.ASCENT))
                .size(30, 30)
                .pos(leftPos + 247, topPos + 190)
                .build());
        this.ascentBtn.active = WorldTier.isUnlocked(player, WorldTier.ASCENT);

        this.summitBtn = this.addRenderableWidget(
            Button.builder(Apotheosis.lang("button", "select_summit"), displayTier(WorldTier.SUMMIT))
                .size(30, 30)
                .pos(leftPos + 343, topPos + 80)
                .build());
        this.summitBtn.active = WorldTier.isUnlocked(player, WorldTier.SUMMIT);

        this.pinnacleBtn = this.addRenderableWidget(
            Button.builder(Apotheosis.lang("button", "select_pinnacle"), displayTier(WorldTier.PINNACLE))
                .size(30, 30)
                .pos(leftPos + 445, topPos + 10)
                .build());
        this.pinnacleBtn.active = WorldTier.isUnlocked(player, WorldTier.PINNACLE);
    }

    @Override
    public void renderBackground(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(gfx, mouseX, mouseY, partialTick);

        int leftPos = (this.width - GUI_WIDTH) / 2;
        int topPos = (this.height - GUI_HEIGHT) / 2;

        gfx.blit(TEXTURE, leftPos, topPos, 0, 0, GUI_WIDTH, GUI_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT);

        PoseStack pose = gfx.pose();
        pose.pushPose();

        float scale = 3;
        pose.scale(scale, scale, 1);
        Component title = Apotheosis.lang("text", "world_tier." + this.displayedTier.getSerializedName());
        gfx.drawString(font, title.getVisualOrderText(), (leftPos + 15) / scale, (topPos + 15) / scale, 0xFFFFFF, true);
        pose.popPose();

        Component desc = Apotheosis.lang("text", "world_tier." + this.displayedTier.getSerializedName() + ".desc");
        gfx.drawString(font, desc, leftPos + 15, topPos + 45, 0xC8C86E);

        // TODO: Texture or something, idk. Not a string of dashes.
        gfx.drawString(font, "-------------------------------------------------", leftPos + 15, topPos + 60, 0xFFFFFF);

        TooltipRenderUtil.renderTooltipBackground(gfx, leftPos + 18, topPos + 73, 225, 150, -5);

        Component info = Apotheosis.lang("text", "tier_info");
        gfx.drawString(font, info, leftPos + 20, topPos + 75, 0xFFFFFF);

        int yPos = topPos + 90;

        LocalPlayer player = Minecraft.getInstance().player;
        AttributeTooltipContext ctx = AttributeTooltipContext.of(player, TooltipContext.of(player.level()), ApothicAttributes.getTooltipFlag());

        Component players = Apotheosis.lang("text", "player_augments").withColor(0x00AAFF);
        gfx.drawString(font, players, leftPos + 20, yPos, 0, true);

        for (TierAugment aug : TierAugmentRegistry.getAugments(this.displayedTier, Target.PLAYERS)) {
            yPos += 10;
            Component comp = aug.getDescription(ctx).plainCopy();
            comp = Apotheosis.lang("text", "dot_prefix", comp).withColor(0x00AAFF);
            gfx.drawString(font, comp, leftPos + 20, yPos, 0, true);
        }

        yPos += 15;

        Component mobs = Apotheosis.lang("text", "monster_augments").withStyle(ChatFormatting.RED);
        gfx.drawString(font, mobs, leftPos + 20, yPos, 0, true);

        for (TierAugment aug : TierAugmentRegistry.getAugments(this.displayedTier, Target.MONSTERS)) {
            yPos += 10;
            Component comp = aug.getDescription(ctx).plainCopy();
            comp = Apotheosis.lang("text", "dot_prefix", comp).withStyle(ChatFormatting.RED);
            gfx.drawString(font, comp, leftPos + 20, yPos, 0, true);
        }

        Component rarities = Apotheosis.lang("text", "rarity_weights").withColor(0xFFFFFF);
        gfx.drawString(font, rarities, leftPos + 20, yPos + 15, 0, true);

        Component weights = RarityRegistry.getDropChances(displayedTier);
        gfx.drawString(font, weights, leftPos + 20, yPos + 25, 0xFFFFFF, true);

        Component purities = Apotheosis.lang("text", "purity_weights").withColor(0xFFFFFF);
        gfx.drawString(font, purities, leftPos + 20, yPos + 40, 0, true);

        Component pWeights = PurityWeightsRegistry.getDropChances(displayedTier);
        gfx.drawString(font, pWeights, leftPos + 20, yPos + 50, 0xFFFFFF, true);
    }

    protected OnPress displayTier(WorldTier tier) {
        // Switches the main screen display to the selected world tier.
        // There's a separate button for actually locking in that world tier.
        return btn -> {
            SelectWorldTierScreen.this.displayedTier = tier;
        };
    }

}
