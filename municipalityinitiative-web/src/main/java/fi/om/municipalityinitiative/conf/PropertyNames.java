package fi.om.municipalityinitiative.conf;

public final class PropertyNames {

    public static final String isTestEmailSender = "test.mail.sender";

    public static final String kapaSaltForHashing = "salt.for.hashing.kapa";

    private PropertyNames() {}

    public static final String omImageDirection = "om.image.directory";

    public static final String omUserSalt = "om.user.salt";

    public static final String baseURL = "app.baseURL";

    public static final String registeredUserSecret = "security.registeredUserSecret";

    public static final String googleMapsApiKey = "googlemaps.apikey";

    public static final String googleMapsEnabled = "googlemaps.enabled";

    public static final String superSearchEnabled = "supersearch.enabled";

    public static final String jdbcDriver = "jdbc.driver";

    public static final String jdbcURL = "jdbc.url";

    public static final String jdbcUser = "jdbc.user";

    public static final String jdbcPassword = "jdbc.password";

    public static final String flywayPassword = "flyway.password";

    public static final String flywayUser = "flyway.user";

    public static final String enableVerifiedInitiatives = "enable.verified.initiatives";

    public static final String vetumaURL = "vetuma.url";

    public static final String vetumaSharedSecret = "vetuma.sharedSecret";

    public static final String vetumaRCVID = "vetuma.rcvid";

    public static final String vetumaSO = "vetuma.so";

    public static final String vetumaSOLIST = "vetuma.solist";

    public static final String vetumaAP = "vetuma.ap";

    public static final String vetumaAPPNAME = "vetuma.appname";

    public static final String vetumaAPPID = "vetuma.appid";


    public static final String emailSmtpServer = "email.smtp.server";

    public static final String emailSmtpServerPort = "email.smtp.server.port";

    public static final String emailSmtpUsername = "email.smtp.username";

    public static final String emailSmtpPassword = "email.smtp.password";

    public static final String emailDefaultReplyTo = "email.default.reply-to";

    public static final String emailSendToOM = "email.send-to.om";

    public static final String errorFeedbackEmail = "error.feedbackEmail";


    public static final String testEmailSendTo = "test.email.send-to";

    public static final String testEmailConsoleOutput = "test.email.consoleOutput";

    public static final String testEmailMunicipalityEmailsToAuthor = "test.email.municipality.emails.to.author";

    public static final String testEmailSendModeratorEmailsToAuthor = "test.email.send.moderator.emails.to.author";

    public static final String testMessageSourceCacheSeconds = "test.messageSourceCacheSeconds";

    public static final String testFreemarkerShowErrorsOnPage = "test.freemarker.showErrorsOnPage";

    public static final String optimizeResources = "app.optimizeResources";

    public static final String resourcesVersion = "app.resourcesVersion";

    public static final String omPiwicId = "om.piwic.id";

    public static final String appVersion = "appVersion";

    public static final String iframeBaseUrl = "urls.iframe.url";

    public static final String apiBaseUrl = "urls.api.url";

    public static final String youthInitiativeBaseUrl = "youth.initiative.base.url";

    public static final String superSearchBaseUrl = "supersearch.baseurl";

    public static final String attachmentDir = "attachment.directory";

    public static final String decisionAttachmentDir = "decisionAttachment.directory";

    public static final String appEnvironment = "app.environment";

    public static final String videoEnabled = "video.enabled";

    public static final String followEnabled = "follow.enabled";

    public static final String samlLoginEnabled = "saml.login.enabled";

}
