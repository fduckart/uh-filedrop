function UploadJsController($scope, Upload, $window) {
    $scope.init = function (maxUploadSize, downloadKey) {
        $scope.files = [];
        $scope.uploadSize = 0;
        $scope.maxUploadSize = maxUploadSize;
        $scope.downloadKey = downloadKey;
    };

    $scope.submit = function () {
        if ($scope.files && $scope.files.length) {
            let count = 0;
            $scope.files.map(file => {
                Upload.upload({
                    url: "/filedrop/prepare/files/",
                    data: {
                        comment: file.comment,
                        file
                    },
                    arrayKey: ""
                })
                      .then(() => {
                          count++;
                          if (count === $scope.files.length) {
                              $window.location.href = "/filedrop/complete/" + $scope.downloadKey;
                          }
                      });
            });
        }
    };

    $scope.addFiles = function (files) {
        $scope.files = $scope.files.concat(files);
        angular.forEach(files, function (file) {
            file.comment = "";
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