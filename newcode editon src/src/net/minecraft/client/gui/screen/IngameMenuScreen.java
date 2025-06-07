package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.advancements.AdvancementsScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.realms.RealmsBridgeScreen;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;

public class IngameMenuScreen extends Screen
{
    private final boolean isFullMenu;

    public IngameMenuScreen(boolean isFullMenu) {
        super(isFullMenu ? new TranslationTextComponent("menu.game") : new TranslationTextComponent("menu.paused"));
        this.isFullMenu = isFullMenu;
    }

    protected void init() {
        if (this.isFullMenu) {
            this.addButtons();
        }

    }

    private void addButtons() {
        this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 144 - 24 + -16, 204, 20, new TranslationTextComponent("Reconect"), (button2) -> {
            this.reconnectToServer();
        }));
        this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 24 + -16, 204, 20, new TranslationTextComponent("menu.returnToGame"), (button2) -> {
            this.minecraft.displayGuiScreen((Screen)null);
            this.minecraft.mouseHelper.grabMouse();
        }));
        this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 48 + -16, 98, 20, new TranslationTextComponent("gui.advancements"), (button2) -> {
            Minecraft var10003 = this.minecraft;
            this.minecraft.displayGuiScreen(new AdvancementsScreen(Minecraft.player.connection.getAdvancementManager()));
        }));
        this.addButton(new Button(this.width / 2 + 4, this.height / 4 + 48 + -16, 98, 20, new TranslationTextComponent("gui.stats"), (button2) -> {
            Minecraft var10004 = this.minecraft;
            this.minecraft.displayGuiScreen(new StatsScreen(this, Minecraft.player.getStats()));
        }));
        String s = SharedConstants.getVersion().isStable() ? "https://aka.ms/javafeedback?ref=game" : "https://aka.ms/snapshotfeedback?ref=game";
        this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 96 - 24 + -16, 98, 20, new TranslationTextComponent("menu.options"), (button2) -> {
            this.minecraft.displayGuiScreen(new OptionsScreen(this, this.minecraft.gameSettings));
        }));
        Button button = (Button)this.addButton(new Button(this.width / 2 + 4, this.height / 4 + 96 - 24 + -16, 98, 20, new TranslationTextComponent("menu.shareToLan"), (button2) -> {
            this.minecraft.displayGuiScreen(new ShareToLanScreen(this));
        }));
        button.active = this.minecraft.isSingleplayer() && !this.minecraft.getIntegratedServer().getPublic();
        Button button1 = (Button)this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 120 - 24 + -16, 204, 20, new TranslationTextComponent("menu.returnToMenu"), (button2) -> {
            boolean flag = this.minecraft.isIntegratedServerRunning();
            boolean flag1 = this.minecraft.isConnectedToRealms();
            button2.active = false;
            this.minecraft.world.sendQuittingDisconnectingPacket();
            if (flag) {
                this.minecraft.unloadWorld(new DirtMessageScreen(new TranslationTextComponent("menu.savingLevel")));
            } else {
                this.minecraft.unloadWorld();
            }

            if (flag) {
                this.minecraft.displayGuiScreen(new MainMenuScreen());
            } else if (flag1) {
                RealmsBridgeScreen realmsbridgescreen = new RealmsBridgeScreen();
                realmsbridgescreen.func_231394_a_(new MainMenuScreen());
            } else {
                this.minecraft.displayGuiScreen(new MultiplayerScreen(new MainMenuScreen()));
            }

        }));
        if (!this.minecraft.isIntegratedServerRunning()) {
            button1.setMessage(new TranslationTextComponent("menu.disconnect"));
        }

        if (!this.minecraft.isIntegratedServerRunning()) {
            button1.setMessage(new TranslationTextComponent("menu.disconnect"));
        }

    }

    private void reconnectToServer() {
        if (this.minecraft.getCurrentServerData() != null) {
            ServerData serverData = this.minecraft.getCurrentServerData();

            if (this.minecraft.world != null) {
                this.minecraft.world.sendQuittingDisconnectingPacket();
                this.minecraft.unloadWorld();
            }

            this.minecraft.displayGuiScreen(new ConnectingScreen(new MultiplayerScreen(new MainMenuScreen()), this.minecraft, serverData));
        }
    }

    public void tick() {
        super.tick();
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (this.isFullMenu) {
            this.renderBackground(matrixStack);
            drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 40, 16777215);
        } else {
            drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 10, 16777215);
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}

