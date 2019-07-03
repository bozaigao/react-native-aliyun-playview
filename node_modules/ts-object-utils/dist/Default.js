"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var ObjectUtil_1 = require("./ObjectUtil");
var Default = /** @class */ (function () {
    function Default() {
    }
    Default.value = function (obj, defaultValue) {
        return ObjectUtil_1.ObjectUtil.isNullOrUndefined(obj) ? defaultValue : obj;
    };
    return Default;
}());
exports.Default = Default;
//# sourceMappingURL=Default.js.map