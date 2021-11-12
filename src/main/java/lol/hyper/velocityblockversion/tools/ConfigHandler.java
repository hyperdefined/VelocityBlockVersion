package lol.hyper.velocityblockversion.tools;

import com.moandjiezana.toml.Toml;
import lol.hyper.velocityblockversion.VelocityBlockVersion;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConfigHandler {

    public Toml config;
    private final VelocityBlockVersion velocityBlockVersion;
    public static final List<Integer> versions = new ArrayList<>();
    public static final double CONFIG_VERSION = 2;

    public ConfigHandler(VelocityBlockVersion velocityBlockVersion) {
        this.velocityBlockVersion = velocityBlockVersion;
    }

    public void loadConfig() {
        File configFile = new File("plugins" + File.separator + "VelocityBlockVersion", "config.toml");
        if (!configFile.exists()) {
            InputStream is = velocityBlockVersion.getClass().getResourceAsStream( "/config.toml");
            File path = new File("plugins" + File.separator + "VelocityBlockVersion");
            try {
                if (path.mkdir()) {
                    Files.copy(is, configFile.toPath());
                    velocityBlockVersion.logger.info("Copying default config...");
                } else {
                    velocityBlockVersion.logger.error("Unable to create config folder!");
                }
            } catch (IOException e) {
                velocityBlockVersion.logger.error("Unable to copy default config!", e);
            }
        }
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(configFile);
        } catch (FileNotFoundException e) {
            velocityBlockVersion.logger.error("Unable to find config!", e);
            return;
        }
        config = new Toml().read(inputStream);

        if (config.getLong("config_version") != CONFIG_VERSION) {
            velocityBlockVersion.logger.warn(
                    "Your config is outdated. We will attempt to load your current config. However, things might not work!");
            velocityBlockVersion.logger.warn(
                    "To fix this, delete your current config and let the server remake it.");
        }
        // for some reason, the config loads the versions as longs
        // we have to convert them this ugly way
        for (Object obj : config.getList("versions")) {
            long t = (long) obj;
            versions.add((int) t);
        }
        if (versions.size() == 0) {
            velocityBlockVersion.logger.warn("There are no versions listed in the config!");
        } else {
            velocityBlockVersion.logger.info("Loaded " + versions.size() + " versions!");
        }
        // use an iterator here so we can remove stuff
        Iterator<Integer> iter = versions.iterator();
        while (iter.hasNext()) {
            int version = iter.next();
            if (!VersionToStrings.versionStrings.containsKey(version)) {
                velocityBlockVersion.logger.warn(
                        "Version " + version + " is NOT a valid version number! Ignoring this version.");
                iter.remove();
            }
        }
    }
}
