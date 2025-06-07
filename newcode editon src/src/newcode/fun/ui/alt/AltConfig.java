package newcode.fun.ui.alt;


import newcode.fun.control.Manager;

import java.io.*;
import java.nio.file.Files;

public class AltConfig {

    public static final File file = new File("C:\\NewCode\\game\\NewCode\\altmanager.json");

    public void init() throws Exception {
        if (!file.exists()) {
            file.createNewFile();
        } else {
            readAlts();
        }
    }

    public static void updateFile() {
        try {
            StringBuilder builder = new StringBuilder();
            for (Account alt : Manager.ALT.accounts) {
                builder.append(alt.accountName + ":" + alt.dateAdded).append("\n");
            }
            Files.write(file.toPath(), builder.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readAlts() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(file.getAbsolutePath()))));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                String username = parts[0];
                Manager.ALT.accounts.add(new Account(username, Long.valueOf(parts[1])));
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}