package org.teamapps.wiki.app;

import org.teamapps.application.api.application.AbstractApplicationBuilder;
import org.teamapps.application.api.application.perspective.PerspectiveBuilder;
import org.teamapps.application.api.config.ApplicationConfig;
import org.teamapps.application.api.localization.LocalizationData;
import org.teamapps.application.api.privilege.ApplicationPrivilegeProvider;
import org.teamapps.application.api.privilege.ApplicationRole;
import org.teamapps.application.api.privilege.PrivilegeGroup;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.api.versioning.ApplicationVersion;
import org.teamapps.icon.emoji.EmojiIcon;
import org.teamapps.universaldb.schema.SchemaInfoProvider;
import org.teamapps.wiki.app.books.BooksPerspectiveBuilder;
import org.teamapps.wiki.app.editor.EditorPerspectiveBuilder;
import org.teamapps.wiki.model.WikiSchema;

import java.io.File;
import java.util.*;

public class WikiApplicationBuilder extends AbstractApplicationBuilder {

    public static WikiPageManager PAGE_MANAGER = new WikiPageManager();

    public WikiApplicationBuilder() {
        super("wiki", EmojiIcon.BOOKS, "Wiki", "Wiki Books");
    }

    @Override
    public List<PerspectiveBuilder> getPerspectiveBuilders() {
        return Arrays.asList(
                new BooksPerspectiveBuilder(),
                new EditorPerspectiveBuilder()
        );
    }

    @Override
    public ApplicationVersion getApplicationVersion() {
        return ApplicationVersion.create(0, 1, 5);
    }

    @Override
    public List<ApplicationRole> getApplicationRoles() {
        return Collections.emptyList();
    }

    @Override
    public List<PrivilegeGroup> getPrivilegeGroups() {
        return Collections.emptyList();
    }

    @Override
    public LocalizationData getLocalizationData() {
        return LocalizationData.createFromPropertyFiles("org.teamapps.wiki.i18n.captions", getClass().getClassLoader(), Locale.ENGLISH);
    }

    @Override
    public SchemaInfoProvider getDatabaseModel() {
        return new WikiSchema();
    }

    @Override
    public boolean useToolbarApplicationMenu() {
        return true;
    }

    @Override
    public ApplicationConfig<?> getApplicationConfig() {
        return null;
    }

    @Override
    public boolean isApplicationAccessible(ApplicationPrivilegeProvider privilegeProvider) {
        return true;
    }

}
