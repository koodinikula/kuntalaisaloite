package fi.om.municipalityinitiative.pdf;

import com.google.common.collect.Lists;
import com.itextpdf.text.*;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import fi.om.municipalityinitiative.newdto.email.CollectableInitiativeEmailInfo;
import fi.om.municipalityinitiative.newdto.service.Participant;
import org.joda.time.DateTime;

import java.io.OutputStream;
import java.util.List;

public class ParticipantToPdfExporter {

    public static final String DATETIME_FORMAT = "dd.MM.yyyy HH:mm:ss";
    public static final String DATE_FORMAT = "dd.MM.yyyy";
    public static final FontFamily FONT_FAMILY = Font.FontFamily.HELVETICA;
    
    private static Font mainTitle = new Font(FONT_FAMILY, 16, Font.BOLD);
    private static Font subTitle = new Font(FONT_FAMILY, 14, Font.BOLD);
    private static Font redFont = new Font(FONT_FAMILY, 12, Font.NORMAL, BaseColor.RED);
    private static Font bodyText = new Font(FONT_FAMILY, 10, Font.NORMAL);
    private static Font bodyTextItalic = new Font(FONT_FAMILY, 10, Font.ITALIC);
    private static Font smallBold = new Font(FONT_FAMILY, 10, Font.BOLD);

    public static void createPdf(CollectableInitiativeEmailInfo emailInfo, OutputStream outputStream) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();
            addMetaData(document, emailInfo);
            addTitlePage(document, emailInfo);
//            addContent(document);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // TODO: Localize

    // iText allows to add metadata to the PDF which can be viewed in your Adobe
    // Reader
    // under File -> Properties
    private static void addMetaData(Document document, CollectableInitiativeEmailInfo emailInfo) {
        document.addTitle("Kuntalaisaloite " + emailInfo.getMunicipalityName());
        document.addSubject(emailInfo.getName());
        document.addKeywords("Java, PDF, iText"); // TODO: Remove
        document.addAuthor("Lars Vogel");
        document.addCreator("Lars Vogel");
    }

    private static void addTitlePage(Document document, CollectableInitiativeEmailInfo emailInfo)
            throws DocumentException {
        Paragraph preface = new Paragraph();
        // We add one empty line
//        addEmptyLine(preface, 1);
        // Lets write a big header
        preface.add(new Paragraph("Kuntalaisaloite - " + emailInfo.getMunicipalityName(), mainTitle));
        preface.add(new Paragraph("Aloite lähetetty kuntaan " + new DateTime().toString(DATETIME_FORMAT), bodyText));
        addEmptyLine(preface, 1);

        // Will create: Report generated by: _name, _date
        preface.add(new Paragraph("Aloitteen otsikko", subTitle));
        preface.add(new Paragraph(emailInfo.getName(), bodyText));
        addEmptyLine(preface, 1);

        List<Participant> participantsFranchise = Lists.newArrayList();
        List<Participant> participantsNoFranchise = Lists.newArrayList();

        parseParticipants(emailInfo.getParticipants(), participantsFranchise, participantsNoFranchise);

        preface.add(new Paragraph("Äänioikeutetut kunnan asukkaat", subTitle));
        
        if (!participantsFranchise.isEmpty()) {
            addEmptyLine(preface, 1);
            createTable(preface, participantsFranchise);
        } else {
            preface.add(new Paragraph("Ei osallistujia", bodyTextItalic));
        }

        addEmptyLine(preface, 2);
        
        preface.add(new Paragraph("Muut kunnan jäsenet ja ei-äänioikeutetut asukkaat", subTitle));
        
        if (!participantsNoFranchise.isEmpty()) {
            addEmptyLine(preface, 1);
            createTable(preface, participantsNoFranchise);
        } else {
            preface.add(new Paragraph("Ei osallistujia", bodyTextItalic));
        }

        document.add(preface);
    }

    private static void parseParticipants(List<Participant> allParticipants,
                                          List<Participant> participantsFranchise,
                                          List<Participant> participantsNoFranchise) {
        for (Participant participant : allParticipants) {
            if (participant.isFranchise()) {
                participantsFranchise.add(participant);
            }
            else {
                participantsNoFranchise.add(participant);
            }
        }
    }

    private static void createTable(Paragraph subCatPart, List<Participant> participants)
            throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        
        table.setWidthPercentage(100);
        table.setWidths(new int[] {6, 10, 45, 18});
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        table.addCell(createCell("#", true));
        table.addCell(createCell("Päivämäärä", true));
        table.addCell(createCell("Nimi", true));
        table.addCell(createCell("Kotikunta", true));

        table.setHeaderRows(1);

        int count = 0;
        for (Participant participant : participants) {
            ++count;
            table.addCell(createCell(String.valueOf(count), false));
            table.addCell(createCell(participant.getParticipateDate().toString(DATE_FORMAT), false));
            table.addCell(createCell(participant.getName(), false));
            table.addCell(createCell(participant.getHomeMunicipality(), false));
        }

        subCatPart.add(table);
    }

    private static PdfPCell createCell(String header, boolean tableHead) {
        Font fontStyle = bodyText;
        
        if (tableHead) {
            fontStyle = smallBold;
        }
        
        PdfPCell c1 = new PdfPCell(new Phrase(header, fontStyle));
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c1.setPadding(4);

        return c1;
    }

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }
}