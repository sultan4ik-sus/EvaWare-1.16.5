package net.minecraft.client.gui.screen.inventory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import newcode.fun.control.Manager;
import newcode.fun.utils.ClientUtils;
import newcode.fun.utils.misc.TimerUtil;

import java.awt.*;

import static newcode.fun.utils.IMinecraft.mc;

public class ChestScreen extends ContainerScreen<ChestContainer> implements IHasContainer<ChestContainer> {
    private final TimerUtil timerUtil = new TimerUtil();
    private boolean buttonSpawned = false;

    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    private final int inventoryRows;

    public ChestScreen(ChestContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.passEvents = false;
        this.inventoryRows = container.getNumRows();
        this.ySize = 114 + this.inventoryRows * 18;
        this.playerInventoryTitleY = this.ySize - 94;
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        if (!buttonSpawned) {
            this.addButton(new net.minecraft.client.gui.widget.button.Button(this.width / 2 + 90, (this.height - this.ySize) / 2 + 24, 60, 20, new StringTextComponent("Сложить"), (button) -> {
                if (mc.player.openContainer instanceof ChestContainer) {
                    ChestContainer container = (ChestContainer) mc.player.openContainer;
                    boolean chestFull = true;

                    for (int index = 0; index < container.getLowerChestInventory().getSizeInventory(); index++) {
                        if (container.getLowerChestInventory().getStackInSlot(index).isEmpty()) {
                            chestFull = false;
                            break;
                        }
                    }

                    for (int index = 27; index < container.inventorySlots.size(); index++) {
                        if (!container.getInventory().get(index).isEmpty() && this.timerUtil.hasTimeElapsed(30L)) {
                            mc.playerController.windowClick(container.windowId, index, 0, ClickType.QUICK_MOVE, mc.player);
                        }

                        if (chestFull) {
                            break;
                        }
                    }

                    this.timerUtil.reset();
                }
            }));

            this.addButton(new Button(width / 2 + 90, (this.height - this.ySize) / 2, 60, 20, new StringTextComponent("Взять"), (button) -> {
                if (mc.player.openContainer instanceof ChestContainer) {
                    ChestContainer container = (ChestContainer) mc.player.openContainer;
                    for (int index = 0; index < container.inventorySlots.size(); index++) {
                        if (container.getLowerChestInventory().getStackInSlot(index).getItem() != Item.getItemById(0) && timerUtil.hasTimeElapsed(30)) {
                            mc.playerController.windowClick(container.windowId, index, 0, ClickType.QUICK_MOVE, mc.player);
                        }
                        if (container.getLowerChestInventory().isEmpty()) {
                            timerUtil.reset();
                        }
                    }
                }
            }));
            buttonSpawned = true;
        }

        this.minecraft.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.blit(matrixStack, i, j, 0, 0, this.xSize, this.inventoryRows * 18 + 17);
        this.blit(matrixStack, i, j + this.inventoryRows * 18 + 17, 0, 126, this.xSize, 96);

        if (!ClientUtils.isConnectedToServer("skytime") && Manager.FUNCTION_MANAGER.assistent.state && Manager.FUNCTION_MANAGER.assistent.mode.is("FunTime") && Manager.FUNCTION_MANAGER.assistent.sell.get()) {
            highlightCheapestItems(matrixStack);
        }
    }

