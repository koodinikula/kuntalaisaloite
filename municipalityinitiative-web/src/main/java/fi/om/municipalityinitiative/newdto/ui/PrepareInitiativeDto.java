package fi.om.municipalityinitiative.newdto.ui;

import fi.om.municipalityinitiative.validation.InitiativeCreateParticipantValidationInfo;

public class PrepareInitiativeDto implements InitiativeCreateParticipantValidationInfo {

    private boolean collectable;
    private Long municipality;
    private Long homeMunicipality;
    private Boolean franchise;

    @Override
    public boolean isCollectable() {
        return collectable;
    }

    @Override
    public Long getHomeMunicipality() {
        return homeMunicipality;
    }

    @Override
    public Long getMunicipality() {
        return municipality;
    }

    @Override
    public Boolean getFranchise() {
        return franchise;
    }

    @Override
    public Boolean getMunicipalMembership() {
        return null;
    }

    public void setCollectable(boolean collectable) {
        this.collectable = collectable;
    }

    public void setMunicipality(Long municipality) {
        this.municipality = municipality;
    }

    public void setHomeMunicipality(Long homeMunicipality) {
        this.homeMunicipality = homeMunicipality;
    }

    public void setFranchise(Boolean franchise) {
        this.franchise = franchise;
    }
}
