package eva.ware.modules.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Category {

    Combat("Combat", "E"),
    Movement("Movement", "D"),
    Visual("Render", "F"),
//    Player("Player", "B"),
    Misc("Misc", "C");
    private final String name;
    private final String icon;


}
