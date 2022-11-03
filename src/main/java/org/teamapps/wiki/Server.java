package org.teamapps.wiki;

import org.teamapps.application.server.system.bootstrap.BootstrapSessionHandler;
import org.teamapps.application.server.ApplicationServer;
import org.teamapps.application.server.system.server.SessionRegistryHandler;
import org.teamapps.application.server.system.session.UserSessionData;
import org.teamapps.ux.session.SessionContext;
import org.teamapps.wiki.app.WikiApplicationBuilder;
import org.teamapps.wiki.model.wiki.Book;
import org.teamapps.wiki.model.wiki.Page;

import java.io.File;

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

        // Generate Demo data
        AccountData.createDemoData();
        BaseData.createDemoData();

        System.out.println("Wiki Content:   Books : " + Book.getCount() + ", Pages : " + Page.getCount());
    }
}
