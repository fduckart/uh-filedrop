const FAQ_URL_LOAD = "/filedrop/api/faq";
const FAQ_ADMIN_URL_LOAD = "/filedrop/api/admin/faq";

function FaqJsController($scope, dataProvider, $uibModal, $sce, $http) {

    $scope.init = function () {
        $scope.faqList = [];
        $scope.loadData();
    };

    $scope.loadData = function () {
        dataProvider.loadData(function (response) {
            $scope.faqList = response.data;
        }, FAQ_URL_LOAD);
    };

    $scope.renderHtml = function (html) {
        return $sce.trustAsHtml(html);
    }

    $scope.openAddModal = function () {
       let modalInstance = $uibModal.open({
           templateUrl: "faqAddModal.html",
           controller: "FaqAddModalController",
           size: 'lg',
        });

       modalInstance.result.then((faq) => {
            $scope.addFaq(faq.question, faq.answer);
       });
    };

    $scope.addFaq = function (question, answer) {
        dataProvider.saveData(function (response) {
            $scope.faqList.push(response.data);
        }, FAQ_ADMIN_URL_LOAD, { question: question, answer: answer })
    };

    $scope.openEditModal = function (faq) {
        const testFaq = faq;
        let modalInstance = $uibModal.open({
            templateUrl: "faqEditModal.html",
            controller: "FaqEditModalController",
            size: 'lg',
            resolve: {
                faq: function () {
                    return testFaq;
                }
            },
        });

        modalInstance.result.then((faq) => {
            $scope.editFaq(faq);
        });
    };

    $scope.editFaq = function (faq) {
        $http({
            method: "POST",
            url: FAQ_ADMIN_URL_LOAD + "/" + faq.id,
            params: {
                question: faq.question,
                answer: faq.answer,
            }
        }).then((response) => {
            let foundFaq = $scope.faqList.find(faqs => faqs.id === faq.id);
            foundFaq.question = faq.question;
            foundFaq.answer = faq.answer;
        });
    };

    $scope.openDeleteModal = function(faq) {
        let modalInstance = $uibModal.open({
            templateUrl: "faqDeleteModal.html",
            controller: "FaqDeleteModalController",
            resolve: {
                faq: function() {
                    return faq;
                }
            },
        });

        modalInstance.result.then((faq) => {
            $scope.deleteFaq(faq);
        });
    };

    $scope.deleteFaq = function (faq) {
        dataProvider.delData(function () {
            let index = $scope.faqList.indexOf(faq);
            if (index > -1) {
                $scope.faqList.splice(index, 1);
            }
        }, FAQ_ADMIN_URL_LOAD + "/" + faq.id);
    };
}

filedropApp.controller("FaqJsController", FaqJsController);

function FaqAddModalController($scope, $uibModalInstance) {
    $scope.ok = function() {
        $uibModalInstance.close({ question: $scope.question, answer: $scope.answer });
    };

    $scope.cancel = function () {
        $uibModalInstance.dismiss();
    }
}

filedropApp.controller("FaqAddModalController", FaqAddModalController);

function FaqEditModalController($scope, $uibModalInstance, faq) {
    $scope.faq = faq;
    $scope.newFaq = { question: $scope.faq.question, answer: $scope.faq.answer };

    $scope.ok = function() {
        $scope.faq.question = $scope.newFaq.question;
        $scope.faq.answer = $scope.newFaq.answer;
        $uibModalInstance.close(faq);
    };

    $scope.cancel = function () {
        $uibModalInstance.dismiss();
    };
}

filedropApp.controller("FaqEditModalController", FaqEditModalController);

function FaqDeleteModalController($scope, $uibModalInstance, faq) {
    $scope.ok = function() {
        $uibModalInstance.close(faq);
    };

    $scope.cancel = function () {
        $uibModalInstance.dismiss();
    }
}

filedropApp.controller("FaqDeleteModalController", FaqDeleteModalController);