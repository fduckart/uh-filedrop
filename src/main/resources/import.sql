-- Messages. 
insert into message (MSG_ID, MSG_ENABLED, MSG_TYPE_ID, MSG_TEXT) values (1, 'Y', 1, 'This application is provided by the University of Hawai''i to allow a limited form of large file sharing between UH faculty and staff. It also allows users affiliated with the University to share files with non-UH users.');
insert into message (MSG_ID, MSG_ENABLED, MSG_TYPE_ID, MSG_TEXT) values (2, 'Y', 1, 'Welcome to the University of Hawai''i FileDrop application.');
insert into message (MSG_ID, MSG_ENABLED, MSG_TYPE_ID, MSG_TEXT) values (3, 'Y', 1, 'FileDrop is currently unavailable.');
insert into message (MSG_ID, MSG_ENABLED, MSG_TYPE_ID, MSG_TEXT) values (4, 'N', 1, 'University of Hawaii Information Technology Services resides in a state-of-the-art, six-story, 74,000-square-foot facility located on the Manoa campus.');

-- Campus codes and names.
insert into campus (id, code, actual, description) values (1,  'HA', 'Y', 'Hawaii Community College');
insert into campus (id, code, actual, description) values (2,  'HI', 'Y', 'UH Hilo');
insert into campus (id, code, actual, description) values (3,  'HO', 'Y', 'Honolulu Community College');
insert into campus (id, code, actual, description) values (4,  'KA', 'Y', 'Kapiolani Community College');
insert into campus (id, code, actual, description) values (5,  'KU', 'Y', 'Kauai Community College');
insert into campus (id, code, actual, description) values (6,  'LE', 'Y', 'Leeward Community College');
insert into campus (id, code, actual, description) values (7,  'MA', 'Y', 'UH Manoa');
insert into campus (id, code, actual, description) values (8,  'MU', 'Y', 'UH Maui College');
insert into campus (id, code, actual, description) values (9,  'WI', 'Y', 'Windward Community College');
insert into campus (id, code, actual, description) values (10, 'WO', 'Y', 'UH West Oahu');
insert into campus (id, code, actual, description) values (11, 'SW', 'N', 'UH System');

-- Campus offices.
insert into office (id, campus_id, sort_id, description) values (1,   1, 1,  'Chancellors Office');
insert into office (id, campus_id, sort_id, description) values (2,   2, 7,  'Chancellors Office');
insert into office (id, campus_id, sort_id, description) values (3,   3, 2,  'Chancellors Office');
insert into office (id, campus_id, sort_id, description) values (4,   4, 3,  'Chancellors Office');
insert into office (id, campus_id, sort_id, description) values (5,   5, 4,  'Chancellors Office');
insert into office (id, campus_id, sort_id, description) values (6,   6, 5,  'Chancellors Office');
insert into office (id, campus_id, sort_id, description) values (7,   7, 8,  'Chancellors Office');
insert into office (id, campus_id, sort_id, description) values (8,   8, 9,  'Chancellors Office');
insert into office (id, campus_id, sort_id, description) values (9,   9, 6,  'Chancellors Office');
insert into office (id, campus_id, sort_id, description) values (10, 10, 10, 'Chancellors Office');
insert into office (id, campus_id, sort_id, description) values (11, 11, 11, 'Information Technology Services');
insert into office (id, campus_id, sort_id, description) values (12, 11, 12, 'Vice President for Community Colleges');

insert into role (id, role, security_role, description) values ( 1, 'NON_UH',         'NON_UH',        'NonUH');
insert into role (id, role, security_role, description) values (13, 'ADMINISTRATOR',  'ADMINISTRATOR', 'Administrator');
insert into role (id, role, security_role, description) values (14, 'SUPER_USER',     'SUPERUSER',     'Superuser');

