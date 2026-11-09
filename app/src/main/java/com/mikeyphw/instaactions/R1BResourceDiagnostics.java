package com.mikeyphw.instaactions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Only static Android view resource names and class names are summarized.
 * Never requests view text, accessibility text, user IDs, captions, media, URLs or fields.
 * Deterministic and bounded to keep runtime evidence concise.
 */
final class R1BResourceDiagnostics {
    static final int MAX_IDS = 24;
    static final int MAX_CLASSES = 12;
    static final int MAX_DISTINCT_CLASSES = 40;
    static final int MAX_LOGS_PER_SESSION = 3;
    static final long HINT_INTERVAL_MS = 8000L;

    private R1BResourceDiagnostics() { }

    static String summarize(Set<String> resourceNames, Map<String, Integer> classCounts,
            int visitedNodes, int visibleNodes, int nodesWithId, int missingResourceIds,
            boolean truncated) {
        List<String> ids = new ArrayList<>();
        for (String raw : resourceNames) {
            String name = clean(raw, 64);
            if (!name.isEmpty()) ids.add(name);
        }
        // Prefer candidate Feed identifiers, but retain the original names for diagnosis.
        ids.sort(Comparator.comparingInt(R1BResourceDiagnostics::hintRank)
                .thenComparing(String::compareTo));
        List<String> classes = new ArrayList<>();
        for (Map.Entry<String, Integer> e : classCounts.entrySet()) {
            String name = clean(e.getKey(), 80);
            if (!name.isEmpty()) classes.add(name + ":" + Math.max(0, e.getValue()));
        }
        Collections.sort(classes);
        return "visited=" + visitedNodes + " visible=" + visibleNodes
                + " nodes_with_id=" + nodesWithId
                + " instagram_static_id_count=" + resourceNames.size()
                + " unresolved_id_count=" + missingResourceIds
                + " truncated=" + truncated
                + " static_ids=" + joinLimited(ids, MAX_IDS)
                + " visible_classes=" + joinLimited(classes, MAX_CLASSES)
                + " content=excluded actions=off";
    }

    private static int hintRank(String id) {
        String lower = id.toLowerCase(Locale.ROOT);
        for (String token : new String[] {"feed", "timeline", "post", "media", "carousel",
                "main", "row", "video", "layout", "container", "list", "home"}) {
            if (lower.contains(token)) return 0;
        }
        return 1;
    }

    private static String clean(String text, int limit) {
        if (text == null) return "";
        // Names can only originate in platform class/resource metadata, never UI content.
        String value = text.replaceAll("[^A-Za-z0-9_.$]", "_");
        return value.length() > limit ? value.substring(0, limit) : value;
    }

    private static String joinLimited(List<String> names, int limit) {
        if (names.isEmpty()) return "none";
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < Math.min(names.size(), limit); i++) {
            if (i > 0) out.append(',');
            out.append(names.get(i));
        }
        if (names.size() > limit) out.append(",more_").append(names.size() - limit);
        return out.toString();
    }
}
