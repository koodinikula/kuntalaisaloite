package fi.om.municipalityinitiative.service;

import fi.om.municipalityinitiative.dao.*;
import fi.om.municipalityinitiative.dto.YouthInitiativeCreateDto;
import fi.om.municipalityinitiative.dto.ui.ContactInfo;
import fi.om.municipalityinitiative.exceptions.AccessDeniedException;
import fi.om.municipalityinitiative.service.email.EmailService;
import fi.om.municipalityinitiative.service.id.NormalAuthorId;
import fi.om.municipalityinitiative.util.Locales;
import fi.om.municipalityinitiative.util.Membership;
import fi.om.municipalityinitiative.util.RandomHashGenerator;
import fi.om.municipalityinitiative.web.Urls;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Locale;

public class YouthInitiativeService {

    @Resource
    private ParticipantDao participantDao;

    @Resource
    private AuthorDao authorDao;

    @Resource
    private InitiativeDao initiativeDao;

    @Resource
    private EmailService emailService;

    @Resource
    private MunicipalityDao municipalityDao;

    @Transactional
    public YouthInitiativeCreateResult prepareYouthInitiative(YouthInitiativeCreateDto createDto) {

        Long municipality = createDto.getMunicipality();
        if (!municipalityDao.getMunicipality(municipality).isActive()) {
            throw new AccessDeniedException("Municipality is not active for initiatives: " + municipality);
        }

        Long youthInitiativeId = initiativeDao.prepareYouthInitiative(createDto.getYouthInitiativeId(), createDto.getName(), createDto.getProposal(), createDto.getExtraInfo(), createDto.getMunicipality());

        Long participantId = participantDao.prepareConfirmedParticipant(
                youthInitiativeId,
                createDto.getContactInfo().getMunicipality(),
                createDto.getContactInfo().getEmail(),
                Membership.none,
                true);

        String managementHash = RandomHashGenerator.longHash();
        NormalAuthorId authorId = authorDao.createAuthor(youthInitiativeId, participantId, managementHash);

        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setEmail(createDto.getContactInfo().getEmail());
        contactInfo.setPhone(createDto.getContactInfo().getPhone());
        contactInfo.setName(createDto.getContactInfo().getName());
        contactInfo.setShowName(true);
        authorDao.updateAuthorInformation(authorId, contactInfo);

        Locale locale = Locales.forLanguageTag(createDto.getLocale());

        emailService.sendPrepareCreatedEmail(youthInitiativeId, authorId, managementHash, locale);

        return new YouthInitiativeCreateResult(youthInitiativeId, Urls.get(locale).loginAuthor(managementHash));
    }

    public class YouthInitiativeCreateResult {

        private final Long youthInitiativeId;
        private final String managementLink;

        public YouthInitiativeCreateResult(Long youthInitiativeId, String managementLink) {
            this.youthInitiativeId = youthInitiativeId;
            this.managementLink = managementLink;
        }

        public Long getYouthInitiativeId() {
            return youthInitiativeId;
        }

        public String getManagementLink() {
            return managementLink;
        }
    }
}
