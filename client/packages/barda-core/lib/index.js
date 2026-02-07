import _ from 'lodash';
import log from 'loglevel';
import { toJson } from 'really-relaxed-json';
import { serialize, compile, middleware, prefixer, stringify } from 'stylis';
import require$$0, { Fragment } from 'react';
import { IntlMessageFormat } from 'intl-messageformat';

/******************************************************************************
Copyright (c) Microsoft Corporation.

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH
REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY
AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR
OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
PERFORMANCE OF THIS SOFTWARE.
***************************************************************************** */
/* global Reflect, Promise, SuppressedError, Symbol, Iterator */

var extendStatics = function(d, b) {
    extendStatics = Object.setPrototypeOf ||
        ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
        function (d, b) { for (var p in b) if (Object.prototype.hasOwnProperty.call(b, p)) d[p] = b[p]; };
    return extendStatics(d, b);
};

function __extends(d, b) {
    if (typeof b !== "function" && b !== null)
        throw new TypeError("Class extends value " + String(b) + " is not a constructor or null");
    extendStatics(d, b);
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
}

var __assign = function() {
    __assign = Object.assign || function __assign(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p)) t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};

function __decorate(decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
}

function __awaiter(thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
}

function __generator(thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g = Object.create((typeof Iterator === "function" ? Iterator : Object).prototype);
    return g.next = verb(0), g["throw"] = verb(1), g["return"] = verb(2), typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (g && (g = 0, op[0] && (_ = 0)), _) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
}

function __spreadArray(to, from, pack) {
    if (pack || arguments.length === 2) for (var i = 0, l = from.length, ar; i < l; i++) {
        if (ar || !(i in from)) {
            if (!ar) ar = Array.prototype.slice.call(from, 0, i);
            ar[i] = from[i];
        }
    }
    return to.concat(ar || Array.prototype.slice.call(from));
}

typeof SuppressedError === "function" ? SuppressedError : function (error, suppressed, message) {
    var e = new Error(message);
    return e.name = "SuppressedError", e.error = error, e.suppressed = suppressed, e;
};

function isEqualArgs(args, cacheArgs, equals) {
    if (!cacheArgs) {
        return false;
    }
    if (args.length === 0 && cacheArgs.length === 0) {
        return true;
    }
    return (args.length === cacheArgs.length &&
        cacheArgs.every(function (arg, index) { var _a, _b; return (_b = (_a = void 0 ) === null || _a === void 0 ? void 0 : _a.call(equals, arg, args[index])) !== null && _b !== void 0 ? _b : arg === args[index]; }));
}
function getCacheResult(thisObj, fnName, args, equals) {
    var _a;
    var cache = (_a = thisObj === null || thisObj === void 0 ? void 0 : thisObj.__cache) === null || _a === void 0 ? void 0 : _a[fnName];
    if (cache && isEqualArgs(args, cache.args, equals)) {
        return cache.result;
    }
}
function cache(fn, args, thisObj, fnName, equals) {
    var result = getCacheResult(thisObj, fnName, args, equals);
    if (result) {
        return result.value;
    }
    var cache = {
        id: Symbol("id"),
        args: args,
        time: Date.now(),
    };
    if (!thisObj.__cache) {
        thisObj.__cache = {};
    }
    thisObj.__cache[fnName] = cache;
    var value = fn.apply(thisObj, args);
    cache.result = { value: value };
    return value;
}
function memoized(equals) {
    return function (target, fnName, descriptor) {
        var fn = descriptor.value;
        descriptor.value = function () {
            var args = [];
            for (var _i = 0; _i < arguments.length; _i++) {
                args[_i] = arguments[_i];
            }
            return cache(fn, args, this, fnName, equals);
        };
        return descriptor;
    };
}

var COST_MS_PRINT_THR = 0;
var RecursivePerfUtil = /** @class */ (function () {
    function RecursivePerfUtil() {
        var _this = this;
        this.root = Symbol("root");
        this.stack = [];
        this.initRecord = function () {
            return { obj: _this.root, name: "@root", childrenPerfInfo: [], costMs: 0, depth: 0, info: {} };
        };
        this.getRecordByStack = function (stack) {
            var curRecord = _this.record;
            (stack !== null && stack !== void 0 ? stack : _this.stack).forEach(function (idx) {
                curRecord = curRecord.childrenPerfInfo[idx];
            });
            return curRecord;
        };
        this.clear = function () {
            _this.record = _this.initRecord();
        };
        this.print = function (stack, cost_ms_print_thr) {
            if (cost_ms_print_thr === void 0) { cost_ms_print_thr = COST_MS_PRINT_THR; }
            var record = _this.getRecordByStack(stack);
            console.info("~~ PerfInfo. costMs: ".concat(record.costMs.toFixed(3), ", stack: ").concat(stack, ", [name]").concat(record.name, ", [info]"), record.info, ", obj: ", record.obj, ", depth: ".concat(record.depth, ", size: ").concat(_.size(record.childrenPerfInfo)));
            record.childrenPerfInfo.forEach(function (subRecord, idx) {
                if (subRecord.costMs >= cost_ms_print_thr) {
                    console.info("  costMs: ".concat(subRecord.costMs.toFixed(3), " [").concat(idx, "]").concat(subRecord.name, " [info]"), subRecord.info, ". obj: ", subRecord.obj, "");
                }
            });
        };
        this.record = this.initRecord();
    }
    RecursivePerfUtil.prototype.log = function (info, key, log) {
        info[key] = log;
    };
    RecursivePerfUtil.prototype.perf = function (obj, name, fn) {
        {
            return fn(_.noop);
        }
    };
    return RecursivePerfUtil;
}());
var evalPerfUtil = new RecursivePerfUtil();
// @ts-ignore
globalThis.evalPerfUtil = evalPerfUtil;

var AbstractNode = /** @class */ (function () {
    function AbstractNode() {
        this.type = "abstract";
        this.evalCache = {};
    }
    /**
     * 对当前节点进行评估，并考虑依赖关系和缓存。
     * @param exposingNodes - 由外部暴露的节点，按节点ID映射。
     * @param methods - 自定义评估方法。
     * @returns 节点的评估结果。
     */
    AbstractNode.prototype.evaluate = function (exposingNodes, methods) {
        var _this = this;
        return evalPerfUtil.perf(this, "eval", function () {
            exposingNodes = exposingNodes !== null && exposingNodes !== void 0 ? exposingNodes : {};
            var dependingNodeMap = _this.filterNodes(exposingNodes);
            // 当前依赖节点映射与上次相同则使用缓存
            if (dependingNodeMapEquals(_this.evalCache.dependingNodeMap, dependingNodeMap)) {
                return _this.evalCache.value;
            }
            // 初始化循环检测字段
            _this.evalCache.cyclic = false;
            var result = _this.justEval(exposingNodes, methods);
            // 更新缓存
            _this.evalCache.dependingNodeMap = dependingNodeMap;
            _this.evalCache.value = result;
            // 检查子节点是否存在循环依赖
            if (!_this.evalCache.cyclic) {
                _this.evalCache.cyclic = _this.getChildren().some(function (node) { return node.hasCycle(); });
            }
            return result;
        });
    };
    AbstractNode.prototype.hasCycle = function () {
        var _a;
        return (_a = this.evalCache.cyclic) !== null && _a !== void 0 ? _a : false;
    };
    AbstractNode.prototype.dependNames = function () {
        return Object.keys(this.dependValues());
    };
    /**
     * 判断当前评估是否命中缓存
     *
     * 该方法用于确定当前评估是否可以命中缓存，从而避免重复计算
     * 它通过比较当前评估所依赖的节点映射与缓存中的依赖节点映射是否一致来实现
     *
     * @param exposingNodes 可选参数，暴露的节点映射，默认为空对象
     * @returns 返回一个布尔值，表示是否命中缓存
     */
    AbstractNode.prototype.isHitEvalCache = function (exposingNodes) {
        // 如果没有提供暴露的节点映射，则默认为空对象
        exposingNodes = exposingNodes !== null && exposingNodes !== void 0 ? exposingNodes : {};
        // 使用提供的暴露的节点映射来过滤出依赖的节点映射
        var dependingNodeMap = this.filterNodes(exposingNodes);
        // 比较当前评估的依赖节点映射与缓存中的依赖节点映射是否相等，从而判断是否命中缓存
        return dependingNodeMapEquals(this.evalCache.dependingNodeMap, dependingNodeMap);
    };
    return AbstractNode;
}());
/**
 * 将依赖节点映射中的 WrapNode 转换为实际节点。
 * 由于 WrapNode 在评估过程中动态构建，其引用始终在变化。
 */
function unWrapDependingNodeMap(depMap) {
    var nextMap = new Map();
    depMap.forEach(function (p, n) {
        if (n.type === "wrap") {
            nextMap.set(n.delegate, p);
        }
        else {
            nextMap.set(n, p);
        }
    });
    return nextMap;
}
function setEquals(s1, s2) {
    return s2 !== undefined && s1.size === s2.size && Array.from(s2).every(function (v) { return s1.has(v); });
}
/**
 * 检查两个依赖节点映射是否相等
 * - 使用 "===" 检查 Node 的引用相等性
 * - 使用深度比较检查 string[] 的内容是否相等
 *
 * @param dependingNodeMap1 第一个依赖节点映射
 * @param dependingNodeMap2 第二个依赖节点映射
 * @returns 返回两个依赖节点映射是否相等
 */
function dependingNodeMapEquals(dependingNodeMap1, dependingNodeMap2) {
    if (!dependingNodeMap1 || dependingNodeMap1.size !== dependingNodeMap2.size) {
        return false;
    }
    var map1 = unWrapDependingNodeMap(dependingNodeMap1);
    var map2 = unWrapDependingNodeMap(dependingNodeMap2);
    var result = true;
    map2.forEach(function (paths, node) {
        result = result && setEquals(paths, map1.get(node));
    });
    return result;
}

/**
 * 返回一个新节点，以输入节点值作为函数的输入，对函数结果进行求值
 */
var FunctionNode = /** @class */ (function (_super) {
    __extends(FunctionNode, _super);
    function FunctionNode(child, func) {
        var _this = _super.call(this) || this;
        _this.child = child;
        _this.func = func;
        _this.type = "function";
        return _this;
    }
    FunctionNode.prototype.filterNodes = function (exposingNodes) {
        var _this = this;
        return evalPerfUtil.perf(this, "filterNodes", function () {
            return _this.child.filterNodes(exposingNodes);
        });
    };
    FunctionNode.prototype.justEval = function (exposingNodes, methods) {
        return this.func(this.child.evaluate(exposingNodes, methods));
    };
    FunctionNode.prototype.getChildren = function () {
        return [this.child];
    };
    FunctionNode.prototype.dependValues = function () {
        return this.child.dependValues();
    };
    FunctionNode.prototype.fetchInfo = function (exposingNodes, options) {
        return this.child.fetchInfo(exposingNodes, options);
    };
    __decorate([
        memoized()
    ], FunctionNode.prototype, "filterNodes", null);
    __decorate([
        memoized()
    ], FunctionNode.prototype, "fetchInfo", null);
    return FunctionNode;
}(AbstractNode));
function withFunction(child, func) {
    return new FunctionNode(child, func);
}

/**
 * 向目标 Map 中添加一个依赖项。
 *
 * @param target - 要添加依赖项的 Map。
 * @param node - 要添加依赖项的节点。
 * @param paths - 要添加的路径。
 */
function addDepend(target, node, paths) {
    // 如果节点未定义，则返回
    if (!node) {
        return;
    }
    // 获取节点在 Map 中的值
    var value = target.get(node);
    // 如果值未定义，则创建一个新的 Set，并将其添加到 Map 中
    if (value === undefined) {
        value = new Set();
        target.set(node, value);
    }
    // 将路径添加到 Set 中
    paths.forEach(function (p) { return value === null || value === void 0 ? void 0 : value.add(p); });
}
/**
 * 将来自 source Map 的所有依赖项添加到 target Map 中。
 *
 * @param target - 要添加依赖项的 Map。
 * @param source - 要从中添加依赖项的 Map。
 * @returns 已添加了所有依赖项的 target Map。
 */
function addDepends(target, source) {
    // 从 source Map 中添加所有依赖项
    source === null || source === void 0 ? void 0 : source.forEach(function (paths, node) { return addDepend(target, node, paths); });
    // 返回已添加了所有依赖项的 target Map
    return target;
}

/**
 * 评估值是由子节点构造的记录
 */
var RecordNode = /** @class */ (function (_super) {
    __extends(RecordNode, _super);
    function RecordNode(children) {
        var _this = _super.call(this) || this;
        _this.children = children;
        _this.type = "record";
        return _this;
    }
    RecordNode.prototype.filterNodes = function (exposingNodes) {
        var _this = this;
        return evalPerfUtil.perf(this, "filterNodes", function () {
            var result = new Map();
            Object.values(_this.children).forEach(function (node) {
                addDepends(result, node.filterNodes(exposingNodes));
            });
            return result;
        });
    };
    RecordNode.prototype.justEval = function (exposingNodes, methods) {
        var _this = this;
        return _.mapValues(this.children, function (v, key) {
            return evalPerfUtil.perf(_this, "eval-".concat(key), function () { return v.evaluate(exposingNodes, methods); });
        });
    };
    RecordNode.prototype.getChildren = function () {
        return Object.values(this.children);
    };
    RecordNode.prototype.dependValues = function () {
        var nodes = Object.values(this.children);
        if (nodes.length === 1) {
            return nodes[0].dependValues();
        }
        var ret = {};
        nodes.forEach(function (node) {
            Object.entries(node.dependValues()).forEach(function (_a) {
                var key = _a[0], value = _a[1];
                ret[key] = value;
            });
        });
        return ret;
    };
    RecordNode.prototype.fetchInfo = function (exposingNodes, options) {
        var isFetching = false;
        var ready = true;
        Object.entries(this.children).forEach(function (_a) {
            _a[0]; var child = _a[1];
            var fi = child.fetchInfo(exposingNodes, options);
            isFetching = fi.isFetching || isFetching;
            ready = fi.ready && ready;
        });
        return { isFetching: isFetching, ready: ready };
    };
    __decorate([
        memoized()
    ], RecordNode.prototype, "filterNodes", null);
    __decorate([
        memoized()
    ], RecordNode.prototype, "fetchInfo", null);
    return RecordNode;
}(AbstractNode));
function fromRecord(record) {
    return new RecordNode(record);
}

var CachedNode = /** @class */ (function (_super) {
    __extends(CachedNode, _super);
    function CachedNode(child) {
        var _this = _super.call(this) || this;
        _this.type = "cached";
        _this.child = withEvalCache(child);
        return _this;
    }
    CachedNode.prototype.filterNodes = function (exposingNodes) {
        return this.child.filterNodes(exposingNodes);
    };
    CachedNode.prototype.justEval = function (exposingNodes, methods) {
        var isCached = this.child.isHitEvalCache(exposingNodes); // isCached must be set before evaluate() call
        var value = this.child.evaluate(exposingNodes, methods);
        return { value: value, isCached: isCached };
    };
    CachedNode.prototype.getChildren = function () {
        return [this.child];
    };
    CachedNode.prototype.dependValues = function () {
        return this.child.dependValues();
    };
    CachedNode.prototype.fetchInfo = function (exposingNodes) {
        return this.child.fetchInfo(exposingNodes);
    };
    __decorate([
        memoized()
    ], CachedNode.prototype, "filterNodes", null);
    return CachedNode;
}(AbstractNode));
function withEvalCache(node) {
    var newNode = withFunction(node, function (x) { return x; });
    newNode.evalCache = __assign({}, node.evalCache);
    return newNode;
}
/**
 * 创建一个新节点，该节点包含两个输入节点。
 * - 如果 mainNode 永远未求值，则 (新节点).evaluate 等于 mainNode.evaluate
 * - 如果 mainNode 已求值，则 (新节点).evaluate 等于 minorNode.evaluate
 *
 * @remarks
 * 封装逻辑：2 个节点 -> CachedNode(mainNode) + minorNode -> RecordNode({main, minor}) -> FunctionNode
 *
 * @warn 不当使用可能导致意外行为，请小心。
 * @param mainNode 主节点
 * @param minorNode 次要节点
 * @returns 新节点
 */
function evalNodeOrMinor(mainNode, minorNode) {
    var nodeRecord = { main: new CachedNode(mainNode), minor: minorNode };
    return new FunctionNode(new RecordNode(nodeRecord), function (record) {
        var mainCachedValue = record.main;
        if (!mainCachedValue.isCached) {
            return mainCachedValue.value;
        }
        return record.minor;
    });
}

/**
 * 将任意值转换为可读字符串
 *
 * 此函数旨在将各种类型的值转换为字符串表示形式，以便于在用户界面或日志中显示
 * 它处理了多种数据类型，包括正则表达式、未定义、数字、字符串以及对象等
 *
 * @param value 任意类型的值
 * @returns 值的可读字符串表示
 *
 * 注意：
 * - 对于正则表达式，直接使用其toString方法
 * - 对于未定义、NaN、Infinity和-Infinity有特定的处理逻辑
 * - 数字类型直接转换为字符串
 * - 字符串类型会在前后各添加一个双引号
 * - 其他类型（包括对象）使用JSON.stringify进行处理，以提供一个简洁的字符串表示
 *   特别地，函数处理了对象中可能出现的函数、大整数、符号和未定义类型，以及非有限数字
 */
function toReadableString(value) {
    // 处理正则表达式，直接返回其字符串表示
    if (value instanceof RegExp) {
        return value.toString();
    }
    // 处理未定义和数字类型，其中数字包括NaN、Infinity和-Infinity
    // 这里通过直接转换为字符串来处理
    if (value === undefined || typeof value === "number") {
        return value + "";
    }
    // 处理字符串类型，添加双引号以区分字面量字符串
    if (typeof value === "string") {
        return '"' + value + '"';
    }
    // 使用JSON.stringify处理其他类型，如对象
    // 在回调函数中处理不能直接序列化的类型：函数、大整数、符号和未定义
    // 对于非有限数字，也采取类似的处理方式
    return JSON.stringify(value, function (key, val) {
        // 根据值的类型分别处理
        switch (typeof val) {
            case "function":
            case "bigint":
            case "symbol":
            case "undefined":
                // 这些类型直接转换为字符串
                return val + "";
            case "number":
                // 对于非有限数字（NaN或Infinity），也转换为字符串
                if (!isFinite(val)) {
                    return val + "";
                }
        }
        // 其他类型直接返回
        return val;
    });
}

/**
 * 值和消息的封装类。
 *
 * @template T 值的类型
 */
var ValueAndMsg = /** @class */ (function () {
    /**
     * 构造函数
     *
     * @param value 值
     * @param msg 消息
     * @param extra 额外信息
     * @param midValue 中间值
     */
    function ValueAndMsg(value, msg, extra, midValue) {
        this.value = value;
        this.msg = msg;
        this.extra = extra;
        this.midValue = midValue;
    }
    /**
     * 检查是否存在消息
     *
     * @returns 如果存在消息返回 true，否则返回 false
     */
    ValueAndMsg.prototype.hasError = function () {
        return this.msg !== undefined;
    };
    /**
     * 获取消息
     *
     * @param displayValueFn 用于将值转换为可读字符串的函数，默认为 toReadableString
     * @returns 如果存在消息返回消息，否则返回通过 displayValueFn 转换的值
     */
    ValueAndMsg.prototype.getMsg = function (displayValueFn) {
        var _a;
        if (displayValueFn === void 0) { displayValueFn = toReadableString; }
        return (_a = (this.hasError() ? this.msg : displayValueFn(this.value))) !== null && _a !== void 0 ? _a : "";
    };
    return ValueAndMsg;
}());

function dependsErrorMessage(node) {
    return "DependencyError: \"".concat(node.unevaledValue, "\" caused a cyclic dependency.");
}
function getErrorMessage(err) {
    // todo try to use 'err instanceof EvalTypeError' instead
    if (err instanceof TypeError && err.hint) {
        return err.hint + "\n" + err.name + ": " + err.message;
    }
    return err instanceof Error
        ? err.name + ": " + err.message
        : "UnknownError: unknown exception during eval";
}
function mergeNodesWithSameName(map) {
    var nameDepMap = {};
    map.forEach(function (paths, node) {
        paths.forEach(function (p) {
            var path = p.split(".");
            var dep = genDepends(path, node);
            var name = path[0];
            var newDep = mergeNode(nameDepMap[name], dep);
            nameDepMap[name] = newDep;
        });
    });
    return nameDepMap;
}
function genDepends(path, node) {
    var _a;
    if (path.length <= 0) {
        throw new Error("path length should not be 0");
    }
    if (path.length === 1) {
        return node;
    }
    return genDepends(path.slice(0, -1), fromRecord((_a = {}, _a[path[path.length - 1]] = node, _a)));
}
// node2 mostly has one path
function mergeNode(node1, node2) {
    if (!node1 || node1 === node2) {
        return node2;
    }
    if (!nodeIsRecord(node1) || !nodeIsRecord(node2)) {
        throw new Error("unevaledNode should be type of RecordNode");
    }
    var record1 = node1.children;
    var record2 = node2.children;
    var record = __assign({}, record1);
    Object.keys(record2).forEach(function (name) {
        var subNode1 = record1[name];
        var subNode2 = record2[name];
        var subNode = subNode1 ? mergeNode(subNode1, subNode2) : subNode2;
        record[name] = subNode;
    });
    return fromRecord(record);
}
function nodeIsRecord(node) {
    return node.type === "record";
}

var DYNAMIC_SEGMENT_REGEX = /{{([\s\S]*?)}}/;
/**
 * 判断给定的段落是否为动态段落
 *
 * 本函数通过应用正则表达式来检测输入的段落字符串是否符合动态段落的特定模式
 * 动态段落通常包含可以变化的部分，例如占位符或变量，这些可能在运行时被替换或填充
 *
 * @param segment 待检测的段落字符串
 * @returns 如果段落符合动态段落的正则表达式模式，则返回true；否则返回false
 */
function isDynamicSegment(segment) {
    return DYNAMIC_SEGMENT_REGEX.test(segment);
}
/**
 * 将字符串分割为动态和静态部分
 * 动态部分以{{开头，}}结尾，可能包含嵌套的动态部分
 * 静态部分为动态部分之间的文本
 *
 * @param input 待处理的字符串
 * @returns 分割后的字符串数组，过滤掉空字符串
 */
function getDynamicStringSegments(input) {
    var segments = [];
    var position = 0;
    var start = input.indexOf("{{");
    while (start >= 0) {
        var i = start + 2;
        while (i < input.length && input[i] === "{")
            i++;
        var end = input.indexOf("}}", i);
        if (end < 0) {
            break;
        }
        var nextStart = input.indexOf("{{", end + 2);
        var maxIndex = nextStart >= 0 ? nextStart : input.length;
        var maxStartOffset = i - start - 2;
        var sum = i - start;
        var minValue = Number.MAX_VALUE;
        var minOffset = Number.MAX_VALUE;
        for (; i < maxIndex; i++) {
            switch (input[i]) {
                case "{":
                    sum++;
                    break;
                case "}":
                    sum--;
                    if (input[i - 1] === "}") {
                        var offset = Math.min(Math.max(sum, 0), maxStartOffset);
                        var value = Math.abs(sum - offset);
                        if (value < minValue || (value === minValue && offset < minOffset)) {
                            minValue = value;
                            minOffset = offset;
                            end = i + 1;
                        }
                    }
                    break;
            }
        }
        segments.push(input.slice(position, start + minOffset), input.slice(start + minOffset, end));
        position = end;
        start = nextStart;
    }
    segments.push(input.slice(position));
    return segments.filter(function (t) { return t; });
}

