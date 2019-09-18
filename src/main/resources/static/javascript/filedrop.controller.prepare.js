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
        if (/^\s*$/.test($scope.recipient) || $scope.recipient === undefined || $scope.hasRecipient($scope.recipient)) {
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
        $scope.recipients.forEach(function(recipient) {
            recipients.push(recipient.uid ? recipient.uid : recipient.name);
        });
        return recipients.join(",");
    };

    $scope.hasRecipient = function(recipient) {
        return $scope.recipients.includes($scope.recipients.find(function (r) { return (r.uid === recipient) || (r.name === recipient) }));
    }
}

filedropApp.controller("PrepareJsController", PrepareJsController);