/**
 * Copyright 2022 Vladimir Gorej.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * source: https://vladimirgorej.com/blog/advanced-memoization-for-javascript-functions-with-lodash-memoize/
 *
 */

import { isEqual as lodashIsEqual } from "lodash";
import { MapCache, memoize } from "lodash";
import * as Collections from "typescript-collections";

type EqualFn = (o1: any, o2: any) => boolean;

export const sameValueZero = (x: any, y: any) => x === y || lodashIsEqual(x, y);
export const strictEquality = (x: any, y: any) => x === y;

const makeShallowArrayEquals = (comparators: Array<EqualFn> | EqualFn, order?: number[]) => (a: any) => (b: any) => {
  if (!Array.isArray(a) || !Array.isArray(b) || a.length !== b.length) {
    return false;
  }
  const indices = order || Array.from({ length: a.length }, (_, i) => i);
  return indices.every((targetIndex, currentIndex) => {
    const comparator = typeof comparators === "function" ? comparators : comparators[targetIndex] || sameValueZero;
    return comparator(a[targetIndex], b[targetIndex]);
  });
};
const list = (...args: any) => args;

class Cache implements MapCache {
  shallowArrayEquals = makeShallowArrayEquals(sameValueZero);

  map = new Map();

  queue = new Collections.Queue();
  capacity = 8;

  keys() {
    return this.map.keys();
  }

  delete(key: any): boolean {
    return this.map.delete(key);
  }

  get(key: any) {
    const keys = Array.from(this.keys());
    const foundKey = keys.find(this.shallowArrayEquals(key));
    // log.debug("get key: ", key, "found key: ", foundKey);
    return this.map.get(foundKey);
  }
  has(key: any) {
    const keys = Array.from(this.keys());
    const yes = keys.findIndex(this.shallowArrayEquals(key)) !== -1;
    // log.debug("has key: ", key, "found key: ", yes);
    return yes;
  }
  set(key: any, value: any): this {
    if (!this.has(key)) {
      this.mayPopQueue();
      this.queue.enqueue(key);
    }
    this.map.set(key, value);
    // log.debug("setCache. length: ", this.queue.size(), " key size: ", _.size(Array.from(this.keys())));
    // log.debug("setCache. queue: ", JSON.stringify(this.queue), " keys: ", Array.from(this.keys()));
    return this;
  }
  mayPopQueue() {
    while (this.queue.size() >= this.capacity) {
      const key = this.queue.dequeue();
      this.map.delete(key);
    }
  }
}

export const memoizeN = (
  fn: (...args: any) => any,
  { resolver = list, comparators = sameValueZero, capacity = 8, order }: { resolver?: (...args: any[]) => any, comparators?: Array<EqualFn> | EqualFn, capacity?: number, order?: number[] }
) => {
  const { Cache: OriginalCache } = memoize;
  memoize.Cache = Cache;
  const memoized = memoize(fn, resolver || list);
  // @ts-ignore
  memoized.cache.shallowArrayEquals = makeShallowArrayEquals(comparators, order);
  // @ts-ignore
  memoized.cache.capacity = capacity;
  memoize.Cache = OriginalCache;
  return memoized;
};