/**
 * 过滤并收集依赖
 *
 * 该函数旨在处理一个未评估的字符串，从中提取动态段并分析依赖关系
 * 它会遍历给定字符串中的动态段，解析并收集这些动态段所依赖的节点
 *
 * @param unevaledValue 未评估的字符串，可能包含动态段
 * @param exposingNodes 包含潜在依赖节点的记录，键为节点标识，值为节点对象
 * @param maxDepth 可选参数，指定解析依赖的最大深度，用于限制解析的复杂度
 * @returns 返回一个Map对象，其中键为Node对象，值为字符串的Set集合，表示每个节点所依赖的标识集合
 */
function filterDepends(unevaledValue, exposingNodes, maxDepth) {
    // 初始化一个Map用于存储依赖关系，键为Node对象，值为依赖标识的Set集合
    var ret = new Map();
    // 遍历未评估字符串中的动态段
    for (var _i = 0, _a = getDynamicStringSegments(unevaledValue); _i < _a.length; _i++) {
        var segment = _a[_i];
        // 检查当前段是否为动态段
        if (isDynamicSegment(segment)) {
            // 解析动态段内的依赖并添加到结果Map中
            addDepends(ret, parseDepends(segment.slice(2, -2), exposingNodes, maxDepth));
        }
    }
    // 返回收集到的依赖关系Map
    return ret;
}
/**
 * 检查给定的段中是否存在循环依赖
 *
 * @param segment - 待检查的段，其格式应表明允许动态解析
 * @param exposingNodes - 一个映射，键为节点标识符，值为节点实例，表示已暴露的节点
 * @returns 返回一个布尔值，表示给定段中是否存在循环依赖
 */
function hasCycle(segment, exposingNodes) {
    // 如果段不是动态段，则直接返回false，因为静态段不存在循环依赖的问题
    if (!isDynamicSegment(segment)) {
        return false;
    }
    // 初始化返回值为false，表示尚未发现循环依赖
    var ret = false;
    // 去除段的动态标识符“**”前后的内容，然后解析依赖关系
    // 对解析出的每个节点，检查其是否存在循环依赖
    // 一旦发现循环依赖，立即标记返回值为true，并结束检查
    parseDepends(segment.slice(2, -2), exposingNodes).forEach(function (paths, node) {
        ret = ret || node.hasCycle();
    });
    // 返回最终的检查结果
    return ret;
}
/**
 * 修改依赖项的名称
 *
 * 此函数用于在给定的字符串中重命名某个依赖项的名称它支持两种模式：函数模式和非函数模式
 * 在函数模式下，整个字符串作为函数体进行重命名；在非函数模式下，仅对字符串中的动态段进行重命名
 *
 * @param unevaledValue 未评估的字符串值，可能是函数体或包含动态段的字符串
 * @param oldName 需要被替换的原始依赖项名称
 * @param name 新的依赖项名称
 * @param isFunction 可选参数，指示是否将整个字符串作为函数体进行重命名，默认为false
 * @returns 修改后的字符串如果输入参数无效，则返回原始字符串
 */
function changeDependName(unevaledValue, oldName, name, isFunction) {
    // 检查输入参数是否有效，如果有任何一个参数无效，则直接返回原始字符串
    if (!unevaledValue || !oldName || !name) {
        return unevaledValue;
    }
    // 如果是指定作为函数体进行重命名，则直接调用rename函数进行替换
    if (isFunction) {
        return rename(unevaledValue, oldName, name);
    }
    // 对于非函数模式，将字符串分割成动态段和非动态段，分别处理
    // 首先获取字符串中的动态段数组
    return getDynamicStringSegments(unevaledValue)
        .map(function (segment) {
        // 如果当前段不是动态段，则直接返回该段
        if (!isDynamicSegment(segment)) {
            return segment;
        }
        // 如果当前段是动态段，则对该段进行重命名
        return rename(segment, oldName, name);
    })
        .join("");
}
/**
 * 重命名函数，用于替换字符串中指定的标识符
 *
 * 此函数的目的是在给定的字符串段中，将所有的旧标识符替换为新标识符
 * 标识符可以是简单的名称，也可以是包含点或方括号访问的嵌套属性
 *
 * @param segment 待处理的字符串段，可能包含需要替换的旧标识符
 * @param oldName 需要被替换的旧标识符
 * @param name 新的标识符，将替换旧标识符
 * @returns 返回替换旧标识符后的新字符串段
 */
function rename(segment, oldName, name) {
    // 定义访问符，用于匹配点和方括号访问
    var accessors = [".", "["];
    // 定义正则表达式字符串列表，用于匹配标识符
    var regStrList = ["[a-zA-Z_$][a-zA-Z_$0-9.[\\]]*", "\\[[a-zA-Z_][a-zA-Z_0-9.]*"];
    var ret = segment;
    // 遍历正则表达式字符串列表
    for (var _i = 0, regStrList_1 = regStrList; _i < regStrList_1.length; _i++) {
        var regStr = regStrList_1[_i];
        // 创建全局匹配的正则表达式
        var reg = new RegExp(regStr, "g");
        // 使用正则表达式替换匹配到的标识符
        ret = ret.replace(reg, function (s) {
            // 如果匹配到的标识符与旧标识符相同，则直接返回新标识符
            if (s === oldName) {
                return name;
            }
            var origin = oldName;
            var target = name;
            var matched = false;
            // 检查标识符是否以指定的旧标识符作为开头，并进行替换
            if (s.startsWith("[".concat(origin))) {
                origin = "[".concat(origin);
                target = "[".concat(name);
                matched = true;
            }
            // 遍历访问符，检查并替换符合条件的标识符
            for (var _i = 0, accessors_1 = accessors; _i < accessors_1.length; _i++) {
                var accessor = accessors_1[_i];
                if (s.startsWith(origin + accessor)) {
                    matched = true;
                    target = target + accessor + s.substring(origin.length + accessor.length);
                    break;
                }
            }
            // 如果匹配成功，则返回替换后的标识符
            if (matched) {
                return target;
            }
            // 如果没有匹配到，返回原字符串
            return s;
        });
    }
    // 返回替换完成的字符串段
    return ret;
}
/**
 * 从给定的JavaScript代码片段中提取标识符
 * 标识符可以是变量名、属性访问等，符合JavaScript的标识符命名规则
 * 此函数递归处理嵌套的属性访问和索引
 *
 * @param jsSnippet 输入的JavaScript代码片段字符串
 * @returns 提取的标识符数组如果未找到标识符，则返回原始输入数组
 */
function getIdentifiers(jsSnippet) {
    // 存储最终提取的标识符数组
    var ret = [];
    // 使用正则表达式匹配常见的标识符（不包括索引）
    var commonReg = /[a-zA-Z_$][a-zA-Z_$0-9.[\]]*/g;
    // 匹配输入字符串中的所有常见标识符
    var commonIds = jsSnippet.match(commonReg);
    if (commonIds) {
        // 将所有匹配到的常见标识符添加到结果数组中
        ret.push.apply(ret, commonIds);
    }
    // 存储匹配到的索引标识符数组
    var indexIds = [];
    // 使用正则表达式匹配索引标识符，如 `[identifier]`
    (jsSnippet.match(/\[[a-zA-Z_][a-zA-Z_0-9\[\].]*\]/g) || []).forEach(function (i) {
        // 递归处理索引标识符内部的子标识符
        indexIds.push.apply(indexIds, getIdentifiers(i.slice(1, -1)));
    });
    // 将索引标识符添加到结果数组中
    ret.push.apply(ret, indexIds);
    // 如果未找到任何标识符，则返回包含原始输入的数组
    if (ret.length === 0) {
        return [jsSnippet];
    }
    // 返回最终的标识符数组
    return ret;
}
/**
 * 解析代码片段中的依赖关系
 *
 * 此函数旨在从给定的JavaScript代码片段中解析出所有依赖项它通过分析代码片段中的标识符，
 * 并根据这些标识符在提供的节点对象中查找对应的依赖项该函数可以限制依赖路径的最大深度，
 * 以便在解析时忽略掉过于深层的依赖
 *
 * @param jsSnippet JavaScript代码片段
 * @param exposingNodes 包含所有可能的依赖项的节点对象，键为节点名称，值为对应的节点对象
 * @param maxDepth 依赖路径的最大深度，可选参数，用于限制解析依赖时的最大深度
 * @returns 返回一个Map对象，其中键是依赖项的节点对象，值是一个字符串的Set集合，表示该节点的依赖项
 */
function parseDepends(jsSnippet, exposingNodes, maxDepth) {
    // 创建一个Map来存储解析出的依赖关系，键为依赖项的节点对象，值为依赖项的名称集合
    var depends = new Map();
    // 获取JavaScript代码片段中的所有标识符
    var identifiers = getIdentifiers(jsSnippet);
    // 遍历所有标识符，为每个标识符解析依赖
    identifiers.forEach(function (identifier) {
        // 将标识符转换为路径形式
        var subpaths = _.toPath(identifier);
        // 根据最大深度限制和路径获取依赖项的节点
        var depend = getDependNode(maxDepth ? subpaths.slice(0, maxDepth) : subpaths, exposingNodes);
        // 如果找到了依赖项，则添加到依赖集合中
        if (depend) {
            addDepend(depends, depend[0], [depend[1]]);
        }
    });
    // 返回解析出的依赖关系Map
    return depends;
}
/**
 * 根据子路径和暴露的节点集合，获取依赖的节点信息
 *
 * 此函数的目的是在一个层级结构中，根据给定的路径片段（subPaths）
 * 查找对应的节点（node）以及其完整路径。它会在提供的暴露节点集合（exposingNodes）
 * 中递层深入地查找，直到找到最终的节点或遍历完所有路径片段
 *
 * @param subPaths 字符串数组，表示路径的各个层级的标识符
 * @param exposingNodes 记录类型，键是字符串，值为泛型Node，表示可访问的节点集合
 * @returns 返回找到的节点及其路径字符串，如果未找到则返回undefined
 */
function getDependNode(subPaths, exposingNodes) {
    // 当路径为空时，直接返回undefined
    if (subPaths.length <= 0) {
        return undefined;
    }
    // 初始化待检查的节点集合为暴露的节点集合
    var nodes = exposingNodes;
    // 用于存储最终找到的节点
    var node = undefined;
    // 存储成功匹配的路径片段
    var path = [];
    // 遍历路径片段，尝试逐层查找节点
    for (var _i = 0, subPaths_1 = subPaths; _i < subPaths_1.length; _i++) {
        var subPath = subPaths_1[_i];
        // 尝试获取当前路径片段对应的子节点
        var subNode = nodes[subPath];
        // 如果当前节点集合中不存在该路径片段，或者对应的子节点不存在，则停止查找
        if (!nodes.hasOwnProperty(subPath) || !subNode) {
            break;
        }
        // 更新当前节点为找到的子节点
        node = subNode;
        // 将当前路径片段添加到已匹配路径中
        path.push(subPath);
        // 如果当前节点不是记录类型（即不再是层级结构的中间节点），则停止查找
        if (!nodeIsRecord(node)) {
            break;
        }
        // 更新待检查的节点集合为当前节点的子节点集合
        nodes = node.children;
    }
    // 根据查找结果，返回节点及其路径字符串，如果未找到则返回undefined
    return node ? [node, path.join(".")] : undefined;
}

// 全局变量黑名单，在 jsQuery/jsAction 中禁止使用
var functionBlacklist = new Set([
    "top",
    "parent",
    "document",
    "location",
    "chrome",
    "fetch",
    "XMLHttpRequest",
    "importScripts",
    "Navigator",
    "MutationObserver",
]);
var expressionBlacklist = new Set(__spreadArray(__spreadArray([], Array.from(functionBlacklist.values()), true), [
    "setTimeout",
    "setInterval",
    "setImmediate",
], false));
var globalVarNames = new Set(["window", "globalThis", "self", "global"]);
/**
 * 创建一个黑洞对象，该对象在被访问时会返回自身。
 *
 * 黑洞对象的作用是在任何属性被访问时，返回一个新的黑洞对象，
 * 避免了因为访问不存在的属性而抛出错误。这对于模拟一些
 * 应该吞噬所有交互而不产生任何效果的对象时非常有用。
 *
 * @returns 返回一个黑洞对象，该对象在被访问时返回自身。
 */
function createBlackHole() {
    // 使用 Proxy 创建黑洞对象，拦截并处理属性访问。
    return new Proxy(function () {
        return createBlackHole();
    }, {
        // 当访问黑洞对象的属性时，get 陷阱会被触发。
        get: function (t, p, r) {
            // 如果尝试访问 toString 方法，返回一个函数，该函数返回空字符串。
            if (p === "toString") {
                return function () {
                    return "";
                };
            }
            // 如果尝试访问 Symbol.toPrimitive，也返回一个函数，该函数返回空字符串。
            // 这是为了确保黑洞对象在需要原始值时的行为也是定义好的。
            if (p === Symbol.toPrimitive) {
                return function () {
                    return "";
                };
            }
            // 记录日志，指示访问了黑洞对象的哪个属性，并返回一个新的黑洞对象。
            log.log("[Sandbox] access ".concat(String(p), " on black hole, return mock object"));
            return createBlackHole();
        },
    });
}
/**
 * 创建一个模拟的全局对象
 *
 * 此函数用于创建一个模拟的全局对象（如window），该对象在处理特定属性时会有特殊行为
 * 特别适用于测试或者沙箱环境，以拦截和模拟全局对象的行为
 *
 * @param base 可选的基础对象，用于继承属性和方法，默认为空对象
 * @param blacklist 属性黑名单，定义了哪些属性会被限制访问
 * @param onSet 可选的回调函数，当属性设置时被调用
 * @param disableLimit 是否禁用黑名单限制，用于特定情况下允许访问黑名单中的属性
 * @returns 返回创建的模拟全局对象
 */
function createMockWindow(base, blacklist, onSet, disableLimit) {
    if (blacklist === void 0) { blacklist = expressionBlacklist; }
    // 创建一个代理对象，用于拦截并处理对全局对象的访问
    var win = new Proxy(Object.assign({}, base), {
        // 拦截'has'操作，始终返回true，确保所有属性都被认为是存在的
        has: function () {
            return true;
        },
        // 拦截'set'操作，处理属性设置时的行为
        set: function (target, p, newValue) {
            // 如果属性名是字符串，调用onSet回调函数（如果提供的话）
            if (typeof p === "string") {
                onSet === null || onSet === void 0 ? void 0 : onSet(p);
            }
            // 使用Reflect.set来设置目标对象的属性值，并返回结果
            return Reflect.set(target, p, newValue);
        },
        // 拦截'get'操作，处理属性访问时的行为
        get: function (target, p) {
            // 如果目标对象中已存在该属性，则直接返回
            if (p in target) {
                return Reflect.get(target, p);
            }
            // 如果请求的属性是预定义的全局变量名之一，返回模拟对象自身
            if (globalVarNames.has(p)) {
                return win;
            }
            // 如果属性在黑名单中且没有禁用限制，则创建并返回一个黑洞对象以吸收该访问
            if (typeof p === "string" && (blacklist === null || blacklist === void 0 ? void 0 : blacklist.has(p)) && !disableLimit) {
                log.log("[Sandbox] access ".concat(String(p), " on mock window, return mock object"));
                return createBlackHole();
            }
            // 对于其他属性，从原生的全局对象中获取并返回
            return getPropertyFromNativeWindow(p);
        },
    });
    // 返回创建的模拟全局对象
    return win;
}
var mockWindow;
var currentDisableLimit = false;
function clearMockWindow() {
    mockWindow = createMockWindow();
}
function isDomElement(obj) {
    return obj instanceof Element || obj instanceof HTMLCollection;
}
function getPropertyFromNativeWindow(prop) {
    var ret = Reflect.get(window, prop);
    if (typeof ret === "function" && !ret.prototype) {
        return ret.bind(window);
    }
    // get DOM element by id, serializing may cause error
    if (isDomElement(ret)) {
        return undefined;
    }
    return ret;
}
/**
 * 创建一个代理沙箱环境，用于安全地运行不受信任的代码
 *
 * @param context 用户传入的上下文环境，代理沙箱将会以此为基础创建
 * @param methods 可选参数，包含一系列允许在沙箱中使用的自定义方法
 * @param options 可选参数，用于配置沙箱的行为
 *
 * 此函数的主要目的是提供一个隔离的环境，使得在沙箱中的代码不会对全局环境造成意外的修改或破坏
 */
function proxySandbox(context, methods, options) {
    // 从选项中提取配置，默认值为禁用限制、作用域为"expression"，并应用用户提供的全局变量设置
    var _a = options || {}, _b = _a.disableLimit, disableLimit = _b === void 0 ? false : _b, _c = _a.scope, scope = _c === void 0 ? "expression" : _c, onSetGlobalVars = _a.onSetGlobalVars;
    // 检查一个属性是否受保护，受保护的属性不能被修改或删除
    var isProtectedVar = function (key) {
        return key in context || key in (methods || {}) || globalVarNames.has(key);
    };
    // 创建一个缓存对象用于存储对context对象属性的引用，以提高检索性能
    var cache = {};
    // 根据作用域选择相应的黑名单，函数作用域和表达式作用域有不同的限制
    var blacklist = scope === "function" ? functionBlacklist : expressionBlacklist;
    // 如果作用域为函数、mockWindow不存在或禁用限制的设置发生变化，则重新创建mockWindow
    if (scope === "function" || !mockWindow || disableLimit !== currentDisableLimit) {
        mockWindow = createMockWindow(mockWindow, blacklist, onSetGlobalVars, disableLimit);
    }
    // 更新当前的禁用限制设置
    currentDisableLimit = disableLimit;
    // 返回mockWindow的代理，用于拦截并处理对沙箱环境的访问和修改
    return new Proxy(mockWindow, {
        has: function (target, p) {
            // 代理所有变量，确保沙箱环境中所有属性都可以被检测到
            return true;
        },
        get: function (target, p, receiver) {
            // 处理属性访问，根据规则决定返回什么值
            if (p === Symbol.unscopables) {
                return undefined;
            }
            if (p === "toJSON") {
                return target;
            }
            if (globalVarNames.has(p)) {
                return target;
            }
            if (p in context) {
                if (p in cache) {
                    return Reflect.get(cache, p);
                }
                var value = Reflect.get(context, p, receiver);
                if (typeof value === "object" && value !== null) {
                    if (methods && p in methods) {
                        value = Object.assign({}, value, Reflect.get(methods, p));
                    }
                    Object.freeze(value);
                    Object.values(value).forEach(Object.freeze);
                }
                Reflect.set(cache, p, value);
                return value;
            }
            if (disableLimit) {
                return getPropertyFromNativeWindow(p);
            }
            return Reflect.get(target, p, receiver);
        },
        set: function (target, p, value, receiver) {
            // 防止修改受保护的变量
            if (isProtectedVar(p)) {
                throw new Error(p.toString() + " can't be modified");
            }
            return Reflect.set(target, p, value, receiver);
        },
        defineProperty: function (target, p, attributes) {
            // 防止定义新的受保护变量
            if (isProtectedVar(p)) {
                throw new Error("can't define property:" + p.toString());
            }
            return Reflect.defineProperty(target, p, attributes);
        },
        deleteProperty: function (target, p) {
            // 防止删除受保护的变量
            if (isProtectedVar(p)) {
                throw new Error("can't delete property:" + p.toString());
            }
            return Reflect.deleteProperty(target, p);
        },
        setPrototypeOf: function (target, v) {
            // 禁止修改原型链，以保持沙箱环境的隔离和稳定
            throw new Error("can't invoke setPrototypeOf");
        },
    });
}
function evalScript(script, context, methods) {
    return evalFunc("return (".concat(script, "\n);"), context, methods);
}
function evalFunc(functionBody, context, methods, options, isAsync) {
    var code = "with(this){\n    return (".concat(isAsync ? "async " : "", "function() {\n      'use strict';\n      ").concat(functionBody, ";\n    }).call(this);\n  }");
    // eslint-disable-next-line no-new-func
    var vm = new Function(code);
    var sandbox = proxySandbox(context, methods, options);
    var result = vm.call(sandbox);
    return result;
}

/**
 * 该函数将宽松的 JSON 文本字符串转换为严格的 JSON 字符串。
 *
 * @param text 要转换的宽松的 JSON 文本字符串
 * @param compact 布尔值，指示是否返回紧凑的 JSON 字符串。如果为 true，则返回紧凑的 JSON 字符串，否则返回格式化的 JSON 字符串。
 * @returns 严格的 JSON 字符串
 */
function relaxedJSONToJSON(text, compact) {
    if (text.trim().length === 0) {
        return "";
    }
    // 注意：JSON 宽松序列化比 JSON.parse 快大约 70 倍
    return toJson(text, compact);
}

