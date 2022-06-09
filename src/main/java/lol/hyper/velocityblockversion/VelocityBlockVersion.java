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
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import lol.hyper.githubreleaseapi.GitHubRelease;
import lol.hyper.githubreleaseapi.GitHubReleaseAPI;
import lol.hyper.velocityblockversion.commands.CommandReload;
import lol.hyper.velocityblockversion.events.JoinEvent;
import lol.hyper.velocityblockversion.tools.ConfigHandler;
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
    public JoinEvent joinEvent;
    public CommandReload commandReload;
    public final String VERSION = "1.0.5";

    public final Logger logger;
    private final Metrics.Factory metricsFactory;
    private final ProxyServer server;
    private final CommandManager commandManager;
    public final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Inject
    public VelocityBlockVersion(ProxyServer server, Logger logger, Metrics.Factory metricsFactory, CommandManager commandManager) {
        this.server = server;
        this.logger = logger;
        this.metricsFactory = metricsFactory;
        this.commandManager = commandManager;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        configHandler = new ConfigHandler(this);
        joinEvent = new JoinEvent(this);
        commandReload = new CommandReload(this);
        configHandler.loadConfig();
        metricsFactory.make(this, 13308);
        server.getScheduler().buildTask(this, this::checkForUpdates).schedule();
        server.getEventManager().register(this, joinEvent);

        CommandMeta meta = commandManager.metaBuilder("vbvreload").build();
        commandManager.register(meta, commandReload);
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
