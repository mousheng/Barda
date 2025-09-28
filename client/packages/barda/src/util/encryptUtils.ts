
/**
 * 一个前端用于处理公钥加密的实用异步加载类。
 */
class encryptUtils {
    /**
     * 公钥字符串。
     */
    private publicKey: string | undefined;

    /**
     * JSEncrypt 实例。
     */
    private JSEncryptInstance: any;


    /**
     * 构造函数，初始化公钥并加载 JSEncrypt 实例。
     * @param publicKey 公钥字符串。
     */
    constructor(publicKey: string | undefined) {
        this.publicKey = publicKey
        this.loadEncrypt();
    }

    /**
     * 异步加载 JSEncrypt 实例并设置公钥。
     * 如果 JSEncrypt 实例尚未加载，并且公钥已设置，则先进行加载。
     *
     * @returns {Promise<void>} 一个 Promise，在 JSEncrypt 实例加载并设置公钥后解析。
     */
    async loadEncrypt() {
        if (this.publicKey && !this.JSEncryptInstance) {
            const JSEncrypt = await import("jsencrypt");
            this.JSEncryptInstance = new JSEncrypt.JSEncrypt()
            this.JSEncryptInstance.setPublicKey(this.publicKey)
        }
    }

    /**
     * 设置新的公钥并重新加载 JSEncrypt 实例。
     * @param publicKey 新的公钥字符串。
     */
    setPublickey(publicKey: string | undefined) {
        this.publicKey = publicKey
        this.loadEncrypt()
    }

    /**
     * 异步使用 JSEncrypt 实例对数据进行加密。
     * 如果公钥未设置，则返回原始数据。
     *
     * @param data 要加密的原始数据。
     * @returns 加密后的数据，如果公钥未设置，则返回原始数据。
     */
    async encrypt(data: string) {
        if (!this.publicKey) return data;
        await this.loadEncrypt();
        return this.JSEncryptInstance.encrypt(data)
    }

}

export default encryptUtils;