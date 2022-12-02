package org.teamapps.wiki;

import org.teamapps.application.server.ApplicationServer;
import org.teamapps.application.server.system.bootstrap.BootstrapSessionHandler;
import org.teamapps.model.controlcenter.User;
import org.teamapps.wiki.app.WikiApplicationBuilder;
import org.teamapps.wiki.model.wiki.Book;
import org.teamapps.wiki.model.wiki.Page;

import java.io.File;

public class Server {
    public static void main(String[] args) throws Exception {
        File basePath = new File("./data");
        ApplicationServer applicationServer = new ApplicationServer(basePath);

        BootstrapSessionHandler bootstrapSessionHandler = new BootstrapSessionHandler() {
            @Override
            public void createInitialUser() {
                AccountData.createDemoData();
            }
        };
		applicationServer.setSessionHandler(bootstrapSessionHandler);
		applicationServer.start();

        bootstrapSessionHandler.getSystemRegistry().installAndLoadApplication(new WikiApplicationBuilder());

        // Generate Demo data
        BaseData.createDemoData();

        System.out.println("User accounts:   ");
        User.getAll().forEach(user -> System.out.println("   Name: " + user.getFirstName() + " " + user.getLastName() + ",  Login: " + user.getLogin()));
        System.out.println("Wiki Content:   Books : " + Book.getCount() + ", Pages : " + Page.getCount());
    }
}
