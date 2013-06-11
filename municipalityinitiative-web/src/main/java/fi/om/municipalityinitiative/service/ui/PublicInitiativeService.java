package fi.om.municipalityinitiative.service.ui;

import fi.om.municipalityinitiative.dto.InitiativeCounts;
import fi.om.municipalityinitiative.dto.InitiativeSearch;
import fi.om.municipalityinitiative.dto.service.AuthorMessage;
import fi.om.municipalityinitiative.dto.service.ManagementSettings;
import fi.om.municipalityinitiative.dto.ui.*;
import fi.om.municipalityinitiative.dto.user.LoginUserHolder;
import fi.om.municipalityinitiative.service.email.EmailService;
import fi.om.municipalityinitiative.service.operations.PublicInitiativeServiceOperations;
import fi.om.municipalityinitiative.util.Maybe;

import javax.annotation.Resource;

import java.util.List;
import java.util.Locale;

import static fi.om.municipalityinitiative.service.operations.PublicInitiativeServiceOperations.ParticipantCreatedData;
import static fi.om.municipalityinitiative.service.operations.PublicInitiativeServiceOperations.PreparedInitiativeData;


public class PublicInitiativeService {

    @Resource
    private PublicInitiativeServiceOperations operations;

    @Resource
    private EmailService emailService;

    public List<InitiativeListInfo> findMunicipalityInitiatives(InitiativeSearch search, LoginUserHolder loginUserHolder) {

        // XXX: This switching from all to omAll is pretty nasty, because value must be set back to original after usage
        // because UI is not prepared to omAll value, it's only for dao actually.
        if (loginUserHolder.getUser().isOmUser() && search.getShow() == InitiativeSearch.Show.all) {
            search.setShow(InitiativeSearch.Show.omAll);
        }

        if (search.getShow().isOmOnly()) {
            loginUserHolder.assertOmUser();
        }

        List<InitiativeListInfo> initiativeListInfos = operations.findInitiatives(search);
        if (search.getShow() == InitiativeSearch.Show.omAll)
            search.setShow(InitiativeSearch.Show.all);
        return initiativeListInfos;
    }

    public ManagementSettings getManagementSettings(Long initiativeId) {
        return operations.getManagementSettings(initiativeId);
    }

    public Long createParticipant(ParticipantUICreateDto participant, Long initiativeId, Locale locale) {

        ParticipantCreatedData participantCreatedData
                = operations.doCreateParticipant(participant, initiativeId);

        emailService.sendParticipationConfirmation(
                initiativeId,
                participant.getParticipantEmail(),
                participantCreatedData.participantId,
                participantCreatedData.confirmationCode,
                locale
        );

        return participantCreatedData.participantId;
    }

    public Long prepareInitiative(PrepareInitiativeUICreateDto createDto, Locale locale) {

        PreparedInitiativeData preparedInitiativeData = operations.doPrepareInitiative(createDto);
        emailService.sendPrepareCreatedEmail(preparedInitiativeData.initiativeId, preparedInitiativeData.authorId, preparedInitiativeData.managementHash, locale);

        return preparedInitiativeData.initiativeId;
    }

    public InitiativeViewInfo getMunicipalityInitiative(Long initiativeId) {
        return InitiativeViewInfo.parse(operations.getInitiative(initiativeId));
    }

    public InitiativeCounts getInitiativeCounts(Maybe<Long> municipality, LoginUserHolder loginUserHolder) {
        if (loginUserHolder.getUser().isNotOmUser()) {
            return operations.getInitiativeCounts(municipality);
        }
        else return operations.getInitiativeCounts(municipality, true);
    }

    public Long confirmParticipation(Long participantId, String confirmationCode) {
        return operations.doConfirmParticipation(participantId, confirmationCode);
    }

    public void addAuthorMessage(AuthorUIMessage authorUIMessage, Locale locale) {
        AuthorMessage authorMessage = operations.doAddAuthorMessage(authorUIMessage);
        emailService.sendAuthorMessageConfirmationEmail(authorMessage.getInitiativeId(), authorMessage, locale);
    }

    public Long confirmAndSendAuthorMessage(String confirmationCode) {
        AuthorMessage authorMessage = operations.doConfirmAuthorMessage(confirmationCode);

        emailService.sendAuthorMessages(authorMessage.getInitiativeId(), authorMessage);
        return authorMessage.getInitiativeId();

    }


}