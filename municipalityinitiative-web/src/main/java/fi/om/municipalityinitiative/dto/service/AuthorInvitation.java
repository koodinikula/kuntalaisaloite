package fi.om.municipalityinitiative.dto.service;

import fi.om.municipalityinitiative.dao.InvitationNotValidException;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.Optional;

public class AuthorInvitation {

    private static final Days INVITATION_EXPIRE_TIME = Days.days(7); // TODO: Move to properties later

    private Long initiativeId;
    private String confirmationCode;
    private String name;
    private String email;
    private DateTime invitationTime;
    private Optional<DateTime> rejectTime;

    public Long getInitiativeId() {
        return initiativeId;
    }

    public void setInitiativeId(Long initiativeId) {
        this.initiativeId = initiativeId;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setInvitationTime(DateTime invitationTime) {
        this.invitationTime = invitationTime;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public DateTime getInvitationTime() {
        return invitationTime;
    }

    public boolean isExpired() {
        return invitationTime.isBefore(new DateTime().minus(INVITATION_EXPIRE_TIME));
    }

    public void setRejectTime(Optional<DateTime> rejectTime) {
        this.rejectTime = rejectTime;
    }

    public boolean isRejected() {
        return rejectTime.isPresent();
    }

    public Optional<DateTime> getRejectTime() {
        return rejectTime;
    }

    public  void assertNotRejectedOrExpired() {
        if (isExpired()) {
            throw new InvitationNotValidException("Invitation is expired");
        }
        if (isRejected()) {
            throw new InvitationNotValidException("Invitation is rejected");
        }
    }
}
