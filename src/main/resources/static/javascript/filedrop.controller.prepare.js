function PrepareJsController($scope, dataProvider, $http) {
    $scope.init = function(sender, recipients, expiration, authentication) {
        $scope.recipient = "";
        $scope.sender = sender;
        $scope.recipients = [];
        $scope.authentication = true;
        $scope.senderEmails = [];
        $scope.expiration = "7200";

        if (recipients !== "null") {
            let recipientsSub = recipients.substring(1, recipients.length - 1)
                                          .split(",");
            for (let r of recipientsSub) {
                $scope.addRecipient(r);
            }
        }

        if (expiration !== "null") {
            $scope.expiration = expiration;
        }

        if (authentication !== null) {
            $scope.authentication = authentication;
        }

        $http({
            method: "GET",
            url: "/filedrop/api/ldap/" + $scope.sender
        })
        .then((response) => {
            response.data.mails.map((mail) => $scope.senderEmails.push({ display: mail, value: mail }));
        });
    };

    $scope.addRecipient = function (recipient) {
        if (/^\s*$/.test(recipient) || recipient === undefined || $scope.hasRecipient(recipient)) {
            return;
        }

        dataProvider.loadData(function (response) {
            let data = response.data;
            if (data.cn) {
                $scope.recipients.push({ name: data.cn, mail: data.mails[0] });
            } else if (recipient.indexOf("@") > -1) {
                if ($scope.authentication) {
                    $scope.showPopup();
                } else {
                    $scope.recipients.push({ name: recipient });
                }
            }
        }, "/filedrop/api/ldap/" + recipient);

        $scope.recipient = "";
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
            recipients.push(recipient.mail ? recipient.mail : recipient.name);
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

    $scope.disabled = () => $scope.recipient.length > 0;
}

filedropApp.controller("PrepareJsController", PrepareJsController);