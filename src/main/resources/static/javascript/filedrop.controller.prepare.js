function PrepareJsController($scope, dataProvider, $http, $window, $log, $uibModal) {

    $scope.init = function() {
        $scope.recipient = "";
        $scope.recipients = [];
        $scope.sendToSelf = false;
        $scope.authentication = $scope.getAuthentication();
        $scope.expiration = $scope.getExpiration();
        $scope.message = $scope.getMessage();
        $scope.loadRecipients();

        $log.debug("init; Sender:", $scope.sender);
        $log.debug("init; Current user:", $scope.currentUser().uid);
        $log.debug("init; Recipients:", $scope.recipients);
        $log.debug("init; Authentication:", $scope.authentication);
        $log.debug("init; Expiration:", $scope.expiration);
        $log.debug("init; Message:", $scope.message);
        $log.debug("init; FileDrop Sender:", $scope.getFileDrop().sender);

        $scope.addStep = "";
    };

    $scope.isCurrentUserMail = function(mail) {
        if (!mail) {
            return false;
        }

        const currentUser = $scope.currentUser();
        for (let m of currentUser.mails) {
            if (m.toLowerCase() === mail.toLowerCase()) {
                return true;
            }
        }

        return false; // Not found.
    };

    $scope.isCurrentUserUid = function(uid) {
        if (!uid) {
            return false;
        }

        return $scope.currentUser().uid === uid;
    };

    $scope.isRecipientMail = function(mail) {
        const currentUser = $scope.currentUser();
        for (let m of currentUser.mails) {
            for (let r of $scope.recipients) {
                if (r.mail.toLowerCase() === mail.toLowerCase()) {
                    return true;
                }
            }
        }

        return false; // Not found.
    };

    $scope.makeRecipient = function(name, mail, mails, uid) {
        // Note: cannot do this yet.
        // let Recipient = class {
        //     constructor() {
        //         this.name = name;
        //         this.mail = mail;
        //         this.mails = mails;
        //         this.uid = uid;
        //     }
        // }
        // return new Recipient(name, mail, mails, uid);

        return {
            name: name,
            mail: mail,
            mails: mails,
            uid: uid
        };
    }

    $scope.addRecipient = function(recipientToAdd) {
        if ($scope.hasRecipient(recipientToAdd)) {
            $scope.error = {message: "Recipient is already added."};
            $scope.recipient = "";
            $scope.addStep = "_already_added_"
            return;
        }

        const currentUser = $scope.currentUser();

        if ($scope.isCurrentUserUid(recipientToAdd) || $scope.isCurrentUserMail(recipientToAdd)) {
            $scope.sendToSelf = true;
            $scope.recipients.push(
                $scope.makeRecipient(
                    currentUser.cn,
                    currentUser.mails[0],
                    [currentUser.mails[0]],
                    currentUser.uid)
            );
            $scope.error = undefined;
            $scope.recipient = "";
            $scope.addStep = "_two_"

            return;
        }

        const url = "/filedrop/prepare/recipient/add";
        const data = {
            recipient: recipientToAdd,
            authenticationRequired: $scope.authentication
        };

        $http({
            method: "POST",
            url: "/filedrop/prepare/recipient/add",
            params: {
                recipient: recipientToAdd,
                //authenticationRequired: $scope.authentication
            }
        }).then((response) => {
            const person = response.data;
            $log.debug("addRecipient;", currentUser.uid, "searched", recipientToAdd, "and found", person.cn);

            if (!($scope.isEmptyPerson(person))) {
                $scope.recipients.push({
                    name: person.cn,
                    mail: person.mails[0],
                    mails: person.mails,
                    uid: person.uid
                });
                $scope.addStep = "_four_"
            } else {
                $scope.recipients.push({name: recipientToAdd, mail: recipientToAdd})
                $scope.addStep = "_three_"
            }

        }, (response) => {
            $log.debug("addRecipient;", response.data.message);
            $scope.error = {message: response.data.message};
            if (response.status === 405) {
                $scope.showPopup();
            }
            $scope.addStep = "_add_failure_"
        });

        $scope.addStep = "_done_"

        $scope.recipient = "";
    }

    $scope.isEmptyPerson = function(person) {
        if (!person || !person.mails) {
            return true;
        }

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
        return $scope.recipients ? $scope.recipients : [];
    };

    $scope.getRecipientsString = function() {
        let result = "";
        let recipients = [];
        if ($scope.recipients) {
            $scope.recipients.forEach(function(r) {
                recipients.push(r.uid ? r.uid : r.name);
            });
            result = recipients.join(",");
        }
        return result;
    };

    $scope.hasRecipient = function(recipient) {
        if (recipient) {
            const value = recipient.toLowerCase();
            let recipients = $scope.getRecipients();
            for (let r of recipients) {
                if (r.uid && r.uid.toLowerCase() === value) {
                    return true;
                } else if (r.mail && r.mail.toLowerCase() === value) {
                    return true;
                } else if (r.name && r.name.toLowerCase() === value) {
                    return true;
                }
            }
        }
        return false;

        // return $scope.recipients.includes($scope.recipients.find(function(r) {
        //     return
        //     (r.uid ? r.uid.toLowerCase() === recipient.toLowerCase() : false) ||
        //     (r.name ? r.name.toLowerCase() === recipient.toLowerCase() : false) ||
        //     (r.mail ? r.mail.toUpperCase() === recipient.mail.toUpperCase() : false);
        // }));
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

    $scope.cancel = function() {
        $uibModalInstance.dismiss();
    }
}

filedropApp.controller("PrepareModalController", PrepareModalController);