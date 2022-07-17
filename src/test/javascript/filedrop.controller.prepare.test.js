describe("PrepareJsController", function() {

    beforeEach(module('filedropApp', function($provide) {
        $provide.service('$window', function() {
            this.user = {
                uid: "fd",
                cn: "f r d",
                mails: ["f@h.x", "d@h.y"]
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

    beforeEach(inject(function($rootScope, $controller, _$window_, _dataProvider_) {
        scope = $rootScope.$new();
        window = _$window_;
        dataProvider = _dataProvider_;
        controller = $controller('PrepareJsController', {
            $scope: scope,
            dataProvider: _dataProvider_,
            $window: _$window_
        });
    }));

    it("checkBasics", function() {
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
        let user = scope.getCurrentUser();
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
        expect(fileDrop.authentication).toEqual(true);
        expect(fileDrop.message).toEqual("What is the what?");

        // Not directly, but via the mock.
        expect(scope.sender).toBeDefined();
        expect(scope.sender.mails).toBeDefined();
        expect(scope.sender.mails.length).toEqual(user.mails.length);
        expect(scope.sender.mails[0]).toEqual("f@h.x");
        expect(scope.sender.mails[1]).toEqual("d@h.y");
    });

    it("checkInitFunction", function() {
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
        expect(scope.sendToSelf).toEqual(true); // See mock.
        expect(scope.authentication).toBeDefined();
        expect(scope.authentication).toEqual(true);
        expect(scope.getFileDrop().authentication).toEqual(true); // See mock.
        expect(scope.expiration).toBeDefined();

        let fileDrop = scope.getFileDrop();
        expect(scope.expiration).toEqual(fileDrop.expiration);
        expect(scope.message).toBeDefined();
        expect(scope.message).toEqual(fileDrop.message);
        expect(scope.sender).toBeDefined();

        let user = scope.getCurrentUser();
        expect(scope.sender.mails).toBeDefined();
        expect(scope.sender.mails.length).toEqual(user.mails.length);
        expect(scope.sender.mails[0]).toEqual(user.mails[0]);
        expect(scope.sender.mails[1]).toEqual(user.mails[1]);

        expect(scope.sender.model).toBeDefined();
        expect(scope.sender.model).toEqual(fileDrop.sender);
        expect(scope.sender.model).toEqual(scope.sender.mails[1]);

        scope.sender.model = null;
        expect(scope.sender.model).toBeDefined();
        expect(scope.sender.model).toEqual(null);

    });

});
