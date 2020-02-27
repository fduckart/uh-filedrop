function PrepareJsController($scope, dataProvider, $http, $window, $log) {
    $scope.init = function() {
        $scope.recipient = "";
        $scope.sender = $scope.currentUser().mails[0];
        $scope.recipients = [];
        $scope.loadRecipients();
        $scope.authentication = $scope.getAuthentication();
        $scope.senderEmails = $scope.getEmails();
        $scope.expiration = $scope.getExpiration();
        $scope.message = $scope.getMessage();
        $log.debug("Current user:", $scope.currentUser().uid);
        $log.debug("Recipients:", $scope.recipients);
        $log.debug("Authentication:", $scope.authentication);
        $log.debug("Expiration:", $scope.expiration);
        $log.debug("Message:", $scope.message);
    };

    $scope.addRecipient = function (recipient) {
        if (/^\s*$/.test(recipient) || recipient === undefined || $scope.hasRecipient(recipient)) {
            return;
        }

        dataProvider.loadData(function(response) {
            let data = response.data;
            if (data.cn) {
                $log.debug($scope.currentUser().uid + " searched " + recipient + " and found " + data.cn);
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

    $scope.loadRecipients = function() {
        let recipientsStr = $scope.getFileDrop().recipients;
        if (recipientsStr && recipientsStr.length > 0) {
            let recipientsSub = recipientsStr.substring(1, recipientsStr.length - 1)
                                             .split(",");
            for (let r of recipientsSub) {
                $scope.addRecipient(r);
            }
        }
    };

    $scope.getAuthentication = () => $scope.getFileDrop().authentication !== null ?
        $scope.getFileDrop().authentication : true;

    $scope.getExpiration = () => $scope.getFileDrop().expiration ?
        $scope.getFileDrop()
              .expiration
              .toString() : "7200";

    $scope.getMessage = () => $scope.getFileDrop().message ? $scope.getFileDrop().message : "";
}

filedropApp.controller("PrepareJsController", PrepareJsController);