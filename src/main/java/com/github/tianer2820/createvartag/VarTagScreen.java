package com.github.tianer2820.createvartag;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

import com.github.tianer2820.createvartag.network.SetVarTagPacket;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;

public class VarTagScreen extends Screen {
    private EditBox editBox;
    private final String initialName;
    private final List<String> nearbyVars;

    public VarTagScreen(String initialName, List<String> nearbyVars) {
        super(Component.literal("VarTag Menu"));
        this.initialName = initialName;
        this.nearbyVars = nearbyVars;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = 40;

        this.editBox = new EditBox(this.font, centerX - 100, centerY - 20, 200, 20, Component.literal("Variable Name"));
        this.editBox.setValue(this.initialName);
        this.addRenderableWidget(this.editBox);

        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> {
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(new SetVarTagPacket(this.editBox.getValue()));
            this.onClose();
        }).bounds(centerX - 50, centerY + 10, 100, 20).build());

        if (!this.nearbyVars.isEmpty()) {
            VarSelectionList list = new VarSelectionList(this.minecraft, 220, this.height - (centerY + 50) - 20,
                    centerY + 50, 16);
            list.setX(centerX - 110);
            this.addRenderableWidget(list);
        }

        this.setInitialFocus(this.editBox);
        this.editBox.setCursorPosition(this.initialName.length());
        this.editBox.setHighlightPos(0);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    class VarSelectionList extends ObjectSelectionList<VarSelectionList.Entry> {
        public VarSelectionList(Minecraft mc, int width, int height, int y, int itemHeight) {
            super(mc, width, height, y, itemHeight);
            for (String varName : VarTagScreen.this.nearbyVars) {
                this.addEntry(new Entry(varName));
            }
        }

        @Override
        public int getRowWidth() {
            return 200;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getX() + this.width - 6;
        }

        class Entry extends ObjectSelectionList.Entry<Entry> {
            private final String varName;

            public Entry(String varName) {
                this.varName = varName;
            }

            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX,
                    int mouseY, boolean isMouseOver, float partialTick) {
                int color = isMouseOver ? 0xFFFFFF : 0xAAAAAA;
                guiGraphics.drawString(VarTagScreen.this.font, this.varName, left + 10, top + 2, color, false);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                VarTagScreen.this.editBox.setValue(this.varName);
                return true;
            }

            @Override
            public Component getNarration() {
                return Component.literal(this.varName);
            }
        }
    }
}
