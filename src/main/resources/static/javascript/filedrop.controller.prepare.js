function PrepareJsController($scope, dataProvider) {
    $scope.init = function (sender, helpdesk) {
        $scope.sender = sender;
        $scope.recipients = [];
        $scope.authentication = true;
        $scope.senderEmails = [];

        if (helpdesk) {
            $scope.recipient = "help@hawaii.edu";
            $scope.addRecipient();
        }

        dataProvider.loadData(function(response) {
           let data = response.data;
           $scope.senderEmails = data.mails;
        }, "/filedrop/api/ldap/" + sender);
    };

    $scope.addRecipient = function (recipient) {
        if (/^\s*$/.test(recipient) || recipient === undefined || $scope.hasRecipient(recipient)) {
            return;
        }

        dataProvider.loadData(function (response) {
            let data = response.data;
            if (data.cn) {
                $scope.recipients.push({ name: data.cn, uid: data.uid });
            } else {
                $scope.authentication = false;
                $scope.recipients.push({ name: recipient });
            }
        }, "/filedrop/api/ldap/" + recipient);

        $scope.recipient = '';
    };

    $scope.removeRecipient = function (recipient) {
        let index = $scope.recipients.indexOf(recipient);
        if (index > -1) {
            $scope.recipients.splice(index, 1);
        }
    };

    $scope.getRecipients = function () {
        var recipients = [];
        $scope.recipients.forEach(function (recipient) {
            recipients.push(recipient.uid ? recipient.uid : recipient.name);
        });
        return recipients.join(",");
    };

    $scope.hasRecipient = function (recipient) {
        return $scope.recipients.includes($scope.recipients.find(function (r) {
            return r.uid === recipient || r.name === recipient;
        }));
    };

    $scope.userHasMultipleEmails = function() {
        return $scope.senderEmails.length >= 2;
    };
}

filedropApp.controller("PrepareJsController", PrepareJsController);