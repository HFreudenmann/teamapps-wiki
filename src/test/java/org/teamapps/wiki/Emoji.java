package org.teamapps.wiki;

import org.teamapps.icon.emoji.EmojiIconBrowser;
import org.teamapps.server.jetty.embedded.TeamAppsJettyEmbeddedServer;
import org.teamapps.ux.component.Component;
import org.teamapps.ux.component.rootpanel.RootPanel;
import org.teamapps.webcontroller.WebController;

public class Emoji {
    public static void main(String[] args) throws Exception {
        WebController controller = (sessionContext) -> {
            RootPanel rootPanel = new RootPanel();
            sessionContext.addRootPanel((String)null, rootPanel);
            Component emojiIconBrowser = (new EmojiIconBrowser(sessionContext)).getUI();
            rootPanel.setContent(emojiIconBrowser);
        };
        (new TeamAppsJettyEmbeddedServer(controller, 8082)).start();
    }
}
