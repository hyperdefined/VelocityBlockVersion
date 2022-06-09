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

package lol.hyper.velocityblockversion;

import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import lol.hyper.githubreleaseapi.GitHubRelease;
import lol.hyper.githubreleaseapi.GitHubReleaseAPI;
import lol.hyper.velocityblockversion.tools.ConfigHandler;
import lol.hyper.velocityblockversion.tools.VersionToStrings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.io.IOException;

@Plugin(
        id = "velocityblockversion",
        name = "VelocityBlockVersion",
        version = "1.0.5",
        authors = {"hyperdefined"},
        description = "Block certain Minecraft versions from connecting to your network.",
        url = "https://github.com/hyperdefined/VelocityBlockVersion"
)
public class VelocityBlockVersion {

    public ConfigHandler configHandler;
    public final String VERSION = "1.0.5";

    public final Logger logger;
    private final Metrics.Factory metricsFactory;
    private final ProxyServer server;
    public final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Inject
    public VelocityBlockVersion(ProxyServer server, Logger logger, Metrics.Factory metricsFactory) {
        this.server = server;
        this.logger = logger;
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        configHandler = new ConfigHandler(this);
        configHandler.loadConfig();
        metricsFactory.make(this, 13308);
        server.getScheduler().buildTask(this, this::checkForUpdates).schedule();
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerLogin(PreLoginEvent event) {
        int version = event.getConnection().getProtocolVersion().getProtocol();
        if (ConfigHandler.versions.contains(version)) {
            String allowedVersions = VersionToStrings.allowedVersions(ConfigHandler.versions);
            String blockedMessage = configHandler.config.getString("disconnect_message");
            if (allowedVersions == null) {
                blockedMessage = "<red>All versions are currently blocked from playing.</red>";
            }
            if (blockedMessage.contains("{VERSIONS}")) {
                blockedMessage = blockedMessage.replace("{VERSIONS}", allowedVersions);
            }
            Component message = miniMessage.deserialize(blockedMessage);
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(message));
            logger.info("Blocking player " + event.getUsername() + " because they are playing on version "
                    + VersionToStrings.versionStrings.get(version) + " which is blocked!");
        }
    }

    public void checkForUpdates() {
        GitHubReleaseAPI api;
        try {
            api = new GitHubReleaseAPI("velocityblockversion", "hyperdefined");
        } catch (IOException e) {
            logger.warn("Unable to check updates!");
            e.printStackTrace();
            return;
        }
        GitHubRelease current = api.getReleaseByTag(VERSION);
        GitHubRelease latest = api.getLatestVersion();
        if (current == null) {
            logger.warn("You are running a version that does not exist on GitHub. If you are in a dev environment, you can ignore this. Otherwise, this is a bug!");
            return;
        }
        int buildsBehind = api.getBuildsBehind(current);
        if (buildsBehind == 0) {
            logger.info("You are running the latest version.");
        } else {
            logger.warn("A new version is available (" + latest.getTagVersion() + ")! You are running version " + current.getTagVersion() + ". You are " + buildsBehind + " version(s) behind.");
        }
    }
}
