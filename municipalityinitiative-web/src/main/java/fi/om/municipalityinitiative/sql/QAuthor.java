package fi.om.municipalityinitiative.sql;

import com.mysema.query.types.Path;
import com.mysema.query.types.PathMetadata;
import com.mysema.query.types.path.NumberPath;
import com.mysema.query.types.path.StringPath;

import javax.annotation.Generated;

import static com.mysema.query.types.PathMetadataFactory.forVariable;


/**
 * QAuthor is a Querydsl query type for QAuthor
 */
@Generated("com.mysema.query.sql.codegen.MetaDataSerializer")
public class QAuthor extends com.mysema.query.sql.RelationalPathBase<QAuthor> {

    private static final long serialVersionUID = 313430235;

    public static final QAuthor author = new QAuthor("author");

    public final StringPath address = createString("address");

    public final NumberPath<Long> initiativeId = createNumber("initiative_id", Long.class);

    public final StringPath managementHash = createString("management_hash");

    public final NumberPath<Long> participantId = createNumber("participant_id", Long.class);

    public final StringPath phone = createString("phone");

    public final com.mysema.query.sql.PrimaryKey<QAuthor> authorPk = createPrimaryKey(participantId);

    public final com.mysema.query.sql.ForeignKey<QMunicipalityInitiative> authorInitiativeIdFk = createForeignKey(initiativeId, "id");

    public final com.mysema.query.sql.ForeignKey<QParticipant> authorParticipantFk = createForeignKey(participantId, "id");

    public QAuthor(String variable) {
        super(QAuthor.class, forVariable(variable), "municipalityinitiative", "author");
    }

    public QAuthor(Path<? extends QAuthor> path) {
        super(path.getType(), path.getMetadata(), "municipalityinitiative", "author");
    }

    public QAuthor(PathMetadata<?> metadata) {
        super(QAuthor.class, metadata, "municipalityinitiative", "author");
    }

}

