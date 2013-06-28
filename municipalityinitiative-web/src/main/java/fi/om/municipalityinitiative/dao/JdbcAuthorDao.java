package fi.om.municipalityinitiative.dao;


import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mysema.query.sql.postgres.PostgresQueryFactory;
import com.mysema.query.types.Expression;
import com.mysema.query.types.expr.DateTimeExpression;
import fi.om.municipalityinitiative.exceptions.NotFoundException;
import fi.om.municipalityinitiative.exceptions.OperationNotAllowedException;
import fi.om.municipalityinitiative.dto.Author;
import fi.om.municipalityinitiative.dto.service.AuthorInvitation;
import fi.om.municipalityinitiative.dto.ui.ContactInfo;
import fi.om.municipalityinitiative.service.id.VerifiedUserId;
import fi.om.municipalityinitiative.sql.*;
import org.joda.time.DateTime;

import javax.annotation.Resource;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static fi.om.municipalityinitiative.sql.QMunicipalityInitiative.municipalityInitiative;

@SQLExceptionTranslated
public class JdbcAuthorDao implements AuthorDao {

    private static final Expression<DateTime> CURRENT_TIME = DateTimeExpression.currentTimestamp(DateTime.class);

    @Resource
    PostgresQueryFactory queryFactory;

    @Override
    public Long createAuthor(Long initiativeId, Long participantId, String managementHash) {

        JdbcInitiativeDao.assertSingleAffection(queryFactory.insert(QAuthor.author)
                .set(QAuthor.author.managementHash, managementHash)
                .set(QAuthor.author.participantId, participantId)
                .execute());
        return participantId;
    }

    @Override
    public void updateAuthorInformation(Long authorId, ContactInfo contactInfo) {

        Long participantId = queryFactory.from(QParticipant.participant)
//                .where(QParticipant.participant.municipalityInitiativeId.eq(initiativeId))
                .innerJoin(QParticipant.participant._authorParticipantFk, QAuthor.author)
                .where(QAuthor.author.participantId.eq(authorId))
                .singleResult(QParticipant.participant.id);

        JdbcInitiativeDao.assertSingleAffection(queryFactory.update(QParticipant.participant)
                .set(QParticipant.participant.showName, Boolean.TRUE.equals(contactInfo.isShowName()))
                .set(QParticipant.participant.name, contactInfo.getName())
                .set(QParticipant.participant.email, contactInfo.getEmail())
                .where(QParticipant.participant.id.eq(participantId))
                .execute());

        JdbcInitiativeDao.assertSingleAffection(queryFactory.update(QAuthor.author)
                .set(QAuthor.author.address, contactInfo.getAddress())
                .set(QAuthor.author.phone, contactInfo.getPhone())
                .where(QAuthor.author.participantId.eq(participantId))
                .execute());
    }

    @Override
    public Long addAuthorInvitation(AuthorInvitation authorInvitation) {
        return queryFactory.insert(QAuthorInvitation.authorInvitation)
                .set(QAuthorInvitation.authorInvitation.confirmationCode, authorInvitation.getConfirmationCode())
                .set(QAuthorInvitation.authorInvitation.email, authorInvitation.getEmail())
                .set(QAuthorInvitation.authorInvitation.name, authorInvitation.getName())
                .set(QAuthorInvitation.authorInvitation.invitationTime, authorInvitation.getInvitationTime())
                .set(QAuthorInvitation.authorInvitation.initiativeId, authorInvitation.getInitiativeId())
                .execute();
    }

    @Override
    public AuthorInvitation getAuthorInvitation(Long initiativeId, String confirmationCode) {
        AuthorInvitation authorInvitation = queryFactory.from(QAuthorInvitation.authorInvitation)
                .where(QAuthorInvitation.authorInvitation.initiativeId.eq(initiativeId))
                .where(QAuthorInvitation.authorInvitation.confirmationCode.eq(confirmationCode))
                .singleResult(Mappings.authorInvitationMapping);
        if (authorInvitation == null) {
            throw new InvitationNotValidException("No invitation: " + initiativeId + ", " + confirmationCode);
        }
        return authorInvitation;
    }

    @Override
    public void rejectAuthorInvitation(Long initiativeId, String confirmationCode) {
        JdbcInitiativeDao.assertSingleAffection(queryFactory.update(QAuthorInvitation.authorInvitation)
                .set(QAuthorInvitation.authorInvitation.rejectTime, CURRENT_TIME)
                .where(QAuthorInvitation.authorInvitation.initiativeId.eq(initiativeId))
                .where(QAuthorInvitation.authorInvitation.confirmationCode.eq(confirmationCode))
                .execute());
    }

    @Override
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
    public List<AuthorInvitation> findInvitations(Long initiativeId) {
        return queryFactory.from(QAuthorInvitation.authorInvitation)
                .where(QAuthorInvitation.authorInvitation.initiativeId.eq(initiativeId))
                .orderBy(QAuthorInvitation.authorInvitation.invitationTime.asc())
                .list(Mappings.authorInvitationMapping);
    }

