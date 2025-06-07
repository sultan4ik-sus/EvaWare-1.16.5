package newcode.fun.module.impl.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.potion.Effects;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.text.*;
import newcode.fun.module.api.Module;
import newcode.fun.utils.ClientUtils;
import newcode.fun.utils.IMinecraft;
import newcode.fun.utils.render.*;
import org.joml.Vector4d;
import newcode.fun.control.events.Event;
import newcode.fun.control.events.impl.render.EventRender;
import newcode.fun.control.Manager;
import newcode.fun.module.api.Annotation;
import newcode.fun.module.TypeList;
import newcode.fun.module.settings.imp.BooleanOption;
import newcode.fun.module.settings.imp.MultiBoxSetting;
import newcode.fun.module.settings.imp.SliderSetting;
import newcode.fun.utils.font.Fonts;
import newcode.fun.utils.math.MathUtil;
import newcode.fun.utils.math.PlayerPositionTracker;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Annotation(name = "NameTags", type = TypeList.Render, desc = "Отображает визуальные элементы к игрокам")
public class NameTags extends Module {
    public MultiBoxSetting elements = new MultiBoxSetting("Отображать",
            new BooleanOption("Эффекты", false),
            new BooleanOption("Броня", true),
            new BooleanOption("Чары брони", true));
    public SliderSetting size = new SliderSetting("Размер", 0.9f, 0.01f, 10f, 0.01f);

    public NameTags() {
        addSettings(elements);
    }
    public Object2ObjectOpenHashMap<Vector4d, PlayerEntity> positions = new Object2ObjectOpenHashMap<>();

    @Override
    public boolean onEvent(Event event) {
        if (event instanceof EventRender render) {
            if (render.isRender3D()) {
                updatePlayerPositions(render.partialTicks);
            }

            if (render.isRender2D()) {
                renderPlayerElements(render.matrixStack);
            }
        }
        return false;
    }

    private void updatePlayerPositions(float partialTicks) {
        this.positions.clear();
        Iterator var2 = mc.world.getPlayers().iterator();

        while(true) {
            PlayerEntity player;
            do {
                do {
                    do {
                        if (!var2.hasNext()) {
                            return;
                        }

                        player = (PlayerEntity)var2.next();
                    } while(!PlayerPositionTracker.isInView(player));
                } while(!player.botEntity);

                if (mc.gameSettings.getPointOfView() != PointOfView.FIRST_PERSON) {
                    break;
                }

                Minecraft var10001 = mc;
            } while(player == Minecraft.player);

            Vector4d position = PlayerPositionTracker.updatePlayerPositions(player, partialTicks);
            if (position != null) {
                this.positions.put(position, player);
            }
        }
    }


    private void renderPlayerElements(MatrixStack stack) {
        boolean isConnectedToServer = ClientUtils.isConnectedToServer("space-times") || ClientUtils.isConnectedToServer("reallyworld") || ClientUtils.isConnectedToServer("funtime") || ClientUtils.isConnectedToServer("legendsgrief");
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        IMinecraft.buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);

        for (Map.Entry<Vector4d, PlayerEntity> entry : positions.entrySet()) {
            Vector4d position = entry.getKey();
            PlayerEntity player = entry.getValue();

            PlayerEntity currentPlayer = getCurrentPlayer();

            if (isConnectedToServer && player != null && !player.equals(currentPlayer)) {
                updatePlayerHealth(player); 
            }
        }

        IMinecraft.tessellator.draw();
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();

