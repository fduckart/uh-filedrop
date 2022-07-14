function PrepareJsController($scope, dataProvider, $http, $window, $log, $uibModal) {

    $scope.init = function() {
        $scope.recipient = "";
        $scope.recipients = [];
        $scope.sendToSelf = false;
        $scope.authentication = $scope.getAuthentication();
        $scope.expiration = $scope.getExpiration();
        $scope.message = $scope.getMessage();
        $scope.loadRecipients();
        if ("off" === "") {
            $log.debug("init; Sender:", $scope.sender);
            $log.debug("init; Current user:", $scope.currentUser().uid);
            $log.debug("init; Recipients:", $scope.recipients);
            $log.debug("init; Authentication:", $scope.authentication);
            $log.debug("init; Expiration:", $scope.expiration);
            $log.debug("init; Message:", $scope.message);
            $log.debug("init; FileDrop Sender:", $scope.getFileDrop().sender);
        }
    };

    $scope.addRecipient = function(recipient) {
        const currentUser = $scope.currentUser();

        $log.debug("hmmm; currentUser: ", currentUser);
        $log.debug("hmmm;   recipient: ", recipient);

        if ($scope.hasRecipient(recipient)) {
            $scope.error = {message: "Recipient is already added."};
            $scope.recipient = "";
            return;
        }

        $log.debug("hmmm; after check... $scope.recipients: ");
        $scope.recipients.forEach(r => $log.debug("     $scope.recipients mail: " + r.mail));

        $log.debug("hmmm; after check... currentUser.mails.includes: " + currentUser.mails.includes(recipient));
        $log.debug("hmmm; after check... $scope.recipients.includes: " + $scope.recipients.includes(recipient));

        let userEmail = currentUser.mails[0];
        let isUserEmail = currentUser.mails.includes(recipient);
        for (let m of currentUser.mails) {
            if (m == recipient.toLowerCase()) {
                isUserEmail = true;
                break;
            }
        }

        let isRecipient = false;
        for (let m of currentUser.mails) {
            $log.debug("   <><><>  mail: " + m + "  ???--->  " + recipient.toLowerCase());
            if (isRecipient === false) {
                for (let r of $scope.recipients) {
                    $log.debug("     <><><>  r.mail: " + r.mail + "  ???--->  " + recipient.toLowerCase() + "  EQUAL? " + (r.mail.toLowerCase() === recipient.toLowerCase()));
                    if (r.mail.toLowerCase() === recipient.toLowerCase()) {
                        $log.debug("     <><><>  RETURN TRUE");
                        isRecipient = true;
                    }
                }
            }
        }

        $log.debug("hmmm; after check... isUserEmail: " + isUserEmail);
        $log.debug("hmmm; after check... isRecipient: " + isRecipient);
        $log.debug("hmmm; .......................................................");

        if (recipient === currentUser.uid || isUserEmail || isRecipient) {
            $scope.sendToSelf = true;
            if (!isRecipient) {
                $scope.recipients.push({
                    name: currentUser.cn,
                    mail: currentUser.mails[0],
                    uid: currentUser.uid
                });
            }
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
        }).then((response) => {
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
                $scope.error = {message: response.data.message};
                if (response.status === 405) {
                    $scope.showPopup();
                }
            });

        $scope.recipient = "";
    }

    $scope.isEmptyPerson = function(person) {
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
        $log.debug("    hasRecipient; recipient: " + recipient);
        return $scope.recipients.includes($scope.recipients.find(function(r) {

            //$log.debug("         hasRecipient#inner; R: " + recipient);
            $log.debug("         hasRecipient#inner; r: ", r);
            //$log.debug("         hasRecipient#inner; r.name: " + r.name);
            //$log.debug("         hasRecipient#inner; r.mail: " + r.mail);
            //$log.debug("         hasRecipient#inner; r.mails: " + r.mail);
            //$log.debug("         hasRecipient#inner; r.mails typeof: " + (typeof r.mails));
            $log.debug("         hasRecipient#inner; ..................................");

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
        const recipients = $scope.getFileDrop().recipients;
        if (recipients && recipients.length > 0) {
            for (let recipient of recipients) {
                if ($scope.currentUser().mails.includes(recipient)) {
                    $scope.sendToSelf = true;
                }
                $scope.addRecipient(recipient);
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

    // $scope.sender = {
    //     model: $scope.getFileDrop().sender ? $scope.getFileDrop().sender : $scope.currentUser().mails[0],
    //     mails: $scope.currentUser().mails
    // };
}

filedropApp.controller("PrepareJsController", PrepareJsController);

function PrepareModalController($scope, $uibModalInstance) {
    $scope.ok = function() {
        $uibModalInstance.close(false);
    };

    $scope.cancel = function() {
        $uibModalInstance.dismiss();
    }
}

filedropApp.controller("PrepareModalController", PrepareModalController);