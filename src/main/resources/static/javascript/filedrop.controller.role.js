(function() {

    function RoleJsController($scope, dataProvider) {
        var URL_LOAD = "/filedrop/api/roles";
        $scope.roles = [];

        $scope.init = function() {
            $scope.loadData();
        };

        $scope.loadData = function() {
            dataProvider.loadData(function(data) {
                $scope.roles = data;
            }, URL_LOAD);
        }
    }
    filedropApp.controller("RoleJsController", RoleJsController);

})();
