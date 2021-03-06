package fi.om.municipalityinitiative.service.email;

import fi.om.municipalityinitiative.conf.EnvironmentSettings;
import fi.om.municipalityinitiative.dao.EmailDao;
import fi.om.municipalityinitiative.dao.InitiativeDao;
import fi.om.municipalityinitiative.dao.ParticipantDao;
import fi.om.municipalityinitiative.dto.service.EmailDto;
import fi.om.municipalityinitiative.dto.service.Initiative;
import fi.om.municipalityinitiative.dto.service.Participant;
import fi.om.municipalityinitiative.pdf.ParticipantToPdfExporter;
import fi.om.municipalityinitiative.util.EmailAttachmentType;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.transaction.annotation.Transactional;

import javax.activation.DataSource;
import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class EmailSender {

    private static final Logger log = LoggerFactory.getLogger(EmailSender.class);

    @Resource
    private EmailDao emailDao;

    @Resource
    private InitiativeDao initiativeDao;

    @Resource
    private ParticipantDao participantDao;

    @Resource
    private JavaMailSender javaMailSender;

    @Resource
    private EnvironmentSettings environmentSettings;

    @Transactional(readOnly = false)
    public Optional<EmailDto> popUntriedEmail() {
        return emailDao.popUntriedEmailForUpdate();
    }

    @Transactional(readOnly = false)
    public void failed(EmailDto emailDto) {
        emailDao.failed(emailDto.getEmailId());
    }

    @Transactional(readOnly = false)
    public void succeed(EmailDto emailDto) {
        emailDao.succeed(emailDto.getEmailId());
    }

    @Transactional(readOnly = true)
    public void constructAndSendEmail(EmailDto emailDto) throws MessagingException {
        if (environmentSettings.isTestConsoleOutput()) {
            printEmail(emailDto);
        } else {
            MimeMessageHelper helper = constructEmail(emailDto);
            javaMailSender.send(helper.getMimeMessage());
        }
    }

    private void printEmail(EmailDto emailDto) {
        System.out.println("----------------------------------------------------------");
        System.out.println("To: " + emailDto.getRecipientsAsString());
        System.out.println("Reply-to: " + environmentSettings.getDefaultReplyTo());
        System.out.println("Subject: " + emailDto.getSubject());
        System.out.println("---");
        System.out.println(emailDto.getBodyText());
        System.out.println("----------------------------------------------------------");
    }

    private MimeMessageHelper constructEmail(EmailDto emailDto) throws MessagingException {
        if (environmentSettings.getTestSendTo().isPresent()) {
            log.warn("Replaced recipients email with: "+ environmentSettings.getTestSendTo());
            emailDto.setRecipients(Collections.singletonList(environmentSettings.getTestSendTo().get()));
        }

        MimeMessageHelper helper = new MimeMessageHelper(javaMailSender.createMimeMessage(), true, "UTF-8");
        for (String to : emailDto.getRecipientsList()) {
            helper.addTo(to);
        }
        try {
            helper.setFrom(emailDto.getReplyTo(), emailDto.getSender());
        } catch (UnsupportedEncodingException e) {
            helper.setFrom(emailDto.getReplyTo());
        }
        helper.setReplyTo(emailDto.getReplyTo());
        helper.setSubject(emailDto.getSubject());
        helper.setText(emailDto.getBodyText(), emailDto.getBodyHtml());

        if (emailDto.getAttachmentType() == EmailAttachmentType.PARTICIPANTS) {
            Initiative initiative = initiativeDao.get(emailDto.getInitiativeId());
            addAttachment(helper, initiative, getParticipants(initiative));
        }
        return helper;
    }

    private List<? extends Participant> getParticipants(Initiative initiative) {

        return participantDao.findAllParticipants(initiative.getId(), false, 0, Integer.MAX_VALUE);
    }

    private static final String FILE_NAME = "Kuntalaisaloite_{0}_{1}_osallistujat.pdf";

    private static void addAttachment(MimeMessageHelper multipart, Initiative initiative, List<? extends Participant> participants) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
            new ParticipantToPdfExporter(initiative, participants).createPdf(outputStream);

            byte[] bytes = outputStream.toByteArray();
            DataSource dataSource = new ByteArrayDataSource(bytes, "application/pdf");

            String fileName = MessageFormat.format(FILE_NAME, new LocalDate().toString("yyyy-MM-dd"), initiative.getId());

            multipart.addAttachment(fileName, dataSource);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Package protected for test-usage
    void setJavaMailSender(JavaMailSender javaMailSender) { this.javaMailSender = javaMailSender; }
    void setEmailDao(EmailDao emailDao) { this.emailDao = emailDao; }
    EmailDao getEmailDao() { return this.emailDao; }
}
