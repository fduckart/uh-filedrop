function WhitelistJsController($scope, dataProvider, $uibModal) {
    let URL_LOAD = "/filedrop/api/admin/whitelist";

    $scope.init = function () {
        $scope.whitelist = [];
        $scope.loadData();
    };

    $scope.loadData = function () {
        dataProvider.loadData(function (response) {
            $scope.whitelist = response.data;
        }, URL_LOAD);
    };

    $scope.openAddModal = function () {
        $uibModal.open({
            templateUrl: "whitelistAddModal.html"
        });
    };

    $scope.submit = function () {
        $("#whitelistModal").modal("hide");
    };

    $scope.getEntryName = function (whitelist) {
        return whitelist.entryName ? whitelist.entryName : whitelist.entry;
    };

    $scope.getRegistrantName = function (whitelist) {
        return whitelist.registrantName ? whitelist.registrantName : whitelist.registrant;
    };

    $scope.openDeleteModal = function(whitelist) {
        let modalInstance = $uibModal.open({
            templateUrl: "whitelistDeleteModal.html",
            controller: "WhitelistDeleteModalController",
            resolve: {
                whitelist: function() {
                    return whitelist;
                }
            },
        });

        modalInstance.result.then((whitelist) => {
            $scope.deleteWhitelist(whitelist);
        });
    };

    $scope.deleteWhitelist = function (whitelist) {
        dataProvider.delData(function () {
            let index = $scope.whitelist.indexOf(whitelist);
            if (index > -1) {
                $scope.whitelist.splice(index, 1);
            }
        }, URL_LOAD + "/" + whitelist.id);
    };
}

filedropApp.controller("WhitelistJsController", WhitelistJsController);

function WhitelistDeleteModalController($scope, $uibModalInstance, whitelist) {
    $scope.ok = function() {
        $uibModalInstance.close(whitelist);
    };

    $scope.cancel = function () {
        $uibModalInstance.dismiss('cancel');
    }
}

filedropApp.controller("WhitelistDeleteModalController", WhitelistDeleteModalController);