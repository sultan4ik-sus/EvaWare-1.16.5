package eva.ware.modules.api;

import com.google.common.eventbus.Subscribe;

import eva.ware.Evaware;
import eva.ware.events.EventKey;
import eva.ware.modules.impl.combat.*;
import eva.ware.modules.impl.misc.*;
import eva.ware.modules.impl.movement.*;
import eva.ware.modules.impl.visual.*;
import eva.ware.utils.text.font.ClientFonts;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Getter
public class ModuleManager {
    private final List<Module> modules = new CopyOnWriteArrayList<>();

    private PlayerHelper playerHelper;
    private NoServerDesync noServerDesync;
    private ViewModel viewModel;
    private Hud hud;
    private AutoGapple autoGapple;
    private AutoSprint autoSprint;
    private Velocity velocity;
    private NoRender noRender;
    private Timer timer;
    private InventoryPlus inventoryPlus;
    private ElytraHelper elytrahelper;
    private PotionThrower autopotion;
    private TriggerBot triggerbot;
    private ClickFriend clickfriend;
    private ESP esp;
    private FTHelper FTHelper;
    private ItemCooldown itemCooldown;
    private ClickPearl clickPearl;
    private AutoSwap autoSwap;
    private EntityBox entityBox;
    private AntiPush antiPush;
    private FreeCam freeCam;
    private ChestStealer chestStealer;
    private AutoLeave autoLeave;
    private Fly fly;
    private TargetStrafe targetStrafe;
    private ClientTune clientTune;
    private AutoTotem autoTotem;
    private AutoExplosion autoExplosion;
    private HitAura hitAura;
    private AntiBot antiBot;
    private Crosshair crosshair;
    private DeathEffect deathEffect;
    private Strafe strafe;
    private ElytraFly elytraFly;
    private Predictions predictions;
    private NoEntityTrace noEntityTrace;
    private NoClip noClip;
    private StorageEsp storageESP;
    private Spider spider;
    private NameProtect nameProtect;
    private NoInteract noInteract;
    private Tracers tracers;
    private SelfDestruct selfDestruct;
    private BetterMinecraft betterMinecraft;
    private SeeInvisibles seeInvisibles;
    private Jesus jesus;
    private Speed speed;
    private AirJump airJump;
    private Disabler disabler;
    private WaterSpeed waterSpeed;
    private NoFriendHurt noFriendHurt;
    private SafeWalk safeWalk;
    private ElytraSpeed elytraSpeed;
    private TPInfluence TPInfluence;
    private AutoFix autoFix;
    private ClickGui clickGui;
    private TntTimer tntTimer;
    private Blink blink;
    private SlowPackets slowPackets;
    private Scaffold scaffold;
    private WorldTweaks worldTweaks;
    private Arrows arrows;
    private RWJoiner rwJoiner;
    private ChatHelper chatHelper;
    private CrystalAura crystalAura;
    private FakePlayer fakePlayer;
    private Criticals criticals;
    private AutoActions autoActions;
    private NoFall noFall;
    private FullBright fullBright;
    private AutoDuel autoDuel;
    private SexAura sexAura;
    private AutoSort autoSort;
    private MoveHelper moveHelper;
    private ElytraBooster elytraBooster;
    private HitBubbles hitBubbles;
    private FireFly fireFly;
    private Trails trails;
    private JumpCircle jumpCircle;
    private Particles particles;
    private KTLeave ktLeave;
    private AWP AWP;
    private ClickTeleport clickTeleport;
    private WallHack wallHack;
    private ShaderEsp shaderESP;
    private BowSpammer bowSpammer;
    private PushAttack pushAttack;
    private AimAssist aimAssist;
    private Step step;

