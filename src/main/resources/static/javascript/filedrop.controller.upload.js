function UploadJsController($scope, Upload, $window) {
    $scope.init = function (maxUploadSize) {
        $scope.files = [];
        $scope.uploadSize = 0;
        $scope.maxUploadSize = maxUploadSize;
    };

    $scope.submit = function () {
        if ($scope.files && $scope.files.length) {
            Upload.upload({
                url: "/filedrop/prepare/files",
                data: {
                    files: $scope.files
                },
                arrayKey: ""
            })
                  .then(function () {
                      $window.location.href = "/filedrop";
                  });
        }
    };

    $scope.addFiles = function (files) {
        $scope.files = $scope.files.concat(files);
        angular.forEach(files, function (file) {
            $scope.uploadSize += file.size;
        });
    };

    $scope.removeFile = function (file) {
        var index = $scope.files.indexOf(file);
        if (index > -1) {
            $scope.files.splice(index, 1);
            $scope.uploadSize -= file.size;
        }
    };

    $scope.isUploadLarge = function () {
        return $scope.uploadSize > $scope.maxUploadSize;
    };
}

filedropApp.controller("UploadJsController", UploadJsController);