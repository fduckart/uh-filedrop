describe("RoleJsController", function() {

    let scope;
    let dataProvider;
    let controller;

    beforeEach(module('filedropApp'));

    beforeEach(inject(function($rootScope, _dataProvider_, $controller) {
        scope = $rootScope.$new();
        dataProvider = _dataProvider_;
        controller = $controller('RoleJsController', {
            $scope: scope,
            dataProvider: _dataProvider_
        });
    }));

    it("construction", function() {
        expect(controller).toBeDefined();
        expect(scope.roles).toBeDefined();
        expect(scope.roles).toEqual([]);
    });

    it("init", function() {
        spyOn(scope, "loadData").and.callFake(function() {
            scope.roles.push({
                "id": 1,
                "role": "NON_UH",
                "description": "NonUH"
            });
            scope.roles.push({
                "id": 13,
                "role": "ADMINISTRATOR",
                "description": "Administrator"
            });
            scope.roles.push({
                "id": 14,
                "role": "SUPER_USER",
                "description": "Superuser"
            });
        });

        expect(controller).toBeDefined();
        expect(scope.roles).toBeDefined();
        expect(scope.roles.length).toEqual(0);

        // What we are testing:
        scope.init();

        expect(scope.loadData).toHaveBeenCalled();
        expect(scope.roles).toBeDefined();
        expect(scope.roles.length).toEqual(3);

        expect(scope.roles[0].id).toEqual(1);
        expect(scope.roles[0].role).toEqual("NON_UH");
        expect(scope.roles[0].description).toEqual("NonUH");

        expect(scope.roles[1].id).toEqual(13);
        expect(scope.roles[1].role).toEqual("ADMINISTRATOR");
        expect(scope.roles[1].description).toEqual("Administrator");

        expect(scope.roles[2].id).toEqual(14);
        expect(scope.roles[2].role).toEqual("SUPER_USER");
        expect(scope.roles[2].description).toEqual("Superuser");
    });

});
