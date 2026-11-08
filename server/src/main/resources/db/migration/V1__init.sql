-- 공간(space)은 'garden'(공용 추모 공간) 또는 개인 추모 공간의 id다.

create table memorials (
    id              varchar(32)  primary key,
    name            varchar(40)  not null,
    years           varchar(40),
    intro           varchar(120),
    photo           bytea,
    photo_type      varchar(40),
    created_by      varchar(40),
    edit_token_hash varchar(100) not null,
    created_at      timestamp with time zone not null
);
create index memorials_created_idx on memorials (created_at);

create table letters (
    id            varchar(32)   primary key,
    space         varchar(32)   not null,
    to_name       varchar(60)   not null,
    message       varchar(1200) not null,
    author_name   varchar(40),
    visitor_id    varchar(40),
    hearts        integer       not null default 0,
    comment_count integer       not null default 0,
    created_at    timestamp with time zone not null
);
create index letters_space_idx on letters (space, created_at);

-- 한 방문자는 한 사연에 한 번만 공감한다.
create table letter_hearts (
    letter_id  varchar(32) not null references letters (id) on delete cascade,
    visitor_id varchar(40) not null,
    created_at timestamp with time zone not null,
    primary key (letter_id, visitor_id)
);

create table comments (
    id          varchar(32)  primary key,
    letter_id   varchar(32)  not null references letters (id) on delete cascade,
    parent_id   varchar(32),
    author_name varchar(40),
    visitor_id  varchar(40),
    text        varchar(400) not null,
    created_at  timestamp with time zone not null
);
create index comments_letter_idx on comments (letter_id, created_at);

create table offerings (
    id          varchar(32) primary key,
    space       varchar(32) not null,
    kind        varchar(10) not null,
    author_name varchar(40),
    visitor_id  varchar(40),
    created_at  timestamp with time zone not null
);
create index offerings_space_idx on offerings (space, created_at);

create table chat_messages (
    id          varchar(32)  primary key,
    space       varchar(32)  not null,
    author_name varchar(40),
    visitor_id  varchar(40),
    text        varchar(240) not null,
    created_at  timestamp with time zone not null
);
create index chat_space_idx on chat_messages (space, created_at);

create table avatar_faces (
    visitor_id   varchar(40) primary key,
    image        bytea       not null,
    content_type varchar(40) not null,
    updated_at   timestamp with time zone not null
);
