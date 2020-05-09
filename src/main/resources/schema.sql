# DROP DATABASE IF EXISTS schulplaner;
CREATE DATABASE IF NOT EXISTS schulplaner;
USE schulplaner;

CREATE TABLE entity (
    id   VARCHAR(36),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS user (
    id          VARCHAR(36) DEFAULT (UUID()),
    email       VARCHAR(255),
    name        VARCHAR(255),
    type        VARCHAR(255),
    department  VARCHAR(255),
    PRIMARY KEY (id),
    FOREIGN KEY (id) REFERENCES entity(id)
);

DELIMITER ///
CREATE TRIGGER create_entity_user BEFORE INSERT ON user FOR EACH ROW
BEGIN
    INSERT INTO entity VALUE (NEW.id);
END;
///
DELIMITER ;

CREATE TABLE IF NOT EXISTS `group` (
    id          VARCHAR(36) DEFAULT (UUID()),
    name        VARCHAR(255),
    description VARCHAR(255),
    PRIMARY KEY (id),
    FOREIGN KEY (id) REFERENCES entity(id)
);

DELIMITER ///
CREATE TRIGGER create_entity_group BEFORE INSERT ON `group` FOR EACH ROW
BEGIN
    INSERT INTO entity VALUE (NEW.id);
END;
///
DELIMITER ;

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
    FOREIGN KEY (owner) REFERENCES entity (id)
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
    FOREIGN KEY (owner) REFERENCES entity(id)
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
INSERT INTO calendar(id, owner, name) VALUE ('57162059-91d4-11ea-9fd5-5048494f4e43', '952382dc-063b-4d67-b394-400f1e274dc4', 'Test Calendar');
INSERT INTO calendar_entry(calendar, title, description, start, end) VALUE ('57162059-91d4-11ea-9fd5-5048494f4e43', 'Meeting', 'Project team meeting', '2020-05-11 15:00:00', '2020-05-11 17:00:00')