-- People.
insert into person (id, uhuuid, username, name, email) values ( 2, '17958670', 'duckart'  , 'Frank Duckart',   'duckart@hawaii.edu'    );
insert into person (id, uhuuid, username, name, email) values ( 5, '10000002', 'krichards', 'Keith Richards',  'krichards@example.com' );
insert into person (id, uhuuid, username, name, email) values (22, '12345678', 'test22'   , 'Test Staff3',     'test3@hawaii.edu'      );
insert into person (id, uhuuid, username, name, email) values (23, '89999999', 'test23'   , 'Test Admin',      'admin@example.com'     );
insert into person (id, uhuuid, username, name, email) values (24, '10000001', 'test24'   , 'Test Tester',     'tester@example.com'    );
insert into person (id, uhuuid, username, name, email) values (25, '10000004', 'test25'   , 'Test Admin',      'admin4@example.com'    );
insert into person (id, uhuuid, username, name, email) values (26, '10000003', 'test26'   , 'Test Admin',      'admin3@example.com'    );

insert into system_role (id, person_id, role_id, office_id) values ( 2,  2, 14, 11);
insert into system_role (id, person_id, role_id, office_id) values ( 5,  5, 13, 11);
insert into system_role (id, person_id, role_id, office_id) values (25, 23, 13, 11);
insert into system_role (id, person_id, role_id, office_id) values (26, 24, 13, 11);
insert into system_role (id, person_id, role_id, office_id) values (28, 25, 13, 11);
insert into system_role (id, person_id, role_id, office_id) values (29, 26, 13, 11);
insert into system_role (id, person_id, role_id, office_id) values (30, 26, 13, 11);
insert into system_role (id, person_id, role_id, office_id) values (31,  2, 13, 11);

insert into whitelist (id, entry, registrant, created, expiration_check, expired) values (1, 'uhsm', 'lukemcd9', '2019-09-04', 0, false);
insert into whitelist (id, entry, registrant, created, expiration_check, expired) values (2, 'help', 'lukemcd9', '2019-06-26', 0, false);

insert into filedrop (id, uploader, uploader_fullname, created, upload_key, download_key, encrypt_key, valid_until, is_valid, require_auth) values (1, 'test', 'Test', '2019-11-14T08:30:18.023', 'uploadKey',  'downloadKey',  'encryptionKey',  '2019-11-15T08:30:18.023', 'Y', 'Y');
insert into filedrop (id, uploader, uploader_fullname, created, upload_key, download_key, encrypt_key, valid_until, is_valid, require_auth) values (2, 'test', 'Test', '2019-11-14T08:30:18.023', 'uploadKey2', 'downloadKey2', 'encryptionKey2', '2019-11-15T08:30:18.023', 'Y', 'N');
insert into filedrop (id, uploader, uploader_fullname, created, upload_key, download_key, encrypt_key, valid_until, is_valid, require_auth) values (3, 'jwlennon@hawaii.edu', 'John W Lennon', '2019-11-14T08:30:18.023', 'uploadKey3', 'downloadKey3', 'encryptionKey3', '2019-11-15T08:30:18.023', 'Y', 'Y');
insert into filedrop (id, uploader, uploader_fullname, created, upload_key, download_key, encrypt_key, valid_until, is_valid, require_auth) values (4, 'jwlennon@hawaii.edu', 'John W Lennon', '2019-11-14T08:30:18.023', 'uploadKey4', 'downloadKey4', 'encryptionKey4', '2019-11-15T08:30:18.023', 'N', 'N');

insert into recipient (id, filedrop_id, name) values (1, 1, 'test');
insert into recipient (id, filedrop_id, name) values (2, 2, 'test');
insert into recipient (id, filedrop_id, name) values (3, 3, 'krichards@example.com');
insert into recipient (id, filedrop_id, name) values (4, 4, 'krichards@example.com');
insert into recipient (id, filedrop_id, name) values (5, 4, 'jwlennon@hawaii.edu');

