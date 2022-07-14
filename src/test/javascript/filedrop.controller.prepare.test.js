describe("PrepareJsController", function() {

    beforeEach(module('filedropApp'));
    let scope;
    let controller;
    let dataProvider;

    beforeEach(inject(function($rootScope, $controller, _dataProvider_) {
        scope = $rootScope.$new();
        dataProvider = _dataProvider_;
        controller = $controller('PrepareJsController', {
            $scope: scope,
            dataProvider: _dataProvider_
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

        expect(controller).toBeDefined();
        // expect(scope.recipient).toBeDefined();
        // expect(scope.recipients.length).toEqual(0);
        // expect(scope.sendToSelf).toBeDefined();

        // What we are testing:
        scope.init();

        expect(scope.recipient).toBeDefined();
        expect(scope.recipients).toBeDefined();
        expect(scope.recipients.length).toEqual(0);
        expect(scope.sendToSelf).toBeDefined();
        expect(scope.sendToSelf).toEqual(false);
    });

});
