package org.teamapps.wiki.app;

import org.teamapps.application.api.user.SessionUser;
import org.teamapps.wiki.model.wiki.Page;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class WikiPageManager {
    private final ConcurrentHashMap<Page, PageStatus> pageStatusHashMap = new ConcurrentHashMap<>();

    public PageStatus lockPage(Page page, SessionUser editor) {
        if (Objects.isNull(page)) {
            System.err.println("   Try to lock page = NULL");
        }
        if (Objects.isNull(editor)) {
            // ToDo possible NullPointerException
            System.err.println("   Try to lock page = " + page.getTitle() + " with editor = NULL");
        }
        // ToDo Handle errors : page or editor == NULL
        System.out.println("   lock page = " + page.getId());

        // TODO: Beim Logout nur offene Locks freigeben, anstatt pauschal alle bisher gesetzten
        editor.getSessionContext().onDestroyed.addListener(() -> unlockPage(page, editor));
        return pageStatusHashMap.computeIfAbsent(page, page1 -> new PageStatus(true, editor));
    }

    public void unlockPage(Page page, SessionUser editor) {
        PageStatus pageStatus;

        if (Objects.isNull(page)) {
            System.err.println("   Try to unlock page = NULL");
            return;
        }
        System.out.println("   unlock page = " + page.getId());

        pageStatus = getPageStatus(page);
        if (pageStatus == null) {
            System.err.println("   Failed to unlock page with status = NULL");
            return;
        }

        SessionUser pageLockedByUser = pageStatus.getEditor();
        if (pageLockedByUser == null) {
            System.err.println("   Failed to unlock page; user is NULL!");
        }
        else if (pageLockedByUser.equals(editor)) {
            pageStatusHashMap.remove(page);
        }
    }

    public PageStatus getPageStatus(Page page) {
        return pageStatusHashMap.getOrDefault(page, new PageStatus(false, null));
    }

    public static final class PageStatus {
        private final boolean locked;
        private final SessionUser editor;
        private final Date lockSince;

        public PageStatus(boolean locked, SessionUser editor) {
            this.locked = locked;
            this.editor = editor;
            this.lockSince = new Date();
        }

        public boolean isLocked() {
            return locked;
        }

        public SessionUser getEditor() {
            return editor;
        }

        public Date getLockSince() {
            return lockSince;
        }
    }
}
