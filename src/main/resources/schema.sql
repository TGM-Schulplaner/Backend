CREATE DATABASE IF NOT EXISTS schulplaner;
USE schulplaner;

CREATE TABLE IF NOT EXISTS user (
    id          VARCHAR(36) DEFAULT (UUID()),
    email       VARCHAR(255),
    name        VARCHAR(255),
    type        VARCHAR(255),
    department  VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS `group` (
    id          VARCHAR(36) DEFAULT (UUID()),
    name        VARCHAR(255),
    description VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS member (
    id          VARCHAR(36) DEFAULT (UUID()),
    uid         VARCHAR(36) NOT NULL,
    gid         VARCHAR(36) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (uid) REFERENCES user (id),
    FOREIGN KEY (gid) REFERENCES `group` (id)
);

CREATE TABLE IF NOT EXISTS calendar (
    id          VARCHAR(36) DEFAULT (UUID()),
    owner       VARCHAR(36),
    name        VARCHAR(255),
    PRIMARY KEY (id),
    FOREIGN KEY (owner) REFERENCES user (id),
    FOREIGN KEY (owner) REFERENCES `group` (id)
);

CREATE TABLE IF NOT EXISTS calendar_entry (
    id          VARCHAR(36) DEFAULT (UUID()),
    calendar    VARCHAR(36),
    title       VARCHAR(255),
    description VARCHAR(255),
    start       DATETIME,
    end         DATETIME,
    PRIMARY KEY (id),
    FOREIGN KEY (calendar) REFERENCES calendar (id)
);

CREATE TABLE IF NOT EXISTS todo_list (
    id          VARCHAR(36) DEFAULT (UUID()),
    owner       VARCHAR(36),
    title       VARCHAR(255),
    PRIMARY KEY (id),
    FOREIGN KEY (owner) REFERENCES user (id),
    FOREIGN KEY (owner) REFERENCES `group` (id)
);

CREATE TABLE IF NOT EXISTS todo_item (
    id          VARCHAR(36) DEFAULT (UUID()),
    list        VARCHAR(36),
    name        VARCHAR(255),
    description VARCHAR(255),
    status      ENUM('UNSTARTED','IN_PROGRESS','DONE','FAILED'),
    PRIMARY KEY (id),
    FOREIGN KEY (list) REFERENCES todo_list (id)
);

INSERT INTO user (id, name, email, type, department) VALUES ('952382dc-063b-4d67-b394-400f1e274dc4', 'Georg Burkl', 'gburkl@student.tgm.ac.at', 'schueler', '3DHIT');
