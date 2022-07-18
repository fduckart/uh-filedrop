(function() {
    filedropApp.factory("dataProvider", function($http, $log) {
        return {
            loadData: function loadData(callback, url) {
                $http.get(encodeURI(url)).then(callback, function(response, status) {
                    $log.error("Error in dataProvider; status: ", status);
                });
            },
            delData: function delData(callback, url) {
                $http.delete(encodeURI(url)).then(callback, function(data, status) {
                    $log.error("Error in dataProvider; status: ", status);
                });
            },
            saveData: function saveData(callback, url, data) {
                $http.post(encodeURI(url), data).then(callback, function(response, status) {
                    $log.error("Error in dataProvider; status: ", status);
                });
            },
            postData: function(url, data, successCallback, errorCallback) {
                $http.post(encodeURI(url), data, {})
                    .then(successCallback, errorCallback);
            }
        };
    });
})();