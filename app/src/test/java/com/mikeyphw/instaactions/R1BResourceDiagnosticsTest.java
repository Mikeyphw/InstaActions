package com.mikeyphw.instaactions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public class R1BResourceDiagnosticsTest {
    @Test public void unknownFeedShowsStaticIdsWithoutUserContent() {
        Set<String> ids = new HashSet<>(Arrays.asList("random_button", "timeline_grid", "feed_item_root"));
        Map<String, Integer> classes = new HashMap<>();
        classes.put("com.instagram.ui.FeedItemView", 3);
        String summary = R1BResourceDiagnostics.summarize(ids, classes, 38, 26, 7, 2, false);
        assertTrue(summary.contains("feed_item_root"));
        assertTrue(summary.contains("timeline_grid"));
        assertTrue(summary.contains("visible_classes=com.instagram.ui.FeedItemView:3"));
        assertTrue(summary.contains("content=excluded actions=off"));
        assertFalse(summary.contains("media_id="));
    }
    @Test public void evidenceIsBoundedAndSorted() {
        Set<String> ids = new HashSet<>();
        for (int i=0;i<100;i++) ids.add("z_item_"+i);
        String summary = R1BResourceDiagnostics.summarize(ids, Collections.emptyMap(),384,280,120,0,false);
        assertTrue(summary.contains("instagram_static_id_count=100"));
        assertTrue(summary.contains("more_76"));
        assertTrue(summary.contains("visible_classes=none"));
        assertTrue(summary.length() < 2000);
    }
    @Test public void namesAreSanitized() {
        String summary = R1BResourceDiagnostics.summarize(
                new HashSet<>(Arrays.asList("media\nunsound", "feed,post")),
                Collections.singletonMap("strange\tclass", 1),1,1,1,0,false);
        assertFalse(summary.contains("\n"));
        assertFalse(summary.contains("\t"));
        assertTrue(summary.contains("feed_post"));
    }
}
