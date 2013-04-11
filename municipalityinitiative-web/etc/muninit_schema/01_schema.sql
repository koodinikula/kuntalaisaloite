drop type if exists initiativeType;
drop type if exists initiativeState;

create type initiativeType as enum ('SINGLE','COLLABORATIVE','COLLABORATIVE_COUNCIL','COLLABORATIVE_CITIZEN');
create type initiativeState as enum('DRAFT','REVIEW','ACCEPTED', 'PUBLISHED');

create table municipality (
	id bigserial,
	name varchar(30) constraint municipality_name_nn not null,
	name_sv varchar(30) constraint municipality_name_sv_nn not null,
	email varchar(100),

	constraint municipality_pk primary key(id),
	constraint municipality_name_u unique(name),
	constraint municipality_name_sv_u unique(name)
);
create index municipality_id_index on municipality(id);

create table municipality_initiative (
	id bigserial,
	municipality_id bigserial,
	type initiativeType,
	state initiativeState constraint initiative_state_nn not null default 'DRAFT',

	new_author_id bigserial,

    modified timestamp constraint municipality_initiative_modified_nn not null default now(),

    name varchar(512),
    proposal text,
    management_hash char(40),
    participant_count integer default 0,
    sent timestamp,
    comment varchar(1024),

    contact_name varchar(100),
    contact_email varchar(100),
    contact_phone varchar(30),
    contact_address varchar(256),

	constraint municipality_initiative_pk primary key (id),
	constraint municipality_initiative_municipality_fk foreign key (municipality_id) references municipality(id)

);

create index municipality_initiative_id_index on municipality_initiative(id);

create table participant (
	id bigserial,
	municipality_initiative_id bigserial,

	name varchar(100),

	participate_time date default now(),

	municipality_id bigserial,
    show_name boolean,
    franchise boolean,

    constraint participant_pk primary key (id),
    constraint participant_municipality_initiative_id_fk foreign key (municipality_initiative_id) references municipality_initiative(id),
    constraint participant_municipality_fk foreign key (municipality_id) references municipality(id)
);
create index participant_id_index on participant(id);


create table author (
    id bigserial,
    participant_id bigserial,
    management_hash varchar(40), -- TODO unique

    name varchar(100),
    email varchar(100),
    phone varchar(30),
    address varchar(256),
    confirmed boolean,

    constraint author_pk primary key (id),
    constraint author_participant_fk foreign key (participant_id) references participant(id)
);
create index author_id_index on author(id);

alter table municipality_initiative add constraint initiative_author_fk foreign key (new_author_id) references author(id) INITIALLY DEFERRED;
