function CampusJsController($scope, dataProvider) {
    const URL_CAMPUS_LOAD = "/filedrop/api/campuses";
    $scope.campuses = [];

    $scope.init = function() {
        $scope.loadData();
    };

    $scope.loadData = function() {
        dataProvider.loadData(function(response) {
            $scope.campuses = response.data;
        }, URL_CAMPUS_LOAD);
    };
}
filedropApp.controller("CampusJsController", CampusJsController);