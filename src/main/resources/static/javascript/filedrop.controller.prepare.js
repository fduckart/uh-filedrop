function PrepareJsController($scope, dataProvider, $http, $window, $log, $uibModal) {

    $scope.init = function() {
        $scope.addStep = ["_init_"];
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
        return {
            name: name,
            mail: mail,
            mails: mails,
            uid: uid
        };
    }

    $scope.addRecipient = function(recipientToAdd) {

        $scope.addStep.push("_add_start_");

        if ("off" === "") {
            throw new Error("STOP addrecipient; recipientToAdd: " + recipientToAdd);
        }

        if ($scope.hasRecipient(recipientToAdd)) {
            $scope.error = {message: "Recipient is already added."};
            $scope.recipient = "";
            $scope.addStep.push("_already_added_");
            return;
        }

        const currentUser = $scope.currentUser();

        if ($scope.isCurrentUserUid(recipientToAdd) || $scope.isCurrentUserMail(recipientToAdd)) {
            $scope.sendToSelf = true;
            $scope.recipients.push(
                $scope.makeRecipient(
                    currentUser.cn,
                    currentUser.mails[0],
                    currentUser.mails,
                    currentUser.uid)
                );
            $scope.error = undefined;
            $scope.recipient = "";
            $scope.addStep.push("_add_current_user_" + recipientToAdd);

            return;
        }

        if ("off" === "") {
            throw new Error("STOP addrecipient: ["
            + JSON.stringify(recipientToAdd) + "] == ["
            + JSON.stringify(currentUser) + "] == ["
            + JSON.stringify(currentUser) + "]");
        }

        let successCallback = function(response) {
            const person = response.data;
            $log.debug("addRecipient;", currentUser.uid, "searched", recipientToAdd, "and found", person.cn);

            if (!($scope.isEmptyPerson(person))) {
                if ("aaa" === "") {
                    throw new Error("BBB addrecipient; recipientToAdd: " + recipientToAdd);
                }
                $scope.recipients.push({
                    name: person.cn,
                    mail: person.mails[0],
                    mails: person.mails,
                    uid: person.uid
                });
                $scope.addStep.push("_add_person_" + recipientToAdd);
            } else {
                if ("" === "") {
                    throw new Error("CCC addrecipient; recipientToAdd: " + recipientToAdd);
                }

                $scope.recipients.push({name: recipientToAdd, mail: recipientToAdd})
                $scope.addStep.push("_add_emtpy_person_" + recipientToAdd);
            }
        };

        let errorCallback = function(response) {
            $log.debug("addRecipient;", response.data.message);
            $scope.error = {message: response.data.message};
            if (response.status === 405) {
                $scope.showPopup();
            }
            $scope.addStep.push("_add_failure_");
        };

        // const url = "/filedrop/prepare/recipient/add";
        // const data = {
        //     recipient: recipientToAdd,
        //     authenticationRequired: $scope.authentication
        // };

        $http({
            method: "POST",
            url: "/filedrop/prepare/recipient/add",
            params: {
                recipient: recipientToAdd,
                //authenticationRequired: $scope.authentication
            }
        }).then(successCallback, errorCallback);

        if ("ggg" === "") {
            throw new Error("GGG addrecipient; recipientToAdd: " + recipientToAdd);
        }

        $scope.addStep.push("_add_done_");

        $scope.recipient = "";
    }

    $scope.loadRecipients = function() {
        const recipients = $scope.getFileDrop().recipients;

        if ("off" === "") {
            throw new Error("STOP addrecipient: ["
            + JSON.stringify(recipients) + "]");
        }

        if (recipients && recipients.length > 0) {
            for (let recipient of recipients) {
                if ($scope.currentUser().mails.includes(recipient)) {
                    $scope.sendToSelf = true;
                }
                $scope.addRecipient(recipient);
            }
        }
    };

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
                if (r.mails) {
                    for (let s of r.mails) {
                        if (s && s.toLowerCase() === value) {
                            return true;
                        }
                    }
                }
                if (r.uid && r.uid.toLowerCase() === value) {
                    return true;
                }
                if (r.name && r.name.toLowerCase() === value) {
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