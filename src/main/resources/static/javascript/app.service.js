(function() {

    filedropApp.factory('dataProvider', function($http) {
        return {
            loadData: function(callback, url) {
                $http.get(encodeURI(url)).success(callback).error(function(data, status) {
                    console.log('Error in dataProvider; status: ', status);
                });
            },
            delData: function(callback, url) {
                $http.post(encodeURI(url)).success(callback).error(function(data, status) {
                    console.log('Error in dataProvider; status: ', status);
                });
            },
            saveData: function(callback, url, data) {
                $http.post(encodeURI(url), data).success(callback).error(function(data, status) {
                    console.log('Error in dataProvider; status: ', status);
                });
            }
        }
    });

    filedropApp.service('holidayJsService', [ '$http', function($http) {
        function getHolidays(pageNumber, size) {
            pageNumber = pageNumber > 0 ? pageNumber - 1 : 0;
            return $http({
                method: 'GET',
                url: 'api/holidaygrid/get?page=' + pageNumber + '&size=' + size
            });
        }
        return {
            getHolidays: getHolidays
        };
    } ]);

})();
