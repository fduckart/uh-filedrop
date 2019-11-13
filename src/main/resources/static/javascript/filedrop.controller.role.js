function RoleJsController($scope, dataProvider) {
        let URL_LOAD = "/filedrop/api/roles";
        $scope.roles = [];

        $scope.init = function () {
            $scope.loadData();
        };

        $scope.loadData = function () {
            dataProvider.loadData(function (response) {
                $scope.roles = response.data;
            }, URL_LOAD);
        };
}
filedropApp.controller("RoleJsController", RoleJsController);