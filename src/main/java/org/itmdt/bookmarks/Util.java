package org.itmdt.bookmarks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Util {
    private Util() {}

    public static List<Long> numberStringToLongArray(final String input) throws NumberFormatException {
        ArrayList<Long> ret = new ArrayList<>();

        String[] parts = input.split(",");
        for (String part : parts) {
            Long interpreted = Long.parseLong(part);
            ret.add(interpreted);
        }

        return ret;
    }

    public static String generateRandomUUIDString() {
        return UUID.randomUUID().toString();
    }
}
