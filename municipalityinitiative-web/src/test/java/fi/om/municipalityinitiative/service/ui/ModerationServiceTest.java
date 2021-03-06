package fi.om.municipalityinitiative.service.ui;

import fi.om.municipalityinitiative.dao.*;
import fi.om.municipalityinitiative.dto.service.Initiative;
import fi.om.municipalityinitiative.dto.service.Municipality;
import fi.om.municipalityinitiative.dto.user.OmLoginUserHolder;
import fi.om.municipalityinitiative.exceptions.AccessDeniedException;
import fi.om.municipalityinitiative.exceptions.OperationNotAllowedException;
import fi.om.municipalityinitiative.service.email.EmailMessageType;
import fi.om.municipalityinitiative.service.email.EmailService;
import fi.om.municipalityinitiative.util.FixState;
import fi.om.municipalityinitiative.util.InitiativeState;
import fi.om.municipalityinitiative.util.InitiativeType;
import fi.om.municipalityinitiative.util.Locales;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class ModerationServiceTest {

    public static final long INITIATIVE_ID = 3L;

    private ModerationService moderationService;

    private InitiativeDao initiativeDaoMock;

    private OmLoginUserHolder loginUserHolder;

    private AttachmentDao attachmentDaoMock;

    private ReviewHistoryDao reviewHistoryDaoMock;

    private MunicipalityUserDao municipalityUserDao;

    @Before
    public void setup() throws Exception {
        initiativeDaoMock = mock(InitiativeDao.class);
        moderationService = new ModerationService();

        moderationService.emailService = mock(EmailService.class);

        AuthorDao authorDaoMock = mock(AuthorDao.class);

        moderationService.initiativeDao = initiativeDaoMock;
        moderationService.authorDao = authorDaoMock;
        attachmentDaoMock = mock(AttachmentDao.class);
        moderationService.attachmentDao = attachmentDaoMock;
        reviewHistoryDaoMock = mock(ReviewHistoryDao.class);
        moderationService.reviewHistoryDao = reviewHistoryDaoMock;

        municipalityUserDao = mock(MunicipalityUserDao.class);
        moderationService.municipalityUserDao = municipalityUserDao;

        stub(authorDaoMock.findAuthorEmails(anyLong())).toReturn(Collections.singletonList("")); // Avoid nullpointer temporarily

        loginUserHolder = mock(OmLoginUserHolder.class);
    }

    @Test
    public void all_functions_require_om_rights() throws InvocationTargetException, IllegalAccessException {

        doThrow(new AccessDeniedException("")).when(loginUserHolder).assertOmUser();

        for (Method method : ModerationService.class.getDeclaredMethods()) {
            if (method.getModifiers() != 1) {
                continue;
            }
            Object[] parameters = new Object[method.getParameterTypes().length];
            parameters[0] = loginUserHolder;
            try {
                System.out.println("Checking that method requires om rights: " + method.getName());
                method.invoke(moderationService, parameters);
                fail("Should have checked om-rights for user: " + method.getName());
            } catch (InvocationTargetException e) {
                assertThat(e.getCause(), instanceOf(AccessDeniedException.class));
            }
        }
    }

    @Test(expected = OperationNotAllowedException.class)
    public void accepting_initiative_checks_that_it_can_be_accepted() {
        stub(initiativeDaoMock.get(INITIATIVE_ID)).toReturn(initiative(InitiativeState.ACCEPTED, InitiativeType.UNDEFINED));
        moderationService.accept(loginUserHolder, INITIATIVE_ID, "", null);
    }

    @Test
    public void accepting_initiative_sets_all_attachments_accepted() {
        stub(initiativeDaoMock.get(INITIATIVE_ID)).toReturn(initiative(InitiativeState.REVIEW, InitiativeType.UNDEFINED));
        moderationService.accept(loginUserHolder, INITIATIVE_ID, "", null);
        verify(attachmentDaoMock).acceptAttachments(INITIATIVE_ID);
    }

    @Test
    public void accepting_initiative_adds_review_history_information() {
        stub(initiativeDaoMock.get(INITIATIVE_ID)).toReturn(initiative(InitiativeState.REVIEW, InitiativeType.UNDEFINED));

        String moderatorComment = "Some moderator comment";
        moderationService.accept(loginUserHolder, INITIATIVE_ID, moderatorComment, null);

        verify(reviewHistoryDaoMock).addAccepted(INITIATIVE_ID, moderatorComment);
    }

    @Test
    public void rejecting_initiative_adds_review_history_information() {
        stub(initiativeDaoMock.get(INITIATIVE_ID)).toReturn(initiative(InitiativeState.REVIEW, InitiativeType.UNDEFINED));

        String moderatorComment = "Some moderator comment";
        moderationService.reject(loginUserHolder, INITIATIVE_ID, moderatorComment);

        verify(reviewHistoryDaoMock).addRejected(INITIATIVE_ID, moderatorComment);
    }

    @Test
    public void sending_initiative_back_for_fixing_adds_review_history_information() {
        stub(initiativeDaoMock.get(INITIATIVE_ID)).toReturn(initiative(InitiativeState.PUBLISHED, InitiativeType.COLLABORATIVE));

        String moderatorComment = "Some moderator comment";
        moderationService.sendInitiativeBackForFixing(loginUserHolder, INITIATIVE_ID, moderatorComment);

        verify(reviewHistoryDaoMock).addRejected(INITIATIVE_ID, moderatorComment);
    }


    @Test
    public void accepting_initiative_sets_state_as_accepted_and_saves_comment_if_type_is_undefined() {

        stub(initiativeDaoMock.get(INITIATIVE_ID)).toReturn(initiative(InitiativeState.REVIEW, InitiativeType.UNDEFINED));

        String comment = "this is om-comment";
        moderationService.accept(loginUserHolder, INITIATIVE_ID, comment, Locales.LOCALE_FI);

        verify(initiativeDaoMock).get(INITIATIVE_ID);
        verify(initiativeDaoMock).updateModeratorComment(INITIATIVE_ID, comment);
        verify(initiativeDaoMock).updateInitiativeState(INITIATIVE_ID, InitiativeState.ACCEPTED);
        verifyNoMoreInteractions(initiativeDaoMock);

    }

    @Test
    public void accepting_initiative_sends_correct_state_email_if_type_is_undefined() {

        stub(initiativeDaoMock.get(INITIATIVE_ID)).toReturn(initiative(InitiativeState.REVIEW, InitiativeType.UNDEFINED));

        moderationService.accept(loginUserHolder, INITIATIVE_ID, null, Locales.LOCALE_FI);

        verify(moderationService.emailService).sendStatusEmail(anyLong(), eq(EmailMessageType.ACCEPTED_BY_OM));
    }

    @Test
    public void accepting_initiative_sets_state_as_published_and_saves_comment_and_sets_sent_time_if_type_single() {

        Initiative initiative = initiative(InitiativeState.REVIEW, InitiativeType.SINGLE);
        stub(initiativeDaoMock.get(INITIATIVE_ID)).toReturn(initiative);

        String comment = "this is om-comment";
        moderationService.accept(loginUserHolder, INITIATIVE_ID, comment, Locales.LOCALE_FI);

        verify(initiativeDaoMock).get(INITIATIVE_ID);
        verify(initiativeDaoMock).updateModeratorComment(INITIATIVE_ID, comment);
        verify(initiativeDaoMock).updateInitiativeState(INITIATIVE_ID, InitiativeState.PUBLISHED);
        verify(initiativeDaoMock).markInitiativeAsSent(INITIATIVE_ID);
        verifyNoMoreInteractions(initiativeDaoMock);

    }


    @Test
    public void accepting_initiative_sends_correct_state_email_if_type_single() {

        Initiative initiative = initiative(InitiativeState.REVIEW, InitiativeType.SINGLE);
        stub(initiativeDaoMock.get(INITIATIVE_ID)).toReturn(initiative);

        moderationService.accept(loginUserHolder, INITIATIVE_ID, null, Locales.LOCALE_FI);

        verify(moderationService.emailService).sendStatusEmail(anyLong(), eq(EmailMessageType.ACCEPTED_BY_OM_AND_SENT));
        verify(moderationService.emailService).sendSingleToMunicipality(anyLong(), eq(Locales.LOCALE_FI));

    }

    @Test
    public void accepting_single_initiative_adds_municipality_user() {

        Initiative initiative = initiative(InitiativeState.REVIEW, InitiativeType.SINGLE);
        stub(initiativeDaoMock.get(INITIATIVE_ID)).toReturn(initiative);

        moderationService.accept(loginUserHolder, INITIATIVE_ID, null, Locales.LOCALE_FI);

        verify(municipalityUserDao).assignMunicipalityUser(eq(initiative.getId()), any(String.class));
    }

    @Test
    public void accepting_undefined_initiative_does_not_add_municipality_user() {

        Initiative initiative = initiative(InitiativeState.REVIEW, InitiativeType.UNDEFINED);
        stub(initiativeDaoMock.get(INITIATIVE_ID)).toReturn(initiative);

        moderationService.accept(loginUserHolder, INITIATIVE_ID, null, Locales.LOCALE_FI);

        verify(municipalityUserDao, never()).assignMunicipalityUser(eq(initiative.getId()), any(String.class));
    }

    @Test(expected = OperationNotAllowedException.class)
    public void rejecting_initiative_checks_that_it_can_be_accepted() {
        stub(initiativeDaoMock.get(INITIATIVE_ID)).toReturn(initiative(InitiativeState.ACCEPTED, InitiativeType.UNDEFINED));
        moderationService.reject(loginUserHolder, INITIATIVE_ID, "");
    }

    @Test
    public void rejecting_initiative_sets_state_as_draft_and_saves_comment() {

        stub(initiativeDaoMock.get(INITIATIVE_ID)).toReturn(initiative(InitiativeState.REVIEW, InitiativeType.UNDEFINED));

        String comment = "this is om-comment";
        moderationService.reject(loginUserHolder, INITIATIVE_ID, comment);
        verify(initiativeDaoMock).get(INITIATIVE_ID);
        verify(initiativeDaoMock).updateInitiativeState(INITIATIVE_ID, InitiativeState.DRAFT);
        verify(initiativeDaoMock).updateModeratorComment(INITIATIVE_ID, comment);
        verifyNoMoreInteractions(initiativeDaoMock);
    }

    @Test
    public void rejecting_initiative_sends_email() {

        stub(initiativeDaoMock.get(INITIATIVE_ID)).toReturn(initiative(InitiativeState.REVIEW, InitiativeType.UNDEFINED));

        moderationService.reject(loginUserHolder, INITIATIVE_ID, null);
        verify(moderationService.emailService).sendStatusEmail(anyLong(), eq(EmailMessageType.REJECTED_BY_OM));
    }

    @Test
    public void rejecting_initiative_sets_all_attachments_rejected() {
        stub(initiativeDaoMock.get(INITIATIVE_ID)).toReturn(initiative(InitiativeState.REVIEW, InitiativeType.UNDEFINED));
        moderationService.reject(loginUserHolder, INITIATIVE_ID, "");
        verify(attachmentDaoMock).rejectAttachments(INITIATIVE_ID);
    }

    @Test
    public void accepting_fixState_review_initiative_sets_moderator_comment_and_fixState() {
        stub(initiativeDaoMock.get(INITIATIVE_ID)).toReturn(publishedCollaborative(FixState.REVIEW));

        String moderatorComment = "moderator comment";
        moderationService.accept(loginUserHolder, INITIATIVE_ID, moderatorComment, null);
        verify(initiativeDaoMock).get(INITIATIVE_ID);
        verify(initiativeDaoMock).updateModeratorComment(INITIATIVE_ID, moderatorComment);
        verify(initiativeDaoMock).updateInitiativeFixState(INITIATIVE_ID, FixState.OK);
        verifyNoMoreInteractions(initiativeDaoMock);
    }

    @Test
    public void accepting_fixState_review_initiative_sends_status_email() {
        stub(initiativeDaoMock.get(INITIATIVE_ID)).toReturn(publishedCollaborative(FixState.REVIEW));

        String moderatorComment = "moderator comment";
        moderationService.accept(loginUserHolder, INITIATIVE_ID, moderatorComment, null);
        verify(moderationService.emailService).sendStatusEmail(eq(INITIATIVE_ID), eq(EmailMessageType.ACCEPTED_BY_OM_FIX));
        verifyNoMoreInteractions(moderationService.emailService);
    }

    @Test
    public void rejecting_fixState_review_initiative_sets_moderator_comment_and_fixState() {
        stub(initiativeDaoMock.get(INITIATIVE_ID)).toReturn(publishedCollaborative(FixState.REVIEW));

        String moderatorComment = "moderator comment";
        moderationService.reject(loginUserHolder, INITIATIVE_ID, moderatorComment);
        verify(initiativeDaoMock).get(INITIATIVE_ID);
        verify(initiativeDaoMock).updateModeratorComment(INITIATIVE_ID, moderatorComment);
        verify(initiativeDaoMock).updateInitiativeFixState(INITIATIVE_ID, FixState.FIX);
        verifyNoMoreInteractions(initiativeDaoMock);
    }

    @Test
    public void rejecting_fixState_review_initiative_sends_status_email() {
        stub(initiativeDaoMock.get(INITIATIVE_ID)).toReturn(publishedCollaborative(FixState.REVIEW));

        String moderatorComment = "moderator comment";
        moderationService.reject(loginUserHolder, INITIATIVE_ID, moderatorComment);
        verify(moderationService.emailService).sendStatusEmail(eq(INITIATIVE_ID), eq(EmailMessageType.REJECTED_BY_OM));
        verifyNoMoreInteractions(moderationService.emailService);
    }

    @Test(expected = OperationNotAllowedException.class)
    public void sendInitiativeBackForFixing_checks_that_initiative_may_be_sent_back() {
        stub(initiativeDaoMock.get(INITIATIVE_ID)).toReturn(initiative(InitiativeState.DRAFT, InitiativeType.UNDEFINED));
        moderationService.sendInitiativeBackForFixing(loginUserHolder, INITIATIVE_ID, "");
    }

    @Test
    public void sendInitiativeBackForFixing_rejects_all_attachments() {
        stub(initiativeDaoMock.get(INITIATIVE_ID)).toReturn(initiative(InitiativeState.PUBLISHED, InitiativeType.COLLABORATIVE));
        moderationService.sendInitiativeBackForFixing(loginUserHolder, INITIATIVE_ID, "");
        verify(attachmentDaoMock).rejectAttachments(INITIATIVE_ID);
    }

    @Test
    public void sendInitiativeBackForFixing_sets_initiative_fixState_and_adds_moderator_comment() {
        stub(initiativeDaoMock.get(INITIATIVE_ID)).toReturn(initiative(InitiativeState.PUBLISHED, InitiativeType.COLLABORATIVE));

        String moderatorComment = "some mod comment";
        moderationService.sendInitiativeBackForFixing(loginUserHolder, INITIATIVE_ID, moderatorComment);

        verify(initiativeDaoMock).get(INITIATIVE_ID);
        verify(initiativeDaoMock).updateInitiativeFixState(INITIATIVE_ID, FixState.FIX);
        verify(initiativeDaoMock).updateModeratorComment(INITIATIVE_ID, moderatorComment);

    }

    @Test
    public void sendInitiativeBackForFixing_sends_status_email() {
        stub(initiativeDaoMock.get(INITIATIVE_ID)).toReturn(initiative(InitiativeState.PUBLISHED, InitiativeType.COLLABORATIVE));

        moderationService.sendInitiativeBackForFixing(loginUserHolder, INITIATIVE_ID, "");
        verify(moderationService.emailService).sendStatusEmail(eq(INITIATIVE_ID), eq(EmailMessageType.REJECTED_BY_OM));
    }

    private static Initiative publishedCollaborative(FixState fixState) {
        Initiative initiative = initiative(InitiativeState.PUBLISHED, InitiativeType.COLLABORATIVE);
        initiative.setFixState(fixState);
        return initiative;
    }

    private static Initiative initiative(InitiativeState state, InitiativeType type) {
        Initiative initiative = new Initiative();
        initiative.setId(INITIATIVE_ID);
        initiative.setState(state);
        initiative.setType(type);
        initiative.setFixState(FixState.OK);
        initiative.setMunicipality(new Municipality(0, "", "", false));

        return initiative;

    }

}
