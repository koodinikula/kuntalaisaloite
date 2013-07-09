package fi.om.municipalityinitiative.dao;

import fi.om.municipalityinitiative.dto.service.Participant;
import fi.om.municipalityinitiative.dto.service.ParticipantCreateDto;
import fi.om.municipalityinitiative.dto.ui.ParticipantCount;
import fi.om.municipalityinitiative.service.id.VerifiedUserId;
import fi.om.municipalityinitiative.util.Membership;

import java.util.List;

public interface ParticipantDao {

    Long prepareParticipant(Long initiativeId, Long homeMunicipality, String email, Membership membership);

    Long create(ParticipantCreateDto createDto, String confirmationCode);

    void confirmParticipation(Long participantId, String confirmationCode);

    ParticipantCount getParticipantCount(Long initiativeId);

    List<Participant> findNormalPublicParticipants(Long initiativeId);

    List<Participant> findNormalAllParticipants(Long initiativeId);

    List<Participant> findVerifiedPublicParticipants(Long initiativeId);

    List<Participant> findVerifiedAllParticipants(Long initiativeId);

    Long getInitiativeIdByParticipant(Long participantId);

    void deleteParticipant(Long initiativeId, Long participantId);

    void addVerifiedParticipant(Long initiativeId, VerifiedUserId userId);

    void updateVerifiedParticipantShowName(Long initiativeId, String hash, boolean showName);
}
