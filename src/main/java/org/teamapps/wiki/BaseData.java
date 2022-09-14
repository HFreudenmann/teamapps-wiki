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
                .setTitle("Pieces of wisdom").setDescription("Latin pieces of wisdom from all around the world")
                .save();

        Chapter chapter1 = Chapter.create()
                .setBook(demoBook)
                .setTitle("Introduction").setDescription("Introduction into the world of pieces of wisdom")
                .save();

        Chapter chapter2 = Chapter.create()
                .setBook(demoBook)
                .setTitle("Health").setDescription("Pieces of wisdom concerning health")
                .save();
        Page page1 = Page.create()
                .setChapter(chapter2)
                .setTitle("Happiness").setDescription("...")
                .setContent("<h2>Pieces of wisdom concerning happiness</h2>" +
                        "<h3>... from Europe</h3>" +
                        "<p>" + createRandomLatinPhrases(createRandomCount()) + "</p>" +
                        "<p>" + createRandomLatinPhrases(createRandomCount()) + "</p>" +
                        "<h3>... from Asia</h3>" +
                        "<p>" + createRandomLatinPhrases(createRandomCount()) + "</p>"
                )
                .save();
        Page.create()
                .setChapter(chapter2).setParent(page1)
                .setTitle("Rare phrases I").setDescription("...")
                .setContent("<p>" + createRandomLatinPhrases(createRandomCount()) + "</p>")
                .save();
        Page.create()
                .setChapter(chapter2).setParent(page1)
                .setTitle("Rare phrases II").setDescription("...")
                .setContent("<p>" + createRandomLatinPhrases(createRandomCount()) + "</p>")
                .save();
        Page subpage1_3 = Page.create()
                .setChapter(chapter2).setParent(page1)
                .setTitle("Famous phrases").setDescription("...")
                .setContent("<p>" + createRandomLatinPhrases(createRandomCount()) + "</p>")
                .save();
        Page.create()
                .setChapter(chapter2).setParent(subpage1_3)
                .setTitle("Famous phrases a)").setDescription("...")
                .setContent("<p>" + createRandomLatinPhrases(createRandomCount()) + "</p>")
                .save();
        Page.create()
                .setChapter(chapter2).setParent(subpage1_3)
                .setTitle("Famous phrases b)").setDescription("...")
                .setContent("<p>" + createRandomLatinPhrases(createRandomCount()) + "</p>")
                .save();

        Page.create()
                .setChapter(chapter2)
                .setTitle("Contentment").setDescription("...")
                .setContent("<h2>Pieces of wisdom concerning contentment</h2>" +
                        "<p>" + createRandomLatinPhrases(createRandomCount()) + "</p>" +
                        "<p>" + createRandomLatinPhrases(createRandomCount()) + "</p>")
                .save();

        Chapter chapter3 = Chapter.create()
                .setTitle("Short phrases")
                .setDescription("...")
                .setBook(demoBook)
                .save();
        Page.create()
                .setChapter(chapter3)
                .setContent("<p>Carpe diem. Anno Domini. A priori. Curriculum Vitae. Ora et labora. De facto.</p>")
                .setContentBlocks(List.of(
                        ContentBlock.create()
                                .setContentBlockType(ContentBlockType.RICH_TEXT)
                                .setValue("<h4>Title of content block 1</h4>" +
                                        "<i>A priori. Curriculum Vitae. Ora et labora. De facto.</i>"),
                        ContentBlock.create()
                                .setContentBlockType(ContentBlockType.RICH_TEXT)
                                .setValue("<h5>Title of content block 2</h5>" +
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
                .setTitle("Getting Started")
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
                .setTitle("Refining Your Infrastructure")
                .save();
        Chapter.create()
                .setBook(demoBook)
                .setTitle("Configuring Your Services")
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
                .setTitle("Developing Courage and Self-Confidence")
                .setContent("<p>MORE than five hundred thousend men and women, since 1912, have been members ..</p>")
                .save();

        Chapter.create()
                .setBook(demoBook)
                .setTitle("Self-Confidence Through Preparation").setDescription("...")
                .save();
        Chapter.create()
                .setBook(demoBook)
                .setTitle("How Famous Speakers Prepared Their Addresses").setDescription("...")
                .save();
        Chapter.create()
                .setBook(demoBook)
                .setTitle("The Improvement of Memory").setDescription("...")
                .save();
        Chapter.create()
                .setBook(demoBook)
                .setTitle("Essential Elements of Successful Speaking").setDescription("...")
                .save();
        Chapter.create()
                .setBook(demoBook)
                .setTitle("The Secret of Good Delivery").setDescription("...")
                .save();
        Chapter.create()
                .setBook(demoBook)
                .setTitle("Platform Presence and Personality").setDescription("...")
                .save();
        Chapter.create()
                .setBook(demoBook)
                .setTitle("How to Open a Talk").setDescription("...")
                .save();
        Chapter.create()
                .setBook(demoBook)
                .setTitle("How to Close a Talk").setDescription("...")
                .save();
        Chapter.create()
                .setBook(demoBook)
                .setTitle("How to Make Your Meaning Clear").setDescription("...")
                .save();
        Chapter.create()
                .setBook(demoBook)
                .setTitle("How to Interest Your Audience").setDescription("...")
                .save();
        Chapter.create()
                .setBook(demoBook)
                .setTitle("Improving Your Diction").setDescription("...")
                .save();
    }

    private static void createEmptyBooks() {

        Book.create().setTitle("Video Editing").setDescription("How to cut, modify and arrange videos")
                .save();
        Book.create().setTitle("Film recording").setDescription("How to record professional videos")
                .save();
        Book.create().setTitle("Mixer manual for XYZ").setDescription("Manual of audio mixer XYZ")
                .save();
        Book.create().setTitle("French Grammar").setDescription("...")
                .save();
        Book.create().setTitle("French Dictionary").setDescription("...")
                .save();
        Book.create().setTitle("German Dictionary").setDescription("...")
                .save();
        Book.create().setTitle("Java for Beginners").setDescription("How to programm with Java 17")
                .save();
        Book.create().setTitle("Ubuntu 22.04 Deskop").setDescription("Basics of Ubuntu 22.04 Desktop edition")
                .save();
        Book.create().setTitle("Ubuntu 16.04 Deskop").setDescription("Basics of Ubuntu 16.04 Desktop edition")
                .save();
        Book.create().setTitle("Professional Audio Recording").setDescription("...")
                .save();
    }

    private static int createRandomCount() {
        final int minimumCount = 3;
        final int maximumCount = 111;
        return ThreadLocalRandom.current().nextInt(minimumCount, maximumCount);
    }

    private static String createRandomLatinPhrases(int noOfPhrases) {
        List<String> phrases = List.of("Carpe diem.",
                "<b>Faber est suae quisque fortunae.</b>",
                "Cessante causa cessat effectus.",
                "Amicus certus in re incerta cernitur.",
                "Anno Domini.",
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