function call(content, context, segment) {
    if (!content) {
        return new ValueAndMsg("", undefined, { segments: [{ value: segment, success: true }] });
    }
    try {
        var value = evalScript(content, context);
        return new ValueAndMsg(value, undefined, { segments: [{ value: segment, success: true }] });
    }
    catch (err) {
        return new ValueAndMsg("", getErrorMessage(err), {
            segments: [{ value: segment, success: false }],
        });
    }
}
function evalDefault(unevaledValue, context) {
    return new DefaultParser(unevaledValue, context).parse();
}
var DefaultParser = /** @class */ (function () {
    function DefaultParser(unevaledValue, context) {
        this.context = context;
        this.valueAndMsgs = [];
        this.segments = getDynamicStringSegments(unevaledValue.trim());
    }
    DefaultParser.prototype.parse = function () {
        var _a;
        try {
            var object = this.parseObject();
            if (this.valueAndMsgs.length === 0) {
                return new ValueAndMsg(object);
            }
            return new ValueAndMsg(object, (_a = _.find(this.valueAndMsgs, "msg")) === null || _a === void 0 ? void 0 : _a.msg, {
                segments: this.valueAndMsgs.flatMap(function (v) { var _a, _b; return (_b = (_a = v === null || v === void 0 ? void 0 : v.extra) === null || _a === void 0 ? void 0 : _a.segments) !== null && _b !== void 0 ? _b : []; }),
            });
        }
        catch (err) {
            // return null, the later transform will determine the default value
            return new ValueAndMsg("", getErrorMessage(err));
        }
    };
    DefaultParser.prototype.parseObject = function () {
        var _this = this;
        var values = this.segments.map(function (segment) {
            return isDynamicSegment(segment) ? _this.evalDynamicSegment(segment) : segment;
        });
        return values.length === 1 ? values[0] : values.join("");
    };
    DefaultParser.prototype.evalDynamicSegment = function (segment) {
        var valueAndMsg = call(segment.slice(2, -2).trim(), this.context, segment);
        this.valueAndMsgs.push(valueAndMsg);
        return valueAndMsg.value;
    };
    return DefaultParser;
}());
function evalJson(unevaledValue, context) {
    return new RelaxedJsonParser(unevaledValue, context).parse();
}
// this will also be used in node-service
var RelaxedJsonParser = /** @class */ (function (_super) {
    __extends(RelaxedJsonParser, _super);
    function RelaxedJsonParser(unevaledValue, context) {
        var _this = _super.call(this, unevaledValue, context) || this;
        _this.evalIndexedObject = _this.evalIndexedObject.bind(_this);
        return _this;
    }
    RelaxedJsonParser.prototype.parseObject = function () {
        try {
            return this.parseRelaxedJson();
        }
        catch (e) {
            return _super.prototype.parseObject.call(this);
        }
    };
    RelaxedJsonParser.prototype.parseRelaxedJson = function () {
        // replace the original {{...}} as relaxed-json adaptive \{\{ + ${index} + \}\}
        var indexedRelaxedJsonString = this.segments
            .map(function (s, i) { return (isDynamicSegment(s) ? "\\{\\{" + i + "\\}\\}" : s); })
            .join("");
        if (indexedRelaxedJsonString.length === 0) {
            // return empty, let the later transform determines the default value
            return "";
        }
        // transform to standard JSON strings with RELAXED JSON
        // here is a trick: if "\{\{ \}\}" is in quotes, keep it unchanged; otherwise transform to "{{ }}"
        var indexedJsonString = relaxedJSONToJSON(indexedRelaxedJsonString, true);
        // here use eval instead of JSON.parse, in order to support escaping like JavaScript. JSON.parse will cause error when escaping non-spicial char
        // since eval support escaping, replace "\{\{ + ${index} + \}\}" as "\\{\\{ + ${index} + \\}\\}"
        var indexedJsonObject = evalScript(indexedJsonString.replace(/\\{\\{\d+\\}\\}/g, function (s) { return "\\\\{\\\\{" + s.slice(4, -4) + "\\\\}\\\\}"; }), {});
        return this.evalIndexedObject(indexedJsonObject);
    };
    RelaxedJsonParser.prototype.evalIndexedObject = function (obj) {
        if (typeof obj === "string") {
            return this.evalIndexedStringToObject(obj);
        }
        if (typeof obj !== "object" || obj === null) {
            return obj;
        }
        if (Array.isArray(obj)) {
            return obj.map(this.evalIndexedObject);
        }
        var ret = {};
        for (var _i = 0, _a = Object.entries(obj); _i < _a.length; _i++) {
            var _b = _a[_i], key = _b[0], value = _b[1];
            ret[this.evalIndexedStringToString(key)] = this.evalIndexedObject(value);
        }
        return ret;
    };
    RelaxedJsonParser.prototype.evalIndexedStringToObject = function (indexedString) {
        // if the whole string is "{{ + ${index} + }}", it indicates that the original "{{...}}" is not in quotes, as a standalone JSON value.
        if (indexedString.match(/^{{\d+}}$/)) {
            return this.evalIndexedSnippet(indexedString);
        }
        return this.evalIndexedStringToString(indexedString);
    };
    RelaxedJsonParser.prototype.evalIndexedStringToString = function (indexedString) {
        var _this = this;
        // replace all {{ + ${index} + }} and \{\{ + ${index} \}\}
        return indexedString.replace(/({{\d+}})|(\\{\\{\d+\\}\\})/g, function (s) { return _this.evalIndexedSnippet(s) + ""; });
    };
    // eval {{ + ${index} + }} or \{\{ + ${index} + \}\}
    RelaxedJsonParser.prototype.evalIndexedSnippet = function (snippet) {
        var index = parseInt(snippet.startsWith("{{") ? snippet.slice(2, -2) : snippet.slice(4, -4));
        if (index >= 0 && index < this.segments.length) {
            var segment = this.segments[index];
            if (isDynamicSegment(segment)) {
                return this.evalDynamicSegment(segment);
            }
        }
        return snippet;
    };
    return RelaxedJsonParser;
}(DefaultParser));
function evalFunction(unevaledValue, context, methods, isAsync) {
    try {
        return new ValueAndMsg(function (args, runInHost, scope) {
            if (runInHost === void 0) { runInHost = false; }
            if (scope === void 0) { scope = "function"; }
            return evalFunc(unevaledValue.startsWith("return")
                ? unevaledValue + "\n"
                : "return ".concat(isAsync ? "async " : "", "function(){'use strict'; ").concat(unevaledValue, "\n}()"), args ? __assign(__assign({}, context), args) : context, methods, { disableLimit: runInHost, scope: scope }, isAsync);
        });
    }
    catch (err) {
        return new ValueAndMsg(function () { }, getErrorMessage(err));
    }
}
function evalFunctionResult(unevaledValue, context, methods) {
    return __awaiter(this, void 0, void 0, function () {
        var valueAndMsg, _a, err_1;
        return __generator(this, function (_b) {
            switch (_b.label) {
                case 0:
                    valueAndMsg = evalFunction(unevaledValue, context, methods, true);
                    if (valueAndMsg.hasError()) {
                        return [2 /*return*/, new ValueAndMsg("", valueAndMsg.msg)];
                    }
                    _b.label = 1;
                case 1:
                    _b.trys.push([1, 3, , 4]);
                    _a = ValueAndMsg.bind;
                    return [4 /*yield*/, valueAndMsg.value()];
                case 2: return [2 /*return*/, new (_a.apply(ValueAndMsg, [void 0, _b.sent()]))()];
                case 3:
                    err_1 = _b.sent();
                    return [2 /*return*/, new ValueAndMsg("", getErrorMessage(err_1))];
                case 4: return [2 /*return*/];
            }
        });
    });
}
function string2Fn(unevaledValue, type, methods, isAsync) {
    if (type) {
        switch (type) {
            case "JSON":
                return function (context) { return evalJson(unevaledValue, context); };
            case "Function":
                return function (context) { return evalFunction(unevaledValue, context, methods, isAsync); };
        }
    }
    return function (context) { return evalDefault(unevaledValue, context); };
}

var IS_FETCHING_FIELD = "isFetching";
var LATEST_END_TIME_FIELD = "latestEndTime";
var TRIGGER_TYPE_FIELD = "triggerType";
/**
 * 用户输入节点
 *
 * @remarks
 * CodeNode 应解决循环依赖问题
 * 我们可以假设循环依赖仅由 CodeNode 引入
 *
 * FIXME(libin): 区分 Json CodeNode，因为 wrapContext 可能导致问题。
 */
var CodeNode = /** @class */ (function (_super) {
    __extends(CodeNode, _super);
    function CodeNode(unevaledValue, options) {
        var _this = this;
        var _a, _b;
        _this = _super.call(this) || this;
        _this.unevaledValue = unevaledValue;
        _this.options = options;
        _this.type = "input";
        _this.directDepends = new Map();
        _this.codeType = options === null || options === void 0 ? void 0 : options.codeType;
        _this.evalWithMethods = (_a = options === null || options === void 0 ? void 0 : options.evalWithMethods) !== null && _a !== void 0 ? _a : true;
        _this.isAsync = (_b = options === null || options === void 0 ? void 0 : options.isAsync) !== null && _b !== void 0 ? _b : false;
        return _this;
    }
    // FIXME: optimize later
    CodeNode.prototype.convertedValue = function () {
        if (this.codeType === "Function") {
            return "{{function(){".concat(this.unevaledValue, "}}}");
        }
        return this.unevaledValue;
    };
    /**
     * 使用缓存机制过滤节点
     * 当前方法旨在通过缓存来优化节点的过滤过程，避免循环引用导致的问题
     *
     * @param exposingNodes 一个包含字符串键和Node实例值的记录，表示待过滤的节点集合
     * @returns 返回一个Map对象，其中Node实例作为键，Set对象（包含字符串类型的路径）作为值，
     * 表示过滤后的节点及其相关路径
     */
    CodeNode.prototype.filterNodes = function (exposingNodes) {
        // 检查是否存在循环引用，如果是，则直接返回空Map
        if (!!this.evalCache.inFilterNodes) {
            return new Map();
        }
        // 标记当前状态为正在过滤节点
        this.evalCache.inFilterNodes = true;
        try {
            // 首先过滤直接依赖项
            var filteredDepends = this.filterDirectDepends(exposingNodes);
            // log.log("unevaledValue: ", this.unevaledValue, "\nfilteredDepends:", filteredDepends);
            // 初始化结果Map，并添加直接依赖项
            var result_1 = addDepends(new Map(), filteredDepends);
            // 迭代过滤后的依赖项，递归添加节点的过滤结果
            filteredDepends.forEach(function (paths, node) {
                addDepends(result_1, node.filterNodes(exposingNodes));
            });
            // 为FetchCheck添加isFetching和latestEndTime节点
            // 从转换后的值中过滤出顶级依赖项
            var topDepends = filterDepends(this.convertedValue(), exposingNodes, 1);
            // 遍历顶级依赖项，为特定字段添加依赖
            topDepends.forEach(function (paths, depend) {
                // 检查依赖项是否为记录类型节点
                if (nodeIsRecord(depend)) {
                    var _loop_1 = function (field) {
                        var node = depend.children[field];
                        // 如果节点存在，则添加到结果中
                        if (node) {
                            addDepend(result_1, node, Array.from(paths).map(function (p) { return p + "." + field; }));
                        }
                    };
                    // 为每个需要关注的字段生成依赖项
                    for (var _i = 0, _a = [IS_FETCHING_FIELD, LATEST_END_TIME_FIELD]; _i < _a.length; _i++) {
                        var field = _a[_i];
                        _loop_1(field);
                    }
                }
            });
            // 返回最终的过滤结果
            return result_1;
        }
        finally {
            // 重置标记，表示不再处于过滤节点的状态
            this.evalCache.inFilterNodes = false;
        }
    };
    /**
     * 该方法筛选出当前节点直接依赖的暴露节点
     * 使用 memoized 装饰器缓存方法的结果，以提高性能
     * 当 convertedValue 和 exposingNodes 不变时，避免重复计算
     */
    CodeNode.prototype.filterDirectDepends = function (exposingNodes) {
        return filterDepends(this.convertedValue(), exposingNodes);
    };
    /**
     * 评估当前节点的值，同时检测循环依赖并处理。
     *
     * 此方法主要用于计算当前节点的值，它依赖于其他节点的值。
     * 如果存在循环依赖，会进行特殊处理，返回一个错误信息。
     *
     * @param exposingNodes - 一个记录，键为节点名，值为节点对象。这些节点是当前评估节点所依赖的节点。
     * @param methods - 可选参数，用于评估的方法对象，如果指定，则使用这些方法进行评估。
     * @returns 返回一个 ValueAndMsg 对象，包含评估结果和可能的错误信息。
     */
    CodeNode.prototype.justEval = function (exposingNodes, methods) {
        // log.log("justEval: ", this, "\nexposingNodes: ", exposingNodes);
        // 为了避免死循环，检测当前节点是否正在被评估
        if (!!this.evalCache.inEval) {
            // 发现循环评估，设置标记并返回空值
            this.evalCache.cyclic = true;
            return new ValueAndMsg("");
        }
        // 标记当前节点正在被评估
        this.evalCache.inEval = true;
        try {
            // 筛选直接依赖的节点
            var dependingNodeMap = this.filterDirectDepends(exposingNodes);
            // 将筛选后的依赖节点赋值给直接依赖属性
            this.directDepends = dependingNodeMap;
            // 合并具有相同名称的节点
            var dependingNodes = mergeNodesWithSameName(dependingNodeMap);
            // 将未评估的值和相关配置转换为函数
            var fn = string2Fn(this.unevaledValue, this.codeType, this.evalWithMethods ? methods : {}, this.isAsync);
            // 使用依赖节点和转换后的函数创建评估节点
            var evalNode = withFunction(fromRecord(dependingNodes), fn);
            // 评估当前节点的值
            var valueAndMsg = evalNode.evaluate(exposingNodes);
            // 如果存在循环依赖，处理返回值和错误信息
            if (this.evalCache.cyclic) {
                valueAndMsg = new ValueAndMsg(valueAndMsg.value, (valueAndMsg.msg ? valueAndMsg.msg + "\n" : "") + dependsErrorMessage(this), fixCyclic(valueAndMsg.extra, exposingNodes));
            }
            return valueAndMsg;
        }
        finally {
            // 评估结束后，重置正在被评估的标记
            this.evalCache.inEval = false;
        }
    };
    /**
     * 重写getChildren方法，用于获取当前节点的所有子节点
     * 在这个特定的实现中，子节点实际上是直接依赖的键集合
     *
     * @returns {Node<unknown>[]} 子节点的数组，如果不存在直接依赖，则返回空数组
     */
    CodeNode.prototype.getChildren = function () {
        // 如果directDepends存在直接依赖，将其键转换为数组并返回
        if (this.directDepends) {
            return Array.from(this.directDepends.keys());
        }
        // 如果没有直接依赖，返回空数组
        return [];
    };
    /**
     * 重写dependValues方法
     * 该方法用于获取当前节点直接依赖的值
     * 通过遍历直接依赖的节点和其对应的路径，从评估缓存中获取值，并收集到一个对象中返回
     *
     * @returns {Record<string, unknown>} 一个键值对对象，键为依赖的路径，值为对应节点的评估缓存值
     */
    CodeNode.prototype.dependValues = function () {
        // 初始化一个空对象，用于收集所有依赖的值
        var ret = {};
        // 遍历当前节点的直接依赖项，包括节点和对应的路径
        this.directDepends.forEach(function (paths, node) {
            // 检查依赖的节点是否为AbstractNode的实例
            if (node instanceof AbstractNode) {
                // 遍历当前节点的所有路径
                paths.forEach(function (path) {
                    // 将当前节点评估缓存的值，根据路径添加到返回对象中
                    ret[path] = node.evalCache.value;
                });
            }
        });
        // 返回收集完成的依赖值对象
        return ret;
    };
    /**
     * 使用memoized装饰器优化的fetchInfo方法重写
     * 本方法旨在获取当前依赖关系中的 fetching 状态和准备状态
     * 它会评估当前依赖关系以及依赖于当前节点的其他节点的状态
     *
     * @param exposingNodes 一个包含多个Node的记录，节点类型为<unknown>
     *                      这些节点可能影响当前节点的状态
     * @param options 可选的fetchInfo选项，包含一些特殊的忽略规则
     * @returns FetchInfo对象，包含isFetching和ready两个属性
     *
     * 本方法首先检查是否存在循环依赖 fetching 状态
     * 如果当前节点正在 fetching，则直接返回相应的状态
     * 否则，评估直接依赖项和依赖于当前节点的节点的状态
     * 最终汇总这些状态并返回综合的 fetching 和 ready 状态
     */
    CodeNode.prototype.fetchInfo = function (exposingNodes, options) {
        // 检查是否存在循环依赖的 fetching 状态
        if (!!this.evalCache.inIsFetching) {
            return {
                isFetching: false,
                ready: true,
            };
        }
        // 标记当前节点为正在 fetching
        this.evalCache.inIsFetching = true;
        try {
            // 获取直接依赖项
            var topDepends = filterDepends(this.convertedValue(), exposingNodes, 1);
            // 初始化 fetching 和 ready 状态
            var isFetching_1 = false;
            var ready_1 = true;
            // 遍历直接依赖项，评估它们的状态
            topDepends.forEach(function (paths, depend) {
                var value = depend.evaluate(exposingNodes);
                // 如果根据选项忽略手动触发依赖的 ready 状态，则跳过当前依赖
                if ((options === null || options === void 0 ? void 0 : options.ignoreManualDepReadyStatus) &&
                    _.has(value, TRIGGER_TYPE_FIELD) &&
                    value.triggerType === "manual") {
                    return;
                }
                // 如果依赖项正在 fetching，则更新 fetching 状态
                if (_.has(value, IS_FETCHING_FIELD)) {
                    isFetching_1 = isFetching_1 || value.isFetching === true;
                }
                // 如果依赖项的最新结束时间未定义，则更新 ready 状态
                if (_.has(value, LATEST_END_TIME_FIELD)) {
                    ready_1 = ready_1 && value.latestEndTime > 0;
                }
            });
            // 获取依赖于当前节点的其他节点
            var dependingNodeMap = this.filterNodes(exposingNodes);
            // 遍历这些节点，评估它们的 fetching 和 ready 状态
            dependingNodeMap.forEach(function (paths, depend) {
                var fi = depend.fetchInfo(exposingNodes, options);
                // 综合当前节点和依赖节点的状态
                isFetching_1 = isFetching_1 || fi.isFetching;
                ready_1 = ready_1 && fi.ready;
            });
            // 返回综合的 fetching 和 ready 状态
            return {
                isFetching: isFetching_1,
                ready: ready_1,
            };
        }
        finally {
            // 重置 fetching 标记
            this.evalCache.inIsFetching = false;
        }
    };
    __decorate([
        memoized()
    ], CodeNode.prototype, "filterNodes", null);
    __decorate([
        memoized()
    ], CodeNode.prototype, "filterDirectDepends", null);
    __decorate([
        memoized()
    ], CodeNode.prototype, "fetchInfo", null);
    return CodeNode;
}(AbstractNode));
/**
 * 将未评估的值转换为FunctionNode对象
 *
 * 此函数用于接收一个字符串形式的未评估值，并将其转换为一个FunctionNode对象
 * FunctionNode对象包含一个CodeNode，用于表示原始的未评估值，以及一个用于提取值的函数
 *
 * @param unevaledValue 未评估的值，通常是一个字符串
 * @returns 返回一个FunctionNode对象，该对象包含CodeNode和值提取函数
 */
function fromUnevaledValue(unevaledValue) {
    return new FunctionNode(new CodeNode(unevaledValue), function (valueAndMsg) { return valueAndMsg.value; });
}
/**
 * 修复循环引用问题
 *
 * 当extra参数中包含可能引起循环引用的值时，本函数将调整这些值以解决循环引用问题
 * 循环引用发生在exposingNodes中的节点互相引用形成一个循环的情况，这可能导致某些算法或逻辑无限循环
 * 该函数通过检查每个段（segment）是否成功来处理这个问题，如果一个段的成功标志为true且存在循环引用，
 * 则将其成功标志设置为false如此，可以避免因循环引用而导致的问题
 *
 * @param extra 可能包含引起循环引用的数据的额外值信息如果为undefined，则不需要修复
 * @param exposingNodes 一个记录所有可能参与循环引用的节点的集合，通过其键值可以访问对应的节点
 * @returns 返回经过循环引用修复后的extra值如果输入为undefined，则可能返回undefined
 */
function fixCyclic(extra, exposingNodes) {
    var _a;
    // 遍历extra中所有段（如果存在），检查并调整成功标志以避免循环引用
    (_a = extra === null || extra === void 0 ? void 0 : extra.segments) === null || _a === void 0 ? void 0 : _a.forEach(function (segment) {
        // 根据是否有循环引用，更新段的成功标志
        if (segment.success) {
            segment.success = !hasCycle(segment.value, exposingNodes);
        }
    });
    // 返回经过修复的extra值
    return extra;
}

/**
 * 评估以获取FetchInfo或获取状态
 */
var FetchCheckNode = /** @class */ (function (_super) {
    __extends(FetchCheckNode, _super);
    function FetchCheckNode(child, options) {
        var _this = _super.call(this) || this;
        _this.child = child;
        _this.options = options;
        _this.type = "fetchCheck";
        return _this;
    }
    FetchCheckNode.prototype.filterNodes = function (exposingNodes) {
        return this.child.filterNodes(exposingNodes);
    };
    FetchCheckNode.prototype.justEval = function (exposingNodes) {
        return this.fetchInfo(exposingNodes);
    };
    FetchCheckNode.prototype.getChildren = function () {
        return [this.child];
    };
    FetchCheckNode.prototype.dependValues = function () {
        return this.child.dependValues();
    };
    FetchCheckNode.prototype.fetchInfo = function (exposingNodes) {
        return this.child.fetchInfo(exposingNodes, this.options);
    };
    __decorate([
        memoized()
    ], FetchCheckNode.prototype, "filterNodes", null);
    __decorate([
        memoized()
    ], FetchCheckNode.prototype, "fetchInfo", null);
    return FetchCheckNode;
}(AbstractNode));
function isFetching(node) {
    return new FetchCheckNode(node);
}

const perf =
  typeof performance === 'object' &&
  performance &&
  typeof performance.now === 'function'
    ? performance
    : Date;

const hasAbortController = typeof AbortController === 'function';

// minimal backwards-compatibility polyfill
// this doesn't have nearly all the checks and whatnot that
// actual AbortController/Signal has, but it's enough for
// our purposes, and if used properly, behaves the same.
const AC = hasAbortController
  ? AbortController
  : class AbortController {
      constructor() {
        this.signal = new AS();
      }
      abort(reason = new Error('This operation was aborted')) {
        this.signal.reason = this.signal.reason || reason;
        this.signal.aborted = true;
        this.signal.dispatchEvent({
          type: 'abort',
          target: this.signal,
        });
      }
    };

const hasAbortSignal = typeof AbortSignal === 'function';
// Some polyfills put this on the AC class, not global
const hasACAbortSignal = typeof AC.AbortSignal === 'function';
const AS = hasAbortSignal
  ? AbortSignal
  : hasACAbortSignal
  ? AC.AbortController
  : class AbortSignal {
      constructor() {
        this.reason = undefined;
        this.aborted = false;
        this._listeners = [];
      }
      dispatchEvent(e) {
        if (e.type === 'abort') {
          this.aborted = true;
          this.onabort(e);
          this._listeners.forEach(f => f(e), this);
        }
      }
      onabort() {}
      addEventListener(ev, fn) {
        if (ev === 'abort') {
          this._listeners.push(fn);
        }
      }
      removeEventListener(ev, fn) {
        if (ev === 'abort') {
          this._listeners = this._listeners.filter(f => f !== fn);
        }
      }
    };

const warned = new Set();
const deprecatedOption = (opt, instead) => {
  const code = `LRU_CACHE_OPTION_${opt}`;
  if (shouldWarn(code)) {
    warn(code, `${opt} option`, `options.${instead}`, LRUCache);
  }
};
const deprecatedMethod = (method, instead) => {
  const code = `LRU_CACHE_METHOD_${method}`;
  if (shouldWarn(code)) {
    const { prototype } = LRUCache;
    const { get } = Object.getOwnPropertyDescriptor(prototype, method);
    warn(code, `${method} method`, `cache.${instead}()`, get);
  }
};
const deprecatedProperty = (field, instead) => {
  const code = `LRU_CACHE_PROPERTY_${field}`;
  if (shouldWarn(code)) {
    const { prototype } = LRUCache;
    const { get } = Object.getOwnPropertyDescriptor(prototype, field);
    warn(code, `${field} property`, `cache.${instead}`, get);
  }
};

const emitWarning = (...a) => {
  typeof process === 'object' &&
  process &&
  typeof process.emitWarning === 'function'
    ? process.emitWarning(...a)
    : console.error(...a);
};

const shouldWarn = code => !warned.has(code);

const warn = (code, what, instead, fn) => {
  warned.add(code);
  const msg = `The ${what} is deprecated. Please use ${instead} instead.`;
  emitWarning(msg, 'DeprecationWarning', code, fn);
};

const isPosInt = n => n && n === Math.floor(n) && n > 0 && isFinite(n);

/* istanbul ignore next - This is a little bit ridiculous, tbh.
 * The maximum array length is 2^32-1 or thereabouts on most JS impls.
 * And well before that point, you're caching the entire world, I mean,
 * that's ~32GB of just integers for the next/prev links, plus whatever
 * else to hold that many keys and values.  Just filling the memory with
 * zeroes at init time is brutal when you get that big.
 * But why not be complete?
 * Maybe in the future, these limits will have expanded. */
const getUintArray = max =>
  !isPosInt(max)
    ? null
    : max <= Math.pow(2, 8)
    ? Uint8Array
    : max <= Math.pow(2, 16)
    ? Uint16Array
    : max <= Math.pow(2, 32)
    ? Uint32Array
    : max <= Number.MAX_SAFE_INTEGER
    ? ZeroArray
    : null;

