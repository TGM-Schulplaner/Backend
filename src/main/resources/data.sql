USE schulplaner;

INSERT INTO user (id, name, email, type, department) VALUES ('952382dc-063b-4d67-b394-400f1e274dc4', 'Georg Burkl', 'gburkl@student.tgm.ac.at', 'schueler', '3DHIT');
INSERT INTO `group` (id, name, description) VALUES ('9816248b-929c-11ea-9fd5-5048494f4e43', 'TGM Schulplaner', 'TGM Schulplaner ITP Gruppe');
INSERT INTO member (id, uid, gid) VALUES ('e18d9b3f-929d-11ea-9fd5-5048494f4e43', '952382dc-063b-4d67-b394-400f1e274dc4', '9816248b-929c-11ea-9fd5-5048494f4e43');

INSERT INTO calendar (id, owner, name) VALUE ('57162059-91d4-11ea-9fd5-5048494f4e43', '9816248b-929c-11ea-9fd5-5048494f4e43', 'Test Calendar');
INSERT INTO calendar_entry (id, calendar, title, description, start, end) VALUE ('319be013-91d6-11ea-9fd5-5048494f4e43', '57162059-91d4-11ea-9fd5-5048494f4e43', 'Meeting', 'Project team meeting', '2020-05-11 15:00:00', '2020-05-11 17:00:00');

INSERT INTO todo_list (id, owner, title) VALUES ('49dbfe41-929d-11ea-9fd5-5048494f4e43', '9816248b-929c-11ea-9fd5-5048494f4e43', 'Dev Todo');
INSERT INTO todo_item (id, list, name, description, status) VALUES ('f9b20b59-929d-11ea-9fd5-5048494f4e43', '49dbfe41-929d-11ea-9fd5-5048494f4e43', 'Data modification API', 'Create backend API for data manipulation', 'UNSTARTED');
