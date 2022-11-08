package org.teamapps.wiki.app;

import org.teamapps.application.api.user.SessionUser;

import java.util.Date;

public final class LockSuccessStatus {

    private final boolean successful;
    private final LockFailReason lockFailReason;
    private final LockDetails lockDetails;

    public LockSuccessStatus(SessionUser lockRequestedBy) {
        successful = true;
        lockFailReason = LockFailReason.NONE;
        this.lockDetails = new LockDetails(lockRequestedBy);
    }

    public LockSuccessStatus(LockFailReason reason) {
        successful = false;
        lockFailReason = reason;
        this.lockDetails = null;
    }

    public LockSuccessStatus(LockDetails details) {
        successful = false;
        lockFailReason = LockFailReason.LOCKED_BY_OTHER_USER;
        this.lockDetails = details;
    }


    public boolean hasReceivedLock () { return successful; }
    public LockFailReason getLockFailReason() { return lockFailReason; }

    public SessionUser getLockOwner() {
        if (lockDetails == null) {
            return null;
        }

        return lockDetails.getLockOwner();
    }

    public Date getLockTime() {
        if (lockDetails == null) {
            return null;
        }

        return lockDetails.getLockTime();
    }

    public LockDetails getLockDetails() { return lockDetails; }
}

