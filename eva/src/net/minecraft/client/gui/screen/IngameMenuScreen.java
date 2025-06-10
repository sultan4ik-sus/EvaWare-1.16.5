package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;

import eva.ware.Evaware;
import eva.ware.modules.api.ModuleManager;
import eva.ware.modules.impl.misc.SelfDestruct;
import eva.ware.ui.mainmenu.MainScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.advancements.AdvancementsScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.realms.RealmsBridgeScreen;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class IngameMenuScreen extends Screen {
    private final boolean isFullMenu;

    public IngameMenuScreen(boolean isFullMenu) {
        super(isFullMenu ? new TranslationTextComponent("menu.game") : new TranslationTextComponent("menu.paused"));
        this.isFullMenu = isFullMenu;
    }

    @Override
    protected void init() {
        if (this.isFullMenu) {
            this.addButtons();
        }
    }

    private void reconnectToServer() {
        ServerData serverData = Minecraft.getInstance().getCurrentServerData();
        if (serverData != null || Minecraft.getInstance().player != null || Minecraft.getInstance().world != null || !Minecraft.getInstance().isSingleplayer()) {
            Minecraft.getInstance().world.sendQuittingDisconnectingPacket();
            Minecraft.getInstance().displayGuiScreen(new ConnectingScreen(this, Minecraft.getInstance(), serverData));
        }
    }

    private void addButtons() {
        int i = -16;
        int j = 98;

        if (!SelfDestruct.unhooked) {
            this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 120 + 8, 204, 20, new StringTextComponent("Reconnect"), (button) -> {
                reconnectToServer();
            }));
        }

        this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 24 + -16, 204, 20, new TranslationTextComponent("menu.returnToGame"), (button2) ->
        {
            this.minecraft.displayGuiScreen((Screen) null);
            this.minecraft.mouseHelper.grabMouse();
        }));
        this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 48 + -16, 98, 20, new TranslationTextComponent("gui.advancements"), (button2) ->
        {
            this.minecraft.displayGuiScreen(new AdvancementsScreen(this.minecraft.player.connection.getAdvancementManager()));
        }));
        this.addButton(new Button(this.width / 2 + 4, this.height / 4 + 48 + -16, 98, 20, new TranslationTextComponent("gui.stats"), (button2) ->
        {
            this.minecraft.displayGuiScreen(new StatsScreen(this, this.minecraft.player.getStats()));
        }));
        String s = SharedConstants.getVersion().isStable() ? "https://aka.ms/javafeedback?ref=game" : "https://aka.ms/snapshotfeedback?ref=game";
        this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 72 + -16, 98, 20, new TranslationTextComponent("menu.sendFeedback"), (button2) ->
        {
            this.minecraft.displayGuiScreen(new ConfirmOpenLinkScreen((open) -> {
                if (open) {
                    Util.getOSType().openURI(s);
                }

                this.minecraft.displayGuiScreen(this);
            }, s, true));
        }));
        this.addButton(new Button(this.width / 2 + 4, this.height / 4 + 72 + -16, 98, 20, new TranslationTextComponent("menu.reportBugs"), (button2) ->
        {
            this.minecraft.displayGuiScreen(new ConfirmOpenLinkScreen((open) -> {
                if (open) {
                    Util.getOSType().openURI("https://aka.ms/snapshotbugs?ref=game");
                }

                this.minecraft.displayGuiScreen(this);
            }, "https://aka.ms/snapshotbugs?ref=game", true));
        }));
        this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 96 + -16, 98, 20, new TranslationTextComponent("menu.options"), (button2) ->
        {
            this.minecraft.displayGuiScreen(new OptionsScreen(this, this.minecraft.gameSettings));
        }));
        Button button = this.addButton(new Button(this.width / 2 + 4, this.height / 4 + 96 + -16, 98, 20, new TranslationTextComponent("menu.shareToLan"), (button2) ->
        {
            this.minecraft.displayGuiScreen(new ShareToLanScreen(this));
        }));
        button.active = this.minecraft.isSingleplayer() && !this.minecraft.getIntegratedServer().getPublic();
        Button button1 = this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 120 + -16, 204, 20, new TranslationTextComponent("menu.returnToMenu"), (button2) ->
        {
            boolean flag = this.minecraft.isIntegratedServerRunning();
            boolean flag1 = this.minecraft.isConnectedToRealms();
            button2.active = false;
            this.minecraft.world.sendQuittingDisconnectingPacket();

            if (flag) {
                this.minecraft.unloadWorld(new DirtMessageScreen(new TranslationTextComponent("menu.savingLevel")));
            } else {
                this.minecraft.unloadWorld();
            }

            ModuleManager moduleManager = Evaware.getInst().getModuleManager();
            SelfDestruct selfDestruct = moduleManager.getSelfDestruct();

            if (flag) {
                this.minecraft.displayGuiScreen(selfDestruct.unhooked ? new MainMenuScreen() : new MainScreen());
            } else if (flag1) {
                RealmsBridgeScreen realmsbridgescreen = new RealmsBridgeScreen();
                realmsbridgescreen.func_231394_a_(selfDestruct.unhooked ? new MainMenuScreen() : new MainScreen());
            } else {
                this.minecraft.displayGuiScreen(new MultiplayerScreen(selfDestruct.unhooked ? new MainMenuScreen() : new MainScreen()));
            }
        }));

        if (!this.minecraft.isIntegratedServerRunning()) {
            button1.setMessage(new TranslationTextComponent("menu.disconnect"));
        }
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
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
