DROP DATABASE IF EXISTS schulplaner;
CREATE DATABASE IF NOT EXISTS schulplaner;
USE schulplaner;

CREATE TABLE entity (
    id   VARCHAR(36),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS user (
    id          VARCHAR(36) DEFAULT (UUID()),
    email       VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    type        VARCHAR(255) NOT NULL,
    department  VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY (email),
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
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
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
    UNIQUE KEY member (uid, gid),
    FOREIGN KEY (uid) REFERENCES user (id),
    FOREIGN KEY (gid) REFERENCES `group` (id)
);

CREATE TABLE IF NOT EXISTS calendar (
    id          VARCHAR(36) DEFAULT (UUID()),
    owner       VARCHAR(36),
    name        VARCHAR(255),
    PRIMARY KEY (id),
    UNIQUE KEY calendar (owner, name),
    FOREIGN KEY (owner) REFERENCES entity (id)
);

CREATE TABLE IF NOT EXISTS calendar_entry (
    id          VARCHAR(36) DEFAULT (UUID()),
    calendar    VARCHAR(36) NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    start       DATETIME NOT NULL,
    end         DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY calendar_entry (calendar, title, start, end),
    FOREIGN KEY (calendar) REFERENCES calendar (id)
);

CREATE TABLE IF NOT EXISTS todo_list (
    id          VARCHAR(36) DEFAULT (UUID()),
    owner       VARCHAR(36) NOT NULL,
    title       VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY todo_list (owner, title),
    FOREIGN KEY (owner) REFERENCES entity(id)
);

CREATE TABLE IF NOT EXISTS todo_item (
    id          VARCHAR(36) DEFAULT (UUID()),
    list        VARCHAR(36) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    status      ENUM('UNSTARTED','IN_PROGRESS','DONE','FAILED') NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (list) REFERENCES todo_list (id)
);
