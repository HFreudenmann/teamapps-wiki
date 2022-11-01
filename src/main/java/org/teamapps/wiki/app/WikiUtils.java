package org.teamapps.wiki.app;

import org.teamapps.icon.emoji.EmojiIcon;
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
    public static boolean isChildPage(Page potentialChildPage, Page page) {
        if (potentialChildPage == null) {
            return false;
        }
        Page parent = potentialChildPage.getParent();
        while (parent != null) {
            if (parent.equals(page)){ return true ; }
            parent = parent.getParent();
        }
        return false;
    }

    public static EmojiIcon getIconFromName(String iconName) {
        return (iconName!= null) ? EmojiIcon.forUnicode(iconName) : null;
    }
}
