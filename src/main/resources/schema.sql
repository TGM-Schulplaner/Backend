CREATE TABLE user (
    id          UUID DEFAULT RANDOM_UUID(),
    email       VARCHAR(255),
    name        VARCHAR(255),
    type        VARCHAR(255),
    department  VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE "GROUP" (
    id          UUID DEFAULT RANDOM_UUID(),
    name        VARCHAR(255),
    description VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE member (
    id          UUID DEFAULT RANDOM_UUID(),
    uid         UUID,
    gid         UUID,
    PRIMARY KEY (id),
    FOREIGN KEY (uid) REFERENCES user (id),
    FOREIGN KEY (gid) REFERENCES "GROUP" (id)
);

CREATE TABLE calendar (
    id          UUID DEFAULT RANDOM_UUID(),
    owner       UUID,
    name        VARCHAR(255),
    PRIMARY KEY (id),
    FOREIGN KEY (owner) REFERENCES user (id),
    FOREIGN KEY (owner) REFERENCES "GROUP" (id)
);

CREATE TABLE calendar_entry (
    id          UUID DEFAULT RANDOM_UUID(),
    calendar    UUID,
    name        VARCHAR(255),
    description VARCHAR(255),
    begin       DATETIME,
    end         DATETIME,
    PRIMARY KEY (id),
    FOREIGN KEY (calendar) REFERENCES calendar (id)
);

CREATE TABLE todo_list (
    id          UUID DEFAULT RANDOM_UUID(),
    owner       UUID,
    title       VARCHAR(255),
    PRIMARY KEY (id),
    FOREIGN KEY (owner) REFERENCES user (id),
    FOREIGN KEY (owner) REFERENCES "GROUP" (id)
);

CREATE TABLE todo_item (
    id          UUID DEFAULT RANDOM_UUID(),
    list        UUID,
    name        VARCHAR(255),
    description VARCHAR(255),
    status      ENUM('UNSTARTED','IN_PROGRESS','DONE','FAILED'),
    PRIMARY KEY (id),
    FOREIGN KEY (list) REFERENCES todo_list (id)
);