    @Override
    public List<Author> findAuthors(Long initiativeId) {
            return queryFactory.from(municipalityInitiative)
                    .innerJoin(municipalityInitiative._participantMunicipalityInitiativeIdFk, QParticipant.participant)
                    .innerJoin(QParticipant.participant._authorParticipantFk, QAuthor.author)
                    .innerJoin(QParticipant.participant.participantMunicipalityFk, QMunicipality.municipality)
                    .where(municipalityInitiative.id.eq(initiativeId))
                    .orderBy(QParticipant.participant.id.asc())
                    .list(Mappings.authorMapping);
    }

    @Override
    public Set<Long> getAuthorsInitiatives(String managementHash) {
        List<Long> list = queryFactory.from(QAuthor.author)
                .innerJoin(QAuthor.author.authorParticipantFk, QParticipant.participant)
                .innerJoin(QParticipant.participant.participantMunicipalityInitiativeIdFk, QMunicipalityInitiative.municipalityInitiative)
                .where(QAuthor.author.managementHash.eq(managementHash))
                .list(QMunicipalityInitiative.municipalityInitiative.id);

        TreeSet<Long> initiativeIds = Sets.newTreeSet();
        initiativeIds.addAll(list);

        return initiativeIds;
    }

    @Override
    public Long getAuthorId(String managementHash) {
        return queryFactory.from(QAuthor.author)
                .where(QAuthor.author.managementHash.eq(managementHash))
                .uniqueResult(QAuthor.author.participantId);
    }

    @Override
    public Author getAuthor(Long authorId) {
        return queryFactory.from(QAuthor.author)
                .where(QAuthor.author.participantId.eq(authorId))
                .innerJoin(QAuthor.author.authorParticipantFk, QParticipant.participant)
                .innerJoin(QParticipant.participant.participantMunicipalityFk, QMunicipality.municipality)
                .uniqueResult(Mappings.authorMapping);
    }

    @Override
    public void deleteAuthor(Long authorId) {

        Long initiativeId = queryFactory.from(QAuthor.author)
                .innerJoin(QAuthor.author.authorParticipantFk, QParticipant.participant)
                .where(QAuthor.author.participantId.eq(authorId))
                .uniqueResult(QParticipant.participant.municipalityInitiativeId);

        // Lock all authors of the initiative from another transactions
        queryFactory.from(QAuthor.author)
                .innerJoin(QAuthor.author.authorParticipantFk, QParticipant.participant)
                .where(QParticipant.participant.municipalityInitiativeId.eq(initiativeId))
                .forUpdate().of(QAuthor.author).list(QAuthor.author.participantId);

        JdbcInitiativeDao.assertSingleAffection(queryFactory.delete(QAuthor.author)
                .where(QAuthor.author.participantId.eq(authorId)).execute());
        JdbcInitiativeDao.assertSingleAffection(queryFactory.delete(QParticipant.participant)
                .where(QParticipant.participant.id.eq(authorId)).execute());
        JdbcInitiativeDao.assertSingleAffection(queryFactory.update(QMunicipalityInitiative.municipalityInitiative)
                .set(QMunicipalityInitiative.municipalityInitiative.participantCount, QMunicipalityInitiative.municipalityInitiative.participantCount.subtract(1))
                .where(QMunicipalityInitiative.municipalityInitiative.id.eq(initiativeId))
                .execute());

        long authorCount = queryFactory.from(QAuthor.author)
                .innerJoin(QAuthor.author.authorParticipantFk, QParticipant.participant)
                .where(QParticipant.participant.municipalityInitiativeId.eq(initiativeId))
                .count();

        if (authorCount == 0) {
            throw new OperationNotAllowedException("Deleting last author is forbidden");
        }

    }

    @Override
    public List<String> getAuthorEmails(Long initiativeId) {
        List<String> emails = Lists.newArrayList();
        for (Author author : findAuthors(initiativeId)) {
            emails.add(author.getContactInfo().getEmail());
        }
        return emails;
    }

    @Override
    public void updateManagementHash(Long authorId, String newManagementHash) {
        JdbcInitiativeDao.assertSingleAffection(queryFactory.update(QAuthor.author)
                .set(QAuthor.author.managementHash, newManagementHash)
                .where(QAuthor.author.participantId.eq(authorId))
                .execute());
    }

    @Override
    public Map<String, String> getManagementLinksByAuthorEmails(Long initiativeId) {
        return queryFactory.from(QAuthor.author)
                .innerJoin(QAuthor.author.authorParticipantFk, QParticipant.participant)
                .where(QParticipant.participant.municipalityInitiativeId.eq(initiativeId))
                .map(QParticipant.participant.email, QAuthor.author.managementHash);
    }

    @Override
    public void addVerifiedAuthor(Long initiativeId, VerifiedUserId userId) {
    }
}
