function UploadHelpdeskJsController($scope, Upload, $window) {
    $scope.init = function() {
        $scope.files = [];
        $scope.uploadSize = 0;
        $scope.maxUploadSize = $window.maxUploadSize;
        $scope.uploadKey = $window.uploadKey;
        $scope.expiration = $window.expiration;
        $scope.ticketNumber = $window.ticketNumber;
        $scope.progress = 0;
        $scope.disableUpload = false;
    };

    $scope.submit = function() {
        if ($scope.files && $scope.files.length) {
            $scope.disableUpload = true;
            let count = 0;
            $scope.files.map((file) => {
                Upload.upload({
                    url: "/filedrop/helpdesk/files/" + $scope.uploadKey,
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
                            $window.location.href = "/filedrop/helpdesk/successful/"
                                + $scope.uploadKey
                                + "?expiration=" + $scope.expiration
                                + "&ticketNumber=" + $scope.ticketNumber;
                        }
                    });
            });
        }
    };

    $scope.addFiles = function(files) {
        console.log("UploadHelpdeskJsController#addFiles; TYPEX: " + (typeof files));
        console.log("UploadHelpdeskJsController#addFiles; FILES: ", files);
        $scope.files = $scope.files.concat(files);
        $scope.files.forEach(function(file) {
            file.comment = "";
            $scope.uploadSize += file.size;
        });
        console.log("UploadHelpdeskJsController#addFiles; FINISHED ");
    };

    $scope.removeFile = function(file) {
        let index = $scope.files.indexOf(file);
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
