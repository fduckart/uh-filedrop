function PrepareJsController($scope, dataProvider) {
    $scope.init = function (sender, helpdesk, recipients, expiration, authentication) {
        $scope.recipient = "";
        $scope.sender = sender;
        $scope.recipients = [];
        $scope.authentication = true;
        $scope.senderEmails = [];
        $scope.expiration = "7200";

        if (helpdesk) {
            $scope.recipient = "help@hawaii.edu";
            $scope.addRecipient($scope.recipient);
        }

        if (recipients !== "null") {
            let recipientsSub = recipients.substring(1, recipients.length - 1).split(",");
            for(let r of recipientsSub) {
                $scope.addRecipient(r);
            }
        }

        if(expiration !== "null") {
            $scope.expiration = expiration;
        }

        if (authentication !== null) {
            $scope.authentication = authentication;
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
            } else if(recipient.indexOf("@") > -1) {
                if($scope.authentication) {
                    $scope.showPopup();
                } else {
                    $scope.recipients.push({ name: recipient });
                }
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
        let recipients = [];
        $scope.recipients.forEach(function (recipient) {
            recipients.push(recipient.uid ? recipient.uid : recipient.name);
        });
        return recipients.join(",");
    };

    $scope.hasRecipient = function (recipient) {
        return $scope.recipients.includes($scope.recipients.find(function (r) {
            return (r.uid ? r.uid.toUpperCase() === recipient.toUpperCase() : false) || r.name.toUpperCase() === recipient.toUpperCase();
        }));
    };

    $scope.userHasMultipleEmails = function() {
        return $scope.senderEmails.length > 1;
    };

    $scope.showPopup = function() {
        $("#prepareModal")
        .modal();
    };

    $scope.disabled = () => $scope.recipient.length > 0 && $scope.recipients.length === 0;
}

filedropApp.controller("PrepareJsController", PrepareJsController);