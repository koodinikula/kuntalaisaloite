package fi.om.municipalityinitiative.service;

import com.google.common.collect.Lists;
import fi.om.municipalityinitiative.newdao.InitiativeDao;
import fi.om.municipalityinitiative.newdao.MunicipalityDao;
import fi.om.municipalityinitiative.newdao.ParticipantDao;
import fi.om.municipalityinitiative.newdto.InitiativeSearch;
import fi.om.municipalityinitiative.newdto.json.InitiativeJson;
import fi.om.municipalityinitiative.newdto.json.InitiativeListJson;
import fi.om.municipalityinitiative.newdto.ui.InitiativeListInfo;
import fi.om.municipalityinitiative.newdto.ui.MunicipalityInfo;

import javax.annotation.Resource;

import java.util.List;

public class JsonDataService {

    @Resource
    InitiativeDao initiativeDao;

    @Resource
    ParticipantDao participantDao;

    @Resource
    MunicipalityDao municipalityDao;

    public List<InitiativeListJson> findJsonInitiatives(InitiativeSearch search) {
        List<InitiativeListJson> result = Lists.newArrayList();
        for (InitiativeListInfo initiativeListInfo : initiativeDao.find(search)) {
            result.add(new InitiativeListJson(initiativeListInfo));
        }
        return result;
    }

    public InitiativeJson getInitiative(Long id) {
        return InitiativeJson.from(
                initiativeDao.getById(id),
                participantDao.findPublicParticipants(id),
                participantDao.getParticipantCount(id));

    }

    public List<MunicipalityInfo> getMunicipalities() {
        return municipalityDao.findMunicipalities();
    }
}