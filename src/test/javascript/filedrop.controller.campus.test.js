describe("CampusJsController", function() {

    beforeEach(module('filedropApp'));
    const test = 'test';
    const URL_CAMPUS_LOAD = "/filedrop/api/campuses";
    let scope;
    let controller;
    let dataProvider;

    beforeEach(module(function($provide) {

        const response = {
            data: ["what", "is", "the", "what?"]
        };

        const callback = {
            data: ["what", "is", "the", "what?"],
            response: {}
        };
        callback.response = response;

        function whatup = function() {
            response: {
                data: 
            }
        }

        $provide.value('dataProvider', {
            testme: function() {
                // Empty.
            },
            loadData2: function(callback, url) {
                let values = [
                    {
                        "id": 1,
                        "code": "HA",
                        "description": "Hawaii Community College"
                    },
                    {
                        "id": 10,
                        "code": "WO",
                        "description": "UH West Oahu"
                    }];
                callback.response.data = values;
            },
            loadData: function(success, url) {
                success = callback;

            }
            /*
            dataProvider.loadData(function(response) {
                $scope.campuses = response.data;
            }, URL_CAMPUS_LOAD);
             */
        });
    }));

    beforeEach(inject(function($rootScope, $controller, _dataProvider_) {
        scope = $rootScope.$new();
        dataProvider = _dataProvider_;
        controller = $controller('CampusJsController', {
            $scope: scope,
            dataProvider: _dataProvider_,
        });
    }));

    it("nothing", function() {
        spyOn(dataProvider, "testme").and.callThrough();
        dataProvider.testme();
        expect(dataProvider.testme).toHaveBeenCalled();

        const response = {
            data: ["what", "is", "the", "what?"]
        };
        expect(response.data.length).toEqual(4);

        const callback = {
            response: function() {
                return ["what", "is", "the", "what?"];
            }
        };
        expect(callback.response().length).toEqual(4);

        const person = {
            firstName: "John",
            lastName: "Doe",
            id: 5566,
            fullName: function() {
                return this.firstName + " " + this.lastName;
            },
            what: function() {
                let data = ["what", "is", "the", "what?"];
                return data;
            }
        };
        expect(person.fullName()).toEqual("John Doe");
        expect(person.what()).toEqual(["what", "is", "the", "what?"]);
        expect(person.what().length).toEqual(4);

        // spyOn(dataProvider, "loadData").and.callThrough();
        // dataProvider.loadData();
        // dataProvider.loadData(function(response) {
        //     $scope.campuses = response.data;
        // }, URL_CAMPUS_LOAD);
        // expect(dataProvider.loadData).toHaveBeenCalled();

        expect(controller).toBeDefined();
        expect(scope.campuses).toBeDefined();
        expect(scope.campuses.length).toEqual(0);

        spyOn(dataProvider, "loadData").and.callThrough();

        // What we are testing:
        scope.init();

        expect(dataProvider.loadData).toHaveBeenCalled();

        expect(scope.campuses).toBeDefined();
        expect(scope.campuses.length).toEqual(22);

    });

    // it("checkInitFunction", function() {
    //     let response = [
    //         {
    //             "id": 1,
    //             "code": "HA",
    //             "description": "Hawaii Community College"
    //         },
    //         {
    //             "id": 10,
    //             "code": "WO",
    //             "description": "UH West Oahu"
    //         }];
    //     let callback = {response};
    //
    //     spyOn(dataProvider, "loadData")
    //         .withArgs(callback, URL_CAMPUS_LOAD)
    //         .and.returnValue(callback);
    //
    //     /*
    //     dataProvider.loadData(function(response) {
    //         $scope.campuses = response.data;
    //     }, URL_CAMPUS_LOAD);
    //     */
    //
    //     /*
    //     spyOn(dataProvider, "loadData").callFake(function( ) {
    //         scope.campuses.push({
    //             "id": 1,
    //             "code": "HA",
    //             "description": "Hawaii Community College"
    //         });
    //         scope.campuses.push({
    //             "id": 10,
    //             "code": "WO",
    //             "description": "UH West Oahu"
    //         });
    //     });
    //      */
    //
    //     expect(controller).toBeDefined();
    //     expect(scope.campuses).toBeDefined();
    //     expect(scope.campuses.length).toEqual(0);
    //
    //     // What we are testing:
    //     scope.init();
    //
    //     expect(scope.loadData).toHaveBeenCalled();
    //     expect(scope.campuses).toBeDefined();
    //     expect(scope.campuses.length).toEqual(2);
    //
    //     expect(scope.campuses[0].id).toEqual(1);
    //     expect(scope.campuses[0].code).toEqual("HA");
    //     expect(scope.campuses[0].description).toEqual("Hawaii Community College");
    //
    //     expect(scope.campuses[1].id).toEqual(10);
    //     expect(scope.campuses[1].code).toEqual("WO");
    //     expect(scope.campuses[1].description).toEqual("UH West Oahu");
    // });

});