class ZeroArray extends Array {
  constructor(size) {
    super(size);
    this.fill(0);
  }
}

class Stack {
  constructor(max) {
    if (max === 0) {
      return []
    }
    const UintArray = getUintArray(max);
    this.heap = new UintArray(max);
    this.length = 0;
  }
  push(n) {
    this.heap[this.length++] = n;
  }
  pop() {
    return this.heap[--this.length]
  }
}

class LRUCache {
  constructor(options = {}) {
    const {
      max = 0,
      ttl,
      ttlResolution = 1,
      ttlAutopurge,
      updateAgeOnGet,
      updateAgeOnHas,
      allowStale,
      dispose,
      disposeAfter,
      noDisposeOnSet,
      noUpdateTTL,
      maxSize = 0,
      maxEntrySize = 0,
      sizeCalculation,
      fetchMethod,
      fetchContext,
      noDeleteOnFetchRejection,
      noDeleteOnStaleGet,
      allowStaleOnFetchRejection,
      allowStaleOnFetchAbort,
      ignoreFetchAbort,
    } = options;

    // deprecated options, don't trigger a warning for getting them if
    // the thing being passed in is another LRUCache we're copying.
    const { length, maxAge, stale } =
      options instanceof LRUCache ? {} : options;

    if (max !== 0 && !isPosInt(max)) {
      throw new TypeError('max option must be a nonnegative integer')
    }

    const UintArray = max ? getUintArray(max) : Array;
    if (!UintArray) {
      throw new Error('invalid max value: ' + max)
    }

    this.max = max;
    this.maxSize = maxSize;
    this.maxEntrySize = maxEntrySize || this.maxSize;
    this.sizeCalculation = sizeCalculation || length;
    if (this.sizeCalculation) {
      if (!this.maxSize && !this.maxEntrySize) {
        throw new TypeError(
          'cannot set sizeCalculation without setting maxSize or maxEntrySize'
        )
      }
      if (typeof this.sizeCalculation !== 'function') {
        throw new TypeError('sizeCalculation set to non-function')
      }
    }

    this.fetchMethod = fetchMethod || null;
    if (this.fetchMethod && typeof this.fetchMethod !== 'function') {
      throw new TypeError(
        'fetchMethod must be a function if specified'
      )
    }

    this.fetchContext = fetchContext;
    if (!this.fetchMethod && fetchContext !== undefined) {
      throw new TypeError(
        'cannot set fetchContext without fetchMethod'
      )
    }

    this.keyMap = new Map();
    this.keyList = new Array(max).fill(null);
    this.valList = new Array(max).fill(null);
    this.next = new UintArray(max);
    this.prev = new UintArray(max);
    this.head = 0;
    this.tail = 0;
    this.free = new Stack(max);
    this.initialFill = 1;
    this.size = 0;

    if (typeof dispose === 'function') {
      this.dispose = dispose;
    }
    if (typeof disposeAfter === 'function') {
      this.disposeAfter = disposeAfter;
      this.disposed = [];
    } else {
      this.disposeAfter = null;
      this.disposed = null;
    }
    this.noDisposeOnSet = !!noDisposeOnSet;
    this.noUpdateTTL = !!noUpdateTTL;
    this.noDeleteOnFetchRejection = !!noDeleteOnFetchRejection;
    this.allowStaleOnFetchRejection = !!allowStaleOnFetchRejection;
    this.allowStaleOnFetchAbort = !!allowStaleOnFetchAbort;
    this.ignoreFetchAbort = !!ignoreFetchAbort;

    // NB: maxEntrySize is set to maxSize if it's set
    if (this.maxEntrySize !== 0) {
      if (this.maxSize !== 0) {
        if (!isPosInt(this.maxSize)) {
          throw new TypeError(
            'maxSize must be a positive integer if specified'
          )
        }
      }
      if (!isPosInt(this.maxEntrySize)) {
        throw new TypeError(
          'maxEntrySize must be a positive integer if specified'
        )
      }
      this.initializeSizeTracking();
    }

    this.allowStale = !!allowStale || !!stale;
    this.noDeleteOnStaleGet = !!noDeleteOnStaleGet;
    this.updateAgeOnGet = !!updateAgeOnGet;
    this.updateAgeOnHas = !!updateAgeOnHas;
    this.ttlResolution =
      isPosInt(ttlResolution) || ttlResolution === 0
        ? ttlResolution
        : 1;
    this.ttlAutopurge = !!ttlAutopurge;
    this.ttl = ttl || maxAge || 0;
    if (this.ttl) {
      if (!isPosInt(this.ttl)) {
        throw new TypeError(
          'ttl must be a positive integer if specified'
        )
      }
      this.initializeTTLTracking();
    }

    // do not allow completely unbounded caches
    if (this.max === 0 && this.ttl === 0 && this.maxSize === 0) {
      throw new TypeError(
        'At least one of max, maxSize, or ttl is required'
      )
    }
    if (!this.ttlAutopurge && !this.max && !this.maxSize) {
      const code = 'LRU_CACHE_UNBOUNDED';
      if (shouldWarn(code)) {
        warned.add(code);
        const msg =
          'TTL caching without ttlAutopurge, max, or maxSize can ' +
          'result in unbounded memory consumption.';
        emitWarning(msg, 'UnboundedCacheWarning', code, LRUCache);
      }
    }

    if (stale) {
      deprecatedOption('stale', 'allowStale');
    }
    if (maxAge) {
      deprecatedOption('maxAge', 'ttl');
    }
    if (length) {
      deprecatedOption('length', 'sizeCalculation');
    }
  }

  getRemainingTTL(key) {
    return this.has(key, { updateAgeOnHas: false }) ? Infinity : 0
  }

  initializeTTLTracking() {
    this.ttls = new ZeroArray(this.max);
    this.starts = new ZeroArray(this.max);

    this.setItemTTL = (index, ttl, start = perf.now()) => {
      this.starts[index] = ttl !== 0 ? start : 0;
      this.ttls[index] = ttl;
      if (ttl !== 0 && this.ttlAutopurge) {
        const t = setTimeout(() => {
          if (this.isStale(index)) {
            this.delete(this.keyList[index]);
          }
        }, ttl + 1);
        /* istanbul ignore else - unref() not supported on all platforms */
        if (t.unref) {
          t.unref();
        }
      }
    };

    this.updateItemAge = index => {
      this.starts[index] = this.ttls[index] !== 0 ? perf.now() : 0;
    };

    this.statusTTL = (status, index) => {
      if (status) {
        status.ttl = this.ttls[index];
        status.start = this.starts[index];
        status.now = cachedNow || getNow();
        status.remainingTTL = status.now + status.ttl - status.start;
      }
    };

    // debounce calls to perf.now() to 1s so we're not hitting
    // that costly call repeatedly.
    let cachedNow = 0;
    const getNow = () => {
      const n = perf.now();
      if (this.ttlResolution > 0) {
        cachedNow = n;
        const t = setTimeout(
          () => (cachedNow = 0),
          this.ttlResolution
        );
        /* istanbul ignore else - not available on all platforms */
        if (t.unref) {
          t.unref();
        }
      }
      return n
    };

    this.getRemainingTTL = key => {
      const index = this.keyMap.get(key);
      if (index === undefined) {
        return 0
      }
      return this.ttls[index] === 0 || this.starts[index] === 0
        ? Infinity
        : this.starts[index] +
            this.ttls[index] -
            (cachedNow || getNow())
    };

    this.isStale = index => {
      return (
        this.ttls[index] !== 0 &&
        this.starts[index] !== 0 &&
        (cachedNow || getNow()) - this.starts[index] >
          this.ttls[index]
      )
    };
  }
  updateItemAge(_index) {}
  statusTTL(_status, _index) {}
  setItemTTL(_index, _ttl, _start) {}
  isStale(_index) {
    return false
  }

  initializeSizeTracking() {
    this.calculatedSize = 0;
    this.sizes = new ZeroArray(this.max);
    this.removeItemSize = index => {
      this.calculatedSize -= this.sizes[index];
      this.sizes[index] = 0;
    };
    this.requireSize = (k, v, size, sizeCalculation) => {
      // provisionally accept background fetches.
      // actual value size will be checked when they return.
      if (this.isBackgroundFetch(v)) {
        return 0
      }
      if (!isPosInt(size)) {
        if (sizeCalculation) {
          if (typeof sizeCalculation !== 'function') {
            throw new TypeError('sizeCalculation must be a function')
          }
          size = sizeCalculation(v, k);
          if (!isPosInt(size)) {
            throw new TypeError(
              'sizeCalculation return invalid (expect positive integer)'
            )
          }
        } else {
          throw new TypeError(
            'invalid size value (must be positive integer). ' +
              'When maxSize or maxEntrySize is used, sizeCalculation or size ' +
              'must be set.'
          )
        }
      }
      return size
    };
    this.addItemSize = (index, size, status) => {
      this.sizes[index] = size;
      if (this.maxSize) {
        const maxSize = this.maxSize - this.sizes[index];
        while (this.calculatedSize > maxSize) {
          this.evict(true);
        }
      }
      this.calculatedSize += this.sizes[index];
      if (status) {
        status.entrySize = size;
        status.totalCalculatedSize = this.calculatedSize;
      }
    };
  }
  removeItemSize(_index) {}
  addItemSize(_index, _size) {}
  requireSize(_k, _v, size, sizeCalculation) {
    if (size || sizeCalculation) {
      throw new TypeError(
        'cannot set size without setting maxSize or maxEntrySize on cache'
      )
    }
  }

  *indexes({ allowStale = this.allowStale } = {}) {
    if (this.size) {
      for (let i = this.tail; true; ) {
        if (!this.isValidIndex(i)) {
          break
        }
        if (allowStale || !this.isStale(i)) {
          yield i;
        }
        if (i === this.head) {
          break
        } else {
          i = this.prev[i];
        }
      }
    }
  }

  *rindexes({ allowStale = this.allowStale } = {}) {
    if (this.size) {
      for (let i = this.head; true; ) {
        if (!this.isValidIndex(i)) {
          break
        }
        if (allowStale || !this.isStale(i)) {
          yield i;
        }
        if (i === this.tail) {
          break
        } else {
          i = this.next[i];
        }
      }
    }
  }

  isValidIndex(index) {
    return (
      index !== undefined &&
      this.keyMap.get(this.keyList[index]) === index
    )
  }

  *entries() {
    for (const i of this.indexes()) {
      if (
        this.valList[i] !== undefined &&
        this.keyList[i] !== undefined &&
        !this.isBackgroundFetch(this.valList[i])
      ) {
        yield [this.keyList[i], this.valList[i]];
      }
    }
  }
  *rentries() {
    for (const i of this.rindexes()) {
      if (
        this.valList[i] !== undefined &&
        this.keyList[i] !== undefined &&
        !this.isBackgroundFetch(this.valList[i])
      ) {
        yield [this.keyList[i], this.valList[i]];
      }
    }
  }

  *keys() {
    for (const i of this.indexes()) {
      if (
        this.keyList[i] !== undefined &&
        !this.isBackgroundFetch(this.valList[i])
      ) {
        yield this.keyList[i];
      }
    }
  }
  *rkeys() {
    for (const i of this.rindexes()) {
      if (
        this.keyList[i] !== undefined &&
        !this.isBackgroundFetch(this.valList[i])
      ) {
        yield this.keyList[i];
      }
    }
  }

  *values() {
    for (const i of this.indexes()) {
      if (
        this.valList[i] !== undefined &&
        !this.isBackgroundFetch(this.valList[i])
      ) {
        yield this.valList[i];
      }
    }
  }
  *rvalues() {
    for (const i of this.rindexes()) {
      if (
        this.valList[i] !== undefined &&
        !this.isBackgroundFetch(this.valList[i])
      ) {
        yield this.valList[i];
      }
    }
  }

  [Symbol.iterator]() {
    return this.entries()
  }

  find(fn, getOptions) {
    for (const i of this.indexes()) {
      const v = this.valList[i];
      const value = this.isBackgroundFetch(v)
        ? v.__staleWhileFetching
        : v;
      if (value === undefined) continue
      if (fn(value, this.keyList[i], this)) {
        return this.get(this.keyList[i], getOptions)
      }
    }
  }

  forEach(fn, thisp = this) {
    for (const i of this.indexes()) {
      const v = this.valList[i];
      const value = this.isBackgroundFetch(v)
        ? v.__staleWhileFetching
        : v;
      if (value === undefined) continue
      fn.call(thisp, value, this.keyList[i], this);
    }
  }

  rforEach(fn, thisp = this) {
    for (const i of this.rindexes()) {
      const v = this.valList[i];
      const value = this.isBackgroundFetch(v)
        ? v.__staleWhileFetching
        : v;
      if (value === undefined) continue
      fn.call(thisp, value, this.keyList[i], this);
    }
  }

  get prune() {
    deprecatedMethod('prune', 'purgeStale');
    return this.purgeStale
  }

  purgeStale() {
    let deleted = false;
    for (const i of this.rindexes({ allowStale: true })) {
      if (this.isStale(i)) {
        this.delete(this.keyList[i]);
        deleted = true;
      }
    }
    return deleted
  }

  dump() {
    const arr = [];
    for (const i of this.indexes({ allowStale: true })) {
      const key = this.keyList[i];
      const v = this.valList[i];
      const value = this.isBackgroundFetch(v)
        ? v.__staleWhileFetching
        : v;
      if (value === undefined) continue
      const entry = { value };
      if (this.ttls) {
        entry.ttl = this.ttls[i];
        // always dump the start relative to a portable timestamp
        // it's ok for this to be a bit slow, it's a rare operation.
        const age = perf.now() - this.starts[i];
        entry.start = Math.floor(Date.now() - age);
      }
      if (this.sizes) {
        entry.size = this.sizes[i];
      }
      arr.unshift([key, entry]);
    }
    return arr
  }

  load(arr) {
    this.clear();
    for (const [key, entry] of arr) {
      if (entry.start) {
        // entry.start is a portable timestamp, but we may be using
        // node's performance.now(), so calculate the offset.
        // it's ok for this to be a bit slow, it's a rare operation.
        const age = Date.now() - entry.start;
        entry.start = perf.now() - age;
      }
      this.set(key, entry.value, entry);
    }
  }

  dispose(_v, _k, _reason) {}

  set(
    k,
    v,
    {
      ttl = this.ttl,
      start,
      noDisposeOnSet = this.noDisposeOnSet,
      size = 0,
      sizeCalculation = this.sizeCalculation,
      noUpdateTTL = this.noUpdateTTL,
      status,
    } = {}
  ) {
    size = this.requireSize(k, v, size, sizeCalculation);
    // if the item doesn't fit, don't do anything
    // NB: maxEntrySize set to maxSize by default
    if (this.maxEntrySize && size > this.maxEntrySize) {
      if (status) {
        status.set = 'miss';
        status.maxEntrySizeExceeded = true;
      }
      // have to delete, in case a background fetch is there already.
      // in non-async cases, this is a no-op
      this.delete(k);
      return this
    }
    let index = this.size === 0 ? undefined : this.keyMap.get(k);
    if (index === undefined) {
      // addition
      index = this.newIndex();
      this.keyList[index] = k;
      this.valList[index] = v;
      this.keyMap.set(k, index);
      this.next[this.tail] = index;
      this.prev[index] = this.tail;
      this.tail = index;
      this.size++;
      this.addItemSize(index, size, status);
      if (status) {
        status.set = 'add';
      }
      noUpdateTTL = false;
    } else {
      // update
      this.moveToTail(index);
      const oldVal = this.valList[index];
      if (v !== oldVal) {
        if (this.isBackgroundFetch(oldVal)) {
          oldVal.__abortController.abort(new Error('replaced'));
        } else {
          if (!noDisposeOnSet) {
            this.dispose(oldVal, k, 'set');
            if (this.disposeAfter) {
              this.disposed.push([oldVal, k, 'set']);
            }
          }
        }
        this.removeItemSize(index);
        this.valList[index] = v;
        this.addItemSize(index, size, status);
        if (status) {
          status.set = 'replace';
          const oldValue =
            oldVal && this.isBackgroundFetch(oldVal)
              ? oldVal.__staleWhileFetching
              : oldVal;
          if (oldValue !== undefined) status.oldValue = oldValue;
        }
      } else if (status) {
        status.set = 'update';
      }
    }
    if (ttl !== 0 && this.ttl === 0 && !this.ttls) {
      this.initializeTTLTracking();
    }
    if (!noUpdateTTL) {
      this.setItemTTL(index, ttl, start);
    }
    this.statusTTL(status, index);
    if (this.disposeAfter) {
      while (this.disposed.length) {
        this.disposeAfter(...this.disposed.shift());
      }
    }
    return this
  }

  newIndex() {
    if (this.size === 0) {
      return this.tail
    }
    if (this.size === this.max && this.max !== 0) {
      return this.evict(false)
    }
    if (this.free.length !== 0) {
      return this.free.pop()
    }
    // initial fill, just keep writing down the list
    return this.initialFill++
  }

  pop() {
    if (this.size) {
      const val = this.valList[this.head];
      this.evict(true);
      return val
    }
  }

  evict(free) {
    const head = this.head;
    const k = this.keyList[head];
    const v = this.valList[head];
    if (this.isBackgroundFetch(v)) {
      v.__abortController.abort(new Error('evicted'));
    } else {
      this.dispose(v, k, 'evict');
      if (this.disposeAfter) {
        this.disposed.push([v, k, 'evict']);
      }
    }
    this.removeItemSize(head);
    // if we aren't about to use the index, then null these out
    if (free) {
      this.keyList[head] = null;
      this.valList[head] = null;
      this.free.push(head);
    }
    this.head = this.next[head];
    this.keyMap.delete(k);
    this.size--;
    return head
  }

  has(k, { updateAgeOnHas = this.updateAgeOnHas, status } = {}) {
    const index = this.keyMap.get(k);
    if (index !== undefined) {
      if (!this.isStale(index)) {
        if (updateAgeOnHas) {
          this.updateItemAge(index);
        }
        if (status) status.has = 'hit';
        this.statusTTL(status, index);
        return true
      } else if (status) {
        status.has = 'stale';
        this.statusTTL(status, index);
      }
    } else if (status) {
      status.has = 'miss';
    }
    return false
  }

  // like get(), but without any LRU updating or TTL expiration
  peek(k, { allowStale = this.allowStale } = {}) {
    const index = this.keyMap.get(k);
    if (index !== undefined && (allowStale || !this.isStale(index))) {
      const v = this.valList[index];
      // either stale and allowed, or forcing a refresh of non-stale value
      return this.isBackgroundFetch(v) ? v.__staleWhileFetching : v
    }
  }

  backgroundFetch(k, index, options, context) {
    const v = index === undefined ? undefined : this.valList[index];
    if (this.isBackgroundFetch(v)) {
      return v
    }
    const ac = new AC();
    if (options.signal) {
      options.signal.addEventListener('abort', () =>
        ac.abort(options.signal.reason)
      );
    }
    const fetchOpts = {
      signal: ac.signal,
      options,
      context,
    };
    const cb = (v, updateCache = false) => {
      const { aborted } = ac.signal;
      const ignoreAbort = options.ignoreFetchAbort && v !== undefined;
      if (options.status) {
        if (aborted && !updateCache) {
          options.status.fetchAborted = true;
          options.status.fetchError = ac.signal.reason;
          if (ignoreAbort) options.status.fetchAbortIgnored = true;
        } else {
          options.status.fetchResolved = true;
        }
      }
      if (aborted && !ignoreAbort && !updateCache) {
        return fetchFail(ac.signal.reason)
      }
      // either we didn't abort, and are still here, or we did, and ignored
      if (this.valList[index] === p) {
        if (v === undefined) {
          if (p.__staleWhileFetching) {
            this.valList[index] = p.__staleWhileFetching;
          } else {
            this.delete(k);
          }
        } else {
          if (options.status) options.status.fetchUpdated = true;
          this.set(k, v, fetchOpts.options);
        }
      }
      return v
    };
    const eb = er => {
      if (options.status) {
        options.status.fetchRejected = true;
        options.status.fetchError = er;
      }
      return fetchFail(er)
    };
    const fetchFail = er => {
      const { aborted } = ac.signal;
      const allowStaleAborted =
        aborted && options.allowStaleOnFetchAbort;
      const allowStale =
        allowStaleAborted || options.allowStaleOnFetchRejection;
      const noDelete = allowStale || options.noDeleteOnFetchRejection;
      if (this.valList[index] === p) {
        // if we allow stale on fetch rejections, then we need to ensure that
        // the stale value is not removed from the cache when the fetch fails.
        const del = !noDelete || p.__staleWhileFetching === undefined;
        if (del) {
          this.delete(k);
        } else if (!allowStaleAborted) {
          // still replace the *promise* with the stale value,
          // since we are done with the promise at this point.
          // leave it untouched if we're still waiting for an
          // aborted background fetch that hasn't yet returned.
          this.valList[index] = p.__staleWhileFetching;
        }
      }
      if (allowStale) {
        if (options.status && p.__staleWhileFetching !== undefined) {
          options.status.returnedStale = true;
        }
        return p.__staleWhileFetching
      } else if (p.__returned === p) {
        throw er
      }
    };
    const pcall = (res, rej) => {
      this.fetchMethod(k, v, fetchOpts).then(v => res(v), rej);
      // ignored, we go until we finish, regardless.
      // defer check until we are actually aborting,
      // so fetchMethod can override.
      ac.signal.addEventListener('abort', () => {
        if (
          !options.ignoreFetchAbort ||
          options.allowStaleOnFetchAbort
        ) {
          res();
          // when it eventually resolves, update the cache.
          if (options.allowStaleOnFetchAbort) {
            res = v => cb(v, true);
          }
        }
      });
    };
    if (options.status) options.status.fetchDispatched = true;
    const p = new Promise(pcall).then(cb, eb);
    p.__abortController = ac;
    p.__staleWhileFetching = v;
    p.__returned = null;
    if (index === undefined) {
      // internal, don't expose status.
      this.set(k, p, { ...fetchOpts.options, status: undefined });
      index = this.keyMap.get(k);
    } else {
      this.valList[index] = p;
    }
    return p
  }

  isBackgroundFetch(p) {
    return (
      p &&
      typeof p === 'object' &&
      typeof p.then === 'function' &&
      Object.prototype.hasOwnProperty.call(
        p,
        '__staleWhileFetching'
      ) &&
      Object.prototype.hasOwnProperty.call(p, '__returned') &&
      (p.__returned === p || p.__returned === null)
    )
  }

