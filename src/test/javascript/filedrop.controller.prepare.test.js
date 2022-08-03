describe("PrepareJsController", function() {

    beforeEach(module('filedropApp', function($provide) {
        $provide.service('$window', function() {
            this.user = {
                uid: "fd",
                cn: "f r d",
                mails: ["f@h.x", "d@h.y"],
                mail: "f@h.x"
            };

            this.fileDrop = {
                sender: this.user.mails[1],
                recipients: ["a@b.c", "b@c.d", "c@d.e", this.user.mails[0]],
                expiration: "4800",
                authentication: true,
                message: "What is the what?"
            };
        });
    }));

    let scope;
    let controller;
    let window;
    let dataProvider;

    beforeEach(inject(function($rootScope, $controller, _$window_, _dataProvider_, _$httpBackend_) {
        scope = $rootScope.$new();
        window = _$window_;
        dataProvider = _dataProvider_;
        $httpBackend = _$httpBackend_;
        controller = $controller('PrepareJsController', {
            $scope: scope,
            dataProvider: _dataProvider_,
            $window: _$window_
        });
    }));

    // afterEach(function() {
    //     $httpBackend.verifyNoOutstandingExpectation();
    //     $httpBackend.verifyNoOutstandingRequest();
    // });

    xit("construction", function() {
        expect(controller).toBeDefined();
        expect(scope.recipient).not.toBeDefined();
        expect(scope.recipients).not.toBeDefined();
        expect(scope.sendToSelf).not.toBeDefined();
        expect(scope.authentication).not.toBeDefined();
        expect(scope.expiration).not.toBeDefined();
        expect(scope.message).not.toBeDefined();
        expect(scope.message).not.toBeDefined();
        expect(window.user).toBeDefined();
        expect(window.fileDrop).toBeDefined();

        // See mock above.
        let user = scope.currentUser();
        expect(user).toBeDefined();
        expect(user.uid).toEqual("fd");
        expect(user.cn).toEqual("f r d");
        expect(user.mails.length).toEqual(2);
        expect(user.mails[0]).toEqual("f@h.x");
        expect(user.mails[1]).toEqual("d@h.y");

        // See mock above.
        let fileDrop = scope.getFileDrop();
        // expect(fileDrop.sender).toEqual(user.mails);
        expect(fileDrop.recipients.length).toEqual(4);
        expect(fileDrop.recipients[0]).toEqual("a@b.c");
        expect(fileDrop.recipients[1]).toEqual("b@c.d");
        expect(fileDrop.recipients[2]).toEqual("c@d.e",);
        expect(fileDrop.recipients[3]).toEqual(user.mails[0]);
        expect(fileDrop.expiration).toEqual("4800");
        expect(fileDrop.authentication).toBeTrue();
        expect(fileDrop.message).toEqual("What is the what?");

        // Not directly, but via the mock.
        expect(scope.sender).toBeDefined();
        expect(scope.sender.mails).toBeDefined();
        expect(scope.sender.mails.length).toEqual(user.mails.length);
        expect(scope.sender.mails[0]).toEqual("f@h.x");
        expect(scope.sender.mails[1]).toEqual("d@h.y");
    });

    xit("init", function() {
        expect(controller).toBeDefined();
        expect(scope.recipient).not.toBeDefined();
        expect(scope.recipients).not.toBeDefined();
        expect(scope.sendToSelf).not.toBeDefined();
        expect(scope.authentication).not.toBeDefined();
        expect(scope.expiration).not.toBeDefined();
        expect(scope.message).not.toBeDefined();
        expect(scope.message).not.toBeDefined();
        expect(window.user).toBeDefined();
        expect(window.fileDrop).toBeDefined();

        // What we are testing:
        scope.init();

        expect(scope.recipient).toBeDefined();
        expect(scope.recipients).toBeDefined();
        expect(scope.recipients.length).toEqual(1);
        expect(scope.recipients[0].uid).toEqual("fd");
        expect(scope.recipients[0].name).toEqual("f r d"); // Weird.
        expect(scope.sendToSelf).toBeDefined();
        expect(scope.sendToSelf).toBeTrue(); // See mock.
        expect(scope.authentication).toBeDefined();
        expect(scope.authentication).toBeTrue();
        expect(scope.getFileDrop().authentication).toBeTrue(); // See mock.
        expect(scope.expiration).toBeDefined();

        let fileDrop = scope.getFileDrop();
        expect(scope.expiration).toEqual(fileDrop.expiration);
        expect(scope.message).toBeDefined();
        expect(scope.message).toEqual(fileDrop.message);
        expect(scope.sender).toBeDefined();

        let user = scope.currentUser();
        expect(scope.sender.mails.length).toEqual(user.mails.length);
        expect(scope.sender.mails[0]).toEqual(user.mails[0]);
        expect(scope.sender.mails[1]).toEqual(user.mails[1]);

        expect(scope.sender.model).toBeDefined();
        expect(scope.sender.model).toEqual(fileDrop.sender);
        expect(scope.sender.model).toEqual(scope.sender.mails[1]);
    });

    xit("isCurrentUserMail", function() {
        expect(scope.isCurrentUserMail(null)).toBeFalse();
        expect(scope.isCurrentUserMail("")).toBeFalse();
        expect(scope.isCurrentUserMail(undefined)).toBeFalse();

        let user = scope.currentUser();
        expect(scope.isCurrentUserMail(user.mails[0])).toBeTrue();
        expect(scope.isCurrentUserMail(user.mails[1])).toBeTrue();
        expect(scope.isCurrentUserMail("_" + user.mails[1])).toBeFalse();
    });

    xit("isCurrentUserUid", function() {
        expect(scope.isCurrentUserUid(null)).toBeFalse();
        expect(scope.isCurrentUserUid("")).toBeFalse();
        expect(scope.isCurrentUserUid(undefined)).toBeFalse();

        let user = scope.currentUser();
        expect(scope.isCurrentUserUid(user.uid)).toBeTrue();
        expect(scope.isCurrentUserUid("_" + user.uid)).toBeFalse();
    });

    xit("isEmptyPerson", function() {
        expect(scope.isEmptyPerson(null)).toBeTrue();
        expect(scope.isEmptyPerson("")).toBeTrue();
        expect(scope.isEmptyPerson(undefined)).toBeTrue();

        let person = null;
        expect(scope.isEmptyPerson(person)).toBeTrue();

        person = {};
        expect(scope.isEmptyPerson(person)).toBeTrue();

        person = {cn: null};
        expect(scope.isEmptyPerson(person)).toBeTrue();

        person = {cn: null, uid: null};
        expect(scope.isEmptyPerson(person)).toBeTrue();

        person = {cn: null, uid: null, mails: null};
        expect(scope.isEmptyPerson(person)).toBeTrue();

        person = {cn: null, uid: null, mails: []};
        expect(scope.isEmptyPerson(person)).toBeTrue();

        person = {uid: null, mails: []};
        expect(scope.isEmptyPerson(person)).toBeTrue();

        person = {mails: []};
        expect(scope.isEmptyPerson(person)).toBeTrue();

        person = {mails: ["d@e.f"]};
        expect(scope.isEmptyPerson(person)).toBeFalse();

        person = {uid: "d", mails: []};
        expect(scope.isEmptyPerson(person)).toBeFalse();

        person = {cn: "fd"};
        expect(scope.isEmptyPerson(person)).toBeTrue();

        person = {cn: "fd", mails: []};
        expect(scope.isEmptyPerson(person)).toBeFalse();

        person = {cn: "fd", uid: "d", mails: []};
        expect(scope.isEmptyPerson(person)).toBeFalse();
    });

    it("getRecipients", function() {
        expect(scope.error).toBeUndefined();
        expect(scope.addStep).toBeUndefined();
        expect(scope.sendToSelf).toBeUndefined();
        let recipients = scope.getRecipients();
        expect(typeof recipients).toEqual("object");
        var expectedRecipientCount = 0;
        expect(recipients.length).toEqual(expectedRecipientCount);
        let recipientsStr = scope.getRecipientsString();
        expect(typeof recipientsStr).toEqual("string");
        expect(recipientsStr.length).toEqual(0);
        expect(scope.error).toBeUndefined();
        expect(scope.sendToSelf).toBeUndefined();
        expect(scope.addStep).toBeUndefined();

        const filedropRecipients = scope.getFileDrop().recipients;
        expect(filedropRecipients.length).toEqual(4);
        expect(filedropRecipients[0]).toEqual("a@b.c");
        expect(filedropRecipients[1]).toEqual("b@c.d");
        expect(filedropRecipients[2]).toEqual("c@d.e");
        expect(filedropRecipients[3]).toEqual("f@h.x");
        expect(scope.hasRecipient(filedropRecipients[0])).toEqual(false);
        expect(scope.hasRecipient(filedropRecipients[1])).toEqual(false);
        expect(scope.hasRecipient(filedropRecipients[2])).toEqual(false);
        expect(scope.hasRecipient(filedropRecipients[3])).toEqual(false);
        expect(scope.isCurrentUserUid(filedropRecipients[0])).toEqual(false);
        expect(scope.isCurrentUserUid(filedropRecipients[1])).toEqual(false);
        expect(scope.isCurrentUserUid(filedropRecipients[2])).toEqual(false);
        expect(scope.isCurrentUserUid(filedropRecipients[3])).toEqual(false);
        expect(scope.isCurrentUserMail(filedropRecipients[0])).toEqual(false);
        expect(scope.isCurrentUserMail(filedropRecipients[1])).toEqual(false);
        expect(scope.isCurrentUserMail(filedropRecipients[2])).toEqual(false);
        expect(scope.isCurrentUserMail(filedropRecipients[3])).toEqual(true); // <--
        expect(scope.isEmptyPerson(filedropRecipients[0])).toEqual(true);
        expect(scope.isEmptyPerson(filedropRecipients[1])).toEqual(true);
        expect(scope.isEmptyPerson(filedropRecipients[2])).toEqual(true);
        expect(scope.isEmptyPerson(filedropRecipients[3])).toEqual(true);

        expect(scope.getRecipients().length).toEqual(expectedRecipientCount);

        expect(expectedRecipientCount).toBe(0);

        // ------
        recipients = scope.getRecipients();
        expect(recipients.length).toEqual(expectedRecipientCount);
        expect(recipients.length).toEqual(0);
        expect(scope.getRecipients().length).toEqual(0);
        expect(filedropRecipients.length).toBe(4);
        // ------

        scope.init(); // <-- Note.

        expect(scope.addStep.length).toEqual(9);
        expect(scope.addStep[0]).toEqual("_init_");
        expect(scope.addStep[1]).toEqual("_add_start_a@b.c");
        expect(scope.addStep[2]).toEqual("_add_done_a@b.c");
        expect(scope.addStep[3]).toEqual("_add_start_b@c.d");
        expect(scope.addStep[4]).toEqual("_add_done_b@c.d");
        expect(scope.addStep[5]).toEqual("_add_start_c@d.e");
        expect(scope.addStep[6]).toEqual("_add_done_c@d.e");
        expect(scope.addStep[5]).toEqual("_add_start_c@d.e");
        expect(scope.addStep[6]).toEqual("_add_done_c@d.e");
        expect(scope.addStep[7]).toEqual("_add_start_f@h.x");
        expect(scope.addStep[8]).toEqual("_add_current_user_f@h.x");

        /// recipients: ["a@b.c", "b@c.d", "c@d.e", this.user.mails[0]],
        /// mails: ["f@h.x", "d@h.y"],

        if ("off" === "") {

            const postUrlBase = "/filedrop/prepare/recipient/add";

            // Only three will result in a http post because
            // the fourth address is the current user.
            let mmm = 0;
            for (let i = 0; i < 3; i++) {
                let r = filedropRecipients[i];
                // for (let r in filedropRecipients) {
                let url = postUrlBase + "?recipient=" + r;
                $httpBackend.whenPOST(url)
                    .respond(200, scope.makeRecipient(undefined, r, [r], undefined));
                $httpBackend.expectPOST(url);
                expectedRecipientCount++;
                mmm++;
            }

            expect(mmm).toBe(3);

            // ------
            expect(postUrlBase + "?recipient=" + filedropRecipients[0]).toBe("/filedrop/prepare/recipient/add?recipient=a@b.c");
            expect(postUrlBase + "?recipient=" + filedropRecipients[1]).toBe("/filedrop/prepare/recipient/add?recipient=b@c.d");
            expect(postUrlBase + "?recipient=" + filedropRecipients[2]).toBe("/filedrop/prepare/recipient/add?recipient=c@d.e");
            expect(postUrlBase + "?recipient=" + filedropRecipients[3]).toBe("/filedrop/prepare/recipient/add?recipient=f@h.x");

            expect(expectedRecipientCount).toBe(3);
            expect(mmm).toBe(3);
            expect(filedropRecipients.length).toBe(4);

            //expect(scope.addStep).toBeUndefined();
            // expect(scope.addStep.length).toEqual(12);

            let t = filedropRecipients[3];
            let url = postUrlBase + "?recipient=" + t;
            // $httpBackend.whenPOST(url).respond(200, scope.makeRecipient(undefined, t, [t], undefined));
            // $httpBackend.expectPOST(url);

            // ------

            // scope.init(); // <-- Note.

            // ------
            expect(scope.addStep.length).toEqual(9);
            // ------

            $httpBackend.flush();

            // ------
            // expect(scope.getRecipients().length).toEqual(666);
            // recipients = scope.getRecipients();
            //expect(recipients[0]).toBe("what");
            // ------

            // The init loaded the mocked recipients we defined above.

            expect(scope.addStep.length).toEqual(9 + 3);
            expect(scope.addStep[0]).toEqual("_init_");
            expect(scope.addStep[1]).toEqual("_add_start_");
            expect(scope.addStep[2]).toEqual("_add_done_");
            expect(scope.addStep[3]).toEqual("_add_start_");
            expect(scope.addStep[4]).toEqual("_add_done_");
            expect(scope.addStep[5]).toEqual("_add_start_");
            expect(scope.addStep[6]).toEqual("_add_done_");
            expect(scope.addStep[7]).toEqual("_add_start_");
            expect(scope.addStep[8]).toEqual("_add_current_user_" + filedropRecipients[3]);
            expect(scope.addStep[9]).toEqual("_add_person_" + filedropRecipients[0]);
            expect(scope.addStep[10]).toEqual("_add_person_" + filedropRecipients[1]);
            expect(scope.addStep[11]).toEqual("_add_person_" + filedropRecipients[2]);
            expect(scope.sendToSelf).toBeTrue();
            expect(scope.error).toBeUndefined();
            recipients = scope.getRecipients();
            ///expect(recipients.length).toEqual(expectedRecipientCount);
            expect(recipients[3].mail).toEqual(filedropRecipients[2]);
            expect(recipients[2].mail).toEqual(filedropRecipients[1]);
            expect(recipients[1].mail).toEqual(filedropRecipients[0]);
            expect(recipients[0].mail).toEqual(filedropRecipients[3]);

            expect(recipients[3].mail).toEqual("c@d.e");
            expect(recipients[2].mail).toEqual("b@c.d");
            expect(recipients[1].mail).toEqual("a@b.c");
            expect(recipients[0].mail).toEqual("f@h.x");

            const currentUser = scope.currentUser();
            let user = {
                name: currentUser.cn,
                mail: currentUser.mail,
                mails: currentUser.mails,
                uid: currentUser.uid
            }

            expect(recipients[0]).toEqual(user); // Note.

            recipientsStr = scope.getRecipientsString();
            expect(recipientsStr).toEqual("fd,,,"); // FIXME
            expect(scope.recipient).toEqual("");
            expect(scope.authentication).toBeTrue();
            expect(scope.error).toBeUndefined();

            const person = {
                name: "n",
                mail: "m@n.o",
                mails: ["m@n.o"],
                uid: "sy"
            };
            expect(scope.isEmptyPerson(person)).toEqual(false);
            expect(scope.isEmptyPerson(person)).toBeFalse();

            const recipientToAdd = person.uid;
            expect(scope.hasRecipient(recipientToAdd)).toBeFalse();
            expect(scope.isCurrentUserUid(recipientToAdd)).toBeFalse();
            expect(scope.isCurrentUserMail(recipientToAdd)).toBeFalse();

            expect(recipientToAdd).toEqual("sy");
            expect(recipientToAdd).toEqual(person.uid);

            let postUrl = postUrlBase + "?recipient=" + recipientToAdd;
            $httpBackend.whenPOST(postUrl).respond(200, person);
            $httpBackend.expectPOST(postUrl);

            scope.addRecipient(recipientToAdd);

            $httpBackend.flush();

            expect(scope.addStep.length).toEqual(15);
            expect(scope.addStep[12]).toEqual("_add_start_");
            expect(scope.addStep[13]).toEqual("_add_done_");
            expect(scope.addStep[14]).toEqual("_add_person_sy");

            expect(scope.recipient).toEqual(""); // ???

            recipientsStr = scope.getRecipientsString();
            expect(recipientsStr).toEqual("fd,,,,sy"); // FIXME

            recipients = scope.getRecipients();
            expect(recipients.length).toEqual(5);

            if ("off" === "") {
                throw new Error("STOP addrecipient: ["
                + JSON.stringify(recipients, function(k, v) {
                    return v === undefined ? undefined : v;
                }) + "]");
            }

            expect(recipients[0]).toEqual({name: "f r d", mail: "f@h.x", mails: ["f@h.x", "d@h.y"], uid: "fd"});
            expect(recipients[1]).toEqual({name: undefined, mail: "a@b.c", mails: ["a@b.c"], uid: undefined});
            expect(recipients[2]).toEqual({name: undefined, mail: "b@c.d", mails: ["b@c.d"], uid: undefined});
            expect(recipients[3]).toEqual({name: undefined, mail: "c@d.e", mails: ["c@d.e"], uid: undefined});
            expect(recipients[4]).toEqual({name: undefined, mail: "m@n.o", mails: ["m@n.o"], uid: "sy"});

            expect(recipients[0].mail).toEqual("f@h.x");
            expect(recipients[1].mail).toEqual("a@b.c");
            expect(recipients[2].mail).toEqual("b@c.d");
            expect(recipients[3].mail).toEqual("c@d.e");
            expect(recipients[4].mail).toEqual("m@n.o");

            let a = {what: "hey"};
            let b = {what: "hey"};

            expect(a).not.toBe(b);
            expect(a.what).toBe(b.what);
            expect(a).toEqual(b);
        }
    });

});

