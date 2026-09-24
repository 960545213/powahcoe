package org.shuashuashua.powahcoe.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/** Native Forge equivalent of the 0.0.6 nine-input/nine-output LDLib2 menu. */
public class CableOrbScreen extends AbstractContainerScreen<CableOrbContainer> {
    public CableOrbScreen(CableOrbContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 218;
        inventoryLabelY = 124;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
        titleLabelY = 6;
        addRenderableWidget(new AbstractButton(leftPos + 7, topPos + 98, 100, 20,
                Component.translatable("button.powahcoe.auto_eject")) {
            @Override
            public void onPress() {
                if (minecraft != null && minecraft.gameMode != null) {
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
                }
            }

            @Override
            protected void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
                int x = getX(), y = getY() + 3;
                gui.fill(x, y, x + 14, y + 14, isHoveredOrFocused() ? 0xFFFFFFFF : 0xFF373737);
                gui.fill(x + 1, y + 1, x + 13, y + 13, 0xFF555555);
                // Always display server menu data; clicks never overwrite it locally.
                if (menu.isAutoEject()) {
                    gui.fill(x + 3, y + 7, x + 5, y + 10, 0xFFFFFFFF);
                    gui.fill(x + 5, y + 9, x + 7, y + 11, 0xFFFFFFFF);
                    gui.fill(x + 7, y + 6, x + 9, y + 10, 0xFFFFFFFF);
                    gui.fill(x + 9, y + 3, x + 11, y + 7, 0xFFFFFFFF);
                }
                gui.drawString(font, getMessage(), x + 18, y + 3, 0xFF333333, false);
            }

            @Override
            protected void updateWidgetNarration(NarrationElementOutput output) {
                defaultButtonNarrationText(output);
            }
        });
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        gui.fill(x, y, x + imageWidth, y + imageHeight, 0xFFC6C6C6);
        gui.fill(x, y, x + imageWidth, y + 2, 0xFFFFFFFF);
        gui.fill(x, y, x + 2, y + imageHeight, 0xFFFFFFFF);
        gui.fill(x + imageWidth - 2, y, x + imageWidth, y + imageHeight, 0xFF555555);
        gui.fill(x, y + imageHeight - 2, x + imageWidth, y + imageHeight, 0xFF555555);
        panel(gui, x + 5, y + 26, 59, 59);
        panel(gui, x + 113, y + 26, 59, 59);
        panel(gui, x + 113, y + 96, 59, 24);
        for (Slot slot : menu.slots) {
            int sx = x + slot.x - 1, sy = y + slot.y - 1;
            gui.fill(sx, sy, sx + 18, sy + 18, 0xFF373737);
            gui.fill(sx + 1, sy + 1, sx + 18, sy + 18, 0xFFFFFFFF);
            gui.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF8B8B8B);
        }
    }

    private void panel(GuiGraphics gui, int x, int y, int width, int height) {
        gui.fill(x, y, x + width, y + height, 0xFF373737);
        gui.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF8B8B8B);
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        super.renderLabels(gui, mouseX, mouseY);
        gui.drawString(font, Component.translatable("gui.powahcoe.input"), 8, 17, 0x404040, false);
        gui.drawString(font, Component.translatable("gui.powahcoe.output"), 116, 17, 0x404040, false);
        gui.drawString(font, Component.translatable("slot.powahcoe.upgrade"), 116, 87, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui);
        super.render(gui, mouseX, mouseY, partialTick);
        renderTooltip(gui, mouseX, mouseY);
    }
}