  // this takes the union of get() and set() opts, because it does both
  async fetch(
    k,
    {
      // get options
      allowStale = this.allowStale,
      updateAgeOnGet = this.updateAgeOnGet,
      noDeleteOnStaleGet = this.noDeleteOnStaleGet,
      // set options
      ttl = this.ttl,
      noDisposeOnSet = this.noDisposeOnSet,
      size = 0,
      sizeCalculation = this.sizeCalculation,
      noUpdateTTL = this.noUpdateTTL,
      // fetch exclusive options
      noDeleteOnFetchRejection = this.noDeleteOnFetchRejection,
      allowStaleOnFetchRejection = this.allowStaleOnFetchRejection,
      ignoreFetchAbort = this.ignoreFetchAbort,
      allowStaleOnFetchAbort = this.allowStaleOnFetchAbort,
      fetchContext = this.fetchContext,
      forceRefresh = false,
      status,
      signal,
    } = {}
  ) {
    if (!this.fetchMethod) {
      if (status) status.fetch = 'get';
      return this.get(k, {
        allowStale,
        updateAgeOnGet,
        noDeleteOnStaleGet,
        status,
      })
    }

    const options = {
      allowStale,
      updateAgeOnGet,
      noDeleteOnStaleGet,
      ttl,
      noDisposeOnSet,
      size,
      sizeCalculation,
      noUpdateTTL,
      noDeleteOnFetchRejection,
      allowStaleOnFetchRejection,
      allowStaleOnFetchAbort,
      ignoreFetchAbort,
      status,
      signal,
    };

    let index = this.keyMap.get(k);
    if (index === undefined) {
      if (status) status.fetch = 'miss';
      const p = this.backgroundFetch(k, index, options, fetchContext);
      return (p.__returned = p)
    } else {
      // in cache, maybe already fetching
      const v = this.valList[index];
      if (this.isBackgroundFetch(v)) {
        const stale =
          allowStale && v.__staleWhileFetching !== undefined;
        if (status) {
          status.fetch = 'inflight';
          if (stale) status.returnedStale = true;
        }
        return stale ? v.__staleWhileFetching : (v.__returned = v)
      }

      // if we force a refresh, that means do NOT serve the cached value,
      // unless we are already in the process of refreshing the cache.
      const isStale = this.isStale(index);
      if (!forceRefresh && !isStale) {
        if (status) status.fetch = 'hit';
        this.moveToTail(index);
        if (updateAgeOnGet) {
          this.updateItemAge(index);
        }
        this.statusTTL(status, index);
        return v
      }

      // ok, it is stale or a forced refresh, and not already fetching.
      // refresh the cache.
      const p = this.backgroundFetch(k, index, options, fetchContext);
      const hasStale = p.__staleWhileFetching !== undefined;
      const staleVal = hasStale && allowStale;
      if (status) {
        status.fetch = hasStale && isStale ? 'stale' : 'refresh';
        if (staleVal && isStale) status.returnedStale = true;
      }
      return staleVal ? p.__staleWhileFetching : (p.__returned = p)
    }
  }

  get(
    k,
    {
      allowStale = this.allowStale,
      updateAgeOnGet = this.updateAgeOnGet,
      noDeleteOnStaleGet = this.noDeleteOnStaleGet,
      status,
    } = {}
  ) {
    const index = this.keyMap.get(k);
    if (index !== undefined) {
      const value = this.valList[index];
      const fetching = this.isBackgroundFetch(value);
      this.statusTTL(status, index);
      if (this.isStale(index)) {
        if (status) status.get = 'stale';
        // delete only if not an in-flight background fetch
        if (!fetching) {
          if (!noDeleteOnStaleGet) {
            this.delete(k);
          }
          if (status) status.returnedStale = allowStale;
          return allowStale ? value : undefined
        } else {
          if (status) {
            status.returnedStale =
              allowStale && value.__staleWhileFetching !== undefined;
          }
          return allowStale ? value.__staleWhileFetching : undefined
        }
      } else {
        if (status) status.get = 'hit';
        // if we're currently fetching it, we don't actually have it yet
        // it's not stale, which means this isn't a staleWhileRefetching.
        // If it's not stale, and fetching, AND has a __staleWhileFetching
        // value, then that means the user fetched with {forceRefresh:true},
        // so it's safe to return that value.
        if (fetching) {
          return value.__staleWhileFetching
        }
        this.moveToTail(index);
        if (updateAgeOnGet) {
          this.updateItemAge(index);
        }
        return value
      }
    } else if (status) {
      status.get = 'miss';
    }
  }

  connect(p, n) {
    this.prev[n] = p;
    this.next[p] = n;
  }

  moveToTail(index) {
    // if tail already, nothing to do
    // if head, move head to next[index]
    // else
    //   move next[prev[index]] to next[index] (head has no prev)
    //   move prev[next[index]] to prev[index]
    // prev[index] = tail
    // next[tail] = index
    // tail = index
    if (index !== this.tail) {
      if (index === this.head) {
        this.head = this.next[index];
      } else {
        this.connect(this.prev[index], this.next[index]);
      }
      this.connect(this.tail, index);
      this.tail = index;
    }
  }

  get del() {
    deprecatedMethod('del', 'delete');
    return this.delete
  }

  delete(k) {
    let deleted = false;
    if (this.size !== 0) {
      const index = this.keyMap.get(k);
      if (index !== undefined) {
        deleted = true;
        if (this.size === 1) {
          this.clear();
        } else {
          this.removeItemSize(index);
          const v = this.valList[index];
          if (this.isBackgroundFetch(v)) {
            v.__abortController.abort(new Error('deleted'));
          } else {
            this.dispose(v, k, 'delete');
            if (this.disposeAfter) {
              this.disposed.push([v, k, 'delete']);
            }
          }
          this.keyMap.delete(k);
          this.keyList[index] = null;
          this.valList[index] = null;
          if (index === this.tail) {
            this.tail = this.prev[index];
          } else if (index === this.head) {
            this.head = this.next[index];
          } else {
            this.next[this.prev[index]] = this.next[index];
            this.prev[this.next[index]] = this.prev[index];
          }
          this.size--;
          this.free.push(index);
        }
      }
    }
    if (this.disposed) {
      while (this.disposed.length) {
        this.disposeAfter(...this.disposed.shift());
      }
    }
    return deleted
  }

  clear() {
    for (const index of this.rindexes({ allowStale: true })) {
      const v = this.valList[index];
      if (this.isBackgroundFetch(v)) {
        v.__abortController.abort(new Error('deleted'));
      } else {
        const k = this.keyList[index];
        this.dispose(v, k, 'delete');
        if (this.disposeAfter) {
          this.disposed.push([v, k, 'delete']);
        }
      }
    }

    this.keyMap.clear();
    this.valList.fill(null);
    this.keyList.fill(null);
    if (this.ttls) {
      this.ttls.fill(0);
      this.starts.fill(0);
    }
    if (this.sizes) {
      this.sizes.fill(0);
    }
    this.head = 0;
    this.tail = 0;
    this.initialFill = 1;
    this.free.length = 0;
    this.calculatedSize = 0;
    this.size = 0;
    if (this.disposed) {
      while (this.disposed.length) {
        this.disposeAfter(...this.disposed.shift());
      }
    }
  }

  get reset() {
    deprecatedMethod('reset', 'clear');
    return this.clear
  }

  get length() {
    deprecatedProperty('length', 'size');
    return this.size
  }

  static get AbortController() {
    return AC
  }
  static get AbortSignal() {
    return AS
  }
}

/**
 * 直接提供数据
 */
var SimpleNode = /** @class */ (function (_super) {
    __extends(SimpleNode, _super);
    function SimpleNode(value) {
        var _this = _super.call(this) || this;
        _this.value = value;
        _this.type = "simple";
        return _this;
    }
    SimpleNode.prototype.filterNodes = function (exposingNodes) {
        return evalPerfUtil.perf(this, "filterNodes", function () {
            return new Map();
        });
    };
    SimpleNode.prototype.justEval = function (exposingNodes) {
        return this.value;
    };
    SimpleNode.prototype.getChildren = function () {
        return [];
    };
    SimpleNode.prototype.dependValues = function () {
        return {};
    };
    SimpleNode.prototype.fetchInfo = function (exposingNodes) {
        return {
            isFetching: false,
            ready: true,
        };
    };
    __decorate([
        memoized()
    ], SimpleNode.prototype, "filterNodes", null);
    return SimpleNode;
}(AbstractNode));
/**
 * 提供简单的值，不需要eval
 */
function fromValue(value) {
    return new SimpleNode(value);
}
var lru = new LRUCache({ max: 16384 });
function fromValueWithCache(value) {
    var res = lru.get(value);
    if (res === undefined) {
        res = fromValue(value);
        lru.set(value, res);
    }
    return res;
}

// 封装模块节点，使用指定的暴露节点和输入节点
var WrapNode = /** @class */ (function (_super) {
    __extends(WrapNode, _super);
    function WrapNode(delegate, moduleExposingNodes, moduleExposingMethods, inputNodes) {
        var _this = _super.call(this) || this;
        _this.delegate = delegate;
        _this.moduleExposingNodes = moduleExposingNodes;
        _this.moduleExposingMethods = moduleExposingMethods;
        _this.inputNodes = inputNodes;
        _this.type = "wrap";
        return _this;
    }
    WrapNode.prototype.wrap = function (exposingNodes, exposingMethods) {
        if (!this.inputNodes) {
            return this.moduleExposingNodes;
        }
        var inputNodeEntries = Object.entries(this.inputNodes);
        if (inputNodeEntries.length === 0) {
            return this.moduleExposingNodes;
        }
        var inputNodes = {};
        inputNodeEntries.forEach(function (_a) {
            var name = _a[0], node = _a[1];
            var targetNode = typeof node === "string" ? exposingNodes[node] : node;
            if (!targetNode) {
                return;
            }
            inputNodes[name] = new WrapNode(targetNode, exposingNodes, exposingMethods);
        });
        return __assign(__assign({}, this.moduleExposingNodes), inputNodes);
    };
    WrapNode.prototype.filterNodes = function (exposingNodes) {
        return this.delegate.filterNodes(this.wrap(exposingNodes, {}));
    };
    WrapNode.prototype.justEval = function (exposingNodes, methods) {
        return this.delegate.evaluate(this.wrap(exposingNodes, methods), this.moduleExposingMethods);
    };
    WrapNode.prototype.fetchInfo = function (exposingNodes) {
        return this.delegate.fetchInfo(this.wrap(exposingNodes, {}));
    };
    WrapNode.prototype.getChildren = function () {
        return [this.delegate];
    };
    WrapNode.prototype.dependValues = function () {
        return {};
    };
    __decorate([
        memoized()
    ], WrapNode.prototype, "filterNodes", null);
    __decorate([
        memoized()
    ], WrapNode.prototype, "fetchInfo", null);
    return WrapNode;
}(AbstractNode));

var WrapContextNode = /** @class */ (function (_super) {
    __extends(WrapContextNode, _super);
    function WrapContextNode(child) {
        var _this = _super.call(this) || this;
        _this.child = child;
        _this.type = "wrapContext";
        return _this;
    }
    WrapContextNode.prototype.filterNodes = function (exposingNodes) {
        return this.child.filterNodes(exposingNodes);
    };
    WrapContextNode.prototype.justEval = function (exposingNodes, methods) {
        var _this = this;
        return function (params) {
            var nodes;
            if (params) {
                nodes = __assign({}, exposingNodes);
                Object.entries(params).forEach(function (_a) {
                    var key = _a[0], value = _a[1];
                    nodes[key] = fromValueWithCache(value);
                });
            }
            else {
                nodes = exposingNodes;
            }
            return _this.child.evaluate(nodes, methods);
        };
    };
    WrapContextNode.prototype.getChildren = function () {
        return [this.child];
    };
    WrapContextNode.prototype.dependValues = function () {
        return this.child.dependValues();
    };
    WrapContextNode.prototype.fetchInfo = function (exposingNodes) {
        return this.child.fetchInfo(exposingNodes);
    };
    __decorate([
        memoized()
    ], WrapContextNode.prototype, "filterNodes", null);
    return WrapContextNode;
}(AbstractNode));
function wrapContext(node) {
    return new WrapContextNode(node);
}

/**
 * 通过在子节点中设置新的依赖节点来构建新节点
 */
var WrapContextNodeV2 = /** @class */ (function (_super) {
    __extends(WrapContextNodeV2, _super);
    function WrapContextNodeV2(child, paramNodes) {
        var _this = _super.call(this) || this;
        _this.child = child;
        _this.paramNodes = paramNodes;
        _this.type = "wrapContextV2";
        return _this;
    }
    WrapContextNodeV2.prototype.filterNodes = function (exposingNodes) {
        return this.child.filterNodes(exposingNodes);
    };
    WrapContextNodeV2.prototype.justEval = function (exposingNodes, methods) {
        return this.child.evaluate(this.wrap(exposingNodes), methods);
    };
    WrapContextNodeV2.prototype.getChildren = function () {
        return [this.child];
    };
    WrapContextNodeV2.prototype.dependValues = function () {
        return this.child.dependValues();
    };
    WrapContextNodeV2.prototype.fetchInfo = function (exposingNodes) {
        return this.child.fetchInfo(this.wrap(exposingNodes));
    };
    WrapContextNodeV2.prototype.wrap = function (exposingNodes) {
        return __assign(__assign({}, exposingNodes), this.paramNodes);
    };
    __decorate([
        memoized()
    ], WrapContextNodeV2.prototype, "filterNodes", null);
    __decorate([
        memoized()
    ], WrapContextNodeV2.prototype, "wrap", null);
    return WrapContextNodeV2;
}(AbstractNode));

/**
 * 创建一个转换包装器，它将一个函数作为参数，并返回一个新的函数，
 * 该函数将原始函数应用于输入值，并返回一个新的值和消息。
 *
 * @param transformFn - 要应用于输入值的函数。
 * @param defaultValue - 转换函数在发生错误时返回的默认值。
 * @returns 一个新的函数，该函数将原始函数应用于输入值，并返回一个新的值和消息。
 */
function transformWrapper(transformFn, defaultValue) {
    /**
     * 一个新的函数，它将原始函数应用于输入值，并返回一个新的值和消息。
     *
     * @param valueAndMsg - 包含输入值的原始值和消息的对象。
     * @returns 一个新的值和消息的对象。
     */
    function transformWithMsg(valueAndMsg) {
        var _a;
        var result;
        try {
            // 尝试使用原始函数转换输入值
            var value = transformFn(valueAndMsg.value);
            // 创建一个新的 ValueAndMsg 对象，并返回
            result = new ValueAndMsg(value, valueAndMsg.msg, valueAndMsg.extra, valueAndMsg.value);
        }
        catch (err) {
            var value = void 0;
            try {
                // 如果原始函数转换发生错误，尝试使用默认值或空字符串转换
                value = defaultValue !== null && defaultValue !== void 0 ? defaultValue : transformFn("");
            }
            catch (err2) {
                // 如果默认值或空字符串转换也发生错误，将值设置为 undefined
                value = undefined;
            }
            // 获取错误消息
            var errorMsg = (_a = valueAndMsg.msg) !== null && _a !== void 0 ? _a : getErrorMessage(err);
            // 创建一个新的 ValueAndMsg 对象，并返回
            result = new ValueAndMsg(value, errorMsg, valueAndMsg.extra, valueAndMsg.value);
        }
        // log.trace(
        //   "transformWithMsg. func: ",
        //   transformFn.name,
        //   "\nsource: ",
        //   valueAndMsg,
        //   "\nresult: ",
        //   result
        // );
        // 返回新的 ValueAndMsg 对象
        return result;
    }
    // 返回 transformWithMsg 函数
    return transformWithMsg;
}

function styleNamespace(id) {
    return "style-for-".concat(id);
}
/**
 * 在页面上评估并应用给定ID的样式规则
 *
 * 此函数通过接受一个ID和一个CSS字符串数组，生成相应的样式节点并将其添加到页面中
 * 它处理CSS规则的编译和序列化，跳过空的CSS字符串，使用预定义的中间件处理CSS，
 * 然后将编译后的CSS字符串赋给样式节点，从而在浏览器中生效
 *
 * @param id 样式的ID，用于生成唯一的样式节点ID和选择器
 * @param css CSS规则数组，每个规则作为一个数组元素
 */
function evalStyle(id, css) {
    var _a;
    // 根据给定的ID生成样式节点的唯一ID
    var styleId = styleNamespace(id);
    // 初始化编译后的CSS字符串
    var compiledCSS = "";
    // 遍历CSS规则数组，编译非空的CSS规则
    css.forEach(function (i) {
        if (!i.trim()) {
            // 如果CSS规则为空，则跳过
            return;
        }
        // 编译和序列化CSS规则，然后添加到编译后的CSS字符串中
        compiledCSS += serialize(compile("#".concat(id, "{").concat(i, "}")), middleware([prefixer, stringify]));
    });
    // 尝试查询已存在的样式节点，如果没有找到则创建一个新的样式节点
    var styleNode = document.querySelector("#".concat(styleId));
    if (!styleNode) {
        styleNode = document.createElement("style");
        styleNode.setAttribute("type", "text/css");
        styleNode.setAttribute("id", styleId);
        styleNode.setAttribute("data-style-src", "eval");
        // 将样式节点添加到文档的头部
        (_a = document.querySelector("head")) === null || _a === void 0 ? void 0 : _a.appendChild(styleNode);
    }
    // 将编译后的CSS字符串赋给样式节点，使其在浏览器中生效
    styleNode.textContent = compiledCSS;
}
/**
 * 清除内联样式评价函数
 * 该函数用于清除特定的内联样式，如果未指定ID，则清除所有匹配的数据源样式节点
 *
 * @param id 可选参数，指定要清除的样式的ID如果未提供，则清除所有通过eval方式注入的样式
 */
function clearStyleEval(id) {
    // 如果提供了ID，则计算该ID的样式命名空间值
    var styleId = id && styleNamespace(id);
    // 查询所有通过eval方式注入的样式节点
    var styleNode = document.querySelectorAll("style[data-style-src=eval]");
    // 遍历并清除匹配的样式节点
    if (styleNode) {
        styleNode.forEach(function (i) {
            // 如果没有指定ID，或者当前节点的ID与计算出的样式ID匹配，则移除该节点
            if (!styleId || styleId === i.id) {
                i.remove();
            }
        });
    }
}

/**
 * 该函数返回一个新的 Promise，该 Promise 在原始 Promise 成功解析或在指定超时时间后解析为默认值。
 *
 * @param promise - 要进行超时处理的原始 Promise。
 * @param timeout - 超时时间（以毫秒为单位）。
 * @param defaultValue - 超时后返回的默认值。
 * @param timeoutMessage - 超时时返回的错误消息（可选，默认为 "timeout"）。
 * @returns 一个新的 Promise，该 Promise 在原始 Promise 成功解析或在指定超时时间后解析为默认值。
 */
function promiseWithDefaultOnTimeout(promise, timeout, defaultValue, timeoutMessage) {
    // 用于存储 setTimeout 的返回值，以便在 finally 块中清除计时器。
    var timer;
    // 创建一个新的 Promise，在超时时解析为 defaultValue。
    var timeoutPromise = new Promise(function (resolve) {
        timer = setTimeout(function () { return resolve(defaultValue); }, timeout);
    });
    // 使用 Promise.race 并行运行 timeoutPromise 和原始 promise，并在 finally 块中清除计时器。
    return Promise.race([timeoutPromise, promise])
        .finally(function () { return clearTimeout(timer); });
}
/**
 * 该函数返回一个新的 Promise，该 Promise 在原始 Promise 成功解析或在指定超时时间后拒绝。
 *
 * @param promise - 要进行超时处理的原始 Promise。
 * @param timeout - 超时时间（以毫秒为单位）。
 * @param timeoutMessage - 超时时返回的错误消息（可选，默认为 "timeout"）。
 * @returns 一个新的 Promise，该 Promise 在原始 Promise 成功解析或在指定超时时间后拒绝。
 */
function promiseWithTimeout(promise, timeout, timeoutMessage) {
    if (timeoutMessage === void 0) { timeoutMessage = "timeout"; }
    // 用于存储 setTimeout 的返回值，以便在 finally 块中清除计时器。
    var timer;
    // 创建一个新的 Promise，在超时时拒绝并返回 timeoutMessage。
    var timeoutPromise = new Promise(function (_, reject) {
        timer = setTimeout(function () { return reject(new Error(timeoutMessage)); }, timeout);
    });
    // 使用 Promise.race 并行运行 timeoutPromise 和原始 promise，并在 finally 块中清除计时器。
    return Promise.race([timeoutPromise, promise])
        .finally(function () { return clearTimeout(timer); });
}

var CompActionTypes;
(function (CompActionTypes) {
    CompActionTypes["CHANGE_VALUE"] = "CHANGE_VALUE";
    CompActionTypes["RENAME"] = "RENAME";
    CompActionTypes["MULTI_CHANGE"] = "MULTI_CHANGE";
    CompActionTypes["DELETE_COMP"] = "DELETE_COMP";
    CompActionTypes["REPLACE_COMP"] = "REPLACE_COMP";
    CompActionTypes["ONLY_EVAL"] = "NEED_EVAL";
    // UPDATE_NODES = "UPDATE_NODES",
    CompActionTypes["UPDATE_NODES_V2"] = "UPDATE_NODES_V2";
    CompActionTypes["EXECUTE_QUERY"] = "EXECUTE_QUERY";
    CompActionTypes["TRIGGER_MODULE_EVENT"] = "TRIGGER_MODULE_EVENT";
    /**
   * 此操作可以通过名称将数据传递给组件
   */
    CompActionTypes["ROUTE_BY_NAME"] = "ROUTE_BY_NAME";
    /**
     * 执行带有上下文的操作。例如，表格列中的按钮应将 currentRow 作为上下文。
     * 注意：这是一个广播消息，可以通过继承机制来改进。
     */
    CompActionTypes["UPDATE_ACTION_CONTEXT"] = "UPDATE_ACTION_CONTEXT";
    /**
   * comp-specific action 应当不全局地放置。
   * 请统一使用 CUSTOM。
   */
    CompActionTypes["CUSTOM"] = "CUSTOM";
    /**
   * 在组件树结构中广播其他操作。
   * 用于封装 MultiBaseComp
   */
    CompActionTypes["BROADCAST"] = "BROADCAST";
})(CompActionTypes || (CompActionTypes = {}));

/**
 * 创建一个自定义操作对象
 *
 * 此函数用于生成一个自定义操作对象，该对象表示一种非标准的、可由前端开发者自定义的UI操作
 * 它包含了执行操作所需的数据以及是否编辑DSL（领域特定语言）的标志
 *
 * @param value - 操作中传递的数据，类型由使用者自定义
 * @param editDSL - 表示操作是否涉及编辑DSL的布尔值
 * @returns 返回一个包含类型、路径、值和是否编辑DSL的自定义操作对象
 */
function customAction(value, editDSL) {
    return {
        type: CompActionTypes.CUSTOM,
        path: [],
        value: value,
        editDSL: editDSL,
    };
}
/**
 * 创建一个更新操作上下文的广播操作
 *
 * @param context - 要更新的操作上下文
 * @returns 返回一个包含类型、路径、操作值和是否编辑DSL的广播操作对象
 *
 * @remarks
 * 该函数用于生成一个广播操作，该操作表示更新操作上下文
 * 它包含了执行操作所需的数据以及是否编辑DSL（领域特定语言）的标志
 *
 */
function updateActionContextAction(context) {
    var value = {
        type: CompActionTypes.UPDATE_ACTION_CONTEXT,
        path: [],
        editDSL: false,
        context: context,
    };
    return {
        type: CompActionTypes.BROADCAST,
        path: [],
        editDSL: false,
        action: value,
    };
}
/**
 * 检查是否为当前的自定义操作
 *
 * 该函数用于检查一个操作是否为自定义操作，并保证类型安全
 * 它使用了泛型来保证类型安全，使用者需要保证传入的类型与 T 相同，否则可能引起 bug
 *
 * @param action - 要检查的操作
 * @param type - 自定义操作的类型
 * @returns 如果操作是自定义操作且类型匹配，则返回 true，否则返回 false
 */
