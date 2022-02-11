package org.teamapps.wiki;

import org.teamapps.application.api.password.SecurePasswordHash;
import org.teamapps.application.server.system.bootstrap.BootstrapSessionHandler;
import org.teamapps.application.server.system.server.ApplicationServer;
import org.teamapps.application.server.system.server.SessionRegistryHandler;
import org.teamapps.application.server.system.session.UserSessionData;
import org.teamapps.application.server.system.utils.ValueConverterUtils;
import org.teamapps.model.controlcenter.OrganizationUnit;
import org.teamapps.model.controlcenter.OrganizationUnitType;
import org.teamapps.model.controlcenter.User;
import org.teamapps.model.controlcenter.UserAccountStatus;
import org.teamapps.universaldb.index.translation.TranslatableText;
import org.teamapps.ux.session.SessionContext;
import org.teamapps.wiki.app.WikiApplicationBuilder;
import org.teamapps.wiki.model.wiki.Book;
import org.teamapps.wiki.model.wiki.Page;

import java.io.File;
import java.util.Arrays;

public class Server {
    public static void main(String[] args) throws Exception {
        File basePath = new File("./data");
        ApplicationServer applicationServer = new ApplicationServer(basePath);

        BootstrapSessionHandler bootstrapSessionHandler = new BootstrapSessionHandler(new SessionRegistryHandler() {
			@Override
			public void handleNewSession(SessionContext context) {
				// context.getIconProvider().setDefaultStyleForIconClass(StandardIcon.class, StandardIconStyles.VIVID_STANDARD_SHADOW_1);
			}

			@Override
			public void handleAuthenticatedUser(UserSessionData userSessionData, SessionContext sessionContext) {

			}
		});
		applicationServer.setSessionHandler(bootstrapSessionHandler);
		applicationServer.start();


        bootstrapSessionHandler.getSystemRegistry().installAndLoadApplication(new WikiApplicationBuilder());


        if (User.getCount() == 0) {
            OrganizationUnitType unitType = OrganizationUnitType.create().setName(TranslatableText.create("en", "Unit"));
            OrganizationUnit.create().setType(unitType).setName(TranslatableText.create("en", "Organization")).save();
            User.create().setFirstName("Super").setLastName("Admin").setLogin("admin").setPassword(SecurePasswordHash.createDefault().createSecureHash("teamapps!")).setUserAccountStatus(UserAccountStatus.SUPER_ADMIN).setLanguages(ValueConverterUtils.compressStringList(Arrays.asList("de", "en", "fr"))).save();
        }
        // // Generate Demo data
        BaseData.createBaseData();
        System.out.println("Books:" + Book.getCount());
        System.out.println("Pages:" + Page.getCount());
    }
}
