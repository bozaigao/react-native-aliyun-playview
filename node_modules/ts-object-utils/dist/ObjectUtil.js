"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var ObjectUtil = /** @class */ (function () {
    function ObjectUtil() {
    }
    ObjectUtil.isNullOrUndefined = function (obj) {
        var ref = obj;
        return !(ref || ref === 0 || ref === false);
    };
    ObjectUtil.isNull = function (obj) {
        return obj === null;
    };
    ObjectUtil.isUndefined = function (obj) {
        return obj === undefined;
    };
    ObjectUtil.isNumber = function (obj) {
        return typeof obj === "number";
    };
    ObjectUtil.isString = function (obj) {
        return typeof obj === "string";
    };
    ObjectUtil.isObject = function (obj) {
        return typeof obj === "object";
    };
    return ObjectUtil;
}());
exports.ObjectUtil = ObjectUtil;
//# sourceMappingURL=ObjectUtil.js.map