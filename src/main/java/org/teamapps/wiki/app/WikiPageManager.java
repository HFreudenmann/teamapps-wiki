package org.teamapps.wiki.app;

import org.teamapps.application.api.user.SessionUser;
import org.teamapps.wiki.model.wiki.Page;

import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class WikiPageManager {
    private final ConcurrentHashMap<Page, LockDetails> pageLockHashMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<SessionUser, HashSet<Page>> userLockHashMap = new ConcurrentHashMap<>();

    public void addReleaseUserLockListener(SessionUser editor) {
        System.out.println("addReleaseUserLockListener : " + editor.getName(false));
        editor.getSessionContext().onDestroyed.addListener(() -> releaseLocksFor(editor));
        editor.onUserLogout().addListener(() -> releaseLocksFor(editor));
    }

    public LockSuccessStatus lockPage(Page page, SessionUser editor) {

        LockSuccessStatus lockStatus;

        if (Objects.isNull(page)) {
            System.err.println("   Try to lock page = NULL");
            return new LockSuccessStatus(LockFailReason.INVALID_INPUT);
        }
        if (Objects.isNull(editor)) {
            System.err.println("   Try to lock page = " + page.getId() + " with editor = NULL");
            return new LockSuccessStatus(LockFailReason.INVALID_INPUT);
        }

        System.out.println("   lock page = " + page.getId());
        if (pageLockHashMap.containsKey(page)) {
            LockDetails existingLock = pageLockHashMap.get(page);

            if (editor.equals(existingLock.getLockOwner())) {
                System.err.println("Try repeatedly to lock by user : " + editor.getName(false));
                return new LockSuccessStatus(editor);
            } else {
                return new LockSuccessStatus(existingLock);
            }
        } else {
            LockSuccessStatus successfulLockStatus = new LockSuccessStatus(editor);
            pageLockHashMap.put(page, successfulLockStatus.getLockDetails());
            addPageLockToUserMap(page, editor);
            return successfulLockStatus;
        }
    }

    public void unlockPage(Page page, SessionUser editor) {
        LockDetails existingLock;

        if (Objects.isNull(page)) {
            System.err.println("   Try to unlock page = NULL");
            return;
        }
        if (Objects.isNull(editor)) {
            System.err.println("   Try to unlock page = " + page.getId() + " with editor = NULL");
            return;
        }
        System.out.println("   unlock page = " + page.getId());

        existingLock = pageLockHashMap.get(page);
        if (existingLock == null) {
            System.err.println("   Failed to unlock page. Page not found");
        } else {
            SessionUser lockOwner = existingLock.getLockOwner();
            if (lockOwner == null) {
                System.err.println("   Failed to unlock page; user is NULL!");
            } else if (lockOwner.equals(editor)) {
                pageLockHashMap.remove(page);
                removePageLockFromUserMap(page, editor);
            } else {
                System.err.println("   Failed to unlock page; page is locked by another user : " + lockOwner.getName(false));
            }
        }
    }

    private void addPageLockToUserMap(Page page, SessionUser lockOwner) {

        HashSet<Page> userLocks;

        if (userLockHashMap.containsKey(lockOwner)) {
            userLocks = userLockHashMap.get(lockOwner);
        } else {
            userLocks = new HashSet<>();
            userLockHashMap.put(lockOwner, userLocks);
        }
        if (!userLocks.add(page)) {
            System.err.println("addPageLockToUserMap: page " + page.getId() + " is already in the map!");
        }
    }

    private void removePageLockFromUserMap(Page page, SessionUser lockOwner) {

        if (userLockHashMap.containsKey(lockOwner)) {
            HashSet<Page> userLocks = userLockHashMap.get(lockOwner);

            if (!userLocks.remove(page)) {
                System.err.println("removePageLockFromUserMap: page " + page.getId() + " is not in the map!");
            }
        } else {
            System.err.println("removePageLockFromUserMap: user " + lockOwner.getName(false) + " is not in the map!");
        }
    }

    private void releaseLocksFor(SessionUser user) {

        if (Objects.isNull(user)) {
            System.err.println("releaseLocks: user is NULL");
            return;
        }
        System.out.println("releaseLocksFor: user " + user.getName(false));

        if (userLockHashMap.containsKey(user)) {
            HashSet<Page> userLocks = userLockHashMap.get(user);
            for (Page page : userLocks) {
                System.out.println("   release lock : " + page.getId());
                pageLockHashMap.remove(page);
            }
            userLocks.clear();
        }
    }

}
