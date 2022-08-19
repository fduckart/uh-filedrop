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

insert into allowlist (id, entry, registrant, created, expiration_check, expired) values (1, 'uhsm', 'lukemcd9', '2019-09-04', 0, false);
insert into allowlist (id, entry, registrant, created, expiration_check, expired) values (2, 'help', 'lukemcd9', '2019-06-26', 0, false);

insert into fd_filedrop (id, uploader, uploader_fullname, created, upload_key, download_key, encrypt_key, valid_until, is_valid, require_auth) values (1, 'test', 'Test', '2019-11-14T08:30:18.023', 'uploadKey',  'downloadKey',  'encryptionKey',  '2019-11-15T08:30:18.023', 'Y', 'Y');
insert into fd_filedrop (id, uploader, uploader_fullname, created, upload_key, download_key, encrypt_key, valid_until, is_valid, require_auth) values (2, 'test', 'Test', '2019-11-14T08:30:18.023', 'uploadKey2', 'downloadKey2', 'rc2:rsRiB-TJDhV-EhcKB-PVRCv', '2051-11-15T08:30:18.023', 'Y', 'N');
insert into fd_filedrop (id, uploader, uploader_fullname, created, upload_key, download_key, encrypt_key, valid_until, is_valid, require_auth) values (3, 'jwlennon@hawaii.edu', 'John W Lennon', '2019-11-14T08:30:18.023', 'uploadKey3', 'downloadKey3', 'encryptionKey3', '2051-11-15T08:30:18.023', 'Y', 'Y');
insert into fd_filedrop (id, uploader, uploader_fullname, created, upload_key, download_key, encrypt_key, valid_until, is_valid, require_auth) values (4, 'jwlennon@hawaii.edu', 'John W Lennon', '2019-11-14T08:30:18.023', 'uploadKey4', 'downloadKey4', 'encryptionKey4', '2019-11-15T08:30:18.023', 'N', 'N');

insert into recipient (id, filedrop_id, name) values (1, 1, 'test');
insert into recipient (id, filedrop_id, name) values (2, 2, 'test');
insert into recipient (id, filedrop_id, name) values (3, 3, 'krichards@example.com');
insert into recipient (id, filedrop_id, name) values (4, 4, 'krichards@example.com');
insert into recipient (id, filedrop_id, name) values (5, 4, 'jwlennon@hawaii.edu');
insert into recipient (id, filedrop_id, name) values (6, 2, 'lukemcd9');

insert into fd_fileset (id, filedrop_id, file_name, type, comment, size) values (1, 2, 'test.txt',   'text/plain', '', 1000);
insert into fd_fileset (id, filedrop_id, file_name, type, comment, size) values (2, 2, 'a-test.txt', 'text/plain', '', 66);
insert into fd_fileset (id, filedrop_id, file_name, type, comment, size) values (3, 2, '1984.jpg',   'image/jpeg', '', 221090);

insert into fd_download (id, filedrop_id, file_name, started, completed, ip_addr) values (1, 2, 'test.txt', '2019-11-14T08:30:18.023', '2019-11-14T08:31:18.023', '0.0.0.0');

insert into setting (id, `key`, `value`) values (1, 'disableLanding', 'false');

insert into faq(id, question, answer) values (1, 'How does it work?', 'The basic idea behind this service is simple: we allow people to upload potentially large files (although not too <a href="#size">large</a>) to our servers, where they will be stored for a limited period. Once the files have been completely uploaded, we send an email containing a download URL to the address or UH username you have specified as the recipient. That''s it, in a nutshell. <br/><br/>There are, of course, a few additional complications. First, this service is provided to the UH community, and is not meant to be open to the general public. However, we do wish to provide the ability for people unaffiliated with the University to use the service to share files with people within UH. In all cases, we require at least one end of the transaction (sender or receiver, that is) to be UH staff or faculty. The only practical consequence of this is that for a UH person to send files to a non-UH person, the UH person must use the link on the service home page to login prior to uploading files.');
insert into faq(id, question, answer) values (2, 'I am having problems logging in.', 'Please try closing and re-opening your browser, which usually corrects a number of problems with how your browser stores login information.');
insert into faq(id, question, answer) values (3, 'Can I send files to multiple recipients?', 'Yes, you can directly use the service to send files to multiple recipients.');
insert into faq(id, question, answer) values (4, 'Are there any restrictions?', 'There are a few restrictions on the service. The total size of the upload must not exceed <span id=\"size\">999MB</span> You can use the service as many times as you like, but any single upload cannot exceed that size. Also, we provide the files for download for a limited time only. Finally, the use of the service must comply with all relevant University policies, including <a href="https://www.hawaii.edu/policy/?action=viewPolicy&amp;policySection=ep&amp;policyChapter=2&amp;policyNumber=210" target="_e2210">E2.210</a>. <br/><br/>For obvious reasons, it''s important to be aware of these restrictions, particularly the limited download time window. If you are using the service to upload files, you must clearly inform the recipients that they must download the files prior to the closure of this window.');
insert into faq(id, question, answer) values (5, 'How long will the files remain on the server?', 'Currently, the files will be available for 30 minutes, 1 hour, or 1 to 14 days. A notice containing the date and time at which the files will be expired will be displayed once the upload is completed. Both the confirmation email sent to you (the uploader) and the email sent by the system to your recipient will contain the expiration date and time. <br/><br/>Once the period has run, the files are irretrievably deleted.');
insert into faq(id, question, answer) values (6, 'How can I securely transfer files?', 'Even in the basic operation described above, the file transfer (both in uploading and downloading) is secure, in the sense that "SSL" is used to encrypt the traffic. This should provide sufficient security for most users. <br/><br/>We do offer an additional level of protection, for users exchanging files containing sensitive information. When uploading files, you can check off the <span style="font-style: italic">"Require Authentication"</span> checkbox. If this checkbox is selected, the UH user who is the recipient of the transfer will have to login before retrieving the files. That is, the recipient must know both the secret URL and the recipient''s UH Username and password. The recipient must correspond to a UH user. If you would like your departmental email address to be included in those that can require authentication, please use the Feedback form to send a message to the FileDrop team.');
insert into faq(id, question, answer) values (7, 'Who can answer some basic questions about the application?', 'Send an email to <a href=\"mailto:filedrop-l@lists.hawaii.edu\">ITS Enterprise Systems</a>');
insert into faq(id, question, answer) values (8, 'Can I get the source code for this project?', 'Sure thing. It is available at this <a href=\"https://github.com/fduckart/uh-filedrop\" target=\"\_git\_repo\">github repository.</a>');
