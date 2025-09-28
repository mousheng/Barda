package com.barda.infra.util;

import org.apache.commons.lang3.StringUtils;

import com.barda.infra.constant.NewUrl;

/**
 * 资产工具类。
 * 它提供静态方法来处理和转换与资产相关的操作。
 */
public final class AssetUtils {

    /**
     * 将资产ID转换为资产路径。
     *
     * @param assetId 资产ID
     * @return 资产路径
     */
    public static String toAssetPath(CharSequence assetId) {
        // 如果资产ID为空或空白，则返回空字符串
        return StringUtils.isBlank(assetId)? "" : NewUrl.ASSET_URL + "/" + assetId;
    }
}
