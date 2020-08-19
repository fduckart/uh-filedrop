function PrepareJsController($scope, dataProvider, $http, $window, $log, $uibModal) {
    
    $scope.init = function() {
        $scope.recipient = "";
        $scope.recipients = [];
        $scope.sendToSelf = false;
        $scope.loadRecipients();
        $scope.authentication = $scope.getAuthentication();
        $scope.expiration = $scope.getExpiration();
        $scope.message = $scope.getMessage();
        $log.debug("init; Sender:", $scope.sender);
        $log.debug("init; Current user:", $scope.currentUser().uid);
        $log.debug("init; Recipients:", $scope.recipients);
        $log.debug("init; Authentication:", $scope.authentication);
        $log.debug("init; Expiration:", $scope.expiration);
        $log.debug("init; Message:", $scope.message);
        $log.debug("init; FileDrop Sender:", $scope.getFileDrop().sender);
    };

    $scope.addRecipient = function(recipient) {
        const currentUser = $scope.currentUser();

        if ($scope.hasRecipient(recipient)) {
            $scope.error = { message: "Recipient is already added." };
            $scope.recipient = "";
            return;
        }

        if (recipient === currentUser.uid || currentUser.mails.includes(recipient)) {
            $scope.sendToSelf = true;
            $scope.recipients.push({ name: currentUser.cn, mail: currentUser.mails[0], uid: currentUser.uid });
            $scope.recipient = "";
            return;
        }

        $http({
            method: "POST",
            url: "/filedrop/prepare/recipient/add",
            params: {
                recipient: recipient,
                authenticationRequired: $scope.authentication
            }
        })
        .then((response) => {
            const person = response.data;
            $log.debug("addRecipient;", currentUser.uid, "searched", recipient, "and found", person.cn);
            if ($scope.isEmptyPerson(person)) {
                $scope.recipients.push({name: recipient, mail: recipient})
            } else {
                $scope.recipients.push({name: person.cn, mail: person.mails[0], uid: person.uid});
            }
        },
        (response) => {
            $log.debug("addRecipient;", response.data.message);
            $scope.error = { message: response.data.message };
            if (response.status === 405) {
                $scope.showPopup();
            }
        });

        $scope.recipient = "";
    }

    $scope.isEmptyPerson = function (person) {
        return !person.cn && !person.uid && person.mails.length === 0;
    }

    $scope.removeRecipient = function(recipient) {
        if ($scope.currentUser().mails.includes(recipient.mail)) {
            $scope.sendToSelf = false;
        }

        let index = $scope.recipients.indexOf(recipient);
        if (index > -1) {
            $scope.recipients.splice(index, 1);
        }
    };

    $scope.getRecipients = function() {
        let recipients = [];
        $scope.recipients.forEach(function(recipient) {
            recipients.push(recipient.uid ? recipient.uid : recipient.name);
        });
        return recipients.join(",");
    };

    $scope.hasRecipient = function(recipient) {
        return $scope.recipients.includes($scope.recipients.find(function(r) {
            return (r.uid ? r.uid.toUpperCase() === recipient.toUpperCase() : false) ||
                r.name.toUpperCase() === recipient.toUpperCase() ||
                r.mail.toUpperCase() === recipient.toUpperCase();
        }));
    };

    $scope.userHasMultipleEmails = function() {
        return $scope.currentUser().mails.length > 1;
    };

    $scope.showPopup = function() {
        let modalInstance = $uibModal.open({
            templateUrl: "prepareModal.html",
            controller: "PrepareModalController"
        });

        modalInstance.result.then((authentication) => {
            $scope.authentication = authentication;
        });
    };

    $scope.sendSelf = function() {
        $scope.sendToSelf = !$scope.sendToSelf;
        $scope.addRecipient($scope.currentUser().uid);
    };

    $scope.disableSendSelf = function() {
        return $scope.sendToSelf || $scope.recipient.length > 0;
    };
    
    $scope.disabled = function() {
        return $scope.recipient.length > 0 || $scope.recipients.length === 0;
    };
    
    $scope.currentUser = () => $window.user;

    $scope.getFileDrop = () => $window.fileDrop;

    $scope.loadRecipients = function() {
        let recipientsStr = $scope.getFileDrop().recipients;
        if (recipientsStr && recipientsStr.length > 0) {
            let recipientsSub = recipientsStr.substring(1, recipientsStr.length - 1)
                                             .split(",");
            for (let r of recipientsSub) {
                if ($scope.currentUser().mails.includes(r)) {
                    $scope.sendToSelf = true;
                }
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

    $scope.sender = {
        model: $scope.getFileDrop().sender ? $scope.getFileDrop().sender : $scope.currentUser().mails[0],
        mails: $scope.currentUser().mails
    };
}

filedropApp.controller("PrepareJsController", PrepareJsController);

function PrepareModalController($scope, $uibModalInstance) {
    $scope.ok = function() {
        $uibModalInstance.close(false);
    };

    $scope.cancel = function () {
        $uibModalInstance.dismiss();
    }
}

filedropApp.controller("PrepareModalController", PrepareModalController);