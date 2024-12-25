package dev.shadowsoffire.apotheosis.client;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.net.WorldTierPayload;
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
import net.neoforged.neoforge.network.PacketDistributor;

public class SelectWorldTierScreen extends Screen {

    public static final ResourceLocation TEXTURE = Apotheosis.loc("textures/gui/mountain.png");
    public static final ResourceLocation BTN_TEX_HAVEN = Apotheosis.loc("textures/gui/buttons/haven.png");
    public static final ResourceLocation BTN_TEX_FRONTIER = Apotheosis.loc("textures/gui/buttons/frontier.png");
    public static final ResourceLocation BTN_TEX_ASCENT = Apotheosis.loc("textures/gui/buttons/ascent.png");
    public static final ResourceLocation BTN_TEX_SUMMIT = Apotheosis.loc("textures/gui/buttons/summit.png");
    public static final ResourceLocation BTN_TEX_PINNACLE = Apotheosis.loc("textures/gui/buttons/pinnacle.png");
    public static final ResourceLocation SEPARATOR_LINE = Apotheosis.loc("textures/gui/separator_line.png");

    public static final int GUI_WIDTH = 480;
    public static final int GUI_HEIGHT = 270;
    public static final int IMAGE_WIDTH = 498;
    public static final int IMAGE_HEIGHT = 286;

    protected SimpleTexButton havenBtn, frontierBtn, ascentBtn, summitBtn, pinnacleBtn;

    protected Button activateButton;

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
            SimpleTexButton.builder()
                .size(30, 30)
                .pos(leftPos + 100, topPos + 215)
                .texture(BTN_TEX_HAVEN)
                .texSize(30, 90)
                .action(displayTier(WorldTier.HAVEN))
                .message(Apotheosis.lang("button", "haven"))
                .inactiveMessage(tierLocked(WorldTier.HAVEN))
                .build());
        this.havenBtn.active = WorldTier.isUnlocked(player, WorldTier.HAVEN);

        this.frontierBtn = this.addRenderableWidget(
            SimpleTexButton.builder()
                .size(30, 30)
                .pos(leftPos + 210, topPos + 205)
                .texture(BTN_TEX_FRONTIER)
                .texSize(30, 90)
                .action(displayTier(WorldTier.FRONTIER))
                .message(Apotheosis.lang("button", "frontier"))
                .inactiveMessage(tierLocked(WorldTier.FRONTIER))
                .build());
        this.frontierBtn.active = WorldTier.isUnlocked(player, WorldTier.FRONTIER);

        this.ascentBtn = this.addRenderableWidget(
            SimpleTexButton.builder()
                .size(30, 30)
                .pos(leftPos + 250, topPos + 115)
                .texture(BTN_TEX_ASCENT)
                .texSize(30, 90)
                .action(displayTier(WorldTier.ASCENT))
                .message(Apotheosis.lang("button", "ascent"))
                .inactiveMessage(tierLocked(WorldTier.ASCENT))
                .build());
        this.ascentBtn.active = WorldTier.isUnlocked(player, WorldTier.ASCENT);

        this.summitBtn = this.addRenderableWidget(
            SimpleTexButton.builder()
                .size(30, 30)
                .pos(leftPos + 315, topPos + 60)
                .texture(BTN_TEX_SUMMIT)
                .texSize(30, 90)
                .action(displayTier(WorldTier.SUMMIT))
                .message(Apotheosis.lang("button", "summit"))
                .inactiveMessage(tierLocked(WorldTier.SUMMIT))
                .build());
        this.summitBtn.active = WorldTier.isUnlocked(player, WorldTier.SUMMIT);

        this.pinnacleBtn = this.addRenderableWidget(
            SimpleTexButton.builder()
                .size(30, 30)
                .pos(leftPos + 395, topPos + 0)
                .texture(BTN_TEX_PINNACLE)
                .texSize(30, 90)
                .action(displayTier(WorldTier.PINNACLE))
                .message(Apotheosis.lang("button", "pinnacle"))
                .inactiveMessage(tierLocked(WorldTier.PINNACLE))
                .build());
        this.pinnacleBtn.active = WorldTier.isUnlocked(player, WorldTier.PINNACLE);

        this.activateButton = this.addRenderableWidget(
            Button.builder(Component.literal("Activate"), activateSelectedTier())
                .size(60, 24)
                .pos(leftPos + 198, topPos + 15)
                .build());

        this.updateButtonStatus();
    }

    @Override
    public void renderBackground(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(gfx, mouseX, mouseY, partialTick);

        int imgLeft = (this.width - IMAGE_WIDTH) / 2;
        int imgTop = (this.height - IMAGE_HEIGHT) / 2;

        gfx.blit(TEXTURE, imgLeft, imgTop, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT);

        int leftPos = (this.width - GUI_WIDTH) / 2;
        int topPos = (this.height - GUI_HEIGHT) / 2;

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
        // gfx.drawString(font, "-------------------------------------------------", leftPos + 15, topPos + 60, 0xFFFFFF);

        gfx.blit(SEPARATOR_LINE, leftPos, topPos + 50, 0, 0, 0, 275, 30, 275, 30);

        if (true) return;

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
            SelectWorldTierScreen.this.updateButtonStatus();
        };
    }

    protected OnPress activateSelectedTier() {
        return btn -> {
            WorldTier tier = SelectWorldTierScreen.this.displayedTier;
            if (WorldTier.getTier(Minecraft.getInstance().player) != tier) {
                PacketDistributor.sendToServer(new WorldTierPayload(tier));
            }
            btn.active = false;
            btn.setMessage(Component.literal("Activated"));
        };
    }

    protected void updateButtonStatus() {
        this.havenBtn.forceHovered = this.displayedTier == WorldTier.HAVEN;
        this.frontierBtn.forceHovered = this.displayedTier == WorldTier.FRONTIER;
        this.ascentBtn.forceHovered = this.displayedTier == WorldTier.ASCENT;
        this.summitBtn.forceHovered = this.displayedTier == WorldTier.SUMMIT;
        this.pinnacleBtn.forceHovered = this.displayedTier == WorldTier.PINNACLE;

        this.activateButton.active = WorldTier.getTier(Minecraft.getInstance().player) != this.displayedTier;
        if (this.activateButton.active) {
            this.activateButton.setMessage(Component.literal("Activate"));
        }
        else {
            this.activateButton.setMessage(Component.literal("Activated"));
        }
    }

    private static Component tierLocked(WorldTier tier) {
        Component advName = Apotheosis.lang("advancements", "progression." + tier.getSerializedName() + ".title").withStyle(ChatFormatting.GOLD);
        return Apotheosis.lang("button", "tier_locked", advName).withStyle(ChatFormatting.RED);
    }

}
