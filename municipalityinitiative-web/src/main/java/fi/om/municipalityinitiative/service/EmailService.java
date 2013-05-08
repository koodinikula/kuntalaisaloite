package fi.om.municipalityinitiative.service;

import fi.om.municipalityinitiative.newdto.service.AuthorInvitation;
import fi.om.municipalityinitiative.newdto.service.Initiative;
import fi.om.municipalityinitiative.newdto.service.Participant;

import java.util.List;
import java.util.Locale;

public interface EmailService {

    void sendAuthorConfirmedInvitation(Initiative initiative, String email, String managementHash, Locale locale);

    void sendAuthorInvitation(Initiative initiative, AuthorInvitation authorInvitation);

    void sendSingleToMunicipality(Initiative initiative, String municipalityEmail, Locale locale);

    void sendCollaborativeToMunicipality(Initiative initiative, List<Participant> participants, String municipalityEmail, Locale locale);

    void sendStatusEmail(Initiative initiative, List<String> sendTo, String municipalityEmail, EmailMessageType emailMessageType);

    void sendPrepareCreatedEmail(Initiative byIdWithOriginalAuthor, String authorEmail, Locale locale);

    void sendNotificationToModerator(Initiative initiative, Locale locale, String TEMPORARILY_REPLACING_OM_EMAIL);

    void sendParticipationConfirmation(Initiative initiative, String participantEmail, Long participantId, String confirmationCode, Locale locale);
}