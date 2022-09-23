package org.teamapps.wiki.app;

import org.teamapps.application.api.user.SessionUser;
import org.teamapps.wiki.model.wiki.Page;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class WikiPageManager {
    private final ConcurrentHashMap<Page, PageStatus> pageStatusHashMap = new ConcurrentHashMap<>();

    public PageStatus lockPage(Page page, SessionUser editor) {
        editor.getSessionContext().onDestroyed.addListener(() -> unlockPage(page, editor)); // TODO: onLogout ?
        return pageStatusHashMap.computeIfAbsent(page, page1 -> new PageStatus(true, editor));
    }

    public void unlockPage(Page page, SessionUser editor) {
        PageStatus pageStatus;

        if (page == null || (pageStatus = getPageStatus(page)) == null) {
            return;
        }

        if (pageStatus.getEditor().equals(editor)) {
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