    private void highlightCheapestItems(MatrixStack matrixStack) {
        ChestContainer container = this.getContainer();
        int highlightColor = new Color(77, 214, 32, 137).getRGB();
        int redHighlightColor = new Color(255, 0, 0, 137).getRGB();
        int yellowLowColor = new Color(77, 214, 32, 137).getRGB();

        String titleLower = this.title.getString().toLowerCase();
        boolean isRelevantScreen = titleLower.contains("поиск") || titleLower.contains("аукц");

        if (!isRelevantScreen) {
            return;
        }

        boolean isExperienceScreen = titleLower.contains("опыт") || titleLower.contains("перка");
        boolean isElytraScreen = titleLower.contains("элитры");
        int lowestPrice = Integer.MAX_VALUE;
        Slot targetSlot = null;
        Slot enchantedTargetSlot = null;
        int lowestEnchantedPrice = Integer.MAX_VALUE;

        if (isElytraScreen) {
            for (Slot slot : container.inventorySlots) {
                if (slot.slotNumber > 44) {
                    continue;
                }

                ItemStack stack = slot.getStack();
                if (stack.isEmpty() || !stack.getItem().getTranslationKey().contains("elytra")) {
                    continue;
                }

                if (stack.getMaxDamage() - stack.getDamage() < stack.getMaxDamage()) {
                    continue;
                }

                int currentPrice = extractPriceFromStack(stack);
                if (currentPrice != -1 && currentPrice < lowestPrice) {
                    lowestPrice = currentPrice;
                    targetSlot = slot;
                }
            }
        }

        if (targetSlot != null && isElytraScreen) {
            fill(matrixStack, guiLeft + targetSlot.xPos, guiTop + targetSlot.yPos,
                    guiLeft + targetSlot.xPos + 16, guiTop + targetSlot.yPos + 16, yellowLowColor);
        }

        if (isExperienceScreen) {
            for (Slot slot : container.inventorySlots) {
                if (slot.slotNumber > 44) {
                    continue;
                }

                ItemStack stack = slot.getStack();
                if (titleLower.contains("перка")) {
                    if (stack.isEmpty() || stack.getCount() != 16) {
                        continue;
                    }
                }
                if (titleLower.contains("опыт")) {
                    if (stack.isEmpty() || stack.getCount() != 64) {
                        continue;
                    }
                }

                int currentPrice = extractPriceFromStack(stack);
                if (currentPrice != -1 && currentPrice < lowestPrice) {
                    lowestPrice = currentPrice;
                    targetSlot = slot;
                }
            }
        }

        if (targetSlot == null) {
            for (Slot slot : container.inventorySlots) {
                if (slot.slotNumber > 44) {
                    continue;
                }

                ItemStack stack = slot.getStack();
                if (stack.isEmpty()) {
                    continue;
                }

                int currentPrice = extractPriceFromStack(stack);
                if (currentPrice != -1 && currentPrice < lowestPrice) {
                    lowestPrice = currentPrice;
                    targetSlot = slot;
                }
            }
        }

        if (targetSlot != null && !isElytraScreen) {
            fill(matrixStack, guiLeft + targetSlot.xPos, guiTop + targetSlot.yPos,
                    guiLeft + targetSlot.xPos + 16, guiTop + targetSlot.yPos + 16, highlightColor);
        }

        for (Slot slot : container.inventorySlots) {
            if (slot.slotNumber > 44) {
                continue;
            }

            ItemStack stack = slot.getStack();
            if (!stack.isEmpty() && hasValidEnchantments(stack)) {
                int currentPrice = extractPriceFromStack(stack);
                if (currentPrice != -1 && currentPrice < lowestEnchantedPrice) {
                    lowestEnchantedPrice = currentPrice;
                    enchantedTargetSlot = slot;
                }
            }
        }

        if (enchantedTargetSlot != null) {
            fill(matrixStack, guiLeft + enchantedTargetSlot.xPos, guiTop + enchantedTargetSlot.yPos,
                    guiLeft + enchantedTargetSlot.xPos + 16, guiTop + enchantedTargetSlot.yPos + 16, redHighlightColor);
        }
    }

    private boolean hasValidEnchantments(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag != null && tag.contains("Enchantments", 9)) {
            ListNBT enchantments = tag.getList("Enchantments", 10);
            boolean hasProtectionV = false;
            boolean hasMending = false;
            boolean hasSharpnessVII = false;
            boolean hasKnockback = false;
            boolean hasThorns = false;

            for (int i = 0; i < enchantments.size(); i++) {
                CompoundNBT enchantment = enchantments.getCompound(i);
                String id = enchantment.getString("id");
                int lvl = enchantment.getInt("lvl");

                if (id.equals("minecraft:protection") && lvl == 5) {
                    hasProtectionV = true;
                }
                if (id.equals("minecraft:mending")) {
                    hasMending = true;
                }
                if (id.equals("minecraft:sharpness") && lvl == 7) {
                    hasSharpnessVII = true;
                }
                if (id.equals("minecraft:knockback")) {
                    hasKnockback = true;
                }
                if (id.equals("minecraft:thorns")) {
                    hasThorns = true;
                }
            }

            return (hasProtectionV && hasMending && !hasThorns) ||
                    (hasSharpnessVII && !hasKnockback);
        }
        return false;
    }


    protected int extractPriceFromStack(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag != null && tag.contains("display", 10)) {
            CompoundNBT display = tag.getCompound("display");
            if (display.contains("Lore", 9)) {
                ListNBT lore = display.getList("Lore", 8);
                for (int j = 0; j < lore.size(); j++) {
                    try {
                        JsonObject object = new JsonParser().parse(lore.getString(j)).getAsJsonObject();
                        if (object.has("extra")) {
                            JsonArray array = object.getAsJsonArray("extra");
                            if (array.size() > 2) {
                                JsonObject title = array.get(1).getAsJsonObject();
                                if (title.get("text").getAsString().trim().toLowerCase().contains("ценa")) {
                                    String line = array.get(2).getAsJsonObject().get("text").getAsString().trim().substring(1).replaceAll(",", "");
                                    return Integer.parseInt(line);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return -1;
    }
}