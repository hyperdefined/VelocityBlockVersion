/*
 * This file is part of VelocityBlockVersion.
 *
 * VelocityBlockVersion is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VelocityBlockVersion is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VelocityBlockVersion.  If not, see <https://www.gnu.org/licenses/>.
 */

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
    public final List<Integer> blockVersions = new ArrayList<>();
    public final double CONFIG_VERSION = 3;

    public ConfigHandler(VelocityBlockVersion velocityBlockVersion) {
        this.velocityBlockVersion = velocityBlockVersion;
    }

    public void loadConfig() {
        File configFile = new File("plugins" + File.separator + "VelocityBlockVersion", "config.toml");
        if (!configFile.exists()) {
            InputStream is = velocityBlockVersion.getClass().getResourceAsStream( "/config.toml");
            if (is == null) {
                velocityBlockVersion.logger.error("Unable to load \"config.toml\" from the plugin jar!");
                return;
            }
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
            blockVersions.add((int) t);
        }
        if (blockVersions.size() == 0) {
            velocityBlockVersion.logger.warn("There are no versions listed in the config!");
        } else {
            velocityBlockVersion.logger.info("Loaded " + blockVersions.size() + " versions!");
        }
        // use an iterator here so we can remove stuff
        Iterator<Integer> iter = blockVersions.iterator();
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
