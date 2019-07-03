# ts-object-utils
Best way to check for null or undefined in typescript using a type guard. Other types also supported.
Can strip out null or undefine or complex types like T | null etc.

`npm install --save ts-object-utils`

To use: 
```js
import ObjectUtil from "ts-object-utils";

let a: number | null = 0;

console.log(a*4); //Error: a might be null

if(!ObjectUtil.isNullOrUndefined(a)){
    console.log(a*4); //Works
}
```
