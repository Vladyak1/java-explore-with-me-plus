DROP TABLE IF EXISTS compilation_events;
DROP TABLE IF EXISTS compilations;
DROP TABLE IF EXISTS requests;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS comments;

CREATE TABLE if not exists users
(
    id      integer GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    email   VARCHAR(255) NOT NULL,
    user_name    VARCHAR(255) NOT NULL,
    is_admin  BOOLEAN DEFAULT FALSE
);

CREATE TABLE if not exists categories
(
    id                      INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(255)       NOT NULL
);



CREATE TABLE if not exists events
(
    id                      INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    annotation              TEXT             NOT NULL,
    description             TEXT             NOT NULL,
    title                   TEXT             NOT NULL,
    created_on              TIMESTAMP        NOT NULL,
    event_date              TIMESTAMP        NOT NULL,
    published_on            TIMESTAMP,
    paid                    BOOLEAN          NOT NULL,
    participant_limit       INTEGER          NOT NULL,
    request_moderation      BOOLEAN          NOT NULL,
    views                   BIGINT[],
    state                   VARCHAR(255)     NOT NULL,
    initiator_id            INTEGER          NOT NULL REFERENCES users (id) ON delete CASCADE,
    category_id             INTEGER          NOT NULL REFERENCES categories (id),
    loc_lat                 DOUBLE PRECISION NOT NULL,
    loc_lon                 DOUBLE PRECISION NOT NULL,
    confirmed_requests      INTEGER DEFAULT 0
);

CREATE TABLE if not exists requests (

    id                      bigint GENERATED BY DEFAULT AS IDENTITY,
    created_on              TIMESTAMP   NOT NULL,
    event_id                BIGINT      NOT NULL,
    requester_id            BIGINT      NOT NULL,
    status                  VARCHAR(15) NOT NULL,
    PRIMARY KEY (id)

);

CREATE TABLE if not exists compilations (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    is_pinned               BOOLEAN     NOT NULL,
    title                   VARCHAR(50) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE if not exists compilations_events
(
    compilation_id          BIGINT      NOT NULL references compilations (id) on delete cascade,
    event_id                BIGINT      NOT NULL references events (id) on delete cascade,
    PRIMARY KEY (compilation_id, event_id)
);

CREATE TABLE if not exists comments (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    text                    VARCHAR(2048) NOT NULL,
    event_id                BIGINT      REFERENCES events (id) on delete cascade,
    author_id               BIGINT      REFERENCES users (id) on delete cascade,
    created_on              TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);
