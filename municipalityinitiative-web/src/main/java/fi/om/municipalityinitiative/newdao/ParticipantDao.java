package fi.om.municipalityinitiative.newdao;

import fi.om.municipalityinitiative.newdto.service.Participant;
import fi.om.municipalityinitiative.newdto.service.ParticipantCreateDto;
import fi.om.municipalityinitiative.newdto.ui.ParticipantCount;

import java.util.List;

public interface ParticipantDao {

    Long create(ParticipantCreateDto createDto, String confirmationCode);

    ParticipantCount getParticipantCount(Long initiativeId);

    List<Participant> findPublicParticipants(Long initiativeId);

    List<Participant> findAllParticipants(Long initiativeId);

    Long prepareParticipant(Long initiativeId, Long homeMunicipality, String email, Boolean franchise);
}
