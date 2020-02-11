function PrepareJsController($scope, dataProvider, $http, $window) {
    $scope.init = function() {
        $scope.recipient = "";
        $scope.sender = $scope.currentUser().mails[0];
        $scope.recipients = [];
        $scope.recipientsStr = $scope.getFileDrop().recipients ?
            $scope.getFileDrop().recipients : [];
        $scope.authentication = $scope.getFileDrop().authentication !== null ?
            $scope.getFileDrop().authentication : true;
        $scope.senderEmails = $scope.getEmails();
        $scope.expiration = $scope.getFileDrop().expiration ?
            $scope.getFileDrop().expiration.toString() : "7200";
        $scope.message = $scope.getFileDrop().message ? $scope.getFileDrop().message : "";

        if ($scope.recipientsStr && $scope.recipientsStr.length > 0) {
            let recipientsSub = $scope.recipientsStr.substring(1, $scope.recipientsStr.length - 1)
                                      .split(",");
            for (let r of recipientsSub) {
                $scope.addRecipient(r);
            }
        }
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

    $scope.currentUser = () => $window.user;

    $scope.getFileDrop = () => $window.fileDrop;

    $scope.getEmails = function() {
        let emails = [];
        for (let mail of $scope.currentUser().mails) {
            emails.push({ value: mail, display: mail });
        }
        return emails;
    };
}

filedropApp.controller("PrepareJsController", PrepareJsController);