function isMyCustomAction(action, type) {
    return !isChildAction(action) && isCustomAction(action, type);
}
/**
 * 检查是否为自定义操作
 *
 * 该函数用于检查一个操作是否为自定义操作，并保证类型安全
 * 它使用了泛型来保证类型安全，使用者需要保证传入的类型与 T 相同，否则可能引起 bug
 *
 * @param action - 要检查的操作
 * @param type - 自定义操作的类型
 * @returns 如果操作是自定义操作且类型匹配，则返回 true，否则返回 false
 */
function isCustomAction(action, type) {
    return action.type === CompActionTypes.CUSTOM && _.get(action.value, "type") === type;
}
/**
 * 执行查询的动作。
 * 精确地指向查询的路径路由。
 * 传递queryName时，RootComp将正确更改路径。
 *
 * @param props - 执行查询操作的属性。
 * @param props.args - 查询的参数。
 * @param props.afterExecFunc - 查询执行后要执行的函数。
 * @returns 返回一个ExecuteQueryAction对象。
 */
function executeQueryAction(props) {
    return __assign({ type: CompActionTypes.EXECUTE_QUERY, path: [], editDSL: false }, props);
}
/**
 * 触发模块事件的操作。
 *
 * @param name - 要触发的模块事件的名称。
 * @returns 返回一个TriggerModuleEventAction对象。
 */
function triggerModuleEventAction(name) {
    return {
        type: CompActionTypes.TRIGGER_MODULE_EVENT,
        path: [],
        editDSL: false,
        name: name,
    };
}
/**
 * 更改值操作,最好使用comp.dispatchChangeValueAction来保证类型安全
 *
 * @param value - 要更改的值。
 * @param editDSL - 一个布尔值，表示是否编辑DSL（领域特定语言）。
 * @returns 返回一个ChangeValueAction对象。
 */
function changeValueAction(value, editDSL) {
    return {
        type: CompActionTypes.CHANGE_VALUE,
        path: [],
        editDSL: editDSL,
        value: value,
    };
}
function isBroadcastAction(action, type) {
    return action.type === CompActionTypes.BROADCAST && _.get(action.action, "type") === type;
}
function renameAction(oldName, name) {
    var value = {
        type: CompActionTypes.RENAME,
        path: [],
        editDSL: true,
        oldName: oldName,
        name: name,
    };
    return {
        type: CompActionTypes.BROADCAST,
        path: [],
        editDSL: true,
        action: value,
    };
}
function routeByNameAction(name, action) {
    return {
        type: CompActionTypes.ROUTE_BY_NAME,
        path: [],
        name: name,
        editDSL: action.editDSL,
        action: action,
    };
}
function multiChangeAction(changes) {
    var editDSL = Object.values(changes).some(function (action) { return !!action.editDSL; });
    console.assert(Object.values(changes).every(function (action) { return !_.isNil(action.editDSL) && action.editDSL === editDSL; }), "multiChangeAction should wrap actions with the same editDSL value in property. editDSL: ".concat(editDSL, "\nchanges:"), changes);
    return {
        type: CompActionTypes.MULTI_CHANGE,
        path: [],
        editDSL: editDSL,
        changes: changes,
    };
}
function deleteCompAction() {
    return {
        type: CompActionTypes.DELETE_COMP,
        path: [],
        editDSL: true,
    };
}
function replaceCompAction(compFactory) {
    return {
        type: CompActionTypes.REPLACE_COMP,
        path: [],
        editDSL: false,
        compFactory: compFactory,
    };
}
function onlyEvalAction() {
    return {
        type: CompActionTypes.ONLY_EVAL,
        path: [],
        editDSL: false,
    };
}
function wrapChildAction(childName, action) {
    return __assign(__assign({}, action), { path: __spreadArray([childName], action.path, true) });
}
function isChildAction(action) {
    var _a, _b;
    return ((_b = (_a = action === null || action === void 0 ? void 0 : action.path) === null || _a === void 0 ? void 0 : _a.length) !== null && _b !== void 0 ? _b : 0) > 0;
}
function unwrapChildAction(action) {
    return [action.path[0], __assign(__assign({}, action), { path: action.path.slice(1) })];
}
function changeChildAction(childName, value, editDSL) {
    return wrapChildAction(childName, changeValueAction(value, editDSL));
}
function updateNodesV2Action(value) {
    return {
        type: CompActionTypes.UPDATE_NODES_V2,
        path: [],
        editDSL: false,
        value: value,
    };
}
/**
 * 包装操作的额外信息。
 *
 * @param action - 要包装的操作。
 * @param extraInfos - 要添加的额外信息。
 * @returns 返回一个包含额外信息的操作。
 */
function wrapActionExtraInfo(action, extraInfos) {
    return __assign(__assign({}, action), { extraInfo: __assign(__assign({}, action.extraInfo), extraInfos) });
}
/**
 * 推迟执行操作。
 *
 * @param action - 要推迟的操作。
 * @returns 返回一个推迟执行的操作。
 */
function deferAction(action) {
    return __assign(__assign({}, action), { priority: "defer" });
}
/**
 * 更改操作的编辑DSL标志。
 *
 * @param action - 要更改的操作。
 * @param editDSL - 一个布尔值，表示是否编辑DSL（领域特定语言）。
 * @returns 返回一个更改了编辑DSL标志的操作。
 */
function changeEditDSLAction(action, editDSL) {
    return __assign(__assign({}, action), { editDSL: editDSL });
}

var CACHE_PREFIX = "__cache__";
/**
 * 一个用于缓存函数结果的装饰器，忽略参数。
 *
 * @remarks
 * 缓存存储在 `__cache__xxx` 字段中。
 * `ObjectUtils.setFields` 不会保存此缓存。
 *
 */
function memo(target, propertyKey, descriptor) {
    var originalMethod = descriptor.value;
    var cachePropertyKey = CACHE_PREFIX + propertyKey;
    descriptor.value = function () {
        var args = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            args[_i] = arguments[_i];
        }
        var thisObj = this;
        if (!thisObj[cachePropertyKey]) {
            // 将结果放入数组中，以表示 `undefined`
            thisObj[cachePropertyKey] = [originalMethod.apply(this, args)];
        }
        return thisObj[cachePropertyKey][0];
    };
}

/**
 * 判断两个对象是否具有相同的键值对。
 * 这个函数用于比较两个对象的浅层属性是否完全一致，主要用于React组件的shouldComponentUpdate生命周期方法中，
 * 以避免不必要的组件重新渲染。
 *
 * @param obj1 第一个需要比较的对象。
 * @param obj2 第二个需要比较的对象。
 * @returns 如果两个对象的键值对完全相同，则返回true；否则，返回false。
 */
function shallowEqual(obj1, obj2) {
    // 如果两个对象引用相同，则直接返回true，因为它们是同一个对象。
    if (obj1 === obj2) {
        return true;
    }
    // 检查两个对象的键数量是否相同，并且每个键值对是否相等。
    // 使用Object.keys来确保只比较对象自身的属性，而不是原型链上的属性。
    return (Object.keys(obj1).length === Object.keys(obj2).length &&
        Object.keys(obj1).every(function (key) { return obj2.hasOwnProperty(key) && obj1[key] === obj2[key]; }));
}
/**
 * 检查对象是否包含指定的字段
 *
 * 此函数用于验证对象 `obj` 是否包含了由 `fields` 指定的所有字段，并且这些字段的值与 `fields` 中的相应值相等
 * 如果所有指定的字段都存在于 `obj` 中且值相等，则返回 `true`；否则，返回 `false`
 *
 * @param obj - 待检查的对象，类型为 `Record<string, any>`，允许任何字符串键对应的任何类型值
 * @param fields - 可选参数，指定需要检查的字段及其值的对象，类型为 `Record<string, any>` 如果未提供（即 `undefined`），函数默认返回 `true`
 * @returns 返回一个布尔值，表示 `obj` 是否包含 `fields` 中指定的所有字段且值相等
 */
function containFields(obj, fields) {
    // 如果未指定需要检查的字段，直接返回 true，表示默认所有字段都符合要求
    if (fields === undefined) {
        return true;
    }
    // 查找第一个不符合要求的字段索引
    var notEqualIndex = Object.keys(fields).findIndex(function (key) {
        // 如果字段的值不相等，则返回对应的索引
        return obj[key] !== fields[key];
    });
    // 如果所有字段的值都相等，则 `findIndex` 返回 -1，此时返回 true；否则返回 false
    return notEqualIndex === -1;
}
/**
* 类型不安全，用户应自行保管。
* 优点：此函数可以支持私有字段。
*/
function setFieldsNoTypeCheck(obj, fields, params) {
    var res = Object.assign(Object.create(Object.getPrototypeOf(obj)), obj);
    Object.keys(res).forEach(function (key) {
        if (key.startsWith(CACHE_PREFIX)) {
            var propertyKey = key.slice(CACHE_PREFIX.length);
            if (!(params === null || params === void 0 ? void 0 : params.keepCacheKeys) || !(params === null || params === void 0 ? void 0 : params.keepCacheKeys.includes(propertyKey))) {
                delete res[key];
            }
        }
    });
    return Object.assign(res, fields);
}

var AbstractComp = /** @class */ (function () {
    function AbstractComp(params) {
        var _a;
        this.dispatch = (_a = params.dispatch) !== null && _a !== void 0 ? _a : (function (_action) { });
    }
    AbstractComp.prototype.changeDispatch = function (dispatch) {
        return setFieldsNoTypeCheck(this, { dispatch: dispatch }, { keepCacheKeys: ["node"] });
    };
    /**
   * 调用 `changeValueAction` 并保证类型安全。
   *
   * @param value 要修改的值
   */
    AbstractComp.prototype.dispatchChangeValueAction = function (value) {
        this.dispatch(this.changeValueAction(value));
    };
    AbstractComp.prototype.changeValueAction = function (value) {
        return changeValueAction(value, true);
    };
    /**
   * 不要重写函数，重写nodeWithout函数
   * FIXME：如果更改了此对象，则不能更改节点引用
   */
    AbstractComp.prototype.node = function () {
        return this.nodeWithoutCache();
    };
    __decorate([
        memo
    ], AbstractComp.prototype, "node", null);
    return AbstractComp;
}());

/**
 * 将调度程序包装为子调度程序
 *
 * @param dispatch 输入调度程序
 * @param childName 子调度程序的键
 * @returns 包含子调度程序的包装调度程序
 */
function wrapDispatch(dispatch, childName) {
    return function (action) {
        if (dispatch) {
            dispatch(wrapChildAction(childName, action));
        }
    };
}
/**
 * 多功能的核心类，用于构建 comps 的树形结构
 * @remarks
 * 如果需要，可以对函数进行缓存
 **/
var MultiBaseComp = /** @class */ (function (_super) {
    __extends(MultiBaseComp, _super);
    function MultiBaseComp(params) {
        var _this = _super.call(this, params) || this;
        _this.IGNORABLE_DEFAULT_VALUE = {};
        _this.children = _this.parseChildrenFromValue(params);
        return _this;
    }
    MultiBaseComp.prototype.reduce = function (action) {
        var comp = this.reduceOrUndefined(action);
        if (!comp) {
            console.warn("不支持的操作，不应该发生，操作：", action, "\n当前组件：", this);
            return this;
        }
        return comp;
    };
    // 如果基类无法处理此操作，则返回 undefined
    MultiBaseComp.prototype.reduceOrUndefined = function (action) {
        var _a, _b;
        var _c;
        // log.debug("reduceOrUndefined. action: ", action, " this: ", this);
        // 必须处理父级中的 DELETE 操作
        if (action.type === CompActionTypes.DELETE_COMP && action.path.length === 1) {
            return this.setChildren(_.omit(this.children, action.path[0]));
        }
        if (action.type === CompActionTypes.REPLACE_COMP && action.path.length === 1) {
            var NextComp = action.compFactory;
            if (!NextComp) {
                return this;
            }
            var compName = action.path[0];
            var currentComp = this.children[compName];
            var value = currentComp.toJsonValue();
            var nextComp = new NextComp({
                value: value,
                dispatch: wrapDispatch(this.dispatch, compName),
            });
            return this.setChildren(__assign(__assign({}, this.children), (_a = {}, _a[compName] = nextComp, _a)));
        }
        if (isChildAction(action)) {
            var _d = unwrapChildAction(action), childName = _d[0], childAction = _d[1];
            var child = this.children[childName];
            if (!child) {
                log.error("找到了无效的操作路径 ", childName, ", children:", this.children);
                return this;
            }
            var newChild = child.reduce(childAction);
            return this.setChild(childName, newChild);
        }
        // 键值对
        switch (action.type) {
            // 批量更改
            case CompActionTypes.MULTI_CHANGE: {
                var changes_1 = action.changes;
                // 处理父级中的 DELETE 操作
                var mcChildren = _.omitBy(this.children, function (comp, childName) {
                    var innerAction = changes_1[childName];
                    return (innerAction &&
                        innerAction.type === CompActionTypes.DELETE_COMP &&
                        innerAction.path.length === 0);
                });
                // 更改
                mcChildren = _.mapValues(mcChildren, function (comp, childName) {
                    var innerAction = changes_1[childName];
                    if (innerAction) {
                        return comp.reduce(innerAction);
                    }
                    return comp;
                });
                return this.setChildren(mcChildren);
            }
            // 更新节点 V2
            case CompActionTypes.UPDATE_NODES_V2: {
                var value_1 = action.value;
                if (value_1 === undefined) {
                    return this;
                }
                var cacheKey = CACHE_PREFIX + "REDUCE_UPDATE_NODE";
                // 如果是通过值构建的，则直接返回
                if (this[cacheKey] === value_1) {
                    // console.info("UPDATE_NODE_V2 缓存命中，操作：", action, "\n值：", value, "\nthis：", this);
                    return this;
                }
                var children = _.mapValues(this.children, function (comp, childName) {
                    if (value_1.hasOwnProperty(childName)) {
                        return comp.reduce(updateNodesV2Action(value_1[childName]));
                    }
                    return comp;
                });
                var extraFields = (_c = this.extraNode()) === null || _c === void 0 ? void 0 : _c.updateNodeFields(value_1);
                if (shallowEqual(children, this.children) && containFields(this, extraFields)) {
                    return this;
                }
                return setFieldsNoTypeCheck(this, __assign((_b = { children: children }, _b[cacheKey] = value_1, _b), extraFields), { keepCacheKeys: ["node"] });
            }
            // 更改值
            case CompActionTypes.CHANGE_VALUE: {
                return this.setChildren(this.parseChildrenFromValue({
                    dispatch: this.dispatch,
                    value: action.value,
                }));
            }
            // 广播
            case CompActionTypes.BROADCAST: {
                return this.setChildren(_.mapValues(this.children, function (comp) {
                    return comp.reduce(action);
                }));
            }
            // 仅求值
            case CompActionTypes.ONLY_EVAL: {
                return this;
            }
        }
    };
    MultiBaseComp.prototype.setChild = function (childName, newChild) {
        var _a;
        if (this.children[childName] === newChild) {
            return this;
        }
        return this.setChildren(__assign(__assign({}, this.children), (_a = {}, _a[childName] = newChild, _a)));
    };
    MultiBaseComp.prototype.setChildren = function (children, params) {
        if (shallowEqual(children, this.children)) {
            return this;
        }
        return setFieldsNoTypeCheck(this, { children: children }, params);
    };
    /**
     * 扩展的接口
     *
     * @return 用于添加节点的 node，用于处理 UPDATE_NODE 事件的 updateNodeFields
     * FIXME: 请使类型安全
     */
    MultiBaseComp.prototype.extraNode = function () {
        return undefined;
    };
    MultiBaseComp.prototype.childrenNode = function () {
        var _this = this;
        var result = {};
        Object.keys(this.children).forEach(function (key) {
            var node = _this.children[key].node();
            if (node !== undefined) {
                result[key] = node;
            }
        });
        return result;
    };
    MultiBaseComp.prototype.nodeWithoutCache = function () {
        var _a;
        return fromRecord(__assign(__assign({}, this.childrenNode()), (_a = this.extraNode()) === null || _a === void 0 ? void 0 : _a.node));
    };
    MultiBaseComp.prototype.changeDispatch = function (dispatch) {
        var newChildren = _.mapValues(this.children, function (comp, childName) {
            return comp.changeDispatch(wrapDispatch(dispatch, childName));
        });
        return _super.prototype.changeDispatch.call(this, dispatch).setChildren(newChildren, { keepCacheKeys: ["node"] });
    };
    MultiBaseComp.prototype.ignoreChildDefaultValue = function () {
        return false;
    };
    MultiBaseComp.prototype.toJsonValue = function () {
        var _this = this;
        var result = {};
        var ignore = this.ignoreChildDefaultValue();
        Object.keys(this.children).forEach(function (key) {
            var comp = _this.children[key];
            // FIXME: 这是一个不太好的实现，更好的做法是选择一个封装的实现
            if (comp.hasOwnProperty("NO_PERSISTENCE")) {
                return;
            }
            var value = comp.toJsonValue();
            if (ignore && _.isEqual(value, comp["IGNORABLE_DEFAULT_VALUE"])) {
                return;
            }
            result[key] = value;
        });
        return result;
    };
    // FIXME: autoHeight 应封装在 UIComp/UICompBuilder 中
    MultiBaseComp.prototype.autoHeight = function () {
        return true;
    };
    MultiBaseComp.prototype.changeChildAction = function (childName, value) {
        return wrapChildAction(childName, this.children[childName].changeValueAction(value));
    };
    return MultiBaseComp;
}(AbstractComp));
function mergeExtra(e1, e2) {
    if (e1 === undefined) {
        return e2;
    }
    return {
        node: __assign(__assign({}, e1.node), e2.node),
        updateNodeFields: function (value) {
            return __assign(__assign({}, e1.updateNodeFields(value)), e2.updateNodeFields(value));
        },
    };
}

/**
 * 一个简单的抽象组件，用于维护一个JSON值。
 * 它不包含任何特殊的功能，仅用作其他组件的基础。
 */
var SimpleAbstractComp = /** @class */ (function (_super) {
    __extends(SimpleAbstractComp, _super);
    /**
     * 构造函数。
     *
     * @param params - 组件参数。
     */
    function SimpleAbstractComp(params) {
        var _this = this;
        var _a;
        _this = _super.call(this, params) || this;
        _this.value = (_a = _this.oldValueToNew(params.value)) !== null && _a !== void 0 ? _a : _this.getDefaultValue();
        return _this;
    }
    /**
     * 可能重写此方法来实现兼容性。
     *
     * @param value - 旧的值。
     * @returns 新值，如果不需要更改，可以返回undefined。
     */
    SimpleAbstractComp.prototype.oldValueToNew = function (value) {
        return value;
    };
    /**
     * 重写reduce方法来处理CHANGE_VALUE操作。
     *
     * @param action - 要处理的操作。
     * @returns 如果值没有更改，返回this；否则，返回一个新的组件实例。
     */
    SimpleAbstractComp.prototype.reduce = function (action) {
        if (action.type === CompActionTypes.CHANGE_VALUE) {
            if (this.value === action.value) {
                return this;
            }
            return setFieldsNoTypeCheck(this, { value: action.value });
        }
        return this;
    };
    /**
     * 重写nodeWithoutCache方法来返回一个Node实例。
     *
     * @returns 一个Node实例，包含组件的值。
     */
    SimpleAbstractComp.prototype.nodeWithoutCache = function () {
        return fromValue(this.value);
    };
    /**
     * 暴露一个方法来获取组件的Node实例。
     *
     * @returns 组件的Node实例。
     */
    SimpleAbstractComp.prototype.exposingNode = function () {
        return this.node();
    };
    /**
     * 重写toJsonValue方法来返回组件的值。
     * 可以在defaultValue中使用
     *
     * @returns 组件的值。
     */
    SimpleAbstractComp.prototype.toJsonValue = function () {
        return this.value;
    };
    return SimpleAbstractComp;
}(AbstractComp));
var SimpleComp = /** @class */ (function (_super) {
    __extends(SimpleComp, _super);
    function SimpleComp() {
        return _super !== null && _super.apply(this, arguments) || this;
    }
    SimpleComp.prototype.getView = function () {
        return this.value;
    };
    return SimpleComp;
}(SimpleAbstractComp));

var jsxRuntimeExports = {};
var jsxRuntime = {
  get exports(){ return jsxRuntimeExports; },
  set exports(v){ jsxRuntimeExports = v; },
};

var reactJsxRuntime_production_min = {};

