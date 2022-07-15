describe("PrepareJsController", function() {

    beforeEach(module('filedropApp', function($provide) {
        $provide.service('$window', function() {
            this.user = jasmine.createSpy('user');
        });
    }));

    let scope;
    let controller;
    let window;
    let dataProvider;
    let user;

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

    it("checkInitFunction", function() {
        spyOn(scope, 'getFileDrop').and
            .callFake(function() {
                return {
                    authentication: true,
                    sender: "duckart@hawaii.edu"
                }
            });

        spyOn(scope, 'currentUser').and
            .callFake(function() {
                return {
                    mails: ["duckart@hawaii.edu", "frank.duckart@hawaii.edu"]
                }
            });

        expect(controller).toBeDefined();
        expect(scope.recipient).not.toBeDefined();
        expect(scope.recipients).not.toBeDefined();
        expect(scope.sendToSelf).not.toBeDefined();
        expect(scope.authentication).not.toBeDefined();
        expect(scope.expiration).not.toBeDefined();
        expect(scope.message).not.toBeDefined();

        // What we are testing:
        scope.init();

        expect(scope.recipient).toBeDefined();
        expect(scope.recipients).toBeDefined();
        expect(scope.recipients.length).toEqual(0);
        expect(scope.sendToSelf).toBeDefined();
        expect(scope.sendToSelf).toEqual(false);
        expect(scope.authentication).toBeDefined();
        expect(scope.authentication).toEqual(true);
        expect(scope.expiration).toBeDefined();
        expect(scope.expiration).toEqual("7200");
        expect(scope.message).toBeDefined();
        expect(scope.message).toEqual("");
        expect(scope.sender).toBeDefined();
        expect(scope.sender.mails).toBeDefined();
    });

});
