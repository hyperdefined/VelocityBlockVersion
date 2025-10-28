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

import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import lol.hyper.velocityblockversion.tools.ConfigHandler;
import org.slf4j.Logger;

public final class JoinEvent {
    @Inject
    private Logger logger;
    @Inject
    private ConfigHandler configHandler;

    /**
     * Called by Velocity when a player joins.
     * @param event The context of the event.
     */
    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerLogin(final PreLoginEvent event) {
        final int version = event.getConnection().getProtocolVersion().getProtocol();
        if (configHandler.getConfig().getBoolean("log_connection_versions", false)) {
            logger.info("Player is connecting with protocol version: {}", version);
        }

        boolean isBlacklist = configHandler.getOperationMode().equals(ConfigHandler.OperationMode.BLACKLIST);
        if (configHandler.getVersionsSet().contains(version) ^ isBlacklist) {
            return;
        }

        event.setResult(PreLoginEvent.PreLoginComponentResult.denied(configHandler.getDeniedMessage()));
        logger.info(
                "Blocking player {} because they are playing on version {} which is not allowed!",
                event.getUsername(),
                ProtocolVersion.getProtocolVersion(version).getMostRecentSupportedVersion()
        );
    }
}
