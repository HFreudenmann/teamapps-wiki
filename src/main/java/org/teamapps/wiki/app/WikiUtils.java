package org.teamapps.wiki.app;

import org.teamapps.wiki.model.wiki.Page;

public class WikiUtils {
    public static int getPageLevel(Page page) {
        int level = 0;
        Page parent = page.getParent();
        while (parent != null) {
            level++;
            parent = parent.getParent();
        }
        return level;
    }
}