    public void init() {
        registerAll(aimAssist = new AimAssist(), step = new Step(),
                shaderESP = new ShaderEsp(), clickTeleport = new ClickTeleport(), pushAttack = new PushAttack(),
                ktLeave = new KTLeave(), AWP = new AWP(), wallHack = new WallHack(), bowSpammer = new BowSpammer(),
                fireFly = new FireFly(), trails = new Trails(), jumpCircle = new JumpCircle(),
                moveHelper = new MoveHelper(), elytraBooster = new ElytraBooster(), hitBubbles = new HitBubbles(),
                fullBright = new FullBright(), autoDuel = new AutoDuel(), sexAura = new SexAura(), autoSort = new AutoSort(),
                crystalAura = new CrystalAura(), fakePlayer = new FakePlayer(),
                criticals = new Criticals(), autoActions = new AutoActions(), noFall = new NoFall(), rwJoiner = new RWJoiner(),
                hud = new Hud(), chatHelper = new ChatHelper(), arrows = new Arrows(),
                noServerDesync = new NoServerDesync(), playerHelper = new PlayerHelper(),
                worldTweaks = new WorldTweaks(), scaffold = new Scaffold(), slowPackets = new SlowPackets(),
                blink = new Blink(), tntTimer = new TntTimer(), deathEffect = new DeathEffect(),
                clickGui = new ClickGui(), autoFix = new AutoFix(), TPInfluence = new TPInfluence(),
                elytraSpeed = new ElytraSpeed(), particles = new Particles(), safeWalk = new SafeWalk(),
                noFriendHurt = new NoFriendHurt(), waterSpeed = new WaterSpeed(), disabler = new Disabler(), jesus = new Jesus(),
                airJump = new AirJump(), speed = new Speed(), autoGapple = new AutoGapple(), autoSprint = new AutoSprint(),
                velocity = new Velocity(), noRender = new NoRender(), inventoryPlus = new InventoryPlus(),
                seeInvisibles = new SeeInvisibles(), elytrahelper = new ElytraHelper(), autopotion = new PotionThrower(),
                noClip = new NoClip(), triggerbot = new TriggerBot(), clickfriend = new ClickFriend(),
                esp = new ESP(), FTHelper = new FTHelper(), entityBox = new EntityBox(), antiPush = new AntiPush(),
                freeCam = new FreeCam(), chestStealer = new ChestStealer(), autoLeave = new AutoLeave(),
                fly = new Fly(), clientTune = new ClientTune(), autoExplosion = new AutoExplosion(),
                antiBot = new AntiBot(), crosshair = new Crosshair(), autoTotem = new AutoTotem(), itemCooldown = new ItemCooldown(),
                hitAura = new HitAura(autopotion), clickPearl = new ClickPearl(itemCooldown),
                autoSwap = new AutoSwap(autoTotem), targetStrafe = new TargetStrafe(hitAura),
                strafe = new Strafe(targetStrafe, hitAura), viewModel = new ViewModel(hitAura),
                elytraFly = new ElytraFly(), predictions = new Predictions(), noEntityTrace = new NoEntityTrace(),
                storageESP = new StorageEsp(), spider = new Spider(), timer = new Timer(), nameProtect = new NameProtect(),
                noInteract = new NoInteract(), tracers = new Tracers(), selfDestruct = new SelfDestruct(),
                betterMinecraft = new BetterMinecraft(), new LongJump(), new OreFinder(), new RWHelper()
        );

        sortModulesByWidth();


        Evaware.getInst().getEventBus().register(this);
    }

    private void registerAll(Module... modules) {
        this.modules.addAll(List.of(modules));
    }

    private void sortModulesByWidth() {
        try {
            modules.sort(Comparator.comparingDouble(module ->
                    ClientFonts.tenacity[19].getWidth(module.getClass().getName())
            ).reversed());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Module> get(final Category category) {
        return modules.stream().filter(module -> module.getCategory() == category).collect(Collectors.toList());
    }

    public int countEnabledModules() {
        return (int) modules.stream().filter(Module::isEnabled).count();
    }

    @Subscribe
    private void onKey(EventKey e) {
        if (selfDestruct.unhooked) return;
        modules.stream().filter(module -> module.getBind() == e.getKey()).forEach(Module::toggle);
    }

}
