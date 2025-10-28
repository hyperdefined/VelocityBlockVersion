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

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.plugin.annotation.DataDirectory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public final class ConfigHandler {
    public enum OperationMode {
        BLACKLIST, WHITELIST;

        public static OperationMode fromString(String mode) {
            switch (mode) {
                case "blacklist":
                    return BLACKLIST;
                case "whitelist":
                    return WHITELIST;
                default:
                    return null;
            }
        }
    };

    @Inject
    private Logger logger;
    @Inject
    @DataDirectory
    private Path folderPath;

    private Toml config;
    private OperationMode operationMode = OperationMode.BLACKLIST;
    private Set<Integer> versionsSet = ImmutableSet.of();
    private Component deniedMessage = MiniMessage.miniMessage().deserialize("");
    public final long CONFIG_VERSION = 6;

    @Inject
    public ConfigHandler() {}

    public boolean loadConfig() {
        if (Files.notExists(folderPath)) {
            try {
                Files.createDirectory(folderPath);
            } catch(IOException e) {
                logger.error("Unable to create config folder!", e);
                return false;
            }
        }

        final Path configFile = folderPath.resolve("config.toml");
        if (Files.notExists(configFile)) {
            try (InputStream is = ConfigHandler.class.getClassLoader().getResourceAsStream( "config.toml")){
                if (is == null) {
                    logger.error("Unable to load 'config.toml' from the plugin jar!");
                    return false;
                }
                Files.copy(is, configFile);
            } catch (IOException e) {
                logger.error("Unable to copy default config!", e);
                return false;
            }
        }
        try (final InputStream is = Files.newInputStream(folderPath.resolve("config.toml"))) {
            config = new Toml().read(is);
        } catch (IOException e) {
            logger.error("Unable to find config!", e);
            return false;
        }

        if (config.getLong("config_version") != CONFIG_VERSION) {
            logger.warn(
                    "Your config is outdated. We will attempt to load your current config. However, things might not work!");
            logger.warn(
                    "To fix this, delete your current config and let the server remake it.");
        }

        operationMode = OperationMode.fromString(config.getString("mode", "blacklist"));
        if (operationMode == null) {
            logger.error("Unexpected mode option found. Using default value.");
            operationMode = OperationMode.BLACKLIST;
        }
        logger.info("Operation mode: {}", operationMode.toString());

        List<Integer> versionsList = new ArrayList<>();
        // for some reason, the config loads the versions as longs
        // we have to convert them this ugly way
        for (Object obj : config.getList("versions", List.of())) {
            if (obj instanceof Number) {
                int t = ((Number) obj).intValue();
                versionsList.add(t);
            } else {
                logger.error("Unexpected versions configuration input {}", obj);
            }
        }

        if (versionsList.isEmpty()) {
            logger.warn("There are no versions listed in the config!");
        } else {
            versionsList.sort((a, b) -> a.compareTo(b));
            versionsList = versionsList.stream().filter(elm -> ProtocolVersion.ID_TO_PROTOCOL_CONSTANT.containsKey(elm))
                                                .distinct().sorted().toList();
            logger.info("Loaded {} versions!", versionsList.size());
        }
        logger.info("Loaded versions: {}", versionsList.stream().map(String::valueOf).collect(Collectors.joining(", ")));

        String versionString = VersionToStrings.versionRange(versionsList);
        String disconnectMessage = config.getString("disconnect_message");

        deniedMessage = MiniMessage.miniMessage().deserialize(
                disconnectMessage,
                Placeholder.unparsed("versions", versionString)
        );
        
        versionsSet = ImmutableSet.copyOf(versionsList);
        return true;
    }

    public Set<Integer> getVersionsSet() {
        return versionsSet;
    }

    public OperationMode getOperationMode() {
        return operationMode;
    }

    public Component getDeniedMessage() {
        return deniedMessage;
    }

    public Toml getConfig() {
        return config;
    }
}
