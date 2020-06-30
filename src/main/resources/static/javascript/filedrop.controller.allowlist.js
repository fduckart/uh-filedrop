const URL_LOAD = "/filedrop/api/admin/allowlist";

function AllowlistJsController($scope, dataProvider, $uibModal, $http) {

    $scope.init = function () {
        $scope.allowlist = [];
        $scope.loadData();
    };

    $scope.loadData = function () {
        dataProvider.loadData(function (response) {
            $scope.allowlist = response.data;
        }, URL_LOAD);
    };

    $scope.openAddModal = function () {
       let modalInstance = $uibModal.open({
           templateUrl: "allowlistAddModal.html",
           controller: "AllowlistAddModalController",
        });

       modalInstance.result.then((allowlist) => {
            $scope.addAllowlist(allowlist.entry, allowlist.registrant);
       });
    };

    $scope.addAllowlist = function (entry, registrant) {
        dataProvider.saveData(function (response) {
            $scope.allowlist.push(response.data);
        }, URL_LOAD, { entry: entry, registrant: registrant })
    };

    $scope.getEntryName = function (allowlist) {
        return allowlist.entryName ? allowlist.entryName : allowlist.entry;
    };

    $scope.getRegistrantName = function (allowlist) {
        return allowlist.registrantName ? allowlist.registrantName : allowlist.registrant;
    };

    $scope.openDeleteModal = function(allowlist) {
        let modalInstance = $uibModal.open({
            templateUrl: "allowlistDeleteModal.html",
            controller: "AllowlistDeleteModalController",
            resolve: {
                allowlist: function() {
                    return allowlist;
                }
            },
        });

        modalInstance.result.then((allowlist) => {
            $scope.deleteAllowlist(allowlist);
        });
    };

    $scope.deleteAllowlist = function (allowlist) {
        dataProvider.delData(function () {
            let index = $scope.allowlist.indexOf(allowlist);
            if (index > -1) {
                $scope.allowlist.splice(index, 1);
            }
        }, URL_LOAD + "/" + allowlist.id);
    };
}

filedropApp.controller("AllowlistJsController", AllowlistJsController);

function AllowlistAddModalController($scope, $uibModalInstance) {
    $scope.ok = function() {
        $uibModalInstance.close({ entry: $scope.entry, registrant: $scope.registrant });
    };

    $scope.cancel = function () {
        $uibModalInstance.dismiss('cancel');
    }
}

filedropApp.controller("AllowlistAddModalController", AllowlistAddModalController);

function AllowlistDeleteModalController($scope, $uibModalInstance, allowlist) {
    $scope.ok = function() {
        $uibModalInstance.close(allowlist);
    };

    $scope.cancel = function () {
        $uibModalInstance.dismiss('cancel');
    }
}

filedropApp.controller("AllowlistDeleteModalController", AllowlistDeleteModalController);