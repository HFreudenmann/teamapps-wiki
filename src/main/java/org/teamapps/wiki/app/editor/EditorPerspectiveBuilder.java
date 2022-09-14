package org.teamapps.wiki.app.editor;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.perspective.AbstractPerspectiveBuilder;
import org.teamapps.application.api.application.perspective.ApplicationPerspective;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.databinding.MutableValue;
import org.teamapps.icon.emoji.EmojiIcon;

public class EditorPerspectiveBuilder extends AbstractPerspectiveBuilder {

    public EditorPerspectiveBuilder() {
        super("wikiEditor", EmojiIcon.WRITING_HAND, "Edit Books", "Manage Wiki pages and content");
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
        return new EditorPerspective(applicationInstanceData, perspectiveInfoBadgeValue);
    }
}
