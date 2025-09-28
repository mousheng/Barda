import _ from "lodash";

/**
 * 名称自动生成器，用于生成组件的默认名称
 *
 * @remarks
 * 自动生成器根据传入的类型名称自动生成项目名称。
 * 例如：传入 typeName="button"，当前按钮生成数量为 2，则返回 itemName="button3"
 */
export class NameGenerator {
  /**
  * 用于记录当前编号进度的结构体
  */
  protected record: Record<string, number> = {};

  isDistinct(name: string): boolean {
    const nameSet: Set<string> = this.getNameSet();
    return !nameSet.has(name);
  }

  /**
   * 自动生成项目名称
   * @remarks
   * 具有副作用，会改变生成器的状态
   * @param typeName 要获取 itemName 的 typeName
   * @returns 对应于 typeName 的生成的 itemName 结果
   */
  genItemName(typeName: string): string {
    let name;
    do {
      name = this.genDefaultItemName(typeName);
    } while (!this.isDistinct(name));
    return name;
  }

  init(itemNames: Array<string>): this {
    this.clear();
    const regex = new RegExp("^(.+?)(\\d+)$", "i");
    itemNames.forEach((name: string) => {
      const result = regex.exec(name);
      // log.log("result", result);
      if (result) {
        const [, typeName, itemName] = result;
        if (typeName && itemName) {
          this.update(typeName, _.ceil(+itemName));
        }
      }
    });
    return this;
  }

  private clear() {
    this.record = {};
  }

  private update(typeName: string, num: number) {
    if (!Number.isInteger(num)) {
      return;
    }
    const recNum = this.record[typeName] ?? 0;
    if (recNum < num) {
      this.record[typeName] = num;
    }
  }

  private getNameSet(): Set<string> {
    // FIXME: add name
    return new Set();
  }

  private genDefaultItemName(typeName: string): string {
    const nextNum = (this.record[typeName] ?? 0) + 1;
    this.update(typeName, nextNum);
    return typeName + nextNum;
  }
}
