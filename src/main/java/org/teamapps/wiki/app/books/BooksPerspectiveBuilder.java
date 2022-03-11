package org.teamapps.wiki.app.books;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.perspective.AbstractPerspectiveBuilder;
import org.teamapps.application.api.application.perspective.ApplicationPerspective;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.databinding.MutableValue;
import org.teamapps.icon.emoji.EmojiIcon;

public class BooksPerspectiveBuilder extends AbstractPerspectiveBuilder {

    public BooksPerspectiveBuilder() {
        super("booksPerspective", EmojiIcon.BOOKS, "Book Overview", "View & Organize Books");
    }

    @Override
    public boolean isPerspectiveAccessible(ApplicationPrivilegeProvider privilegeProvider) {
        return true;
    }

    @Override
    public boolean useToolbarPerspectiveMenu() {
        return true;
    }

    @Override
    public boolean autoProvisionPerspective() {
        return true;
    }

    @Override
    public ApplicationPerspective build(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
        return new BooksPerspective(applicationInstanceData, perspectiveInfoBadgeValue);
    }
}
