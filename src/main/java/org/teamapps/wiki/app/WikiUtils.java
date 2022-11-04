package org.teamapps.wiki.app;

import org.jetbrains.annotations.NotNull;
import org.teamapps.icon.emoji.EmojiIcon;
import org.teamapps.ux.component.tree.TreeNodeInfo;
import org.teamapps.ux.component.tree.TreeNodeInfoImpl;
import org.teamapps.wiki.model.wiki.Page;

import java.util.function.Function;

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

    @NotNull
    public static Function<Page, TreeNodeInfo> getPageTreeNodeInfoFunction() {

        return page -> {
            // System.out.println("   getPageTreeNodeInfoFunction : page [" + p.getId() + "]");
            return new TreeNodeInfoImpl<>(page.getParent(), true, true, false);};
    }

}
