(function() {

    filedropApp.factory("dataProvider", function($http) {
        return {
            loadData: function(callback, url) {
                $http.get(encodeURI(url))
                     .then(callback, function(response, status) {
                         console.log("Error in dataProvider; status: ", status);
                     });
            },
            delData: function(callback, url) {
                $http.delete(encodeURI(url))
                     .then(callback,
                         function(data, status) {
                             console.log("Error in dataProvider; status: ", status);
                         }
                     );
            },
            saveData: function(callback, url, data) {
                $http.post(encodeURI(url), data)
                     .then(callback, function(response, status) {
                         console.log("Error in dataProvider; status: ", status);
                     });
            }
        };
    });

})();
