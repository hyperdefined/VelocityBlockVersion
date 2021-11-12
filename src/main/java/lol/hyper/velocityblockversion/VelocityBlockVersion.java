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
import lol.hyper.velocityblockversion.tools.ConfigHandler;
import lol.hyper.velocityblockversion.tools.VersionToStrings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

@Plugin(
        id = "velocityblockversion",
        name = "VelocityBlockVersion",
        version = "1.0",
        authors = {"hyperdefined"},
        description = "Block certain Minecraft versions from connecting to your network."
)
public class VelocityBlockVersion {

    public ConfigHandler configHandler;

    public final Logger logger;
    private final Metrics.Factory metricsFactory;

    @Inject
    public VelocityBlockVersion(Logger logger, Metrics.Factory metricsFactory) {
        this.logger = logger;
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        VersionToStrings.init();
        configHandler = new ConfigHandler(this);
        configHandler.loadConfig();
        Metrics metrics = metricsFactory.make(this, 13308);
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
            Component message = MiniMessage.get().parse(blockedMessage);
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(message));
            logger.info("Blocking player " + event.getUsername() + " because they are playing on version "
                    + VersionToStrings.versionStrings.get(version) + " which is blocked!");
        }
    }
}