package newcode.fun.module.api;

import newcode.fun.module.impl.combat.*;
import newcode.fun.module.impl.display.Aspect;
import newcode.fun.module.impl.display.ClickGui;
import newcode.fun.module.impl.display.Interface;
import newcode.fun.module.impl.movement.*;
import newcode.fun.module.impl.player.*;
import newcode.fun.module.impl.render.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Manager {

    private final List<Module> modules = new CopyOnWriteArrayList<>();
    public final Triangle arrowsFunction;
    public final Gamma fullBrightFunction;
    public final Sprint sprint;
    public final Flight flightFunction;
    public final Strafe strafe;
    public final AntiTarget antiTarget;
    public final SyncTps syncTps;
    public final Timer timer;
    public final AutoPotion autoPotionFunction;
    public final AutoRespawn autoRespawnFunction;
    public final Velocity velocityFunction;
    public final ClickPearl middleClickPearlFunction;
    public final AutoTotem autoTotemFunction;
    public final InventoryMove inventoryMoveFunction;
    public final NoPush noPushFunction;
    public final HitBox hitBoxFunction;
    public final NoSlow noSlow;
    public final SeeInvisibles seeInvisibles;
    public final AttackAura auraFunction;
    public final ElytraVector elytraVector;
    public final CustomSwing swingAnimationFunction;
    public final NoRender noRenderFunction;
    public final TargetEsp targetEsp;
    public final ChinaHat chinaHat;
    public final ItemsCooldown gappleCooldownFunction;
    public final Optimization optimization;
    public final ServerJoiner serverJoiner;
    public final AutoSwap autoSwapFunction;
    public final Aspect aspect;
    public final ItemScroller itemScroller;
    public final Esp espFunction;
    public final NameTags nameTags;
    public final Criticals criticals;
    public final ElytraBooster elytraBooster;
    public final NoInteract noInteractFunction;
    public final Ambience ambience;
    public final AutoMessage autoMessage;
    public final CustomDance customDance;
    public final Assistent assistent;
    public final ElytraHelper elytraHelper;
    public final ClientSounds clientSounds;
    public final Crosshair crosshair;
    public final NameProtect nameProtect;
    public final ViewMode viewMode;
    public final AutoExplosion autoExplosionFunction;
    public final HitColor hitColor;
    public final FreeCam freeCam;
    public final ClickGui clickGui;
    public Interface hud2;
    public final ItemPhysics itemPhysics;
    public final AutoFish autoFish;

    public Manager() {
        this.modules.addAll(Arrays.asList(
                this.clickGui = new ClickGui(),
                this.autoFish = new AutoFish(),
                this.chinaHat = new ChinaHat(),
                this.autoMessage = new AutoMessage(),
                this.elytraBooster = new ElytraBooster(),
                this.crosshair = new Crosshair(),
                this.targetEsp = new TargetEsp(),
                this.arrowsFunction = new Triangle(),
                this.serverJoiner = new ServerJoiner(),
                this.assistent = new Assistent(),
                this.fullBrightFunction = new Gamma(),
                this.noRenderFunction = new NoRender(),
                this.sprint = new Sprint(),
                this.customDance = new CustomDance(),
                this.criticals = new Criticals(),
                this.seeInvisibles = new SeeInvisibles(),
                this.elytraHelper = new ElytraHelper(),
                this.flightFunction = new Flight(),
                this.strafe = new Strafe(),
                this.timer = new Timer(),
                this.velocityFunction = new Velocity(),
                this.middleClickPearlFunction = new ClickPearl(),
                this.autoTotemFunction = new AutoTotem(),
                this.inventoryMoveFunction = new InventoryMove(),
                this.autoRespawnFunction = new AutoRespawn(),
                this.autoSwapFunction = new AutoSwap(),
                this.syncTps = new SyncTps(),
                this.noPushFunction = new NoPush(),
                this.hitBoxFunction = new HitBox(),
                this.noSlow = new NoSlow(),
                this.antiTarget = new AntiTarget(),
                this.autoPotionFunction = new AutoPotion(),
                this.swingAnimationFunction = new CustomSwing(),
                this.gappleCooldownFunction = new ItemsCooldown(),
                this.optimization = new Optimization(),
                this.itemScroller = new ItemScroller(),
                this.aspect = new Aspect(),
                this.espFunction = new Esp(),
                this.noInteractFunction = new NoInteract(),
                this.ambience = new Ambience(),
                this.clientSounds = new ClientSounds(),
                this.nameProtect = new NameProtect(),
                this.itemPhysics = new ItemPhysics(),
                this.viewMode = new ViewMode(),
                this.hitColor = new HitColor(),
                this.elytraVector = new ElytraVector(),
                this.auraFunction = new AttackAura(),
                new WaterSpeed(),
                new AutoTool(),
                new ChestStealer(),
                new Tracers(),
                new NoFriendDamage(),
                new ItemEsp(),
                new PearlPrediction(),
                new AutoTpaccept(),
                new ClickFriend(),
                new JumpCircle(),
                this.autoExplosionFunction = new AutoExplosion(),
                new Trails(),
                new NoPlayerInteract(),
                new NoWeb(),
                new Resolve(),
                new Speed(),
                new AntiAfk(),
                new DeathCoords(),
                new Spider(),
                freeCam = new FreeCam(),
                new NoClip(),
                new BlockEsp(),
                this.nameTags = new NameTags(),
                new TriggerBot(),
                new AntiBot(),
                new AutoLeave(),
                new Scaffold(),
                new Chams(),
                new NoDelay(),
                new AncientXray(),
                new Particles(),
                new SprintReset(),
                new ElytraFly(),
                new FastBow(),
                new AutoEat(),
                new AimingBalls(),
                new FastBreak(),
                new TapeMouse(),
                new NoFall(),
                new AutoDuel(),
                new AntiAim(),
                new CreeperFarm(),
                new DragonFly(),
                new ItemSwapFix(),
                new AutoOpenEC(),
                new LegitAura(),
                new Jesus(),
                hud2 = new Interface()
        ));
    }

    public List<Module> getFunctions() {
        return modules;
    }

    public Module get(String name) {
        for (Module module : modules) {
            if (module != null && module.name.equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }
}
