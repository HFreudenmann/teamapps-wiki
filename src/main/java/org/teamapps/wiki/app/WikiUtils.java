package org.teamapps.wiki.app;

import org.jetbrains.annotations.NotNull;
import org.teamapps.icon.emoji.EmojiIcon;
import org.teamapps.ux.component.tree.TreeNodeInfo;
import org.teamapps.ux.component.tree.TreeNodeInfoImpl;
import org.teamapps.ux.session.CurrentSessionContext;
import org.teamapps.wiki.model.wiki.Page;

import java.util.function.Function;

public class WikiUtils {

    public static EmojiIcon getIconFromName(String iconName) {
        return (iconName!= null) ? EmojiIcon.forUnicode(iconName) : null;
    }

    @NotNull
    public static Function<Page, TreeNodeInfo> getPageTreeNodeInfoFunction() {

        return page -> {
            // System.out.println("   getPageTreeNodeInfoFunction : page [" + p.getId() + "]");
            return new TreeNodeInfoImpl<>(page.getParent(), true, true, false);};
    }

    public static void showWarning(String message) {
        CurrentSessionContext.get().showNotification(EmojiIcon.WARNING, message);
    }

}
