package newcode.fun.ui.clickgui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.MathHelper;
import newcode.fun.ui.clickgui.objects.ModuleObject;
import newcode.fun.ui.clickgui.objects.Object;
import newcode.fun.control.Manager;
import newcode.fun.module.api.Module;
import newcode.fun.module.TypeList;
import newcode.fun.ui.midnight.StyleManager;
import newcode.fun.utils.SoundUtils;
import newcode.fun.utils.render.*;
import newcode.fun.utils.render.animation.AnimationMath;
import org.joml.Vector4i;
import org.lwjgl.opengl.GL11;
import newcode.fun.utils.font.Fonts;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

import static newcode.fun.utils.IMinecraft.sr;
import static newcode.fun.utils.render.ColorUtils.rgba;

public class Panel {
    TypeList typeList;
    float x;
    float y;
    float width;
    float height;
    float scrolling;
    float scrollingOut;
    float animationProgress;
    ArrayList<ModuleObject> moduleObjects = new ArrayList<>();
    TypeList current;
    private boolean isOpen;

    public Panel(TypeList typeList, float x, float y, float width, float height) {
        this.typeList = typeList;
        this.x = x + 73;
        this.y = y + 19f;
        this.width = width - 25;
        this.height = height - 87f;
        this.animationProgress = 0.0f;
        this.isOpen = false;
        for (Module m2 : Manager.FUNCTION_MANAGER.getFunctions().stream().filter(m -> m.category == typeList).toList()) {
            this.moduleObjects.add(new ModuleObject(m2));
        }
    }

    int firstColor = ColorUtils.getColorStyle(0);
    int secondColor = ColorUtils.getColorStyle(90);
    int firstColor2 = (Manager.FUNCTION_MANAGER.hud2.mode.is("13") ? ColorUtils.getColorStyle(0.0F) : StyleManager.getCurrentStyle().getSecondaryColor());
    int secondColor2 = (Manager.FUNCTION_MANAGER.hud2.mode.is("13") ? ColorUtils.getColorStyle(91F) : StyleManager.getCurrentStyle().getFerstyColor());

