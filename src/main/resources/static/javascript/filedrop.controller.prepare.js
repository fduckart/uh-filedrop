function PrepareJsController($scope, dataProvider) {
    $scope.init = function(sender) {
        $scope.sender = sender;
        $scope.recipients = [];
    };

    $scope.addRecipient = function() {
        if (/^\s*$/.test($scope.recipient) || $scope.recipient === undefined || $scope.recipients.indexOf($scope.recipient) > -1) {
            return;
        }
        if ($scope.recipient.indexOf("@") > -1 && $scope.recipient.split("@")[1] !== "hawaii.edu") {
            $scope.recipients.push($scope.recipient);
        }
        dataProvider.loadData(function(data) {
            if (data.cn) {
                $scope.recipients.push(data.cn);
            }
        }, "/filedrop/api/ldap/" + $scope.recipient);
        $scope.recipient = "";
    };

    $scope.removeRecipient = function(recipient) {
        var index = $scope.recipients.indexOf(recipient);
        if (index > -1) {
            $scope.recipients.splice(index, 1);
        }
    };

    $scope.getRecipients = function() {
        return $scope.recipients.join(",");
    };
}

filedropApp.controller("PrepareJsController", PrepareJsController);