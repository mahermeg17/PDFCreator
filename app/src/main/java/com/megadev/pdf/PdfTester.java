package com.megadev.pdf;

import android.graphics.Color;
import android.os.Environment;
import android.util.Log;

import com.google.common.io.Files;
import com.penta.boxable_android.BaseTable;
import com.penta.boxable_android.Cell;
import com.penta.boxable_android.Row;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfTester {
    private static final String TAG = "PdfTester";

    private static final PDFont FONT = PDType1Font.HELVETICA;
    private static final float FONT_SIZE = 12;
    private static final float LEADING = -1.5f * FONT_SIZE;
    float marginY = 80;
    float marginX = 60;
    private static float contentWidth;

    private static float X_START_PAGE;
    private float Y_CURSOR;
    private float Y_UpperRight;

    private PDPage currentPage;
    private PDPageContentStream contentStream;

    private PDDocument doc;

    public void initDocument() {
        try {
            doc = new PDDocument();
            currentPage = new PDPage();
            doc.addPage(currentPage);
            contentStream = new PDPageContentStream(doc, currentPage);

            PDRectangle mediaBox = currentPage.getMediaBox();

            contentWidth = mediaBox.getWidth() - 2 * marginX;
            X_START_PAGE = mediaBox.getLowerLeftX() + marginX;
            Y_UpperRight = Y_CURSOR = mediaBox.getUpperRightY() - marginY;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void generateReport() {
        try {

            initDocument();
            String text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt" +
                    " ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco" +
                    " laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in " +
                    " ut labore et dolore magna aliqua. Ut enim ad minim veniam,";

            Log.i(TAG, ">> new DOC : Y_CURSOR=" + Y_CURSOR);
            addParagraph(Y_CURSOR, text);
            addParagraph(Y_CURSOR, text);
            addParagraph(Y_CURSOR, text);

            addTable();

            contentStream = new PDPageContentStream(doc, currentPage, true, true);
            addParagraph(Y_CURSOR, "******** New table");

            addTable();

            contentStream = new PDPageContentStream(doc, currentPage, true, true);
            addParagraph(Y_CURSOR, text);
            addParagraph(Y_CURSOR, text);
            addParagraph(Y_CURSOR, "******** end of document *********");

            contentStream.close();
            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/Boxable_Test.pdf";
            File file = new File(filePath);

            Log.i(TAG, "Sample file saved at : " + file.getAbsolutePath());
            Files.createParentDirs(file);
            doc.save(file);
            doc.close();
            Log.i(TAG, "doc.close");
        } catch (Exception e) {
            Log.e(TAG, "Exception while trying to create pdf document - " + e);
            e.printStackTrace();
        }
    }


    private void addParagraph(float sy, String text) throws IOException {
        contentStream.beginText();

        List<String> lines = parseLines(text, contentWidth);
        contentStream.setFont(FONT, FONT_SIZE);
        contentStream.newLineAtOffset(X_START_PAGE, sy);
        for (String line : lines) {
            if (Y_CURSOR < marginY) {
                addNewPage();
                contentStream.beginText();
                contentStream.setFont(FONT, FONT_SIZE);
            }
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, LEADING);
            Y_CURSOR += LEADING;
        }
        contentStream.endText();
        Log.i(TAG, "addParagraph end : Y_CURSOR = " + Y_CURSOR);
    }

    private void addNewPage() {
        try {
            Log.i(TAG, ">> addNewPage : Y_CURSOR : " + Y_CURSOR + " > " + Y_UpperRight);
            contentStream.close();
            currentPage = new PDPage();
            doc.addPage(currentPage);
            contentStream = new PDPageContentStream(doc, currentPage, true, true);
            Y_CURSOR = Y_UpperRight;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addTable() throws IOException {

        try {
            // Set margins
            float margin = 60;

            List<String[]> facts = getFacts();

            float yStartNewPage = currentPage.getMediaBox().getHeight() - (2 * margin);

            Log.i(TAG, "addTable : Y_CURSOR = " + Y_CURSOR);
            // Initialize table
            float tableWidth = currentPage.getMediaBox().getWidth() - (2 * margin);
            boolean drawContent = true;

            float bottomMargin = 70;

            BaseTable table = new BaseTable(Y_CURSOR, yStartNewPage, bottomMargin, tableWidth, margin, doc, currentPage, true,
                    drawContent);

            // Create Header row
            Row<PDPage> headerRow = table.createRow(15f);
            Cell<PDPage> cell = headerRow.createCell(100, "Awesome Facts About Belgium");
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFillColor(Color.BLACK);
            cell.setTextColor(Color.WHITE);

            table.addHeaderRow(headerRow);

            // Create 2 column row
            Row<PDPage> row = table.createRow(15f);
            cell = row.createCell(30, "Source:");
            cell.setFont(PDType1Font.HELVETICA);

            cell = row.createCell(70, "http://www.factsofbelgium.com/");
            cell.setFont(PDType1Font.HELVETICA_OBLIQUE);

            // Create Fact header row
            Row<PDPage> factHeaderrow = table.createRow(15f);

            cell = factHeaderrow.createCell((100 / 3f) * 2, "Fact");
            cell.setFont(PDType1Font.HELVETICA);
            cell.setFontSize(6);
            cell.setFillColor(Color.LTGRAY);

            cell = factHeaderrow.createCell((100 / 3f), "Tags");
            cell.setFillColor(Color.LTGRAY);
            cell.setFont(PDType1Font.HELVETICA_OBLIQUE);
            cell.setFontSize(6);

            // Add multiple rows with random facts about Belgium
            for (String[] fact : facts) {
                row = table.createRow(10f);
                cell = row.createCell((100 / 3f) * 2, fact[0]);
                cell.setFont(PDType1Font.HELVETICA);
                cell.setFontSize(6);

                for (int i = 1; i < fact.length; i++) {
                    if (fact[i].startsWith("image:")) {
                    } else {
                        cell = row.createCell((100 / 9f), fact[i]);
                        cell.setFont(PDType1Font.HELVETICA_OBLIQUE);
                        cell.setFontSize(6);
                        // Set colors
                        if (fact[i].contains("beer"))
                            cell.setFillColor(Color.YELLOW);
                        if (fact[i].contains("champion"))
                            cell.setTextColor(Color.GREEN);
                    }
                }
            }

            float tableLastY = table.draw();
            Y_CURSOR = tableLastY + LEADING;
            Log.i(TAG, "after table.draw : Y_CURSOR = " + Y_CURSOR);

            if (table.getCurrentPage() != currentPage) {
                Log.i(TAG, ">> we change the page");
                contentStream.close();
                currentPage = table.getCurrentPage();

            }
            if (Y_CURSOR < marginY) {
                addNewPage();
                contentStream.beginText();
                contentStream.setFont(FONT, FONT_SIZE);
                Y_CURSOR = Y_UpperRight;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> parseLines(String text, float width) throws IOException {
        List<String> lines = new ArrayList<String>();
        int lastSpace = -1;
        while (text.length() > 0) {
            int spaceIndex = text.indexOf(' ', lastSpace + 1);
            if (spaceIndex < 0)
                spaceIndex = text.length();
            String subString = text.substring(0, spaceIndex);
            float size = FONT_SIZE * FONT.getStringWidth(subString) / 1000;
            if (size > width) {
                if (lastSpace < 0) {
                    lastSpace = spaceIndex;
                }
                subString = text.substring(0, lastSpace);
                lines.add(subString);
                text = text.substring(lastSpace).trim();
                lastSpace = -1;
            } else if (spaceIndex == text.length()) {
                lines.add(text);
                text = "";
            } else {
                lastSpace = spaceIndex;
            }
        }
        return lines;
    }

    private static List<String[]> getFacts() {
        List<String[]> facts = new ArrayList<String[]>();
        facts.add(new String[]{"Oil Painting was invented by the Belgian van Eyck brothers", "art", "inventions",
                "science"});
        facts.add(new String[]{"The Belgian Adolphe Sax invented the Saxophone", "inventions", "music", ""});
        facts.add(new String[]{"11 sites in Belgium are on the UNESCO World Heritage List", "art", "history", ""});
        facts.add(new String[]{"Belgium was the second country in the world to legalize same-sex marriage",
                "politics", "image:150dpi.png", ""});
        facts.add(new String[]{"In the seventies, schools served light beer during lunch", "health", "school",
                "beer"});
        facts.add(new String[]{"Belgium has the sixth fastest domestic internet connection in the world", "science",
                "technology", ""});
        facts.add(new String[]{"Belgium hosts the World's Largest Sand Sculpture Festival", "art", "festivals",
                "world championship"});
        facts.add(
                new String[]{"Belgium has compulsary education between the ages of 6 and 18", "education", "", ""});
        facts.add(new String[]{
                "Belgium also has more comic makers per square kilometer than any other country in the world", "art",
                "social", "world championship"});
        facts.add(new String[]{
                "Belgium has one of the lowest proportion of McDonald's restaurants per inhabitant in the developed world",
                "food", "health", ""});
        facts.add(new String[]{"Belgium has approximately 178 beer breweries", "beer", "food", ""});
        facts.add(new String[]{"Gotye was born in Bruges, Belgium", "music", "celebrities", ""});
        facts.add(new String[]{"The Belgian Coast Tram is the longest tram line in the world", "technology",
                "world championship", ""});
        facts.add(new String[]{"Stefan Everts is the only motocross racer with 10 World Championship titles.",
                "celebrities", "sports", "world champions"});
        facts.add(new String[]{"Tintin was conceived by Belgian artist Hergé", "art", "celebrities", "inventions"});
        facts.add(new String[]{"Brussels Airport is the world's biggest selling point of chocolate", "food",
                "world champions", ""});
        facts.add(new String[]{"Tomorrowland is the biggest electronic dance music festival in the world",
                "festivals", "music", "world champion"});
        facts.add(new String[]{"French Fries are actually from Belgium", "food", "inventions", "image:300dpi.png"});
        facts.add(new String[]{"Herman Van Rompy is the first full-time president of the European Council",
                "politics", "", ""});
        facts.add(new String[]{"Belgians are the fourth most money saving people in the world", "economy", "social",
                ""});
        facts.add(new String[]{
                "The Belgian highway system is the only man-made structure visible from the moon at night",
                "technology", "world champions", ""});
        facts.add(new String[]{"Andreas Vesalius, the founder of modern human anatomy, is from Belgium",
                "celebrities", "education", "history"});

        return facts;
    }

}
