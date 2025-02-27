package com.timess.apiclientsdk.utils;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;

public class SignUtils {
    /**
     * 生成签名sign
     * @param
     * @param secretKey
     * @return
     */
    public static String genSign(String body, String secretKey){
        Digester md5 = new Digester(DigestAlgorithm.SHA256);
        //加密内容
        String content= body + "." + secretKey;
        return md5.digestHex(content);
    }
}
