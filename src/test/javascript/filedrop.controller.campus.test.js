describe("CampusJsController", function() {

    beforeEach(module('filedropApp'));
    const test = 'test';
    let scope;
    let controller;
    let dataProvider;
    let log;

    beforeEach(inject(function($rootScope, $controller, $logScope, dataProvider) {
        scope = $rootScope.$new();
        log = $logScope.$new();
        controller = $controller('CampusJsController', {
            $scope: scope,
            dataProvider: dataProvider
        });
    }));

    it("checkInitFunction", function() {
        spyOn(dataProvider, "loadData").withArgs(null, null).and.callFake(function() {
            scope.campuses.push({
                "id": 1,
                "code": "HA",
                "description": "Hawaii Community College"
            });
            scope.campuses.push({
                "id": 10,
                "code": "WO",
                "description": "UH West Oahu"
            });
        });

        expect(controller).toBeDefined();
        expect(scope.campuses).toBeDefined();
        expect(scope.campuses.length).toEqual(0);

        // What we are testing:
        scope.init();

        expect(scope.loadData).toHaveBeenCalled();
        expect(scope.campuses).toBeDefined();
        expect(scope.campuses.length).toEqual(2);

        expect(scope.campuses[0].id).toEqual(1);
        expect(scope.campuses[0].code).toEqual("HA");
        expect(scope.campuses[0].description).toEqual("Hawaii Community College");

        expect(scope.campuses[1].id).toEqual(10);
        expect(scope.campuses[1].code).toEqual("WO");
        expect(scope.campuses[1].description).toEqual("UH West Oahu");
    });

});
