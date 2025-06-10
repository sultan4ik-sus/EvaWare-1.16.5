package eva.ware.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import com.mojang.authlib.GameProfile;
import eva.ware.events.EventMotion;
import eva.ware.events.EventUpdate;
import eva.ware.modules.api.Category;
import eva.ware.modules.api.Module;
import eva.ware.modules.api.ModuleRegister;
import eva.ware.utils.math.MathUtility;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector2f;

import java.util.UUID;

@ModuleRegister(name = "FakePlayer", category = Category.Misc)
public class FakePlayer extends Module {

    private RemoteClientPlayerEntity fakePlayer;

    public FakePlayer() {
        setEnabled(false, true);
    }

    @Subscribe
    public void onMotion(EventMotion e) {
        if (fakePlayer == null) return;
        Vector2f rot = MathUtility.rotationToEntity(mc.player);
        fakePlayer.rotationYaw = rot.x;
        fakePlayer.rotationPitch = rot.y;
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {

    }

    private void spawnFakePlayer() {
        UUID var1 = UUID.nameUUIDFromBytes("1337".getBytes());
        fakePlayer = new RemoteClientPlayerEntity(FakePlayer.mc.world, new GameProfile(var1, "Eva Bot"));
        fakePlayer.copyLocationAndAnglesFrom(mc.player);
        fakePlayer.rotationYawHead = mc.player.rotationYawHead;
        fakePlayer.renderYawOffset = mc.player.renderYawOffset;
        fakePlayer.rotationPitchHead = mc.player.rotationPitchHead;
        fakePlayer.container = mc.player.container;
        fakePlayer.inventory = mc.player.inventory;
        fakePlayer.setHealth(1337.0f);
        mc.world.addEntity(1337, fakePlayer);
    }

    @Override
    public void onDisable() {
        removeFakePlayer();
        super.onDisable();
    }

    @Override
    public void onEnable() {
        spawnFakePlayer();
        super.onEnable();
    }

    protected float[] rotations(PlayerEntity player) {
        return new float[0];
    }

    private void removeFakePlayer() {
        FakePlayer.mc.world.removeEntityFromWorld(1337);
    }

}
