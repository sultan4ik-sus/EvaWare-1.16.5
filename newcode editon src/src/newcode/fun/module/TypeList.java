package newcode.fun.module;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum TypeList {
    Combat("Aura, AutoTotem, Other..", "A",0f),
    Movement("Strafe, Speed, Other..", "B", 0f),
    Render("Tracers, ESP, Other...", "C", 0f),
    Player("No Clip, AutoPotion, Other...", "D", 0f),
    Display("MCF, MCP, Other...", "E", 0f);

    public final String description;
    public final String image;
    public double anim;
}
