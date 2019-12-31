function UploadHelpdeskJsController($scope, Upload, $window) {
    $scope.init = function(maxUploadSize, downloadKey, expiration) {
        $scope.files = [];
        $scope.uploadSize = 0;
        $scope.maxUploadSize = maxUploadSize;
        $scope.downloadKey = downloadKey;
        $scope.expiration = expiration;
    };

    $scope.submit = function() {
        if ($scope.files && $scope.files.length) {
            for (let i = 0; i < $scope.files.length; i++) {
                Upload.upload({
                    url: "/filedrop/helpdesk/files/" + $scope.downloadKey,
                    data: {
                        comment: $scope.files[i].comment,
                        file: $scope.files[i],
                        expiration: $scope.expiration
                    },
                    arrayKey: ""
                })
                      .then(function() {
                          if (i === ($scope.files.length - 1)) {
                              $window.location.href = "/filedrop/helpdesk/successful";
                          }
                      });
            }
        }
    };

    $scope.addFiles = function(files) {
        $scope.files = $scope.files.concat(files);
        angular.forEach(files, function(file) {
            file.comment = "";
            $scope.uploadSize += file.size;
        });
    };

    $scope.removeFile = function(file) {
        var index = $scope.files.indexOf(file);
        if (index > -1) {
            $scope.files.splice(index, 1);
            $scope.uploadSize -= file.size;
        }
    };

    $scope.isUploadLarge = function() {
        return $scope.uploadSize > $scope.maxUploadSize;
    };
}

filedropApp.controller("UploadHelpdeskJsController", UploadHelpdeskJsController);