/**
 * @license React
 * react-jsx-runtime.production.min.js
 *
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

var hasRequiredReactJsxRuntime_production_min;

function requireReactJsxRuntime_production_min () {
	if (hasRequiredReactJsxRuntime_production_min) return reactJsxRuntime_production_min;
	hasRequiredReactJsxRuntime_production_min = 1;
var f=require$$0,k=Symbol.for("react.element"),l=Symbol.for("react.fragment"),m=Object.prototype.hasOwnProperty,n=f.__SECRET_INTERNALS_DO_NOT_USE_OR_YOU_WILL_BE_FIRED.ReactCurrentOwner,p={key:true,ref:true,__self:true,__source:true};
	function q(c,a,g){var b,d={},e=null,h=null;void 0!==g&&(e=""+g);void 0!==a.key&&(e=""+a.key);void 0!==a.ref&&(h=a.ref);for(b in a)m.call(a,b)&&!p.hasOwnProperty(b)&&(d[b]=a[b]);if(c&&c.defaultProps)for(b in a=c.defaultProps,a) void 0===d[b]&&(d[b]=a[b]);return {$$typeof:k,type:c,key:e,ref:h,props:d,_owner:n.current}}reactJsxRuntime_production_min.Fragment=l;reactJsxRuntime_production_min.jsx=q;reactJsxRuntime_production_min.jsxs=q;
	return reactJsxRuntime_production_min;
}

var reactJsxRuntime_development = {};

/**
 * @license React
 * react-jsx-runtime.development.js
 *
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

var hasRequiredReactJsxRuntime_development;

function requireReactJsxRuntime_development () {
	if (hasRequiredReactJsxRuntime_development) return reactJsxRuntime_development;
	hasRequiredReactJsxRuntime_development = 1;

	if (process.env.NODE_ENV !== "production") {
	  (function() {

	var React = require$$0;

	// ATTENTION
	// When adding new symbols to this file,
	// Please consider also adding to 'react-devtools-shared/src/backend/ReactSymbols'
	// The Symbol used to tag the ReactElement-like types.
	var REACT_ELEMENT_TYPE = Symbol.for('react.element');
	var REACT_PORTAL_TYPE = Symbol.for('react.portal');
	var REACT_FRAGMENT_TYPE = Symbol.for('react.fragment');
	var REACT_STRICT_MODE_TYPE = Symbol.for('react.strict_mode');
	var REACT_PROFILER_TYPE = Symbol.for('react.profiler');
	var REACT_PROVIDER_TYPE = Symbol.for('react.provider');
	var REACT_CONTEXT_TYPE = Symbol.for('react.context');
	var REACT_FORWARD_REF_TYPE = Symbol.for('react.forward_ref');
	var REACT_SUSPENSE_TYPE = Symbol.for('react.suspense');
	var REACT_SUSPENSE_LIST_TYPE = Symbol.for('react.suspense_list');
	var REACT_MEMO_TYPE = Symbol.for('react.memo');
	var REACT_LAZY_TYPE = Symbol.for('react.lazy');
	var REACT_OFFSCREEN_TYPE = Symbol.for('react.offscreen');
	var MAYBE_ITERATOR_SYMBOL = Symbol.iterator;
	var FAUX_ITERATOR_SYMBOL = '@@iterator';
	function getIteratorFn(maybeIterable) {
	  if (maybeIterable === null || typeof maybeIterable !== 'object') {
	    return null;
	  }

	  var maybeIterator = MAYBE_ITERATOR_SYMBOL && maybeIterable[MAYBE_ITERATOR_SYMBOL] || maybeIterable[FAUX_ITERATOR_SYMBOL];

	  if (typeof maybeIterator === 'function') {
	    return maybeIterator;
	  }

	  return null;
	}

	var ReactSharedInternals = React.__SECRET_INTERNALS_DO_NOT_USE_OR_YOU_WILL_BE_FIRED;

	function error(format) {
	  {
	    {
	      for (var _len2 = arguments.length, args = new Array(_len2 > 1 ? _len2 - 1 : 0), _key2 = 1; _key2 < _len2; _key2++) {
	        args[_key2 - 1] = arguments[_key2];
	      }

	      printWarning('error', format, args);
	    }
	  }
	}

	function printWarning(level, format, args) {
	  // When changing this logic, you might want to also
	  // update consoleWithStackDev.www.js as well.
	  {
	    var ReactDebugCurrentFrame = ReactSharedInternals.ReactDebugCurrentFrame;
	    var stack = ReactDebugCurrentFrame.getStackAddendum();

	    if (stack !== '') {
	      format += '%s';
	      args = args.concat([stack]);
	    } // eslint-disable-next-line react-internal/safe-string-coercion


	    var argsWithFormat = args.map(function (item) {
	      return String(item);
	    }); // Careful: RN currently depends on this prefix

	    argsWithFormat.unshift('Warning: ' + format); // We intentionally don't use spread (or .apply) directly because it
	    // breaks IE9: https://github.com/facebook/react/issues/13610
	    // eslint-disable-next-line react-internal/no-production-logging

	    Function.prototype.apply.call(console[level], console, argsWithFormat);
	  }
	}

	// -----------------------------------------------------------------------------

	var enableScopeAPI = false; // Experimental Create Event Handle API.
	var enableCacheElement = false;
	var enableTransitionTracing = false; // No known bugs, but needs performance testing

	var enableLegacyHidden = false; // Enables unstable_avoidThisFallback feature in Fiber
	// stuff. Intended to enable React core members to more easily debug scheduling
	// issues in DEV builds.

	var enableDebugTracing = false; // Track which Fiber(s) schedule render work.

	var REACT_MODULE_REFERENCE;

	{
	  REACT_MODULE_REFERENCE = Symbol.for('react.module.reference');
	}

	function isValidElementType(type) {
	  if (typeof type === 'string' || typeof type === 'function') {
	    return true;
	  } // Note: typeof might be other than 'symbol' or 'number' (e.g. if it's a polyfill).


	  if (type === REACT_FRAGMENT_TYPE || type === REACT_PROFILER_TYPE || enableDebugTracing  || type === REACT_STRICT_MODE_TYPE || type === REACT_SUSPENSE_TYPE || type === REACT_SUSPENSE_LIST_TYPE || enableLegacyHidden  || type === REACT_OFFSCREEN_TYPE || enableScopeAPI  || enableCacheElement  || enableTransitionTracing ) {
	    return true;
	  }

	  if (typeof type === 'object' && type !== null) {
	    if (type.$$typeof === REACT_LAZY_TYPE || type.$$typeof === REACT_MEMO_TYPE || type.$$typeof === REACT_PROVIDER_TYPE || type.$$typeof === REACT_CONTEXT_TYPE || type.$$typeof === REACT_FORWARD_REF_TYPE || // This needs to include all possible module reference object
	    // types supported by any Flight configuration anywhere since
	    // we don't know which Flight build this will end up being used
	    // with.
	    type.$$typeof === REACT_MODULE_REFERENCE || type.getModuleId !== undefined) {
	      return true;
	    }
	  }

	  return false;
	}

	function getWrappedName(outerType, innerType, wrapperName) {
	  var displayName = outerType.displayName;

	  if (displayName) {
	    return displayName;
	  }

	  var functionName = innerType.displayName || innerType.name || '';
	  return functionName !== '' ? wrapperName + "(" + functionName + ")" : wrapperName;
	} // Keep in sync with react-reconciler/getComponentNameFromFiber


	function getContextName(type) {
	  return type.displayName || 'Context';
	} // Note that the reconciler package should generally prefer to use getComponentNameFromFiber() instead.


	function getComponentNameFromType(type) {
	  if (type == null) {
	    // Host root, text node or just invalid type.
	    return null;
	  }

	  {
	    if (typeof type.tag === 'number') {
	      error('Received an unexpected object in getComponentNameFromType(). ' + 'This is likely a bug in React. Please file an issue.');
	    }
	  }

	  if (typeof type === 'function') {
	    return type.displayName || type.name || null;
	  }

	  if (typeof type === 'string') {
	    return type;
	  }

	  switch (type) {
	    case REACT_FRAGMENT_TYPE:
	      return 'Fragment';

	    case REACT_PORTAL_TYPE:
	      return 'Portal';

	    case REACT_PROFILER_TYPE:
	      return 'Profiler';

	    case REACT_STRICT_MODE_TYPE:
	      return 'StrictMode';

	    case REACT_SUSPENSE_TYPE:
	      return 'Suspense';

	    case REACT_SUSPENSE_LIST_TYPE:
	      return 'SuspenseList';

	  }

	  if (typeof type === 'object') {
	    switch (type.$$typeof) {
	      case REACT_CONTEXT_TYPE:
	        var context = type;
	        return getContextName(context) + '.Consumer';

	      case REACT_PROVIDER_TYPE:
	        var provider = type;
	        return getContextName(provider._context) + '.Provider';

	      case REACT_FORWARD_REF_TYPE:
	        return getWrappedName(type, type.render, 'ForwardRef');

	      case REACT_MEMO_TYPE:
	        var outerName = type.displayName || null;

	        if (outerName !== null) {
	          return outerName;
	        }

	        return getComponentNameFromType(type.type) || 'Memo';

	      case REACT_LAZY_TYPE:
	        {
	          var lazyComponent = type;
	          var payload = lazyComponent._payload;
	          var init = lazyComponent._init;

	          try {
	            return getComponentNameFromType(init(payload));
	          } catch (x) {
	            return null;
	          }
	        }

	      // eslint-disable-next-line no-fallthrough
	    }
	  }

	  return null;
	}

	var assign = Object.assign;

	// Helpers to patch console.logs to avoid logging during side-effect free
	// replaying on render function. This currently only patches the object
	// lazily which won't cover if the log function was extracted eagerly.
	// We could also eagerly patch the method.
	var disabledDepth = 0;
	var prevLog;
	var prevInfo;
	var prevWarn;
	var prevError;
	var prevGroup;
	var prevGroupCollapsed;
	var prevGroupEnd;

	function disabledLog() {}

	disabledLog.__reactDisabledLog = true;
	function disableLogs() {
	  {
	    if (disabledDepth === 0) {
	      /* eslint-disable react-internal/no-production-logging */
	      prevLog = console.log;
	      prevInfo = console.info;
	      prevWarn = console.warn;
	      prevError = console.error;
	      prevGroup = console.group;
	      prevGroupCollapsed = console.groupCollapsed;
	      prevGroupEnd = console.groupEnd; // https://github.com/facebook/react/issues/19099

	      var props = {
	        configurable: true,
	        enumerable: true,
	        value: disabledLog,
	        writable: true
	      }; // $FlowFixMe Flow thinks console is immutable.

	      Object.defineProperties(console, {
	        info: props,
	        log: props,
	        warn: props,
	        error: props,
	        group: props,
	        groupCollapsed: props,
	        groupEnd: props
	      });
	      /* eslint-enable react-internal/no-production-logging */
	    }

	    disabledDepth++;
	  }
	}
	function reenableLogs() {
	  {
	    disabledDepth--;

	    if (disabledDepth === 0) {
	      /* eslint-disable react-internal/no-production-logging */
	      var props = {
	        configurable: true,
	        enumerable: true,
	        writable: true
	      }; // $FlowFixMe Flow thinks console is immutable.

	      Object.defineProperties(console, {
	        log: assign({}, props, {
	          value: prevLog
	        }),
	        info: assign({}, props, {
	          value: prevInfo
	        }),
	        warn: assign({}, props, {
	          value: prevWarn
	        }),
	        error: assign({}, props, {
	          value: prevError
	        }),
	        group: assign({}, props, {
	          value: prevGroup
	        }),
	        groupCollapsed: assign({}, props, {
	          value: prevGroupCollapsed
	        }),
	        groupEnd: assign({}, props, {
	          value: prevGroupEnd
	        })
	      });
	      /* eslint-enable react-internal/no-production-logging */
	    }

	    if (disabledDepth < 0) {
	      error('disabledDepth fell below zero. ' + 'This is a bug in React. Please file an issue.');
	    }
	  }
	}

	var ReactCurrentDispatcher = ReactSharedInternals.ReactCurrentDispatcher;
	var prefix;
	function describeBuiltInComponentFrame(name, source, ownerFn) {
	  {
	    if (prefix === undefined) {
	      // Extract the VM specific prefix used by each line.
	      try {
	        throw Error();
	      } catch (x) {
	        var match = x.stack.trim().match(/\n( *(at )?)/);
	        prefix = match && match[1] || '';
	      }
	    } // We use the prefix to ensure our stacks line up with native stack frames.


	    return '\n' + prefix + name;
	  }
	}
	var reentry = false;
	var componentFrameCache;

	{
	  var PossiblyWeakMap = typeof WeakMap === 'function' ? WeakMap : Map;
	  componentFrameCache = new PossiblyWeakMap();
	}

	function describeNativeComponentFrame(fn, construct) {
	  // If something asked for a stack inside a fake render, it should get ignored.
	  if ( !fn || reentry) {
	    return '';
	  }

	  {
	    var frame = componentFrameCache.get(fn);

	    if (frame !== undefined) {
	      return frame;
	    }
	  }

	  var control;
	  reentry = true;
	  var previousPrepareStackTrace = Error.prepareStackTrace; // $FlowFixMe It does accept undefined.

	  Error.prepareStackTrace = undefined;
	  var previousDispatcher;

	  {
	    previousDispatcher = ReactCurrentDispatcher.current; // Set the dispatcher in DEV because this might be call in the render function
	    // for warnings.

	    ReactCurrentDispatcher.current = null;
	    disableLogs();
	  }

	  try {
	    // This should throw.
	    if (construct) {
	      // Something should be setting the props in the constructor.
	      var Fake = function () {
	        throw Error();
	      }; // $FlowFixMe


	      Object.defineProperty(Fake.prototype, 'props', {
	        set: function () {
	          // We use a throwing setter instead of frozen or non-writable props
	          // because that won't throw in a non-strict mode function.
	          throw Error();
	        }
	      });

	      if (typeof Reflect === 'object' && Reflect.construct) {
	        // We construct a different control for this case to include any extra
	        // frames added by the construct call.
	        try {
	          Reflect.construct(Fake, []);
	        } catch (x) {
	          control = x;
	        }

	        Reflect.construct(fn, [], Fake);
	      } else {
	        try {
	          Fake.call();
	        } catch (x) {
	          control = x;
	        }

	        fn.call(Fake.prototype);
	      }
	    } else {
	      try {
	        throw Error();
	      } catch (x) {
	        control = x;
	      }

	      fn();
	    }
	  } catch (sample) {
	    // This is inlined manually because closure doesn't do it for us.
	    if (sample && control && typeof sample.stack === 'string') {
	      // This extracts the first frame from the sample that isn't also in the control.
	      // Skipping one frame that we assume is the frame that calls the two.
	      var sampleLines = sample.stack.split('\n');
	      var controlLines = control.stack.split('\n');
	      var s = sampleLines.length - 1;
	      var c = controlLines.length - 1;

	      while (s >= 1 && c >= 0 && sampleLines[s] !== controlLines[c]) {
	        // We expect at least one stack frame to be shared.
	        // Typically this will be the root most one. However, stack frames may be
	        // cut off due to maximum stack limits. In this case, one maybe cut off
	        // earlier than the other. We assume that the sample is longer or the same
	        // and there for cut off earlier. So we should find the root most frame in
	        // the sample somewhere in the control.
	        c--;
	      }

	      for (; s >= 1 && c >= 0; s--, c--) {
	        // Next we find the first one that isn't the same which should be the
	        // frame that called our sample function and the control.
	        if (sampleLines[s] !== controlLines[c]) {
	          // In V8, the first line is describing the message but other VMs don't.
	          // If we're about to return the first line, and the control is also on the same
	          // line, that's a pretty good indicator that our sample threw at same line as
	          // the control. I.e. before we entered the sample frame. So we ignore this result.
	          // This can happen if you passed a class to function component, or non-function.
	          if (s !== 1 || c !== 1) {
	            do {
	              s--;
	              c--; // We may still have similar intermediate frames from the construct call.
	              // The next one that isn't the same should be our match though.

	              if (c < 0 || sampleLines[s] !== controlLines[c]) {
	                // V8 adds a "new" prefix for native classes. Let's remove it to make it prettier.
	                var _frame = '\n' + sampleLines[s].replace(' at new ', ' at '); // If our component frame is labeled "<anonymous>"
	                // but we have a user-provided "displayName"
	                // splice it in to make the stack more readable.


	                if (fn.displayName && _frame.includes('<anonymous>')) {
	                  _frame = _frame.replace('<anonymous>', fn.displayName);
	                }

	                {
	                  if (typeof fn === 'function') {
	                    componentFrameCache.set(fn, _frame);
	                  }
	                } // Return the line we found.


	                return _frame;
	              }
	            } while (s >= 1 && c >= 0);
	          }

	          break;
	        }
	      }
	    }
	  } finally {
	    reentry = false;

	    {
	      ReactCurrentDispatcher.current = previousDispatcher;
	      reenableLogs();
	    }

	    Error.prepareStackTrace = previousPrepareStackTrace;
	  } // Fallback to just using the name if we couldn't make it throw.


	  var name = fn ? fn.displayName || fn.name : '';
	  var syntheticFrame = name ? describeBuiltInComponentFrame(name) : '';

	  {
	    if (typeof fn === 'function') {
	      componentFrameCache.set(fn, syntheticFrame);
	    }
	  }

	  return syntheticFrame;
	}
	function describeFunctionComponentFrame(fn, source, ownerFn) {
	  {
	    return describeNativeComponentFrame(fn, false);
	  }
	}

	function shouldConstruct(Component) {
	  var prototype = Component.prototype;
	  return !!(prototype && prototype.isReactComponent);
	}

	function describeUnknownElementTypeFrameInDEV(type, source, ownerFn) {

	  if (type == null) {
	    return '';
	  }

	  if (typeof type === 'function') {
	    {
	      return describeNativeComponentFrame(type, shouldConstruct(type));
	    }
	  }

	  if (typeof type === 'string') {
	    return describeBuiltInComponentFrame(type);
	  }

	  switch (type) {
	    case REACT_SUSPENSE_TYPE:
	      return describeBuiltInComponentFrame('Suspense');

	    case REACT_SUSPENSE_LIST_TYPE:
	      return describeBuiltInComponentFrame('SuspenseList');
	  }

	  if (typeof type === 'object') {
	    switch (type.$$typeof) {
	      case REACT_FORWARD_REF_TYPE:
	        return describeFunctionComponentFrame(type.render);

	      case REACT_MEMO_TYPE:
	        // Memo may contain any component type so we recursively resolve it.
	        return describeUnknownElementTypeFrameInDEV(type.type, source, ownerFn);

	      case REACT_LAZY_TYPE:
	        {
	          var lazyComponent = type;
	          var payload = lazyComponent._payload;
	          var init = lazyComponent._init;

	          try {
	            // Lazy may contain any component type so we recursively resolve it.
	            return describeUnknownElementTypeFrameInDEV(init(payload), source, ownerFn);
	          } catch (x) {}
	        }
	    }
	  }

	  return '';
	}

	var hasOwnProperty = Object.prototype.hasOwnProperty;

	var loggedTypeFailures = {};
	var ReactDebugCurrentFrame = ReactSharedInternals.ReactDebugCurrentFrame;

	function setCurrentlyValidatingElement(element) {
	  {
	    if (element) {
	      var owner = element._owner;
	      var stack = describeUnknownElementTypeFrameInDEV(element.type, element._source, owner ? owner.type : null);
	      ReactDebugCurrentFrame.setExtraStackFrame(stack);
	    } else {
	      ReactDebugCurrentFrame.setExtraStackFrame(null);
	    }
	  }
	}

	function checkPropTypes(typeSpecs, values, location, componentName, element) {
	  {
	    // $FlowFixMe This is okay but Flow doesn't know it.
	    var has = Function.call.bind(hasOwnProperty);

	    for (var typeSpecName in typeSpecs) {
	      if (has(typeSpecs, typeSpecName)) {
	        var error$1 = void 0; // Prop type validation may throw. In case they do, we don't want to
	        // fail the render phase where it didn't fail before. So we log it.
	        // After these have been cleaned up, we'll let them throw.

	        try {
	          // This is intentionally an invariant that gets caught. It's the same
	          // behavior as without this statement except with a better message.
	          if (typeof typeSpecs[typeSpecName] !== 'function') {
	            // eslint-disable-next-line react-internal/prod-error-codes
	            var err = Error((componentName || 'React class') + ': ' + location + ' type `' + typeSpecName + '` is invalid; ' + 'it must be a function, usually from the `prop-types` package, but received `' + typeof typeSpecs[typeSpecName] + '`.' + 'This often happens because of typos such as `PropTypes.function` instead of `PropTypes.func`.');
	            err.name = 'Invariant Violation';
	            throw err;
	          }

	          error$1 = typeSpecs[typeSpecName](values, typeSpecName, componentName, location, null, 'SECRET_DO_NOT_PASS_THIS_OR_YOU_WILL_BE_FIRED');
	        } catch (ex) {
	          error$1 = ex;
	        }

	        if (error$1 && !(error$1 instanceof Error)) {
	          setCurrentlyValidatingElement(element);

	          error('%s: type specification of %s' + ' `%s` is invalid; the type checker ' + 'function must return `null` or an `Error` but returned a %s. ' + 'You may have forgotten to pass an argument to the type checker ' + 'creator (arrayOf, instanceOf, objectOf, oneOf, oneOfType, and ' + 'shape all require an argument).', componentName || 'React class', location, typeSpecName, typeof error$1);

	          setCurrentlyValidatingElement(null);
	        }

	        if (error$1 instanceof Error && !(error$1.message in loggedTypeFailures)) {
	          // Only monitor this failure once because there tends to be a lot of the
	          // same error.
	          loggedTypeFailures[error$1.message] = true;
	          setCurrentlyValidatingElement(element);

	          error('Failed %s type: %s', location, error$1.message);

	          setCurrentlyValidatingElement(null);
	        }
	      }
	    }
	  }
	}

	var isArrayImpl = Array.isArray; // eslint-disable-next-line no-redeclare

	function isArray(a) {
	  return isArrayImpl(a);
	}

	/*
	 * The `'' + value` pattern (used in in perf-sensitive code) throws for Symbol
	 * and Temporal.* types. See https://github.com/facebook/react/pull/22064.
	 *
	 * The functions in this module will throw an easier-to-understand,
	 * easier-to-debug exception with a clear errors message message explaining the
	 * problem. (Instead of a confusing exception thrown inside the implementation
	 * of the `value` object).
	 */
	// $FlowFixMe only called in DEV, so void return is not possible.
	function typeName(value) {
	  {
	    // toStringTag is needed for namespaced types like Temporal.Instant
	    var hasToStringTag = typeof Symbol === 'function' && Symbol.toStringTag;
	    var type = hasToStringTag && value[Symbol.toStringTag] || value.constructor.name || 'Object';
	    return type;
	  }
	} // $FlowFixMe only called in DEV, so void return is not possible.


	function willCoercionThrow(value) {
	  {
	    try {
	      testStringCoercion(value);
	      return false;
	    } catch (e) {
	      return true;
	    }
	  }
	}

	function testStringCoercion(value) {
	  // If you ended up here by following an exception call stack, here's what's
	  // happened: you supplied an object or symbol value to React (as a prop, key,
	  // DOM attribute, CSS property, string ref, etc.) and when React tried to
	  // coerce it to a string using `'' + value`, an exception was thrown.
	  //
	  // The most common types that will cause this exception are `Symbol` instances
	  // and Temporal objects like `Temporal.Instant`. But any object that has a
	  // `valueOf` or `[Symbol.toPrimitive]` method that throws will also cause this
	  // exception. (Library authors do this to prevent users from using built-in
	  // numeric operators like `+` or comparison operators like `>=` because custom
	  // methods are needed to perform accurate arithmetic or comparison.)
	  //
	  // To fix the problem, coerce this object or symbol value to a string before
	  // passing it to React. The most reliable way is usually `String(value)`.
	  //
	  // To find which value is throwing, check the browser or debugger console.
	  // Before this exception was thrown, there should be `console.error` output
	  // that shows the type (Symbol, Temporal.PlainDate, etc.) that caused the
	  // problem and how that type was used: key, atrribute, input value prop, etc.
	  // In most cases, this console output also shows the component and its
	  // ancestor components where the exception happened.
	  //
	  // eslint-disable-next-line react-internal/safe-string-coercion
	  return '' + value;
	}
	function checkKeyStringCoercion(value) {
	  {
	    if (willCoercionThrow(value)) {
	      error('The provided key is an unsupported type %s.' + ' This value must be coerced to a string before before using it here.', typeName(value));

	      return testStringCoercion(value); // throw (to help callers find troubleshooting comments)
	    }
	  }
	}

	var ReactCurrentOwner = ReactSharedInternals.ReactCurrentOwner;
	var RESERVED_PROPS = {
	  key: true,
	  ref: true,
	  __self: true,
	  __source: true
	};
	var specialPropKeyWarningShown;
	var specialPropRefWarningShown;

	function hasValidRef(config) {
	  {
	    if (hasOwnProperty.call(config, 'ref')) {
	      var getter = Object.getOwnPropertyDescriptor(config, 'ref').get;

	      if (getter && getter.isReactWarning) {
	        return false;
	      }
	    }
	  }

	  return config.ref !== undefined;
	}

	function hasValidKey(config) {
	  {
	    if (hasOwnProperty.call(config, 'key')) {
	      var getter = Object.getOwnPropertyDescriptor(config, 'key').get;

	      if (getter && getter.isReactWarning) {
	        return false;
	      }
	    }
	  }

	  return config.key !== undefined;
	}

	function warnIfStringRefCannotBeAutoConverted(config, self) {
	  {
	    if (typeof config.ref === 'string' && ReactCurrentOwner.current && self) ;
	  }
	}

	function defineKeyPropWarningGetter(props, displayName) {
	  {
	    var warnAboutAccessingKey = function () {
	      if (!specialPropKeyWarningShown) {
	        specialPropKeyWarningShown = true;

	        error('%s: `key` is not a prop. Trying to access it will result ' + 'in `undefined` being returned. If you need to access the same ' + 'value within the child component, you should pass it as a different ' + 'prop. (https://reactjs.org/link/special-props)', displayName);
	      }
	    };

	    warnAboutAccessingKey.isReactWarning = true;
	    Object.defineProperty(props, 'key', {
	      get: warnAboutAccessingKey,
	      configurable: true
	    });
	  }
	}

	function defineRefPropWarningGetter(props, displayName) {
	  {
	    var warnAboutAccessingRef = function () {
	      if (!specialPropRefWarningShown) {
	        specialPropRefWarningShown = true;

	        error('%s: `ref` is not a prop. Trying to access it will result ' + 'in `undefined` being returned. If you need to access the same ' + 'value within the child component, you should pass it as a different ' + 'prop. (https://reactjs.org/link/special-props)', displayName);
	      }
	    };

	    warnAboutAccessingRef.isReactWarning = true;
	    Object.defineProperty(props, 'ref', {
	      get: warnAboutAccessingRef,
	      configurable: true
	    });
	  }
	}
	/**
	 * Factory method to create a new React element. This no longer adheres to
	 * the class pattern, so do not use new to call it. Also, instanceof check
	 * will not work. Instead test $$typeof field against Symbol.for('react.element') to check
	 * if something is a React Element.
	 *
	 * @param {*} type
	 * @param {*} props
	 * @param {*} key
	 * @param {string|object} ref
	 * @param {*} owner
	 * @param {*} self A *temporary* helper to detect places where `this` is
	 * different from the `owner` when React.createElement is called, so that we
	 * can warn. We want to get rid of owner and replace string `ref`s with arrow
	 * functions, and as long as `this` and owner are the same, there will be no
	 * change in behavior.
	 * @param {*} source An annotation object (added by a transpiler or otherwise)
	 * indicating filename, line number, and/or other information.
	 * @internal
	 */


	var ReactElement = function (type, key, ref, self, source, owner, props) {
	  var element = {
	    // This tag allows us to uniquely identify this as a React Element
	    $$typeof: REACT_ELEMENT_TYPE,
	    // Built-in properties that belong on the element
	    type: type,
	    key: key,
	    ref: ref,
	    props: props,
	    // Record the component responsible for creating this element.
	    _owner: owner
	  };

	  {
	    // The validation flag is currently mutative. We put it on
	    // an external backing store so that we can freeze the whole object.
	    // This can be replaced with a WeakMap once they are implemented in
	    // commonly used development environments.
	    element._store = {}; // To make comparing ReactElements easier for testing purposes, we make
	    // the validation flag non-enumerable (where possible, which should
	    // include every environment we run tests in), so the test framework
	    // ignores it.

	    Object.defineProperty(element._store, 'validated', {
	      configurable: false,
	      enumerable: false,
	      writable: true,
	      value: false
	    }); // self and source are DEV only properties.

	    Object.defineProperty(element, '_self', {
	      configurable: false,
	      enumerable: false,
	      writable: false,
	      value: self
	    }); // Two elements created in two different places should be considered
	    // equal for testing purposes and therefore we hide it from enumeration.

	    Object.defineProperty(element, '_source', {
	      configurable: false,
	      enumerable: false,
	      writable: false,
	      value: source
	    });

	    if (Object.freeze) {
	      Object.freeze(element.props);
	      Object.freeze(element);
	    }
	  }

	  return element;
	};
	/**
	 * https://github.com/reactjs/rfcs/pull/107
	 * @param {*} type
	 * @param {object} props
	 * @param {string} key
	 */

	function jsxDEV(type, config, maybeKey, source, self) {
	  {
	    var propName; // Reserved names are extracted

	    var props = {};
	    var key = null;
	    var ref = null; // Currently, key can be spread in as a prop. This causes a potential
	    // issue if key is also explicitly declared (ie. <div {...props} key="Hi" />
	    // or <div key="Hi" {...props} /> ). We want to deprecate key spread,
	    // but as an intermediary step, we will use jsxDEV for everything except
	    // <div {...props} key="Hi" />, because we aren't currently able to tell if
	    // key is explicitly declared to be undefined or not.

	    if (maybeKey !== undefined) {
	      {
	        checkKeyStringCoercion(maybeKey);
	      }

	      key = '' + maybeKey;
	    }

	    if (hasValidKey(config)) {
	      {
	        checkKeyStringCoercion(config.key);
	      }

	      key = '' + config.key;
	    }

	    if (hasValidRef(config)) {
	      ref = config.ref;
	      warnIfStringRefCannotBeAutoConverted(config, self);
	    } // Remaining properties are added to a new props object


	    for (propName in config) {
	      if (hasOwnProperty.call(config, propName) && !RESERVED_PROPS.hasOwnProperty(propName)) {
	        props[propName] = config[propName];
	      }
	    } // Resolve default props


	    if (type && type.defaultProps) {
	      var defaultProps = type.defaultProps;

	      for (propName in defaultProps) {
	        if (props[propName] === undefined) {
	          props[propName] = defaultProps[propName];
	        }
	      }
	    }

	    if (key || ref) {
	      var displayName = typeof type === 'function' ? type.displayName || type.name || 'Unknown' : type;

	      if (key) {
	        defineKeyPropWarningGetter(props, displayName);
	      }

	      if (ref) {
	        defineRefPropWarningGetter(props, displayName);
	      }
	    }

	    return ReactElement(type, key, ref, self, source, ReactCurrentOwner.current, props);
	  }
	}

	var ReactCurrentOwner$1 = ReactSharedInternals.ReactCurrentOwner;
	var ReactDebugCurrentFrame$1 = ReactSharedInternals.ReactDebugCurrentFrame;

	function setCurrentlyValidatingElement$1(element) {
	  {
	    if (element) {
	      var owner = element._owner;
	      var stack = describeUnknownElementTypeFrameInDEV(element.type, element._source, owner ? owner.type : null);
	      ReactDebugCurrentFrame$1.setExtraStackFrame(stack);
	    } else {
	      ReactDebugCurrentFrame$1.setExtraStackFrame(null);
	    }
	  }
	}

	var propTypesMisspellWarningShown;

	{
	  propTypesMisspellWarningShown = false;
	}
	/**
	 * Verifies the object is a ReactElement.
	 * See https://reactjs.org/docs/react-api.html#isvalidelement
	 * @param {?object} object
	 * @return {boolean} True if `object` is a ReactElement.
	 * @final
	 */


	function isValidElement(object) {
	  {
	    return typeof object === 'object' && object !== null && object.$$typeof === REACT_ELEMENT_TYPE;
	  }
	}

	function getDeclarationErrorAddendum() {
	  {
	    if (ReactCurrentOwner$1.current) {
	      var name = getComponentNameFromType(ReactCurrentOwner$1.current.type);

	      if (name) {
	        return '\n\nCheck the render method of `' + name + '`.';
	      }
	    }

	    return '';
	  }
	}

	function getSourceInfoErrorAddendum(source) {
	  {

	    return '';
	  }
	}
	/**
	 * Warn if there's no key explicitly set on dynamic arrays of children or
	 * object keys are not valid. This allows us to keep track of children between
	 * updates.
	 */


	var ownerHasKeyUseWarning = {};

	function getCurrentComponentErrorInfo(parentType) {
	  {
	    var info = getDeclarationErrorAddendum();

	    if (!info) {
	      var parentName = typeof parentType === 'string' ? parentType : parentType.displayName || parentType.name;

	      if (parentName) {
	        info = "\n\nCheck the top-level render call using <" + parentName + ">.";
	      }
	    }

	    return info;
	  }
	}
	/**
	 * Warn if the element doesn't have an explicit key assigned to it.
	 * This element is in an array. The array could grow and shrink or be
	 * reordered. All children that haven't already been validated are required to
	 * have a "key" property assigned to it. Error statuses are cached so a warning
	 * will only be shown once.
	 *
	 * @internal
	 * @param {ReactElement} element Element that requires a key.
	 * @param {*} parentType element's parent's type.
	 */


	function validateExplicitKey(element, parentType) {
	  {
	    if (!element._store || element._store.validated || element.key != null) {
	      return;
	    }

	    element._store.validated = true;
	    var currentComponentErrorInfo = getCurrentComponentErrorInfo(parentType);

	    if (ownerHasKeyUseWarning[currentComponentErrorInfo]) {
	      return;
	    }

	    ownerHasKeyUseWarning[currentComponentErrorInfo] = true; // Usually the current owner is the offender, but if it accepts children as a
	    // property, it may be the creator of the child that's responsible for
	    // assigning it a key.

	    var childOwner = '';

	    if (element && element._owner && element._owner !== ReactCurrentOwner$1.current) {
	      // Give the component that originally created this child.
	      childOwner = " It was passed a child from " + getComponentNameFromType(element._owner.type) + ".";
	    }

	    setCurrentlyValidatingElement$1(element);

	    error('Each child in a list should have a unique "key" prop.' + '%s%s See https://reactjs.org/link/warning-keys for more information.', currentComponentErrorInfo, childOwner);

	    setCurrentlyValidatingElement$1(null);
	  }
	}
	/**
	 * Ensure that every element either is passed in a static location, in an
	 * array with an explicit keys property defined, or in an object literal
	 * with valid key property.
	 *
	 * @internal
	 * @param {ReactNode} node Statically passed child of any type.
	 * @param {*} parentType node's parent's type.
	 */


	function validateChildKeys(node, parentType) {
	  {
	    if (typeof node !== 'object') {
	      return;
	    }

	    if (isArray(node)) {
	      for (var i = 0; i < node.length; i++) {
	        var child = node[i];

	        if (isValidElement(child)) {
	          validateExplicitKey(child, parentType);
	        }
	      }
	    } else if (isValidElement(node)) {
	      // This element was passed in a valid location.
	      if (node._store) {
	        node._store.validated = true;
	      }
	    } else if (node) {
	      var iteratorFn = getIteratorFn(node);

	      if (typeof iteratorFn === 'function') {
	        // Entry iterators used to provide implicit keys,
	        // but now we print a separate warning for them later.
	        if (iteratorFn !== node.entries) {
	          var iterator = iteratorFn.call(node);
	          var step;

	          while (!(step = iterator.next()).done) {
	            if (isValidElement(step.value)) {
	              validateExplicitKey(step.value, parentType);
	            }
	          }
	        }
	      }
	    }
	  }
	}
	/**
	 * Given an element, validate that its props follow the propTypes definition,
	 * provided by the type.
	 *
	 * @param {ReactElement} element
	 */


	function validatePropTypes(element) {
	  {
	    var type = element.type;

	    if (type === null || type === undefined || typeof type === 'string') {
	      return;
	    }

	    var propTypes;

	    if (typeof type === 'function') {
	      propTypes = type.propTypes;
	    } else if (typeof type === 'object' && (type.$$typeof === REACT_FORWARD_REF_TYPE || // Note: Memo only checks outer props here.
	    // Inner props are checked in the reconciler.
	    type.$$typeof === REACT_MEMO_TYPE)) {
	      propTypes = type.propTypes;
	    } else {
	      return;
	    }

	    if (propTypes) {
	      // Intentionally inside to avoid triggering lazy initializers:
	      var name = getComponentNameFromType(type);
	      checkPropTypes(propTypes, element.props, 'prop', name, element);
	    } else if (type.PropTypes !== undefined && !propTypesMisspellWarningShown) {
	      propTypesMisspellWarningShown = true; // Intentionally inside to avoid triggering lazy initializers:

	      var _name = getComponentNameFromType(type);

	      error('Component %s declared `PropTypes` instead of `propTypes`. Did you misspell the property assignment?', _name || 'Unknown');
	    }

	    if (typeof type.getDefaultProps === 'function' && !type.getDefaultProps.isReactClassApproved) {
	      error('getDefaultProps is only used on classic React.createClass ' + 'definitions. Use a static property named `defaultProps` instead.');
	    }
	  }
	}
	/**
	 * Given a fragment, validate that it can only be provided with fragment props
	 * @param {ReactElement} fragment
	 */


	function validateFragmentProps(fragment) {
	  {
	    var keys = Object.keys(fragment.props);

	    for (var i = 0; i < keys.length; i++) {
	      var key = keys[i];

	      if (key !== 'children' && key !== 'key') {
	        setCurrentlyValidatingElement$1(fragment);

	        error('Invalid prop `%s` supplied to `React.Fragment`. ' + 'React.Fragment can only have `key` and `children` props.', key);

	        setCurrentlyValidatingElement$1(null);
	        break;
	      }
	    }

	    if (fragment.ref !== null) {
	      setCurrentlyValidatingElement$1(fragment);

	      error('Invalid attribute `ref` supplied to `React.Fragment`.');

	      setCurrentlyValidatingElement$1(null);
	    }
	  }
	}

	var didWarnAboutKeySpread = {};
	function jsxWithValidation(type, props, key, isStaticChildren, source, self) {
	  {
	    var validType = isValidElementType(type); // We warn in this case but don't throw. We expect the element creation to
	    // succeed and there will likely be errors in render.

	    if (!validType) {
	      var info = '';

	      if (type === undefined || typeof type === 'object' && type !== null && Object.keys(type).length === 0) {
	        info += ' You likely forgot to export your component from the file ' + "it's defined in, or you might have mixed up default and named imports.";
	      }

	      var sourceInfo = getSourceInfoErrorAddendum();

	      if (sourceInfo) {
	        info += sourceInfo;
	      } else {
	        info += getDeclarationErrorAddendum();
	      }

	      var typeString;

	      if (type === null) {
	        typeString = 'null';
	      } else if (isArray(type)) {
	        typeString = 'array';
	      } else if (type !== undefined && type.$$typeof === REACT_ELEMENT_TYPE) {
	        typeString = "<" + (getComponentNameFromType(type.type) || 'Unknown') + " />";
	        info = ' Did you accidentally export a JSX literal instead of a component?';
	      } else {
	        typeString = typeof type;
	      }

	      error('React.jsx: type is invalid -- expected a string (for ' + 'built-in components) or a class/function (for composite ' + 'components) but got: %s.%s', typeString, info);
	    }

	    var element = jsxDEV(type, props, key, source, self); // The result can be nullish if a mock or a custom function is used.
	    // TODO: Drop this when these are no longer allowed as the type argument.

	    if (element == null) {
	      return element;
	    } // Skip key warning if the type isn't valid since our key validation logic
	    // doesn't expect a non-string/function type and can throw confusing errors.
	    // We don't want exception behavior to differ between dev and prod.
	    // (Rendering will throw with a helpful message and as soon as the type is
	    // fixed, the key warnings will appear.)


	    if (validType) {
	      var children = props.children;

	      if (children !== undefined) {
	        if (isStaticChildren) {
	          if (isArray(children)) {
	            for (var i = 0; i < children.length; i++) {
	              validateChildKeys(children[i], type);
	            }

	            if (Object.freeze) {
	              Object.freeze(children);
	            }
	          } else {
	            error('React.jsx: Static children should always be an array. ' + 'You are likely explicitly calling React.jsxs or React.jsxDEV. ' + 'Use the Babel transform instead.');
	          }
	        } else {
	          validateChildKeys(children, type);
	        }
	      }
	    }

	    {
	      if (hasOwnProperty.call(props, 'key')) {
	        var componentName = getComponentNameFromType(type);
	        var keys = Object.keys(props).filter(function (k) {
	          return k !== 'key';
	        });
	        var beforeExample = keys.length > 0 ? '{key: someKey, ' + keys.join(': ..., ') + ': ...}' : '{key: someKey}';

	        if (!didWarnAboutKeySpread[componentName + beforeExample]) {
	          var afterExample = keys.length > 0 ? '{' + keys.join(': ..., ') + ': ...}' : '{}';

	          error('A props object containing a "key" prop is being spread into JSX:\n' + '  let props = %s;\n' + '  <%s {...props} />\n' + 'React keys must be passed directly to JSX without using spread:\n' + '  let props = %s;\n' + '  <%s key={someKey} {...props} />', beforeExample, componentName, afterExample, componentName);

	          didWarnAboutKeySpread[componentName + beforeExample] = true;
	        }
	      }
	    }

	    if (type === REACT_FRAGMENT_TYPE) {
	      validateFragmentProps(element);
	    } else {
	      validatePropTypes(element);
	    }

	    return element;
	  }
	} // These two functions exist to still get child warnings in dev
	// even with the prod transform. This means that jsxDEV is purely
	// opt-in behavior for better messages but that we won't stop
	// giving you warnings if you use production apis.

	function jsxWithValidationStatic(type, props, key) {
	  {
	    return jsxWithValidation(type, props, key, true);
	  }
	}
	function jsxWithValidationDynamic(type, props, key) {
	  {
	    return jsxWithValidation(type, props, key, false);
	  }
	}

	var jsx =  jsxWithValidationDynamic ; // we may want to special case jsxs internally to take advantage of static children.
	// for now we can ship identical prod functions

	var jsxs =  jsxWithValidationStatic ;

	reactJsxRuntime_development.Fragment = REACT_FRAGMENT_TYPE;
	reactJsxRuntime_development.jsx = jsx;
	reactJsxRuntime_development.jsxs = jsxs;
	  })();
	}
	return reactJsxRuntime_development;
}

