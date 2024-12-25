package dev.shadowsoffire.apotheosis.client;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.include.com.google.common.base.Preconditions;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SimpleTexButton extends Button {

    protected final ResourceLocation texture;
    protected final int xTexStart;
    protected final int yTexStart;
    protected final int textureWidth;
    protected final int textureHeight;
    protected Component inactiveMessage = CommonComponents.EMPTY;
    protected boolean forceHovered = false;

    public SimpleTexButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, ResourceLocation texture, Button.OnPress pOnPress) {
        this(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, texture, 256, 256, pOnPress);
    }

    public SimpleTexButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, ResourceLocation texture, int pTextureWidth, int pTextureHeight, Button.OnPress pOnPress) {
        this(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, texture, pTextureWidth, pTextureHeight, pOnPress, CommonComponents.EMPTY);
    }

    public SimpleTexButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, ResourceLocation texture, int pTextureWidth, int pTextureHeight, Button.OnPress pOnPress, Component pMessage) {
        this(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, texture, pTextureWidth, pTextureHeight, pOnPress, DEFAULT_NARRATION, pMessage);
    }

    public SimpleTexButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, ResourceLocation texture, int pTextureWidth, int pTextureHeight, Button.OnPress pOnPress, Button.CreateNarration pOnTooltip,
        Component pMessage) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress, pOnTooltip);
        this.textureWidth = pTextureWidth;
        this.textureHeight = pTextureHeight;
        this.xTexStart = pXTexStart;
        this.yTexStart = pYTexStart;
        this.texture = texture;
    }

    public SimpleTexButton setInactiveMessage(Component msg) {
        this.inactiveMessage = msg;
        return this;
    }

    @Override
    public void setPosition(int pX, int pY) {
        this.setX(pX);
        this.setY(pY);
    }

    @Override
    public void renderWidget(GuiGraphics gfx, int pMouseX, int pMouseY, float pPartialTick) {
        int yTex = this.yTexStart;
        if (!this.isActive()) {
            yTex += this.height;
        }
        else if (this.isHovered() || this.forceHovered) {
            yTex += this.height * 2;
        }

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        gfx.blit(this.texture, this.getX(), this.getY(), this.xTexStart, yTex, this.width, this.height, this.textureWidth, this.textureHeight);
        if (this.isHovered()) {
            this.renderToolTip(gfx, pMouseX, pMouseY);
        }
    }

    public void renderToolTip(GuiGraphics gfx, int pMouseX, int pMouseY) {
        if (this.getMessage() != CommonComponents.EMPTY && this.isHovered()) {
            Component primary = this.getMessage();
            if (!this.active) {
                primary = primary.copy().withStyle(ChatFormatting.GRAY);
            }
            List<Component> tooltips = new ArrayList<>();
            tooltips.add(primary);
            if (!this.active && this.inactiveMessage != CommonComponents.EMPTY) {
                tooltips.add(this.inactiveMessage);
            }
            gfx.renderComponentTooltip(Minecraft.getInstance().font, tooltips, pMouseX, pMouseY);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        protected int x = -1;
        protected int y = -1;
        protected int width = -1;
        protected int height = -1;
        protected int u = 0;
        protected int v = 0;
        protected int textureWidth = 256;
        protected int textureHeight = 256;
        protected Component message = CommonComponents.EMPTY;
        protected Component inactiveMessage = CommonComponents.EMPTY;
        protected ResourceLocation texture = null;
        protected OnPress action = btn -> {};

        public Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder texPos(int u, int v) {
            this.u = u;
            this.v = v;
            return this;
        }

        public Builder texSize(int texWidth, int texHeight) {
            this.textureWidth = texWidth;
            this.textureHeight = texHeight;
            return this;
        }

        public Builder message(Component message) {
            this.message = message;
            return this;
        }

        public Builder inactiveMessage(Component message) {
            this.inactiveMessage = message;
            return this;
        }

        public Builder texture(ResourceLocation texture) {
            this.texture = texture;
            return this;
        }

        public Builder action(OnPress action) {
            this.action = action;
            return this;
        }

        public SimpleTexButton build() {
            Preconditions.checkArgument(this.x >= 0 && this.y >= 0, "Position must be set");
            Preconditions.checkArgument(this.width >= 0 && this.height >= 0, "Size must be set");
            Preconditions.checkNotNull(this.texture, "Texture must bet set");
            return new SimpleTexButton(this.x, this.y, this.width, this.height, this.u, this.v, this.texture, this.textureWidth, this.textureHeight, action, message).setInactiveMessage(this.inactiveMessage);
        }
    }

}
