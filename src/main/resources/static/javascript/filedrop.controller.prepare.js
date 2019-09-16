function PrepareJsController($scope, dataProvider) {
    $scope.init = function(sender, helpdesk) {
        $scope.sender = sender;
        $scope.recipients = [];
        if (helpdesk) {
            $scope.recipient = "help@hawaii.edu";
            $scope.addRecipient();
        }
    };

    $scope.addRecipient = function() {
        if (/^\s*$/.test($scope.recipient) || $scope.recipient === undefined || $scope.recipients.indexOf($scope.recipient) > -1) {
            return;
        }
        if ($scope.recipient.indexOf("@") > -1 && $scope.recipient.split("@")[1] !== "hawaii.edu") {
            $scope.recipients.push({ name: $scope.recipient });
            $scope.recipient = "";
            return;
        }
        dataProvider.loadData(function(data) {
            if (data.cn) {
                $scope.recipients.push({ name: data.cn, uid: data.uid });
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
        var recipients = [];
        for (var i = 0; i < $scope.recipients.length; i++) {
            recipients.push($scope.recipients[i].uid);
        }
        return recipients.join(",");
    };
}

filedropApp.controller("PrepareJsController", PrepareJsController);