    public void render(MatrixStack matrixStack, int mouseX, int mouseY) {
        MatrixStack ms = new MatrixStack();
        this.scrollingOut = AnimationMath.fast(this.scrollingOut, this.scrolling, 15.0f);
        GaussianBlur.startBlur();
        RenderUtils.Render2D.drawRoundedRect(this.x + 5, y, this.width - 9, this.height - 1 + 4 + 31, 4, ColorUtils.rgba(16, 16, 16, 200));
        GaussianBlur.endBlur(6, 1);

        RenderUtils.Render2D.drawRoundedRect(this.x + 5, y, this.width - 9, this.height - 1 + 4 + 31, 4, ColorUtils.rgba(16, 16, 16, 200));

        Fonts.newcode[18].drawCenteredString(ms, this.typeList.name(), (double) (this.x + this.width / 2.0f), (double) (y + 7f), firstColor2);
        float offset = -4f;
        float off = 11f;
        StencilUtils.initStencilToWrite();
        RenderUtils.Render2D.drawGradientRound(this.x + 5, y, this.width - 9, this.height - 4, 4.6f, ColorUtils.setAlpha(secondColor, 50), ColorUtils.setAlpha(firstColor, 50), ColorUtils.setAlpha(secondColor, 50), ColorUtils.setAlpha(firstColor, 50));
        StencilUtils.readStencilBuffer(1);

        float originalWidth = this.width - 1.0f;
        float originalHeight = 15.0f;

        for (ModuleObject m : this.moduleObjects) {
            if (Window.searching) {
                String moduleName = m.module.name.toLowerCase();
                if (!moduleName.contains(Window.searchText.toLowerCase())) {
                    continue;
                }
            }

            SmartScissor.push();
            SmartScissor.setFromComponentCoordinates((int) this.x, (int) y + 18, (int) this.width, (int) this.height + 12);

            m.width = originalWidth;
            m.height = originalHeight;
            m.x = this.x + 1.0f;
            m.y = y + off + offset + this.scrollingOut + 12.5f;

            float totalHeight = 0;
            for (Object object1 : m.object) {
                if (object1.setting != null && object1.setting.visible()) {
                    totalHeight += object1.height;
                }
            }

            float moduleHeight = m.module.expanded ? Math.max(m.height, totalHeight) : m.height;

            float nextModuleY = m.y + moduleHeight + 21;

            if (this.moduleObjects.indexOf(m) < this.moduleObjects.size() - 1) {
                ModuleObject nextModule = this.moduleObjects.get(this.moduleObjects.indexOf(m) + 1);
                float nextModuleYEnd = nextModule.y + 20;
                float spaceBetweenModules = nextModuleYEnd - nextModuleY;

                if (spaceBetweenModules > 0) {
                    moduleHeight += spaceBetweenModules;
                }
            } else {
                if (m.module.expanded) {
                    float remainingSpace = (this.y + this.height) - (m.y + moduleHeight + 21);
                    moduleHeight += remainingSpace + 51f; // Add additional space to the end of the panel
                }
            }

            if (Window.searching) {
                if (m.module.expanded) {
                    if (m.module.state) {
                        RenderUtils.Render2D.drawShadow(m.x + 7.8f, m.y + 5 + 2, 0.5f, 5, 0, ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0));
                        RenderUtils.Render2D.drawShadow(m.x + 7.8f, m.y + 2, 0.5f, 5, 0, ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255));

                        RenderUtils.Render2D.drawRoundedRect(m.x + 7.5f, m.y, m.width - 20 + 5, m.height + (totalHeight * 1.6f), 3.0f, new Color(101, 101, 101, 26).getRGB());
                        RenderUtils.Render2D.drawRoundOutline(m.x + 7.5f, m.y, m.width - 20 + 5, m.height + (totalHeight * 1.6f), 3, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(new Color(73, 73, 73, 128).getRGB(), new Color(73, 73, 73, 128).getRGB(), new Color(73, 73, 73, 128).getRGB(), new Color(73, 73, 73, 128).getRGB()));
                    } else {
                        RenderUtils.Render2D.drawRoundedRect(m.x + 7.5f, m.y, m.width - 20 + 5, m.height + (totalHeight * 1.6f), 3.0f, new Color(58, 58, 58, 26).getRGB());
                        RenderUtils.Render2D.drawRoundOutline(m.x + 7.5f, m.y, m.width - 20 + 5, m.height + (totalHeight * 1.6f), 3, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(new Color(48, 48, 48, 128).getRGB(), new Color(48, 48, 48, 128).getRGB(), new Color(48, 48, 48, 128).getRGB(), new Color(48, 48, 48, 128).getRGB()));
                    }
                } else {
                    if (m.module.state) {
                        RenderUtils.Render2D.drawShadow(m.x + 7.8f, m.y + 5 + 2, 0.5f, 5, 0, ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0));
                        RenderUtils.Render2D.drawShadow(m.x + 7.8f, m.y + 2, 0.5f, 5, 0, ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255));

                        RenderUtils.Render2D.drawRoundedRect(m.x + 7.5f, m.y, m.width - 20 + 5, m.height, 3.0f, new Color(101, 101, 101, 26).getRGB());
                        RenderUtils.Render2D.drawRoundOutline(m.x + 7.5f, m.y, m.width - 20 + 5, m.height, 3, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(new Color(73, 73, 73, 128).getRGB(), new Color(73, 73, 73, 128).getRGB(), new Color(73, 73, 73, 128).getRGB(), new Color(73, 73, 73, 128).getRGB()));
                    } else {
                        RenderUtils.Render2D.drawRoundedRect(m.x + 7.5f, m.y, m.width - 20 + 5, m.height, 3.0f, new Color(58, 58, 58, 26).getRGB());
                        RenderUtils.Render2D.drawRoundOutline(m.x + 7.5f, m.y, m.width - 20 + 5, m.height, 3, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(new Color(48, 48, 48, 128).getRGB(), new Color(48, 48, 48, 128).getRGB(), new Color(48, 48, 48, 128).getRGB(), new Color(48, 48, 48, 128).getRGB()));
                    }
                }
            } else {
                if (m.module.state) {
                    RenderUtils.Render2D.drawShadow(m.x + 7.8f, m.y + 5 + 2, 0.5f, 5, 0, ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0));
                    RenderUtils.Render2D.drawShadow(m.x + 7.8f, m.y + 2, 0.5f, 5, 0, ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255), ColorUtils.setAlpha(firstColor2, 0), ColorUtils.setAlpha(firstColor2, 255));

                    RenderUtils.Render2D.drawRoundedRect(m.x + 7.5f, m.y, m.width - 20 + 5, moduleHeight, 3.0f, new Color(101, 101, 101, 26).getRGB());
                    RenderUtils.Render2D.drawRoundOutline(m.x + 7.5f, m.y, m.width - 20 + 5, moduleHeight, 3, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(new Color(73, 73, 73, 128).getRGB(), new Color(73, 73, 73, 128).getRGB(), new Color(73, 73, 73, 128).getRGB(), new Color(73, 73, 73, 128).getRGB()));
                } else {
                    RenderUtils.Render2D.drawRoundedRect(m.x + 7.5f, m.y, m.width - 20 + 5, moduleHeight, 3.0f, new Color(58, 58, 58, 26).getRGB());
                    RenderUtils.Render2D.drawRoundOutline(m.x + 7.5f, m.y, m.width - 20 + 5, moduleHeight, 3, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(new Color(48, 48, 48, 128).getRGB(), new Color(48, 48, 48, 128).getRGB(), new Color(48, 48, 48, 128).getRGB(), new Color(48, 48, 48, 128).getRGB()));
                }
            }

            java.lang.Object name = m.module.name;
            if (m.isBinding) {
                name = (String) "Select a Key..";
            }
            if (m.module.state) {
                RenderUtils.Render2D.drawRoundOutline((float) ((double) (this.x + 14.0f) + 0.5f) - 3, (float) (double) (y + off + offset + this.scrollingOut + 18.6f) + 0.5f - 1, 4, 4, 2, 0f, ColorUtils.rgba(25, 26, 33, 0), new Vector4i(ColorUtils.setAlpha(firstColor2, 140), ColorUtils.setAlpha(firstColor2, 140), ColorUtils.setAlpha(firstColor2, 140), ColorUtils.setAlpha(firstColor2, 140)));
                RenderUtils.Render2D.drawRoundedRect((float) (double) (this.x + 14.0f) + 1 - 3, (float) (double) (y + off + offset + this.scrollingOut + 18.6f) + 1f - 1, 3, 3f, 1, firstColor2);

                Fonts.newcode[14].drawString(ms, (String) name, (double) (this.x + 18.0f), (double) (y + off + offset + this.scrollingOut + 18.6f), new Color(255, 255, 255, 255).getRGB());
            } else {
                Fonts.newcode[14].drawString(ms, (String) name, (double) (this.x + 13.0f), (double) (y + off + offset + this.scrollingOut + 18.6f), ColorUtils.setAlpha(new Color(140, 140, 140, 128).getRGB(), 255));
            }
            float size = 10.0f;
            float sizeY = 15.0f;
            m.expand_anim = AnimationMath.fast(m.expand_anim, m.module.expanded ? 1.0f : 0.0f, 10.0f);
            GL11.glPushMatrix();
            MatrixStack s = new MatrixStack();
            GL11.glTranslatef((float) -5.0f, (float) 0.0f, (float) 0.0f);
            if (m.module.state) {
                if (!m.module.settingList.isEmpty()) {
                    Fonts.newcode[16].drawCenteredString(ms, "...", (float) (m.x + m.width - size), (float) (m.y + 4.5f), firstColor2);
                }
            } else {
                if (!m.module.settingList.isEmpty()) {
                    Fonts.newcode[16].drawCenteredString(ms, "...", (float) (m.x + m.width - size), (float) (m.y + 4.5f), ColorUtils.setAlpha(new Color(140, 140, 140, 128).getRGB(), 255));
                }
            }

            GL11.glPopMatrix();
            float yd = 6.0f;
            for (Object object1 : m.object) {
                object1.x = this.x;
                object1.y = y + yd + off + offset + this.scrollingOut + 25.0f;
                object1.width = this.width;
                object1.height = 10.0f;
                if (object1.setting == null || !object1.setting.visible()) continue;
                if ((double) m.expand_anim > 0.5) {
                    object1.draw(ms, mouseX, mouseY);
                }
                off += (object1.height + 9.5f) * m.expand_anim;
            }
            off += offset + 20.0f;
            SmartScissor.pop();
            StencilUtils.uninitStencilBuffer();
        }

        float max2 = off - 37;
        this.scrolling = max2 < this.height - 6.0f ? 0.0f : MathHelper.clamp(this.scrolling, -(max2 - (this.height - 16.0f)), 0.0f);

    }

    public static void search(MatrixStack matrixStack) {
        MatrixStack ms = new MatrixStack();
        GaussianBlur.startBlur();
        RenderUtils.Render2D.drawRoundedRect(sr.scaledWidth() / 2 - 55, sr.scaledHeight() / 1.17f, 110, 13, 4, ColorUtils.rgba(16, 16, 16, 200));
        GaussianBlur.endBlur(6, 1);
        RenderUtils.Render2D.drawRoundedRect(sr.scaledWidth() / 2 - 55, sr.scaledHeight() / 1.17f, 110, 13, 4, ColorUtils.rgba(16, 16, 16, 200));
        Fonts.icon[14].drawCenteredString(ms, "l", sr.scaledWidth() / 2 - 46f, sr.scaledHeight() / 1.17f + 5.5f, ColorUtils.rgba(200, 200, 200, 200));
    }

    private boolean isMouseOverButton(int mouseX, int mouseY, int buttonX, int buttonY, int buttonWidth, int buttonHeight) {
        return mouseX >= buttonX && mouseY >= buttonY && mouseX < buttonX + buttonWidth && mouseY < buttonY + buttonHeight;
    }

    public void onClick(double mouseX, double mouseY, int button) {
        Vec2i mo = ScaleMath.getMouse((int) mouseX, (int) mouseY);
        mouseX = mo.getX();
        mouseY = mo.getY();

        int zoneX = (int) this.x;
        int zoneY = (int) this.y + 18;
        int zoneWidth = (int) this.width;
        int zoneHeight = (int) this.height + 12;

        if (RenderUtils.isInRegion(mouseX, mouseY, zoneX, zoneY, zoneWidth, zoneHeight)) {
            float offset = -4f;
            float off = 11f;

            for (ModuleObject m : this.moduleObjects) {
                m.mouseClicked((int) mouseX, (int) mouseY, button);

                if (RenderUtils.isInRegion(mouseX, mouseY, m.x + 8, m.y, m.width - 20 + 4, m.height) && button == 1) {
                    m.module.expanded = !m.module.expanded;

                    if (m.module.expanded && !isOpen) {
                        if (Manager.FUNCTION_MANAGER.clickGui.sounds.get()) {
                            SoundUtils.playSound("moduleopen.wav", 60, false);
                        }
                        isOpen = true;
                    } else if (!m.module.expanded && isOpen) {
                        if (Manager.FUNCTION_MANAGER.clickGui.sounds.get()) {
                            SoundUtils.playSound("moduleclose.wav", 60, false);
                        }
                        isOpen = false;
                    }
                }

                if (m.module.expanded) {
                    float yd = 5.0f;
                    for (Object object1 : m.object) {
                        if (object1.setting == null) {
                            continue;
                        }

                        if (object1.setting.visible()) {
                            object1.y = this.y + yd + off + offset + this.scrollingOut + 25.0f;
                            off += object1.height + 5.0f;
                        }
                    }
                }

                off += offset + 20.0f;
            }
        }
    }


    public void onScroll(double mouseX, double mouseY, double delta) {
        Vec2i m = ScaleMath.getMouse((int) mouseX, (int) mouseY);
        if (RenderUtils.isInRegion(mouseX = (double) m.getX(), mouseY = (double) m.getY(), this.x, this.y, this.width, this.height + 32)) {
            this.scrolling += (float) (delta * 25.0);
        }
    }

    public void onRelease(double mouseX, double mouseY, int button) {
        Vec2i mo = ScaleMath.getMouse((int) mouseX, (int) mouseY);
        mouseX = mo.getX();
        mouseY = mo.getY();
        float offset = -4f;
        float off = 11f;
        for (ModuleObject m : this.moduleObjects) {
            for (Object o : m.object) {
                o.mouseReleased((int) mouseX, (int) mouseY, button);
            }
        }
    }

    public void onKey(int keyCode, int scanCode, int modifiers) {
        if (Window.searching) {
            int volume = new Random().nextInt(13) + 50;
            if (Manager.FUNCTION_MANAGER.clickGui.sounds.get()) {
                SoundUtils.playSound("clack.wav", volume, false);
            }
        }

        for (ModuleObject m : this.moduleObjects) {
            m.keyTyped(keyCode, scanCode, modifiers);
        }
    }
}
