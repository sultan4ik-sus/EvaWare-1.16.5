package newcode.fun.control.drag;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

public class DragManager {
    public static HashMap<String, newcode.fun.control.drag.Dragging> draggables = new HashMap<>();

    public static final File DRAG_DATA = new File("C:\\NewCode\\game\\NewCode\\elements.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();

    public static void save() {

        if (!DRAG_DATA.exists()) {
            DRAG_DATA.getParentFile().mkdirs();
        }
        try {
            Files.writeString(DRAG_DATA.toPath(), GSON.toJson(draggables.values()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void load() {
        if (!DRAG_DATA.exists()) {
            DRAG_DATA.getParentFile().mkdirs();
            return;
        }
        newcode.fun.control.drag.Dragging[] draggings;

        try {
            draggings = GSON.fromJson(Files.readString(DRAG_DATA.toPath()), newcode.fun.control.drag.Dragging[].class);

        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        for (newcode.fun.control.drag.Dragging dragging : draggings) {
            if (dragging == null) return;
            newcode.fun.control.drag.Dragging currentDrag = draggables.get(dragging.getName());
            if (currentDrag == null) continue;
            currentDrag.setX(dragging.getX());
            currentDrag.setY(dragging.getY());
            draggables.put(dragging.getName(), currentDrag);
        }
    }

}