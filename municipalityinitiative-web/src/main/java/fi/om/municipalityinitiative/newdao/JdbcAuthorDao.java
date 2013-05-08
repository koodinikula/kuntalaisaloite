package fi.om.municipalityinitiative.newdao;


import com.google.common.collect.Lists;
import com.mysema.query.sql.postgres.PostgresQueryFactory;
import com.mysema.query.types.Expression;
import com.mysema.query.types.expr.DateTimeExpression;
import fi.om.municipalityinitiative.dao.NotFoundException;
import fi.om.municipalityinitiative.dao.SQLExceptionTranslated;
import fi.om.municipalityinitiative.newdto.Author;
import fi.om.municipalityinitiative.newdto.service.AuthorInvitation;
import fi.om.municipalityinitiative.newdto.ui.ContactInfo;
import fi.om.municipalityinitiative.service.PublicInitiativeService;
import fi.om.municipalityinitiative.sql.QAuthor;
import fi.om.municipalityinitiative.sql.QAuthorInvitation;
import fi.om.municipalityinitiative.sql.QMunicipality;
import fi.om.municipalityinitiative.sql.QParticipant;
import org.joda.time.DateTime;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.List;

import static fi.om.municipalityinitiative.newdao.JdbcInitiativeDao.assertSingleAffection;
import static fi.om.municipalityinitiative.newdao.Mappings.PREPARATION_ID;
import static fi.om.municipalityinitiative.sql.QMunicipalityInitiative.municipalityInitiative;

@SQLExceptionTranslated
@Transactional(readOnly = true)
public class JdbcAuthorDao implements AuthorDao {

    private static final Expression<DateTime> CURRENT_TIME = DateTimeExpression.currentTimestamp(DateTime.class);

