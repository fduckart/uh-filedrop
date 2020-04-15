function UploadJsController($scope, Upload, $window) {
    $scope.init = function() {
        $scope.files = [];
        $scope.uploadSize = 0;
        $scope.maxUploadSize = $window.maxUploadSize;
        $scope.uploadKey = $window.uploadKey;
        $scope.progress = 0;
    };

    $scope.submit = function () {
        if ($scope.files && $scope.files.length) {
            let count = 0;
            $scope.files.map((file) => {
                Upload.upload({
                    url: "/filedrop/prepare/files/" + $scope.uploadKey,
                    data: {
                        comment: file.comment,
                        file
                    },
                    arrayKey: ""
                })
                      .success(() => {
                          count++;
                          $scope.progress = 100 * (count / $scope.files.length);
                          if (count === $scope.files.length) {
                              $window.location.href = "/filedrop/complete/" + $scope.uploadKey;
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