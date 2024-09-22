create table if not exists users
(
    id      integer GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    email   VARCHAR(255) NOT NULL,
    user_name    VARCHAR(255) NOT NULL,
    is_admin  BOOLEAN DEFAULT FALSE
);

create table if not exists categories
(
    id   integer GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);



create table if not exists events
(
    id                      INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    annotation              TEXT     NOT NULL,
    description             TEXT     NOT NULL,
    title                   TEXT     NOT NULL,
    created_on              TIMESTAMP        NOT NULL,
    event_date              TIMESTAMP        NOT NULL,
    published_on            TIMESTAMP,
    paid                    BOOLEAN          NOT NULL,
    participant_limit       INTEGER          NOT NULL,
    request_moderation      BOOLEAN          NOT NULL,
    views                   INTEGER,
    state                   VARCHAR(255)     NOT NULL,
    initiator_id            INTEGER          NOT NULL REFERENCES users (id) ON delete CASCADE,
    category_id             INTEGER          NOT NULL REFERENCES categories (id),
    loc_lat                 DOUBLE PRECISION NOT NULL,
    loc_lon                 DOUBLE PRECISION NOT NULL,
    confirmed_requests      INTEGER DEFAULT 0
);

create table if not exists requests (

    id                      bigint GENERATED BY DEFAULT AS IDENTITY,
    created_on              TIMESTAMP   NOT NULL,
    event_id                BIGINT      NOT NULL,
    requester_id            BIGINT      NOT NULL,
    status                  VARCHAR(15) NOT NULL,
    PRIMARY KEY (id)

);

CREATE TABLE if not exists compilations (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  is_pinned BOOLEAN NOT NULL,
  title VARCHAR(50) NOT NULL,
  PRIMARY KEY (id)
);

create table if not exists compilations_events
(
    compilation_id BIGINT      NOT NULL references compilations (id) on delete cascade,
    event_id       BIGINT      NOT NULL references events (id) on delete cascade,
    PRIMARY KEY (compilation_id, event_id)
);
