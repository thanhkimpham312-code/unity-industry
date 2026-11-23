package com.unityindustry.client;

import com.unityindustry.menu.ImplantMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Uses the research workbench background until dedicated Implant GUI art is ready. */
public final class ImplantScreen extends AbstractContainerScreen<ImplantMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("unityindustry", "textures/gui/research.png");

    public ImplantScreen(ImplantMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override protected void init() {
        super.init();
        if (menu.mode() == ImplantMenu.Mode.REVERSE) {
            addRenderableWidget(Button.builder(Component.literal("Next Core >"), b -> {
                if (minecraft != null && minecraft.gameMode != null) {
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
                }
            }).bounds(leftPos + 88, topPos + 13, 75, 17).build());
        }
    }

    @Override protected void renderBg(GuiGraphics graphics, float delta, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos,
            0.0f, 0.0f, imageWidth, imageHeight, 256, 256);
    }

    @Override public void render(GuiGraphics graphics, int x, int y, float delta) {
        super.render(graphics, x, y, delta);
        renderTooltip(graphics, x, y);
    }
}
