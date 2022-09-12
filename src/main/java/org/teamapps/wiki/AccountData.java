package org.teamapps.wiki;

import org.teamapps.application.api.password.SecurePasswordHash;
import org.teamapps.application.server.system.utils.ValueConverterUtils;
import org.teamapps.model.controlcenter.*;
import org.teamapps.universaldb.index.translation.TranslatableText;

import java.util.Arrays;

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
                .setLanguages(ValueConverterUtils.compressStringList(Arrays.asList("de", "en", "fr", "ru")))
                .save();
        User.create()
                .setFirstName("Demo")
                .setLastName("User")
                .setLogin("demo")
                .setPassword(SecurePasswordHash.createDefault().createSecureHash("demo!"))
                .setUserAccountStatus(UserAccountStatus.ACTIVE)
                .setLanguages(ValueConverterUtils.compressStringList(Arrays.asList("de", "en", "bg", "es")))
                .save();
/*
            User.create()
                    .setEmail("sunshine@mail.de")
                    .setPhone("+49 3333 1234-56")
                    .setLogin("helper")
                    .setPassword(SecurePasswordHash.createDefault().createSecureHash("helper!"))
                    .setUserAccountStatus(UserAccountStatus.ACTIVE)
                    .setPrivateMessages(Message.create().setMessage("Please change your password!"))
                    .setAllGroupMemberships(UserGroupMembership.create().setGroup(bookManager))
                    .setLanguages(ValueConverterUtils.compressStringList(Arrays.asList("de", "en", "fr", "it", "es")))
                    .save();
*/
        System.out.println("    # users created : " + User.getCount());
    }
}