describe("PrepareJsController#getRecipients", function() {
    beforeEach(module('filedropApp', function($provide) {
        $provide.service('$window', function() {
            this.user = {
                uid: "fd",
                cn: "f r d",
                mails: ["f@h.x", "d@h.y"],
                mail: "f@h.x"
            };
            this.fileDrop = {
                sender: null,
                recipients: null,
                expiration: null,
                authentication: null,
                message: null
            };
        });
    }));

    let scope;
    let controller;
    let window;
    let dataProvider;

    beforeEach(inject(function($rootScope, $controller, _$window_, _dataProvider_, _$httpBackend_) {
        scope = $rootScope.$new();
        window = _$window_;
        dataProvider = _dataProvider_;
        $httpBackend = _$httpBackend_;
        controller = $controller('PrepareJsController', {
            $scope: scope,
            dataProvider: _dataProvider_,
            $window: _$window_
        });
    }));

    afterEach(function() {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });

    xit("add-and-get-recipients", function() {
        expect(scope.error).toBeUndefined();
        expect(scope.addStep).toBeUndefined();
        expect(scope.sendToSelf).toBeUndefined();
        let recipients = scope.getRecipients();
        expect(typeof recipients).toEqual("object");
        expect(recipients.length).toEqual(0);
        let recipientsStr = scope.getRecipientsString();
        expect(typeof recipientsStr).toEqual("string");
        expect(recipientsStr.length).toEqual(0);
        expect(scope.error).toBeUndefined();
        expect(scope.sendToSelf).toBeUndefined();
        expect(scope.addStep).toBeUndefined();
        const currentUser = scope.currentUser();
        expect(currentUser.uid).toEqual("fd");
        expect(currentUser.cn).toEqual("f r d");
        expect(currentUser.mails.length).toEqual(2);
        expect(currentUser.mails[0]).toEqual("f@h.x");
        expect(currentUser.mails[1]).toEqual("d@h.y");

        scope.init(); // <-- Note.

        expect(scope.addStep).toEqual("");
        expect(scope.sendToSelf).toBeFalse();
        expect(scope.error).toBeUndefined();
        recipients = scope.getRecipients();
        expect(recipients.length).toEqual(0);
        recipientsStr = scope.getRecipientsString();
        expect(recipientsStr).toEqual("");
        expect(scope.recipient).toEqual("");
        expect(scope.authentication).toBeTrue();
        expect(scope.error).toBeUndefined();
        expect(scope.addStep).toEqual("");

        const person0 = {name: "p", mails: ["s@t.u"], uid: "d"};
        expect(scope.isEmptyPerson(person0)).toEqual(false);
        expect(scope.isEmptyPerson(person0)).toBeFalse();

        let recipientToAdd = person0.uid;
        expect(scope.hasRecipient(recipientToAdd)).toBeFalse();
        expect(scope.isCurrentUserUid(recipientToAdd)).toBeFalse();
        expect(scope.isCurrentUserMail(recipientToAdd)).toBeFalse();
        expect(scope.addStep).toEqual("");

        const postUrlBase = "/filedrop/prepare/recipient/add";
        let postUrl = postUrlBase + "?recipient=" + recipientToAdd;
        $httpBackend.whenPOST(postUrl).respond(200, person0);
        $httpBackend.expectPOST(postUrl);

        scope.addRecipient(recipientToAdd);

        $httpBackend.flush();

        expect(scope.addStep).toEqual("_four_");
        expect(scope.recipient).toEqual("");
        recipientsStr = scope.getRecipientsString();
        expect(recipientsStr.length).toEqual(1);
        expect(recipientsStr).toEqual("d");
        recipients = scope.getRecipients();
        expect(recipients.length).toEqual(1);

        const expectedRecipient0 =
            scope.makeRecipient(person0.cn, person0.mails[0], person0.mails, person0.uid);
        expect(recipients[0]).toBeDefined();
        expect(recipients[0]).toEqual(expectedRecipient0);
        expect(recipients[0].name).toEqual(undefined); // Hmm.
        expect(recipients[0].mail).toEqual(expectedRecipient0.mail);
        expect(recipients[0].mails).toEqual(expectedRecipient0.mails);
        expect(recipients[0].uid).toEqual(expectedRecipient0.uid);

        const person1 = {name: "q", mails: ["w@y.z"], uid: "c"};
        expect(scope.isEmptyPerson(person0)).toEqual(false);
        expect(scope.isEmptyPerson(person0)).toBeFalse();

        recipientToAdd = person1.uid;
        expect(scope.hasRecipient(recipientToAdd)).toBeFalse();
        expect(scope.isCurrentUserUid(recipientToAdd)).toBeFalse();
        expect(scope.isCurrentUserMail(recipientToAdd)).toBeFalse();
        expect(scope.addStep).toEqual("_four_");

        postUrl = postUrlBase + "?recipient=" + recipientToAdd;
        $httpBackend.whenPOST(postUrl).respond(200, person1);
        $httpBackend.expectPOST(postUrl);

        scope.addRecipient(recipientToAdd);

        $httpBackend.flush();

        expect(scope.addStep).toEqual("_four_");
        const expectedRecipient1 =
            scope.makeRecipient(person1.cn, person1.mails[0], person1.mails, person1.uid);
        expect(recipients[1]).toBeDefined();
        expect(recipients[1]).toEqual(expectedRecipient1);
        expect(recipients[1].name).toEqual(undefined); // Hmm.
        expect(recipients[1].mail).toEqual(expectedRecipient1.mail);
        expect(recipients[1].mails).toEqual(expectedRecipient1.mails);
        expect(recipients[1].uid).toEqual(expectedRecipient1.uid);
        // Check that the previous one is still there.
        expect(recipients[0]).toBeDefined();
        expect(recipients[0]).toEqual(expectedRecipient0);
        expect(recipients[0].name).toEqual(undefined); // Hmm.
        expect(recipients[0].mail).toEqual(expectedRecipient0.mail);
        expect(recipients[0].mails).toEqual(expectedRecipient0.mails);
        expect(recipients[0].uid).toEqual(expectedRecipient0.uid);

        // Try re-adding the first person.
        recipientToAdd = person0.uid;
        expect(scope.hasRecipient(recipientToAdd)).toBeTrue();
        expect(person0.uid).not.toEqual(currentUser.uid);
        expect(scope.isCurrentUserUid(recipientToAdd)).toBeFalse();
        expect(scope.isCurrentUserMail(recipientToAdd)).toBeFalse();

        //
        // Don't need a mocked post for this add attempt
        // because it is handled before the POST happens.
        //

        scope.addRecipient(recipientToAdd);

        expect(scope.addStep).toEqual("_already_added_");
        expect(scope.recipient).toEqual("");
        expect(scope.error).toBeDefined();
        expect(scope.error.message).toEqual("Recipient is already added.");
        recipientsStr = scope.getRecipientsString();
        expect(recipientsStr.length).toEqual(3);
        expect(recipientsStr).toEqual("d,c");
        recipients = scope.getRecipients();
        expect(recipients.length).toEqual(2);

        expect(recipients[0]).toBeDefined();
        expect(recipients[0]).toEqual(expectedRecipient0);
        expect(recipients[0].name).toEqual(undefined); // Hmm.
        expect(recipients[0].mail).toEqual(expectedRecipient0.mail);
        expect(recipients[0].mails).toEqual(expectedRecipient0.mails);
        expect(recipients[0].uid).toEqual(expectedRecipient0.uid);

        // Try re-adding the second person using an email.
        recipientToAdd = person1.mails[0];
        expect(scope.hasRecipient(recipientToAdd)).toBeTrue();
        expect(person1.uid).not.toEqual(currentUser.uid);
        expect(scope.isCurrentUserUid(recipientToAdd)).toBeFalse();
        expect(scope.isCurrentUserMail(recipientToAdd)).toBeFalse();

        //
        // Don't need a mocked post for this add attempt
        // because it is handled before the POST happens.
        //

        scope.addRecipient(recipientToAdd);

        expect(scope.addStep).toEqual("_already_added_");
        expect(scope.recipient).toEqual("");
        expect(scope.error).toBeDefined();
        expect(scope.error.message).toEqual("Recipient is already added.");
        recipients = scope.getRecipients();
        expect(recipients.length).toEqual(2);
        recipientsStr = scope.getRecipientsString();
        expect(recipientsStr).toEqual("d,c");

        // Try adding the current user by uid.
        recipientToAdd = currentUser.uid;
        expect(scope.hasRecipient(recipientToAdd)).toBeFalse();
        expect(scope.isCurrentUserUid(recipientToAdd)).toBeTrue();
        expect(scope.isCurrentUserMail(recipientToAdd)).toBeFalse();

        //
        // Don't need a mocked post for this add attempt
        // because it is handled before the POST happens.
        //

        scope.addRecipient(recipientToAdd);

        expect(scope.addStep).toEqual("_two_");
        expect(scope.recipient).toEqual("");
        expect(scope.error).toBeUndefined();
        recipients = scope.getRecipients();
        expect(recipients.length).toEqual(3);
        recipientsStr = scope.getRecipientsString();
        expect(recipientsStr).toEqual("d,c,fd");

        expect(recipients[2]).toBeDefined();
        const expectedRecipient2 =
            scope.makeRecipient(currentUser.cn, currentUser.mails[0],
                currentUser.mails, currentUser.uid);

        expect(recipients[2]).toEqual(expectedRecipient2);
        expect(recipients[2].name).toEqual(expectedRecipient2.name);
        expect(currentUser.name).toBeUndefined(); // Note.
        expect(recipients[2].name).toEqual(currentUser.cn); // Note.
        expect(recipients[2].cn).toBeUndefined(); // Note.
        expect(expectedRecipient2.cn).toBeUndefined(); // Note.
        expect(recipients[2].mail).toEqual(expectedRecipient2.mail);
        expect(recipients[2].mail).toEqual(currentUser.mail);
        expect(recipients[2].uid).toEqual(expectedRecipient2.uid);
        expect(recipients[2].uid).toEqual(currentUser.uid);

        // Try re-adding the current user by mail.
        recipientToAdd = currentUser.mails[0];
        expect(scope.hasRecipient(recipientToAdd)).toBeTrue();
        expect(scope.isCurrentUserUid(recipientToAdd)).toBeFalse();
        expect(scope.isCurrentUserMail(recipientToAdd)).toBeTrue();

        //
        // Don't need a mocked post for this add attempt
        // because it is handled before the POST happens.
        //

        scope.addRecipient(recipientToAdd);

        expect(scope.addStep).toEqual("_already_added_");
        expect(scope.recipient).toEqual("");
        expect(scope.error).toBeDefined();
        expect(scope.error.message).toEqual("Recipient is already added.");
        recipientsStr = scope.getRecipientsString();
        recipients = scope.getRecipients();
        expect(recipients.length).toEqual(3);
        expect(recipientsStr).toEqual("d,c,fd");

        // Try re-adding the current user by mail.
        recipientToAdd = currentUser.mails[1];
        expect(scope.hasRecipient(recipientToAdd)).toBeTrue();
        expect(scope.isCurrentUserUid(recipientToAdd)).toBeFalse();
        expect(scope.isCurrentUserMail(recipientToAdd)).toBeTrue();

        //
        // Don't need a mocked post for this add attempt
        // because it is handled before the POST happens.
        //

        scope.addRecipient(recipientToAdd);

        expect(scope.addStep).toEqual("_already_added_");
        expect(scope.recipient).toEqual("");
        expect(scope.error).toBeDefined();
        expect(scope.error.message).toEqual("Recipient is already added.");
        recipientsStr = scope.getRecipientsString();
        recipients = scope.getRecipients();
        expect(recipients.length).toEqual(3);
        expect(recipientsStr).toEqual("d,c,fd");

        // if ("off" === "") {
        //     throw new Error("STOP test: ["
        //     + JSON.stringify(expectedRecipient2) + "] == ["
        //     + JSON.stringify(recipients[2]) + "]");
        // }
    });
});

