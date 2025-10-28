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
    private static class VersionRange {
        int start;
        int end;

        VersionRange(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            String firstVersion = ProtocolVersion.ID_TO_PROTOCOL_CONSTANT.get(start).getVersionIntroducedIn();
            String lastVersion = ProtocolVersion.ID_TO_PROTOCOL_CONSTANT.get(end).getMostRecentSupportedVersion();
            return firstVersion.equals(lastVersion) ? firstVersion : firstVersion + " - " + lastVersion;
        }
    }

    private VersionToStrings() {}

    /**
     * Builds a string that will show what versions the server supports. Example: 1.8 to 1.14.4
     * @param versionList The list of versions
     * @return Returns the string of versions. Returns "{null}" if the input list is empty.
     */
    public static String versionRange(final List<Integer> versionList) {
        if (versionList.isEmpty()) {
            return "{null}";
        }

        List<VersionRange> ranges = new ArrayList<>();
        List<Integer> supported = new ArrayList<>(ProtocolVersion.SUPPORTED_VERSIONS
                                .stream().map(ProtocolVersion::getProtocol).toList());

        int start = supported.indexOf(versionList.get(0)), prev = start;

        Iterator<Integer> it = versionList.iterator();
        it.next();

        while (it.hasNext()) {
            int version = it.next();
            if (version == supported.get(prev + 1)) {
                prev += 1;
            } else {
                ranges.add(new VersionRange(supported.get(start), supported.get(prev)));
                start = supported.indexOf(version);
                prev = start;
            }
        }
        ranges.add(new VersionRange(supported.get(start), supported.get(prev)));

        return String.join(", ", ranges.stream().map(VersionRange::toString).toList());
    }
}
