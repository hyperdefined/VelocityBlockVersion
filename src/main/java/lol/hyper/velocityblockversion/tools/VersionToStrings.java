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

import com.velocitypowered.api.network.ProtocolVersion;

import java.util.*;

public final class VersionToStrings {
    private VersionToStrings() {}

    /**
     * Builds a string that will show what versions the server supports. Example: 1.8 to 1.14.4
     * @param deniedVersions Versions to deny.
     * @return Returns the string of versions. Returns nulls if there are no versions that are allowed.
     */
    public static String allowedVersions(final List<Integer> deniedVersions) {
        final Map<Integer, ProtocolVersion> versionMap = new HashMap<>(ProtocolVersion.ID_TO_PROTOCOL_CONSTANT);
        versionMap.remove(-1);
        versionMap.remove(-2);
        final List<Integer> allVersions = new ArrayList<>(versionMap.keySet());
        allVersions.removeAll(deniedVersions);
        if (allVersions.isEmpty()) {
            return null;
        }

        final int minVersion = Collections.min(allVersions);
        final int maxVersion = Collections.max(allVersions);

        return versionMap.get(minVersion).toString() + " to " + versionMap.get(maxVersion).toString();
    }
}
