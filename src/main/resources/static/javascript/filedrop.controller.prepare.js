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

    $scope.addRecipient = function (recipient) {
        if ($scope.hasRecipient(recipient)) {
            return;
        }

        if (recipient === $scope.currentUser().uid) {
            $scope.sendToSelf = true;
        }

        dataProvider.loadData(function(response) {
            const data = response.data;
            const checkRecipient = $scope.checkRecipient(data);
            if (data.cn && checkRecipient) {
                $log.debug("addRecipient; ", $scope.currentUser().uid, "searched", recipient, "and found", data.cn);
                $scope.recipients.push({ name: data.cn, mail: data.mails[0], uid: data.uid });
            } else if (recipient.indexOf("@") > -1) {
                if ($scope.authentication) {
                    $scope.showPopup();
                    return;
                } else {
                    $scope.recipients.push({ name: recipient });
                }
            }
            $scope.recipient = "";
        }, "/filedrop/api/ldap/" + recipient);
    };

    $scope.removeRecipient = function(recipient) {
        if ($scope.currentUser().mails.includes(recipient.mail)) {
            $scope.sendToSelf = false;
        }

        let index = $scope.recipients.indexOf(recipient);
        if (index > -1) {
            $scope.recipients.splice(index, 1);
        }
    };

    $scope.clearRecipient = function() {
        $scope.recipient = "";
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
            return (r.uid ? r.uid.toUpperCase() === recipient.toUpperCase() : false) || r.name.toUpperCase() === recipient.toUpperCase();
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

    $scope.getAllowlist = () => $window.allowlist;

    $scope.sender = {
        model: $scope.getFileDrop().sender ? $scope.getFileDrop().sender : $scope.currentUser().mails[0],
        mails: $scope.currentUser().mails
    };

    $scope.checkRecipient = function(recipient) {
        if (recipient.uid === $scope.currentUser().uid) {
            return true;
        }

        //TODO: use the backend checkRecipient somehow.
        if ($scope.currentUser().affiliations.includes("student") || $scope.currentUser().affiliations.includes("affiliate")) {
            return recipient.affiliations.includes("staff") ||
                recipient.affiliations.includes("faculty") ||
                $scope.getAllowlist().includes(recipient.uid);
        } else if($scope.currentUser().affiliations.includes("other")) {
            return recipient.affiliations.includes("staff") ||
                recipient.affiliations.includes("faculty");
        } else {
            return $scope.currentUser().affiliations.includes("staff") || $scope.currentUser().affiliations.includes("faculty");
        }
    };
}

filedropApp.controller("PrepareJsController", PrepareJsController);

function PrepareModalController($scope, $uibModalInstance) {
    $scope.ok = function() {
        $uibModalInstance.close(false);
    };

    $scope.cancel = function () {
        $uibModalInstance.dismiss('cancel');
    }
}

filedropApp.controller("PrepareModalController", PrepareModalController);