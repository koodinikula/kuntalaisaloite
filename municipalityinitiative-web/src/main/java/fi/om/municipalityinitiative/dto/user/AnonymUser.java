package fi.om.municipalityinitiative.dto.user;

import fi.om.municipalityinitiative.dto.service.Municipality;

public class AnonymUser extends User {

    AnonymUser() { }

    @Override
    public boolean tooYoungForVerifiedParticipation() {
        return false;
    }

    @Override
    public boolean isOmUser() {
        return false;
    }

    @Override
    public boolean isVerifiedUser() {
        return false;
    }

    @Override
    public boolean hasRightToInitiative(Long initiativeId) {
        return false;
    }

    @Override
    public boolean hasParticipatedToInitiative(Long initiativeId) {
        return false;
    }

    @Override
    public boolean municipalityOkForVerifiedParticipation(Long initiativeId, Municipality municipality){
        return false;
    }
    
    @Override
    public boolean isLoggedIn() {
        return false;
    }

    @Override
    public boolean isMunicipalityUser(Long initiativeId) {
        return false;
    }

}
