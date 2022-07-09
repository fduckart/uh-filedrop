function HistoryJsController($scope, dataProvider, $window) {

    $scope.init = () => {
        $scope.fileDrops = [];
        $scope.loading = true;
        $scope.loadData();
        $scope.loading = false;
    };

    $scope.loadData = () => {
        dataProvider.loadData(function(response) {
            $scope.fileDrops = response.data;
            $scope.fileDrops.forEach((fileDrop) => {
                const createdDate = new Date(fileDrop.created);
                const expiredDate = new Date(fileDrop.expiration);
                fileDrop.created = `${createdDate.getMonth() + 1}/${createdDate.getDate()}/${createdDate.getFullYear()}`;
                fileDrop.expiration = `${expiredDate.getMonth() + 1}/${expiredDate.getDate()}/${expiredDate.getFullYear()}`;
            });
        }, "/filedrop/api/filedrops");
    };

    $scope.currentUser = () => $window.user;

    $scope.isRecipient = (fileDrop) => fileDrop.uploader !== $scope.currentUser();
}

filedropApp.controller("HistoryJsController", HistoryJsController);

function HistoryCardController($scope) {
    $scope.isCollapsed = true;
}

filedropApp.controller("HistoryCardController", HistoryCardController);