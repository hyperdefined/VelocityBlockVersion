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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class VersionToStrings {

    // Set a list of version strings we can grab via the version number.
    public static final HashMap<Integer, String> versionStrings = new HashMap<>();

    static {
        versionStrings.put(47, "1.8");
        versionStrings.put(107, "1.9");
        versionStrings.put(108, "1.9.1");
        versionStrings.put(109, "1.9.2");
        versionStrings.put(110, "1.9.4");
        versionStrings.put(210, "1.10");
        versionStrings.put(315, "1.11");
        versionStrings.put(316, "1.11.1");
        versionStrings.put(335, "1.12");
        versionStrings.put(338, "1.12.1");
        versionStrings.put(340, "1.12.2");
        versionStrings.put(393, "1.13");
        versionStrings.put(401, "1.13.1");
        versionStrings.put(404, "1.13.2");
        versionStrings.put(477, "1.14");
        versionStrings.put(480, "1.14.1");
        versionStrings.put(485, "1.14.2");
        versionStrings.put(490, "1.14.3");
        versionStrings.put(498, "1.14.4");
        versionStrings.put(573, "1.15");
        versionStrings.put(575, "1.15.1");
        versionStrings.put(578, "1.15.2");
        versionStrings.put(735, "1.16");
        versionStrings.put(736, "1.16.1");
        versionStrings.put(751, "1.16.2");
        versionStrings.put(753, "1.16.3");
        versionStrings.put(754, "1.16.4");
        versionStrings.put(755, "1.17");
        versionStrings.put(756, "1.17.1");
        versionStrings.put(757, "1.18");
        versionStrings.put(758, "1.18.2");
        versionStrings.put(759, "1.19");
    }

    /**
     * Builds a string that will show what versions the server supports. Example: 1.8 to 1.14.4
     * @param deniedVersions Versions to deny.
     * @return Returns the string of versions. Returns nulls if there are no versions that are allowed.
     */
    public static String allowedVersions(List<Integer> deniedVersions) {
        List<Integer> allVersions = new ArrayList<>(versionStrings.keySet());
        allVersions.removeAll(deniedVersions);
        if (allVersions.isEmpty()) {
            return null;
        }
        int minVersion = Collections.min(allVersions);
        int maxVersion = Collections.max(allVersions);

        return versionStrings.get(minVersion) + " to " + versionStrings.get(maxVersion);
    }
}