        for (Map.Entry<Vector4d, PlayerEntity> entry : positions.entrySet()) {
            Vector4d position = entry.getKey();
            PlayerEntity player = entry.getValue();

            double x = position.x;
            double y = position.y;
            double endX = position.z;
            double endY = position.w;

            renderTags(stack, (float) x, (float) y, (float) endX, (float) endY, player);


            if (elements.get(0)) {
                renderEffects(player, (float) y, (float) endX, stack);
            }
        }
    }

    private PlayerEntity getCurrentPlayer() {
        return Minecraft.getInstance().player;
    }

    private void updatePlayerHealth(PlayerEntity player) {
        String myPlayerName = String.valueOf(mc.player.getName());

        if (player.getName().getString().equals(myPlayerName)) {
            return;
        }

        for (Map.Entry<ScoreObjective, Score> entry : IMinecraft.mc.world.getScoreboard().getObjectivesForEntity(player.getName().getString()).entrySet()) {
            Score score = entry.getValue();
            int newHealth = score.getScorePoints();
            player.setHealth(Math.max(newHealth, 1));
        }
    }

    private void renderEffects(PlayerEntity player,
                               float y,
                               float endX,
                               MatrixStack matrices) {
        EffectInstance[] effects = player.getActivePotionEffects().toArray(new EffectInstance[0]);
        int effectCount = effects.length;

        for (int i = 0; i < effectCount; i++) {
            EffectInstance p = effects[i];
            if (p == null) {
                continue;
            }
            String effectName = I18n.format(p.getEffectName());
            String effectAmplifier = I18n.format("enchantment.level." + (p.getAmplifier() + 1));
            String effectDuration = EffectUtils.getPotionDurationString(p, 1);
            String effectString = effectName + " " + effectAmplifier + TextFormatting.WHITE + " (" + TextFormatting.RED + effectDuration + TextFormatting.WHITE + ")" + TextFormatting.RESET;
            Fonts.newcode[10].drawStringWithShadow(matrices, effectString, endX + 2.5f, y - 2 + ((i + 1) * 7) - 0.5f, -1);
        }
    }

    private void renderTags(MatrixStack matrixStack, float posX, float posY, float endPosX, float endPosY, PlayerEntity entity) {
        float maxOffsetY = 0.0F;
        ITextComponent text = entity.getDisplayName();
        TextComponent name = (TextComponent) text;
        String modifiedText = text.getString();

        Map<String, String> replacements = new LinkedHashMap<>();
        replacements.put("HERO", TextFormatting.BLUE + "HERO" + TextFormatting.GRAY);
        replacements.put("TITAN", TextFormatting.YELLOW + "TITAN" + TextFormatting.GRAY);
        replacements.put("AVENGER", TextFormatting.GREEN + "AVENGER" + TextFormatting.GRAY);
        replacements.put("OVERLORD", TextFormatting.AQUA + "OVERLORD" + TextFormatting.GRAY);
        replacements.put("MAGISTER", TextFormatting.GOLD + "MAGISTER" + TextFormatting.GRAY);
        replacements.put("IMPERATOR", TextFormatting.RED + "IMPERATOR" + TextFormatting.GRAY);
        replacements.put("DRAGON", TextFormatting.LIGHT_PURPLE + "DRAGON" + TextFormatting.GRAY);
        replacements.put("D.HELPER", TextFormatting.GOLD + "D.HELPER" + TextFormatting.GRAY);
        replacements.put("BUNNY", TextFormatting.BLACK + "BUNNY" + TextFormatting.GRAY);
        replacements.put("RABBIT", TextFormatting.WHITE + "RABBIT" + TextFormatting.GRAY);
        replacements.put("BULL", TextFormatting.DARK_PURPLE + "BULL" + TextFormatting.GRAY);
        replacements.put("HYDRA", TextFormatting.DARK_GREEN + "HYDRA" + TextFormatting.GRAY);
        replacements.put("DRACULA", TextFormatting.DARK_RED + "DRACULA" + TextFormatting.GRAY);
        replacements.put("COBRA", TextFormatting.GREEN + "COBRA" + TextFormatting.GRAY);
        replacements.put("HELPER", TextFormatting.GOLD + "HELPER" + TextFormatting.GRAY);
        replacements.put("ST.HELPER", TextFormatting.GOLD + "ST.HELPER" + TextFormatting.GRAY);
        replacements.put("D.MODER", TextFormatting.BLUE + "D.MODER" + TextFormatting.GRAY);
        replacements.put("LUCIFER", TextFormatting.RED + "LUCIFER" + TextFormatting.GRAY);
        replacements.put("LOBBY", TextFormatting.GOLD + "LOBBY" + TextFormatting.GRAY);
        replacements.put("ADMIN", TextFormatting.RED + "ADMIN" + TextFormatting.GRAY);
        replacements.put("TikTok", TextFormatting.WHITE + "Tik" + TextFormatting.BLACK + "Tok" + TextFormatting.GRAY);
        replacements.put("TIGER", TextFormatting.GOLD + "TIGER" + TextFormatting.GRAY);
        replacements.put("LEGENDA", TextFormatting.GOLD + "LEGENDA" + TextFormatting.GRAY);
        replacements.put("Барон", TextFormatting.AQUA + "Барон" + TextFormatting.WHITE);
        replacements.put("Страж", TextFormatting.YELLOW + "Страж" + TextFormatting.WHITE);
        replacements.put("Герой", TextFormatting.GREEN + "Герой" + TextFormatting.WHITE);
        replacements.put("Аспид", TextFormatting.AQUA + "Аспид" + TextFormatting.WHITE);
        replacements.put("Сквид", TextFormatting.AQUA + "Сквид" + TextFormatting.WHITE);
        replacements.put("Глава", TextFormatting.GOLD + "Глава" + TextFormatting.WHITE);
        replacements.put("Элита", TextFormatting.DARK_PURPLE + "Элита" + TextFormatting.WHITE);
        replacements.put("Титан", TextFormatting.RED + "Титан" + TextFormatting.WHITE);
        replacements.put("Принц", TextFormatting.RED + "Принц" + TextFormatting.WHITE);
        replacements.put("Князь", TextFormatting.DARK_RED + "Князь" + TextFormatting.WHITE);
        replacements.put("Герцог", TextFormatting.DARK_RED + "Герцог" + TextFormatting.WHITE);
        replacements.put("принц", TextFormatting.GOLD + "принц" + TextFormatting.WHITE);
        replacements.put("киллер", TextFormatting.DARK_RED + "киллер" + TextFormatting.WHITE);

        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            if (modifiedText.contains(entry.getKey())) {
                modifiedText = modifiedText.replaceFirst(entry.getKey(), entry.getValue());
                break;
            }
        }

        ItemStack stack = entity.getHeldItemOffhand();
        String nameS = "";
        String itemName = stack.getDisplayName().getString();

        if (stack.getItem() == Items.PLAYER_HEAD) {
            CompoundNBT tag = stack.getTag();
            if (tag != null && tag.contains("display", 10)) {
                CompoundNBT display = tag.getCompound("display");
                if (display.contains("Lore", 9)) {
                    ListNBT lore = display.getList("Lore", 8);
                    if (!lore.isEmpty()) {
                        String firstLore = lore.getString(0);
                        int levelIndex = firstLore.indexOf("Уровень");
                        if (levelIndex != -1) {
                            String levelString = firstLore.substring(levelIndex + "Уровень".length()).trim();
                            if (levelString.contains("1/3")) {
                                nameS = TextFormatting.DARK_GRAY + "- " + TextFormatting.RED + "1" + TextFormatting.DARK_GRAY + "]";
                            } else if (levelString.contains("2/3")) {
                                nameS = TextFormatting.DARK_GRAY + "- " + TextFormatting.RED + "2" + TextFormatting.DARK_GRAY + "]";
                            } else if (levelString.contains("MAX")) {
                                nameS = TextFormatting.DARK_GRAY + "- " + TextFormatting.RED + "3" + TextFormatting.DARK_GRAY + "]";
                            } else {
                                nameS = "";
                            }
                        }
                    }
                }
            }

            if (itemName.contains("Пандо")) {
                itemName = TextFormatting.DARK_GRAY + " [" + TextFormatting.RED + "PANDORA " + nameS;
            } else if (itemName.contains("Аполл")) {
                itemName = TextFormatting.DARK_GRAY + " [" + TextFormatting.RED + "APOLLON " + nameS;
            } else if (itemName.contains("Атаку")) {
                itemName = "";
            } else if (itemName.contains("Тита")) {
                itemName = TextFormatting.DARK_GRAY + " [" + TextFormatting.RED + "TITANA " + nameS;
            } else if (itemName.contains("Осир")) {
                itemName = TextFormatting.DARK_GRAY + " [" + TextFormatting.RED + "OSIRIS " + nameS;
            } else if (itemName.contains("Андро")) {
                itemName = TextFormatting.DARK_GRAY + " [" + TextFormatting.RED + "ANDROMEDA" + nameS;
            } else if (itemName.contains("Хим")) {
                itemName = TextFormatting.DARK_GRAY + " [" + TextFormatting.RED + "XIMERA " + nameS;
            } else if (itemName.contains("Астр")) {
                itemName = TextFormatting.DARK_GRAY + " [" + TextFormatting.RED + "ASTREYA " + nameS;
            } else {
                itemName = "";
            }
        } else {
            itemName = "";
        }

        name = new StringTextComponent(modifiedText);

        String friendPrefix = Manager.FRIEND_MANAGER.isFriend(entity.getName().getString()) ? "" : "";
        ITextComponent friendText = ITextComponent.getTextComponentOrEmpty(friendPrefix);
        TextComponent friendPrefixComponent = (TextComponent) friendText;
        if (Manager.FRIEND_MANAGER.isFriend(entity.getName().getString()) && Manager.FUNCTION_MANAGER.nameProtect.state && Manager.FUNCTION_MANAGER.nameProtect.friends.get()) {
            friendPrefixComponent.append(new StringTextComponent(TextFormatting.RED + Manager.FUNCTION_MANAGER.nameProtect.name.text));
        } else {
            friendPrefixComponent.append(name);
        }

        name = friendPrefixComponent;
        TextFormatting var10003 = TextFormatting.WHITE;
        name.append(new StringTextComponent("" + var10003 + TextFormatting.DARK_GRAY + " [" + TextFormatting.RED + (int) entity.getHealth() + TextFormatting.DARK_GRAY + "]" + itemName));
        TextComponent finalName;
        finalName = new StringTextComponent(name.getString());

        String finalName2 = finalName.getString();
        float width = Fonts.blod[18].getWidth(finalName2);
        float height = 14.0F;

        int colorsbox2 = Manager.FRIEND_MANAGER.isFriend(entity.getName().getString()) ? (new java.awt.Color(72, 225, 57, 47)).getRGB() : (new java.awt.Color(28, 28, 28, 140)).getRGB();

        MathUtil.scaleElements((posX + endPosX) / 2.0F, posY - height / 2.0F + (mc.gameSettings.keyBindSneak.isPressed() ? -0.2F : 0.2F), 0.645f, () -> {
            if (mc.ingameGUI.getTabList().header == null) {
                return;
            }
            String string = TextFormatting.getTextWithoutFormattingCodes(mc.ingameGUI.getTabList().header.getString());
            if (!string.contains("Хаб") && ClientUtils.isConnectedToServer("funtime")) {
                RenderUtils.Render2D.drawRoundedRect((posX + endPosX) / 2.0F - width / 2.0F - 5.0F + 5f, posY - height - 10.0F + 1.0F + 0.75F, width + 10.0F - 2 - 5.5f, height - 0.5F, 0F, colorsbox2);
                Fonts.blod[18].drawString(matrixStack, finalName, (posX + endPosX) / 2f - width / 2f + 1, posY - height - 4.6F, -1);
            } else {
                if (!string.contains("Lobby") && ClientUtils.isConnectedToServer("reallyworld") || ClientUtils.isConnectedToServer("legendsgrief")) {
                    RenderUtils.Render2D.drawRoundedRect((posX + endPosX) / 2.0F - width / 2.0F - 5.0F + 6f, posY - height - 10.0F + 1.0F + 0.75F, width + 10.0F - 2 - 3 - 5.5f, height - 0.5F, 0F, colorsbox2);
                    Fonts.blod[18].drawString(matrixStack, finalName, (posX + endPosX) / 2f - width / 2f - 1, posY - height - 4.6F, -1);
                } else {
                    RenderUtils.Render2D.drawRoundedRect((posX + endPosX) / 2.0F - width / 2.0F - 5.0F + 3f, posY - height - 10.0F + 1.0F + 0.75F, width + 10.0F - 2 - 3, height - 0.5F, 0F, colorsbox2);
                    Fonts.blod[18].drawString(matrixStack, finalName, (posX + endPosX) / 2f - width / 2f + 1f, posY - height - 4.6F, -1);
                }
            }
        });

        maxOffsetY += 20.5f;

        List<ItemStack> stacks = new ArrayList<>(Arrays.asList(entity.getHeldItemMainhand(), entity.getHeldItemOffhand()));
        entity.getArmorInventoryList().forEach(stacks::add);
        stacks.removeIf(w -> w.getItem() instanceof AirItem);

        int totalSize = stacks.size() * 10;
        maxOffsetY += 19;

        AtomicInteger iterable = new AtomicInteger();
        if (elements.get(1)) {
            float finalMaxOffsetY = maxOffsetY;
            MathUtil.scaleElements((posX + endPosX) / 2.0F, posY - maxOffsetY / 2.0F, 0.66f, () -> {
                this.renderArmorAndEnchantment(stacks, matrixStack, posX, endPosX, posY, finalMaxOffsetY, totalSize, iterable);
            });
        }
    }

    public static void drawItemStack(ItemStack stack,
                                     double x,
                                     double y,
                                     String altText,
                                     boolean withoutOverlay) {

        RenderSystem.translated(x, y, 0.0);
        IMinecraft.mc.getItemRenderer().renderItemAndEffectIntoGUI(stack, 0, 0);
        if (!withoutOverlay) {
            if (ClientUtils.isConnectedToServer("holyworld") || ClientUtils.isConnectedToServer("hollyworld")) {
            } else {
                IMinecraft.mc.getItemRenderer().renderItemOverlayIntoGUI(IMinecraft.mc.fontRenderer, stack, 0, 0, altText);
            }
        }

        RenderSystem.translated(-x, -y, 0.0);

    }


    private void renderArmorAndEnchantment(List<ItemStack> stacks,
                                           MatrixStack matrixStack,
                                           float posX,
                                           float endPosX,
                                           float posY,
                                           float finalMaxOffsetY,
                                           int totalSize,
                                           AtomicInteger iterable) {
        float centerX = (posX + endPosX) / 2f;

        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            if (ClientUtils.isConnectedToServer("holyworld") || ClientUtils.isConnectedToServer("hollyworld")) {
                stack.setDamage(0);
            }
            float itemPosX = centerX + iterable.get() * 20 - totalSize + 2;
            float itemPosY = posY - finalMaxOffsetY + 7;

            boolean isSkull = stack.getItem() instanceof SkullItem;
            boolean isEnchantedTotem = false;

            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                CompoundNBT tag = stack.getTag();
                if (tag != null && tag.contains("Enchantments")) {
                    isEnchantedTotem = true;
                }
            }

            if (isSkull || isEnchantedTotem) {
                RenderUtils.Render2D.drawRoundedRect(itemPosX, itemPosY, 16, 16, 0.5f,
                        new java.awt.Color(246, 50, 50, 76).getRGB());
            }

            drawItemStack(stack, itemPosX, itemPosY, null, false);
            iterable.getAndIncrement();

            if (elements.get(2)) {
                ArrayList<String> enchantments = getEnchantment(stack);
                float enchantmentPosX = (posX + (endPosX - posX) / 2f + iterable.get() * 20) - totalSize - 11;
                float enchantmentPosY = itemPosY - 5;
                int i = 0;

                for (String enchantment : enchantments) {
                    Fonts.newcode[12].drawCenteredString(matrixStack, enchantment,
                            enchantmentPosX,
                            enchantmentPosY - (i * 7),
                            0xFFFFFFFF);
                    i++;
                }
            }
        }
    }

    private ArrayList<String> getEnchantment(ItemStack stack) {
        ArrayList<String> list = new ArrayList<>();
        Item item = stack.getItem();
        if (item instanceof AxeItem) {
            handleAxeEnchantments(list, stack);
        } else if (item instanceof ArmorItem) {
            handleArmorEnchantments(list, stack);
        } else if (item instanceof BowItem) {
            handleBowEnchantments(list, stack);
        } else if (item instanceof SwordItem) {
            handleSwordEnchantments(list, stack);
        } else if (item instanceof ToolItem) {
            handleToolEnchantments(list, stack);
        }
        return list;
    }

    private void handleAxeEnchantments(ArrayList<String> list, ItemStack stack) {
        int sharpness = EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, stack);
        int efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack);
        int unbreaking = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack);

        if (sharpness > 0) {
            list.add("Shr" + sharpness);
        }
        if (efficiency > 0) {
            list.add("Eff" + efficiency);
        }
        if (unbreaking > 0) {
            list.add("Unb" + unbreaking);
        }
    }

    private void handleArmorEnchantments(ArrayList<String> list, ItemStack stack) {
        int protection = EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, stack);
        int thorns = EnchantmentHelper.getEnchantmentLevel(Enchantments.THORNS, stack);
        int unbreaking = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack);
        int mending = EnchantmentHelper.getEnchantmentLevel(Enchantments.MENDING, stack);
        int feather = EnchantmentHelper.getEnchantmentLevel(Enchantments.FEATHER_FALLING, stack);
        int depth = EnchantmentHelper.getEnchantmentLevel(Enchantments.DEPTH_STRIDER, stack);
        int vanishingCurse = EnchantmentHelper.getEnchantmentLevel(Enchantments.VANISHING_CURSE, stack);
        int bindingCurse = EnchantmentHelper.getEnchantmentLevel(Enchantments.BINDING_CURSE, stack);
        int fireProt = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_PROTECTION, stack);
        int blastProt = EnchantmentHelper.getEnchantmentLevel(Enchantments.BLAST_PROTECTION, stack);

        if (vanishingCurse > 0) {
            list.add("Vanish ");
        }
        if (fireProt > 0) {
            list.add("Firp" + fireProt);
        }
        if (blastProt > 0) {
            list.add("Bla" + blastProt);
        }
        if (bindingCurse > 0) {
            list.add("Bindi" + bindingCurse);
        }
        if (depth > 0) {
            list.add("Dep" + depth);
        }
        if (feather > 0) {
            list.add("Fea" + feather);
        }
        if (protection > 0) {
            list.add("Pro" + protection);
        }
        if (thorns > 0) {
            list.add("Thr" + thorns);
        }
        if (mending > 0) {
            list.add("Men" + mending);
        }
        if (unbreaking > 0) {
            list.add("Unb" + unbreaking);
        }
    }

    private void handleBowEnchantments(ArrayList<String> list, ItemStack stack) {
        int vanishingCurse = EnchantmentHelper.getEnchantmentLevel(Enchantments.VANISHING_CURSE, stack);
        int bindingCurse = EnchantmentHelper.getEnchantmentLevel(Enchantments.BINDING_CURSE, stack);
        int infinity = EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack);
        int power = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);
        int punch = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, stack);
        int mending = EnchantmentHelper.getEnchantmentLevel(Enchantments.MENDING, stack);
        int flame = EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, stack);
        int unbreaking = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack);

        if (vanishingCurse > 0) {
            list.add("Vanish" + vanishingCurse);
        }
        if (bindingCurse > 0) {
            list.add("Binding" + bindingCurse);
        }
        if (infinity > 0) {
            list.add("Inf" + infinity);
        }
        if (power > 0) {
            list.add("Pow" + power);
        }
        if (punch > 0) {
            list.add("Pun" + punch);
        }
        if (mending > 0) {
            list.add("Men" + mending);
        }
        if (flame > 0) {
            list.add("Fla" + flame);
        }
        if (unbreaking > 0) {
            list.add("Unb" + unbreaking);
        }
    }

    private void handleSwordEnchantments(ArrayList<String> list, ItemStack stack) {
        int vanishingCurse = EnchantmentHelper.getEnchantmentLevel(Enchantments.VANISHING_CURSE, stack);
        int looting = EnchantmentHelper.getEnchantmentLevel(Enchantments.LOOTING, stack);
        int bindingCurse = EnchantmentHelper.getEnchantmentLevel(Enchantments.BINDING_CURSE, stack);
        int sweeping = EnchantmentHelper.getEnchantmentLevel(Enchantments.SWEEPING, stack);
        int sharpness = EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, stack);
        int knockback = EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK, stack);
        int fireAspect = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, stack);
        int unbreaking = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack);
        int mending = EnchantmentHelper.getEnchantmentLevel(Enchantments.MENDING, stack);

        if (vanishingCurse > 0) {
            list.add("Vanish" + vanishingCurse);
        }
        if (looting > 0) {
            list.add("Loot" + looting);
        }
        if (bindingCurse > 0) {
            list.add("Bindi" + bindingCurse);
        }
        if (sweeping > 0) {
            list.add("Swe" + sweeping);
        }
        if (sharpness > 0) {
            list.add("Shr" + sharpness);
        }
        if (knockback > 0) {
            list.add("Kno" + knockback);
        }
        if (fireAspect > 0) {
            list.add("Fir" + fireAspect);
        }
        if (unbreaking > 0) {
            list.add("Unb" + unbreaking);
        }
        if (mending > 0) {
            list.add("Men" + mending);
        }
    }

    private void handleToolEnchantments(ArrayList<String> list, ItemStack stack) {
        int unbreaking = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack);
        int mending = EnchantmentHelper.getEnchantmentLevel(Enchantments.MENDING, stack);
        int vanishingCurse = EnchantmentHelper.getEnchantmentLevel(Enchantments.VANISHING_CURSE, stack);
        int bindingCurse = EnchantmentHelper.getEnchantmentLevel(Enchantments.BINDING_CURSE, stack);
        int efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack);
        int silkTouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack);
        int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);

        if (unbreaking > 0) {
            list.add("Unb" + unbreaking);
        }
        if (mending > 0) {
            list.add("Men" + mending);
        }
        if (vanishingCurse > 0) {
            list.add("Vanish" + vanishingCurse);
        }
        if (bindingCurse > 0) {
            list.add("Binding" + bindingCurse);
        }
        if (efficiency > 0) {
            list.add("Eff" + efficiency);
        }
        if (silkTouch > 0) {
            list.add("Sil" + silkTouch);
        }
        if (fortune > 0) {
            list.add("For" + fortune);
        }
    }


}


