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

package lol.hyper.velocityblockversion.events;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import lol.hyper.velocityblockversion.VelocityBlockVersion;
import lol.hyper.velocityblockversion.tools.ConfigHandler;
import lol.hyper.velocityblockversion.tools.VersionToStrings;
import net.kyori.adventure.text.Component;

public class JoinEvent {

    private final VelocityBlockVersion velocityBlockVersion;
    private final ConfigHandler configHandler;

    public JoinEvent(VelocityBlockVersion velocityBlockVersion) {
        this.velocityBlockVersion = velocityBlockVersion;
        this.configHandler = velocityBlockVersion.configHandler;
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerLogin(PreLoginEvent event) {
        int version = event.getConnection().getProtocolVersion().getProtocol();
        velocityBlockVersion.logger.info("Player is connecting with protocol version: " + version);
        if (configHandler.blockVersions.contains(version)) {
            String allowedVersions = VersionToStrings.allowedVersions(configHandler.blockVersions);
            String blockedMessage = configHandler.config.getString("disconnect_message");
            if (allowedVersions == null) {
                blockedMessage = "<red>All versions are currently blocked from playing.</red>";
            }
            if (blockedMessage.contains("{VERSIONS}")) {
                blockedMessage = blockedMessage.replace("{VERSIONS}", allowedVersions);
            }
            Component message = velocityBlockVersion.miniMessage.deserialize(blockedMessage);
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(message));
            velocityBlockVersion.logger.info("Blocking player " + event.getUsername() + " because they are playing on version "
                    + VersionToStrings.versionStrings.get(version) + " which is blocked!");
        }
    }
}
