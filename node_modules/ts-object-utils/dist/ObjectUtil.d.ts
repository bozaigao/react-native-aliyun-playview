export declare class ObjectUtil {
    static isNullOrUndefined<T>(obj: null | undefined | T): obj is null | undefined;
    static isNull(obj: any): obj is null;
    static isUndefined(obj: any): obj is undefined;
    static isNumber(obj: any): obj is number;
    static isString(obj: any): obj is string;
    static isObject(obj: any): obj is object;
}
