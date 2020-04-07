function DashboardJsController($scope, dataProvider) {

    $scope.init = () => {
        $scope.fileDrops = [];
        $scope.loadData();
    };

    $scope.loadData = () => {
        dataProvider.loadData(function(response) {
            $scope.fileDrops = response.data;
        }, "/filedrop/api/admin/filedrops");
    };
}

filedropApp.controller("DashboardJsController", DashboardJsController);