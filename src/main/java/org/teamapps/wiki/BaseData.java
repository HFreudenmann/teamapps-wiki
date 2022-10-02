package org.teamapps.wiki;

import org.teamapps.wiki.model.wiki.*;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class BaseData {

    public static void createDemoData() {

        boolean isDemoDataAvailable = (Book.getCount() > 0);

        if (isDemoDataAvailable) {
            System.out.println("    # books available : " + Book.getCount());
            return;
        }

        createLatinDemoBook();
        createUbuntuDemoBook();
        createCarnegieDemoBook();
        createEmptyBooks();

        System.out.println("    # books created : " + Book.getCount());
    }

    private static void createLatinDemoBook() {
        Book demoBook = Book.create()
                .setTitle("Pieces of wisdom").setDescription("Pieces of wisdom in latin from all around the world")
                .save();

        Chapter chapter1 = Chapter.create()
                .setBook(demoBook)
                .setTitle("Patience").setDescription("(Contains initially no pages)")
                .save();

        Chapter chapter2 = Chapter.create()
                .setBook(demoBook)
                .setTitle("Health").setDescription("Pieces of wisdom concerning health")
                .save();
        Page page1 = Page.create()
                .setChapter(chapter2)
                .setTitle("Fortune").setDescription("...")
                .setContent("<h2>Pieces of wisdom concerning fortune and happiness</h2>" +
                        "<h3>... from Occident</h3>" +
                        "<p>" + createRandomLatinPhrases(createRandomCount()) + "<br /></p>" +
                        "<p>" + createRandomLatinPhrases(createRandomCount()) + "<br /></p>" +
                        "<h3>... from Orient</h3>" +
                        "<p>" + createRandomLatinPhrases(createRandomCount()) + "</p>"
                )
                .save();
        Page.create()
                .setChapter(chapter2).setParent(page1)
                .setTitle("").setDescription("(Contains initially an empty string as Title)")
                .setContent("<p>" + createRandomLatinPhrases(createRandomCount()) + "</p>")
                .save();
        Page.create()
                .setChapter(chapter2).setParent(page1)
                .setDescription("(Contains initially no Title)")
                .setContent("<p>" + createRandomLatinPhrases(createRandomCount()) + "</p>")
                .save();
        Page subpage1_3 = Page.create()
                .setChapter(chapter2).setParent(page1)
                .setTitle("Composure").setDescription("...")
                .setContent("<p>" + createRandomLatinPhrases(createRandomCount()) + "</p>")
                .save();
        Page.create()
                .setChapter(chapter2).setParent(subpage1_3)
                .setTitle("Famous phrases").setDescription("...")
                .setContent("<p>" + createRandomLatinPhrases(createRandomCount()) + "</p>")
                .save();
        Page.create()
                .setChapter(chapter2).setParent(subpage1_3)
                .setTitle("Rare phrases").setDescription("...")
                .setContent("<p>" + createRandomLatinPhrases(createRandomCount()) + "</p>")
                .save();

        Page.create()
                .setChapter(chapter2)
                .setTitle("Freedom and wealth").setDescription("...")
                .setContent("<h2>Pieces of wisdom about freedom and wealth</h2>" +
                        "<p>" + createRandomLatinPhrases(createRandomCount()) + "<br /></p>" +
                        "<p>" + createRandomLatinPhrases(createRandomCount()) + "</p>")
                .save();

        Chapter chapter3 = Chapter.create()
                .setTitle("Short expressions").setDescription("Chapter with Content and Content Blocks")
                .setBook(demoBook)
                .save();
        Page.create()
                .setChapter(chapter3)
                .setDescription("A page without title (Title is by default = null)")
                .setContent("<p>Carpe diem. Anno Domini. A priori. Curriculum Vitae. Ora et labora. De facto. <br /><br />" +
                            "HINT: Content blocks are not yet editable.<br /></p>")
                .setContentBlocks(List.of(
                        ContentBlock.create()
                                .setContentBlockType(ContentBlockType.RICH_TEXT)
                                .setValue("<h3><span style=\"color: #a61fe5;\">Content Block 1</span></h3>" +
                                        "<i>A priori. Curriculum Vitae. Ora et labora. De facto.</i>"),
                        ContentBlock.create()
                                .setContentBlockType(ContentBlockType.RICH_TEXT)
                                .setValue("<h4><span style=\"color: #a61fe5;\">Content Block 2</span></h4>" +
                                        "<p>Curriculum Vitae. Ora et labora. De facto.</p>")
                ))
                .save();
    }


    private static void createUbuntuDemoBook() {
        Book demoBook = Book.create().setTitle("Ubuntu Server Manual")
                .setDescription("Information on using Ubuntu Server")
                .save();

        Chapter chapter1 = Chapter.create()
                .setBook(demoBook)
                .setTitle("Getting Started").setDescription("")
                .save();

        Page.create()
                .setChapter(chapter1)
                .setTitle("Basic Installation").setDescription("How to install Ubuntu Server Edition")
                .setContent(    "<h1>Basic installation</h1>" +
                                "<p>This chapter provides ... </p>" +
                                "<h2>Preparing to Install</h2>" +
                                "<p>This section explains ...</p>" +
                                "<h3>System requirements</h3>" +
                                "<p>...</p>" +
                                "<h3>Server and Desktop Edition differences</h3>" +
                                "<p>The main difference is ... </p>"
                )
                .save();
        Page.create()
                .setChapter(chapter1)
                .setTitle("Automated Installation").setDescription("Automate installation using autoinstall")
                .setContent("...")
                .save();
        Page.create()
                .setChapter(chapter1)
                .setTitle("Using Docker containers").setDescription("Run Ubuntu Server in a Docker container")
                .setContent("...")
                .save();
        Page.create()
                .setChapter(chapter1)
                .setTitle("Cloud Images").setDescription("Run Ubuntu Server on a cloud provider")
                .setContent("...")
                .save();

        Chapter.create()
                .setBook(demoBook)
                .setTitle("Refining Your Infrastructure (contains no description)")
                .save();
        Chapter.create()
                .setBook(demoBook)
                .setTitle("Configuring Your Services (contains no description)")
                .save();
    }

    private static void createCarnegieDemoBook() {

        Book demoBook = Book.create().setTitle("How to Develop Self Confidence ...  / D. Carnegie")
                .setDescription("From ways to develop self-confidence and become a good public speaker to the secrets of memory power ...")
                .save();

        Chapter chapter1 = Chapter.create()
                .setBook(demoBook)
                .setTitle("Developing Courage and Self-Confidence").setDescription("...")
                .save();
        Page.create()
                .setChapter(chapter1)
                .setTitle("Developing Courage and Self-Confidence (contains no description)")
                .setContent("<p>'MORE than five hundred thousand men and women, since 1912, have been members ..'</p>")
                .save();

        Chapter.create()
                .setBook(demoBook)
                .setTitle("Self-Confidence Through Preparation").setDescription("(initially empty chapter)")
                .save();
        Chapter.create()
                .setBook(demoBook)
                .setTitle("How Famous Speakers Prepared Their Addresses").setDescription("(initially empty chapter)")
                .save();
        Chapter.create()
                .setBook(demoBook)
                .setTitle("The Improvement of Memory").setDescription("(initially empty chapter)")
                .save();
        Chapter.create()
                .setBook(demoBook)
                .setTitle("Essential Elements of Successful Speaking").setDescription("(initially empty chapter)")
                .save();
        Chapter.create()
                .setBook(demoBook)
                .setTitle("The Secret of Good Delivery").setDescription("(initially empty chapter)")
                .save();
        Chapter.create()
                .setBook(demoBook)
                .setTitle("Platform Presence and Personality").setDescription("(initially empty chapter)")
                .save();
        Chapter.create()
                .setBook(demoBook)
                .setTitle("How to Open a Talk").setDescription("(initially empty chapter)")
                .save();
        Chapter.create()
                .setBook(demoBook)
                .setTitle("How to Close a Talk").setDescription("(initially empty chapter)")
                .save();
        Chapter.create()
                .setBook(demoBook)
                .setTitle("How to Make Your Meaning Clear").setDescription("(initially empty chapter)")
                .save();
        Chapter.create()
                .setBook(demoBook)
                .setTitle("How to Interest Your Audience").setDescription("(initially empty chapter)")
                .save();
        Chapter.create()
                .setBook(demoBook)
                .setTitle("Improving Your Diction").setDescription("(initially empty chapter)")
                .save();
    }

    private static void createEmptyBooks() {

        List<String> bookTitles = List.of(
                "Video Editing", "Film recording", "Mixer manual for XYZ", "Java for Beginners",
                "Ubuntu 22.04 Deskop", "Ubuntu 16.04 Deskop", "Professional Audio Recording",
                "French Grammar", "German Dictionary");

        for (String bookTitle : bookTitles) {
            Book.create().setTitle(bookTitle + ", Vol. 1").setDescription("(initially empty book)")
                    .save();
        }
        for (String bookTitle : bookTitles) {
            Book.create().setTitle(bookTitle + ", Vol. 2").setDescription("(initially empty book)")
                    .save();
        }
        for (String bookTitle : bookTitles) {
            Book.create().setTitle(bookTitle + ", Vol. 3").setDescription("(initially empty book)")
                    .save();
        }
    }

    private static int createRandomCount() {
        final int minimumCount = 9;
        final int maximumCount = 199;
        return ThreadLocalRandom.current().nextInt(minimumCount, maximumCount);
    }

    private static String createRandomLatinPhrases(int noOfPhrases) {
        List<String> phrases = List.of("Carpe diem.",
                "<b>Faber est suae quisque fortunae.</b>",
                "Cessante causa cessat effectus.",
                "Amicus certus in re incerta cernitur.",
                "<b><i>Summae opes inopia cupiditatum.</i></b>",
                "Exercitatio artem parat.",
                "A <b>priori</b>.",
                "Male parta, male dilabuntur.",
                "Malum est <i>consilium</i>, quod mutari non potest!",
                "Contra vim mortis non est medicamen in hortis.",
                "Curriculum Vitae.",
                "Melius est prevenire quam preveniri.",
                "Caritas omnia potest.",
                "Ora et labora.",
                "<i>Quod tibi fieri non vis, <b>alteri</b> ne feceris.</i>",
                "<u>De facto.</u>",
                "Caritas omnia tolerat. Sol lucet omnibus.",
                "Abyssus abyssum invocat.",
                "Qui dare multa potest, multa et amare potest.",
                "Nihil fit sine causa. Omne animal se ipse diligit.",
                "Amor est parens multarum voluptatum.",
                "<b>Si deus pro nobis, quis contra nos?</b>",
                "Abyssus abyssum invocat.",
                "Qui dare multa potest, multa et amare potest.",
                "Nihil fit sine causa.",
                "<i>Omne animal se ipse diligit.</i>",
                "Amor est parens multarum voluptatum.",
                "Si deus pro nobis, quis contra nos?");

        boolean isEndOfListReached;
        StringBuilder combinedString = new StringBuilder();

        // set a random start index
        int j = ThreadLocalRandom.current().nextInt(0, phrases.size() - 1);

        for (int i = 0; i < noOfPhrases; i++) {
            combinedString.append(phrases.get(j)).append(" ");

            j++;
            isEndOfListReached = (j >= phrases.size());
            if (isEndOfListReached) {
                j = 0;
            }
        }

        return combinedString.toString();
    }

}
