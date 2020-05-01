let filedropApp = angular.module("filedropApp", ["ngFileUpload"]);

filedropApp.filter("bytes", function () {
    return function (bytes, precision) {
        if (isNaN(parseFloat(bytes)) || !isFinite(bytes)) {
        	return "-";
        }
        if (typeof precision === "undefined") {
        	precision = 1;
        }
        let units = ["bytes", "kB", "MB", "GB", "TB", "PB"],
            number = Math.floor(Math.log(bytes) / Math.log(1024));
        return (bytes / Math.pow(1024, Math.floor(number))).toFixed(precision) + " " + units[number];
    };
});

filedropApp.directive("validRecipient", function() {
    return {
        require: "ngModel",
    link: function(scope, element, attr, ngModelCtrl) {
        function fromUser(text) {
            if (text) {
                let transformedInput = text.replace(/[^a-zA-Z0-9@.+]*$/g, "");
                if (transformedInput !== text) {
                    ngModelCtrl.$setViewValue(transformedInput);
                    ngModelCtrl.$render();
                }
                return transformedInput;
            }
            return "";
        }
        ngModelCtrl.$parsers.push(fromUser);
    }
};
});