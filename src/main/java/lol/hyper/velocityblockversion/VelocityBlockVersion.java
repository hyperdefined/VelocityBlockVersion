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
import com.google.inject.Injector;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import lol.hyper.githubreleaseapi.GitHubRelease;
import lol.hyper.githubreleaseapi.GitHubReleaseAPI;
import lol.hyper.velocityblockversion.commands.CommandReload;
import lol.hyper.velocityblockversion.events.JoinEvent;
import lol.hyper.velocityblockversion.tools.ConfigHandler;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.io.IOException;

import static lol.hyper.velocityblockversion.VelocityBlockVersion.VERSION;

@Plugin(
        id = "velocityblockversion",
        name = "VelocityBlockVersion",
        version = VERSION,
        authors = {"hyperdefined"},
        description = "Block certain Minecraft versions from connecting to your network.",
        url = "https://github.com/hyperdefined/VelocityBlockVersion"
)
public class VelocityBlockVersion {
    public static final String VERSION = "1.0.9";

    @Inject
    private Logger logger;
    @Inject
    private Metrics.Factory metricsFactory;
    @Inject
    private ProxyServer server;
    @Inject
    private CommandManager commandManager;
    @Inject
    private EventManager eventManager;
    @Inject
    private Injector injector;

    @Subscribe
    public void onProxyInitialization(final ProxyInitializeEvent event) {
        final ConfigHandler configHandler = injector.getInstance(ConfigHandler.class);
        if (!configHandler.loadConfig()) {
            return;
        }

        metricsFactory.make(this, 13308);

        server.getScheduler().buildTask(this, this::checkForUpdates).schedule();

        final JoinEvent joinEvent = injector.getInstance(JoinEvent.class);
        eventManager.register(this, joinEvent);

        final CommandReload commandReload = injector.getInstance(CommandReload.class);
        final CommandMeta meta = commandManager.metaBuilder("vbvreload").plugin(this).build();
        commandManager.register(meta, commandReload);
    }

    public void checkForUpdates() {
        GitHubReleaseAPI api;
        try {
            api = new GitHubReleaseAPI("velocityblockversion", "hyperdefined");
        } catch (IOException e) {
            logger.warn("Unable to check updates!", e);
            return;
        }
        final GitHubRelease current = api.getReleaseByTag(VERSION);
        final GitHubRelease latest = api.getLatestVersion();
        if (current == null) {
            logger.warn("You are running a version that does not exist on GitHub. If you are in a dev environment, you can ignore this. Otherwise, this is a bug!");
            return;
        }
        int buildsBehind = api.getBuildsBehind(current);
        if (buildsBehind == 0) {
            logger.info("You are running the latest version.");
        } else {
            logger.warn("A new version is available ({})! You are running version {}. You are " + buildsBehind + " version(s) behind.", latest.getTagVersion(), current.getTagVersion());
        }
    }
}