(function (module) {

	if (process.env.NODE_ENV === 'production') {
	  module.exports = requireReactJsxRuntime_production_min();
	} else {
	  module.exports = requireReactJsxRuntime_development();
	}
} (jsxRuntime));

var en = {};

var zh = {};

// file examples: en, enGB, zh, zhHK
// fallback example: current locale is zh-HK, fallback order is zhHK => zh => en

var localeData = /*#__PURE__*/Object.freeze({
    __proto__: null,
    en: en,
    zh: zh
});

var defaultLocale = "en";
var MissKeyErrorZh = "I18n错误：读取到未定义的key:%s ,这可能导致页面崩溃！";
var MissKeyErrorEn = "I18n error: Reading an undefined key: %s ,which will cause the page to crash!";
var locales = [defaultLocale];
if (globalThis.navigator) {
    if (navigator.languages && navigator.languages.length > 0) {
        locales = __spreadArray([], navigator.languages, true);
    }
    else {
        locales = [navigator.language || navigator.userLanguage || defaultLocale];
    }
}
function parseLocale(s) {
    var locale = s.trim();
    if (!locale) {
        return;
    }
    try {
        if (Intl.Locale) {
            var _a = new Intl.Locale(locale), language = _a.language, region = _a.region;
            return { locale: locale, language: language, region: region };
        }
        var parts = locale.split("-");
        var r = parts.slice(1, 3).find(function (t) { return t.length === 2; });
        return { locale: locale, language: parts[0].toLowerCase(), region: r === null || r === void 0 ? void 0 : r.toUpperCase() };
    }
    catch (e) {
        log.error("Parse locale:".concat(locale, " failed."), e);
    }
}
function parseLocales(list) {
    return list.map(parseLocale).filter(function (t) { return t; });
}
var fallbackLocaleInfos = parseLocales(locales.includes(defaultLocale) ? locales : __spreadArray(__spreadArray([], locales, true), [defaultLocale], false));
var i18n = __assign({ locales: locales }, fallbackLocaleInfos[0]);
function getValueByLocale(defaultValue, func) {
    for (var _i = 0, fallbackLocaleInfos_1 = fallbackLocaleInfos; _i < fallbackLocaleInfos_1.length; _i++) {
        var info = fallbackLocaleInfos_1[_i];
        var t = func(info);
        if (t !== undefined) {
            return t;
        }
    }
    return defaultValue;
}
function getDataByLocale(fileData, suffix, filterLocales, targetLocales) {
    var localeInfos = __spreadArray([], fallbackLocaleInfos, true);
    var targetLocaleInfo = parseLocales(targetLocales || []);
    if (targetLocaleInfo.length > 0) {
        localeInfos = __spreadArray(__spreadArray([], targetLocaleInfo, true), localeInfos, true);
    }
    var filterNames = parseLocales((filterLocales !== null && filterLocales !== void 0 ? filterLocales : "").split(","))
        .map(function (l) { var _a; return l.language + ((_a = l.region) !== null && _a !== void 0 ? _a : ""); })
        .filter(function (s) { return fileData[s + suffix] !== undefined; });
    var names = __spreadArray(__spreadArray([], localeInfos
        .flatMap(function (_a) {
        var language = _a.language, region = _a.region;
        return [
            region ? language + region : undefined,
            language,
            filterNames.find(function (n) { return n.startsWith(language); }),
        ];
    })
        .filter(function (s) { return s && (!filterLocales || filterNames.includes(s)); }), true), filterNames, true).map(function (s) { return s + suffix; });
    for (var _i = 0, names_1 = names; _i < names_1.length; _i++) {
        var name_1 = names_1[_i];
        var data = fileData[name_1];
        if (data !== undefined) {
            return { data: data, language: name_1.slice(0, 2) };
        }
    }
    throw new Error("Not found ".concat(names));
}
var globalMessageKeyPrefix = "@";
var globalMessages = Object.fromEntries(Object.entries(getDataByLocale(localeData, "").data).map(function (_a) {
    var k = _a[0], v = _a[1];
    return [
        globalMessageKeyPrefix + k,
        v,
    ];
}));
var Translator = /** @class */ (function () {
    function Translator(fileData, filterLocales, locales) {
        var _a = getDataByLocale(fileData, "", filterLocales, locales), data = _a.data, language = _a.language;
        this.messages = Object.assign({}, data, globalMessages);
        this.language = language;
        this.trans = this.trans.bind(this);
        this.transToNode = this.transToNode.bind(this);
    }
    Translator.prototype.trans = function (key, variables) {
        return this.transToNode(key, variables).toString();
    };
    Translator.prototype.transToNode = function (key, variables) {
        var message = this.getMessage(key);
        if (message == undefined)
            console.error(i18n.language == "zh" ? MissKeyErrorZh : MissKeyErrorEn, key);
        var node = new IntlMessageFormat(message, i18n.locale).format(variables);
        if (Array.isArray(node)) {
            return node.map(function (n, i) { return jsxRuntimeExports.jsx(Fragment, { children: n }, i); });
        }
        return node;
    };
    Translator.prototype.getMessage = function (key) {
        var value = this.messages[key];
        if (value !== undefined) {
            return value;
        }
        var obj = this.messages;
        for (var _i = 0, _a = key.split("."); _i < _a.length; _i++) {
            var k = _a[_i];
            if (obj !== undefined) {
                obj = obj[k];
            }
        }
        return obj;
    };
    return Translator;
}());
function getI18nObjects(fileData, filterLocales) {
    return getDataByLocale(fileData, "Obj", filterLocales).data;
}

export { AbstractComp, AbstractNode, CachedNode, CodeNode, CompActionTypes, FetchCheckNode, FunctionNode, MultiBaseComp, RecordNode, RelaxedJsonParser, SimpleAbstractComp, SimpleComp, SimpleNode, Translator, ValueAndMsg, WrapContextNodeV2, WrapNode, changeChildAction, changeDependName, changeEditDSLAction, changeValueAction, clearMockWindow, clearStyleEval, customAction, deferAction, deleteCompAction, dependingNodeMapEquals, evalFunc, evalFunctionResult, evalNodeOrMinor, evalPerfUtil, evalScript, evalStyle, executeQueryAction, fromRecord, fromUnevaledValue, fromValue, fromValueWithCache, getDynamicStringSegments, getI18nObjects, getValueByLocale, i18n, isBroadcastAction, isChildAction, isCustomAction, isDynamicSegment, isFetching, isMyCustomAction, mergeExtra, multiChangeAction, nodeIsRecord, onlyEvalAction, promiseWithDefaultOnTimeout, promiseWithTimeout, relaxedJSONToJSON, renameAction, replaceCompAction, routeByNameAction, transformWrapper, triggerModuleEventAction, unwrapChildAction, updateActionContextAction, updateNodesV2Action, withFunction, wrapActionExtraInfo, wrapChildAction, wrapContext, wrapDispatch };
