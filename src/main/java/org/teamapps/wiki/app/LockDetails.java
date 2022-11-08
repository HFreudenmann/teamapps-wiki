package org.teamapps.wiki.app;

import org.teamapps.application.api.user.SessionUser;
import java.util.Date;

public class LockDetails {
    private final SessionUser setByUser;
    private final Date setAt;

    public LockDetails(SessionUser lockOwner) {
        this.setByUser = lockOwner;
        this.setAt = new Date();
    }

    public SessionUser getLockOwner() {
        return setByUser;
    }

    public Date getLockTime() {
        return setAt;
    }
}