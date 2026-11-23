package com.unityindustry.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import com.unityindustry.menu.StationMenu;

/** Dedicated item slots are synchronized from the server-side crafting menu. */
public final class StationScreen extends AbstractContainerScreen<StationMenu> {
    private static final Identifier INDUSTRY = Identifier.fromNamespaceAndPath("unityindustry", "textures/gui/industry.png");
    private static final Identifier RESEARCH = Identifier.fromNamespaceAndPath("unityindustry", "textures/gui/research.png");

    public StationScreen(StationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        int xo = (width - imageWidth) / 2;
        int yo = (height - imageHeight) / 2;
        Identifier texture = menu.station() == StationMenu.Station.INDUSTRY ? INDUSTRY : RESEARCH;
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, xo, yo, 0.0f, 0.0f,
                imageWidth, imageHeight, 256, 256);
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float delta) {
        super.render(graphics, x, y, delta);
        renderTooltip(graphics, x, y);
    }
}