    @Resource
    PostgresQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = false)
    public void assignAuthor(Long initiativeId, Long authorId) {

        assertSingleAffection(queryFactory.update(municipalityInitiative)
                .set(municipalityInitiative.authorId, authorId)
                .where(municipalityInitiative.id.eq(initiativeId))
                .where(municipalityInitiative.authorId.eq(PREPARATION_ID))
                .execute());
    }

    @Override
    @Transactional(readOnly = false)
    public Long createAuthor(Long initiativeId, Long participantId, String managementHash) {

        return queryFactory.insert(QAuthor.author)
                .set(QAuthor.author.managementHash, managementHash)
                .set(QAuthor.author.participantId, participantId)
                .executeWithKey(QAuthor.author.id);
    }

    @Override
    public Author getAuthorInformation(Long initiativeId, String managementHash) {
        return queryFactory.from(municipalityInitiative)
                .innerJoin(municipalityInitiative._participantMunicipalityInitiativeIdFk, QParticipant.participant)
                .innerJoin(QParticipant.participant._authorParticipantFk, QAuthor.author)
                .innerJoin(QParticipant.participant.participantMunicipalityFk, QMunicipality.municipality)
                .where(municipalityInitiative.id.eq(initiativeId))
                .where(QAuthor.author.managementHash.eq(managementHash))
                .uniqueResult(Mappings.authorMapping);
    }

    @Override
    public void updateAuthorInformation(Long authorId, ContactInfo contactInfo) {

        Long participantId = queryFactory.from(QParticipant.participant)
//                .where(QParticipant.participant.municipalityInitiativeId.eq(initiativeId))
                .innerJoin(QParticipant.participant._authorParticipantFk, QAuthor.author)
                .where(QAuthor.author.id.eq(authorId))
                .singleResult(QParticipant.participant.id);

        assertSingleAffection(queryFactory.update(QParticipant.participant)
                .set(QParticipant.participant.showName, Boolean.TRUE.equals(contactInfo.isShowName()))
                .set(QParticipant.participant.name, contactInfo.getName())
                .set(QParticipant.participant.email, contactInfo.getEmail())
                .where(QParticipant.participant.id.eq(participantId))
                .execute());

        assertSingleAffection(queryFactory.update(QAuthor.author)
                .set(QAuthor.author.address, contactInfo.getAddress())
                .set(QAuthor.author.name, contactInfo.getName())
                .set(QAuthor.author.phone, contactInfo.getPhone())
                .where(QAuthor.author.participantId.eq(participantId))
                .execute());
    }

    @Override
    @Transactional(readOnly = false)
    public Long addAuthorInvitation(AuthorInvitation authorInvitation) {
        return queryFactory.insert(QAuthorInvitation.authorInvitation)
                .set(QAuthorInvitation.authorInvitation.confirmationCode, authorInvitation.getConfirmationCode())
                .set(QAuthorInvitation.authorInvitation.email, authorInvitation.getEmail())
                .set(QAuthorInvitation.authorInvitation.name, authorInvitation.getName())
                .set(QAuthorInvitation.authorInvitation.invitationTime, authorInvitation.getInvitationTime())
                .set(QAuthorInvitation.authorInvitation.initiativeId, authorInvitation.getInitiativeId())
                .executeWithKey(QAuthorInvitation.authorInvitation.id);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthorInvitation getAuthorInvitation(Long initiativeId, String confirmationCode) {
        AuthorInvitation authorInvitation = queryFactory.from(QAuthorInvitation.authorInvitation)
                .where(QAuthorInvitation.authorInvitation.initiativeId.eq(initiativeId))
                .where(QAuthorInvitation.authorInvitation.confirmationCode.eq(confirmationCode))
                .singleResult(Mappings.authorInvitationMapping);
        if (authorInvitation == null) {
            throw new NotFoundException(QAuthorInvitation.authorInvitation.getTableName(), initiativeId + ":" + confirmationCode);
        }
        return authorInvitation;
    }

    @Override
    @Transactional(readOnly = false)
    public void rejectAuthorInvitation(Long initiativeId, String confirmationCode) {
        assertSingleAffection(queryFactory.update(QAuthorInvitation.authorInvitation)
                .set(QAuthorInvitation.authorInvitation.rejectTime, CURRENT_TIME)
                .where(QAuthorInvitation.authorInvitation.initiativeId.eq(initiativeId))
                .where(QAuthorInvitation.authorInvitation.confirmationCode.eq(confirmationCode))
                .execute());
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteAuthorInvitation(Long initiativeId, String confirmationCode) {
        long affectedRows = queryFactory.delete(QAuthorInvitation.authorInvitation)
                .where(QAuthorInvitation.authorInvitation.initiativeId.eq(initiativeId))
                .where(QAuthorInvitation.authorInvitation.confirmationCode.eq(confirmationCode))
                .execute();
        if (affectedRows != 1) {
            throw new NotFoundException(QAuthorInvitation.authorInvitation.getTableName(), initiativeId + ":" + confirmationCode);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public List<AuthorInvitation> findInvitations(Long initiativeId) {
        return queryFactory.from(QAuthorInvitation.authorInvitation)
                .where(QAuthorInvitation.authorInvitation.initiativeId.eq(initiativeId))
                .list(Mappings.authorInvitationMapping);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Author> findAuthors(Long initiativeId) {
            return queryFactory.from(municipalityInitiative)
                    .innerJoin(municipalityInitiative._participantMunicipalityInitiativeIdFk, QParticipant.participant)
                    .innerJoin(QParticipant.participant._authorParticipantFk, QAuthor.author)
                    .innerJoin(QParticipant.participant.participantMunicipalityFk, QMunicipality.municipality)
                    .where(municipalityInitiative.id.eq(initiativeId))
                    .orderBy(QParticipant.participant.id.desc())
                    .list(Mappings.authorMapping);
    }

    @Override
    public List<String> getAuthorEmails(Long initiativeId) {
        List<String> emails = Lists.newArrayList();
        for (Author author : findAuthors(initiativeId)) {
            emails.add(author.getContactInfo().getEmail());
        }
        return emails;
    }
}
