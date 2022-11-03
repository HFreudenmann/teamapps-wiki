package org.teamapps.wiki;

import org.teamapps.application.api.password.SecurePasswordHash;
import org.teamapps.model.controlcenter.OrganizationUnit;
import org.teamapps.model.controlcenter.OrganizationUnitType;
import org.teamapps.model.controlcenter.User;
import org.teamapps.model.controlcenter.UserAccountStatus;
import org.teamapps.universaldb.index.translation.TranslatableText;

public class AccountData {
    public static void createDemoData() {

        boolean areUserAccountsCreated = (User.getCount() > 0);

        if (areUserAccountsCreated) {
            System.out.println("    # user accounts available : " + User.getCount());
            return;
        }

        OrganizationUnitType unitType = OrganizationUnitType.create()
                .setName(TranslatableText.create("Unit-123", "en"));
        OrganizationUnit.create()
                .setType(unitType)
                .setName(TranslatableText.create("Organization-789", "en"))
                .save();
//            Group   bookManager = Group.create().setName("Book manager");

        User.create()
                .setFirstName("Super")
                .setLastName("Admin")
                .setLogin("admin")
                .setPassword(SecurePasswordHash.createDefault().createSecureHash("teamapps!"))
                .setUserAccountStatus(UserAccountStatus.SUPER_ADMIN)
                .setLanguage("de")
                .save();
        User.create()
                .setFirstName("Demo")
                .setLastName("User")
                .setLogin("demo")
                .setPassword(SecurePasswordHash.createDefault().createSecureHash("demo!"))
                .setUserAccountStatus(UserAccountStatus.ACTIVE)
                .setLanguage("en")
                .save();
        User.create()
                .setEmail("sunshine@mail.de")
                .setPhone("+49 3333 1234-56")
                .setLogin("yves")
                .setPassword(SecurePasswordHash.createDefault().createSecureHash("helper!"))
                .setUserAccountStatus(UserAccountStatus.ACTIVE)
                .setLanguage("fr")
                .save();

        System.out.println("    # users created : " + User.getCount());
    }
}
