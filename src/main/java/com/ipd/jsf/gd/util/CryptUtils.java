/**
 * Copyright 2004-2048 .
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ipd.jsf.gd.util;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import com.ipd.jsf.gd.error.InitErrorException;

/**
 * Title: 加解密实现类<br>
 * <p/>
 * Description: 简单的加解密，默认3des<br>
 * <p/>
 */
public class CryptUtils {

    /**
     * 默认加密算法
     */
    private final static String CIPHER_ALGORITHM = "DESede";

    /**
     * 默认加密钥匙
     */
    private final static Key DEFAULT_KEY = getDefaultKey();

    /**
     * 得到key
     *
     * @param keyData
     *         key的字节数据
     * @return Key对象
     * @throws java.security.InvalidKeyException
     *         非法的key异常
     * @throws java.security.NoSuchAlgorithmException
     *         没有该算法异常
     * @throws java.security.spec.InvalidKeySpecException
     *         key错误异常
     */
    public static Key getKeyByBytes(byte[] keyData) throws InvalidKeyException,
            NoSuchAlgorithmException, InvalidKeySpecException {
        // 从原始密匙数据创建DESKeySpec对象
        KeySpec dks = new DESedeKeySpec(keyData);
        // 创建一个密匙工厂，然后用它把DESKeySpec转换成一个SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory
                .getInstance(CIPHER_ALGORITHM);
        SecretKey securekey = keyFactory.generateSecret(dks);
        return securekey;
    }

    /**
     * 得到加密解密对象
     *
     * @param cipherMode
     *         Cipher.DECRYPT_MODE或者Cipher.ENCRYPT_MODE
     * @param key
     *         key对象
     * @param random
     *         个可信任的随机数源
     * @return 加密器
     * @throws java.security.InvalidKeyException
     * @throws javax.crypto.NoSuchPaddingException
     * @throws java.security.NoSuchAlgorithmException
     * @see javax.crypto.Cipher#ENCRYPT_MODE
     * @see javax.crypto.Cipher#DECRYPT_MODE
     */
    public static Cipher getCipher(int cipherMode, Key key, SecureRandom random)
            throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException {
        // Cipher对象实际完成解密操作
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        // 用密匙初始化Cipher对象
        cipher.init(cipherMode, key, random);
        return cipher;
    }

    /**
     * 加密
     *
     * @param src
     *         数据源
     * @param securekey
     *         密钥
     * @return 返回加密后的数据
     * @throws Exception
     *         抛出异常
     */
    public static byte[] encrypt(byte[] src, Key securekey) throws Exception {
        Cipher cipher = getCipher(Cipher.ENCRYPT_MODE, securekey,
                new SecureRandom());
        // 现在，获取数据并加密正式执行加密操作
        return cipher.doFinal(src);
    }

    /**
     * 加密
     *
     * @param src
     *         数据源
     * @param keyData
     *         密钥，长度必须是8的倍数
     * @return 返回加密后的数据
     * @throws Exception
     *         抛出异常
     */
    public static byte[] encrypt(byte[] src, byte[] keyData) throws Exception {
        Key securekey = getKeyByBytes(keyData);
        return encrypt(src, securekey);
    }

    /**
     * 加密
     *
     * @param src
     *         数据源
     * @return 返回加密后的数据
     * @throws Exception
     *         抛出异常
     */
    public static byte[] encrypt(byte[] src) throws Exception {
        return encrypt(src, DEFAULT_KEY);
    }

    /**
     * 数据加密
     *
     * @param data
     *         数据
     * @param key
     *         密钥
     * @return 加密结果
     * @throws Exception
     *         抛出异常
     */
    public final static String encrypt(String data, String key) {
        if (data != null) {
            try {
                return byte2hex(encrypt(data.getBytes(), key.getBytes()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * 数据加密
     *
     * @param data
     *         数据
     * @return 加密结果
     * @throws Exception
     *         抛出异常
     */
    public final static String encrypt(String data) throws Exception {
        if (data != null) {
            return byte2hex(encrypt(data.getBytes(), DEFAULT_KEY));
        }
        return null;
    }

    /**
     * 解密
     *
     * @param src
     *         数据源
     * @param securekey
     *         密钥
     * @return 返回解密后的原始数据
     * @throws Exception
     *         抛出异常
     */
    public static byte[] decrypt(byte[] src, Key securekey) throws Exception {
        Cipher cipher = getCipher(Cipher.DECRYPT_MODE, securekey,
                new SecureRandom());
        // 现在，获取数据并解密正式执行解密操作
        return cipher.doFinal(src);
    }

    /**
     * 解密
     *
     * @param src
     *         数据源
     * @param key
     *         密钥，长度必须是8的倍数
     * @return 返回解密后的原始数据
     * @throws Exception
     *         抛出异常
     */
    public static byte[] decrypt(byte[] src, byte[] key) throws Exception {
        Key securekey = getKeyByBytes(key);
        return decrypt(src, securekey);
    }

    /**
     * 使用默认key解密
     *
     * @param src
     *         数据源
     * @return 返回解密后的原始数据
     * @throws Exception
     *         抛出异常
     */
    public static byte[] decrypt(byte[] src) throws Exception {
        return decrypt(src, DEFAULT_KEY);
    }

    /**
     * 数据解密
     *
     * @param data
     *         数据
     * @param key
     *         密钥字符串
     * @return
     * @throws Exception
     *         抛出异常
     */
    public final static String decrypt(String data, String key)
            throws Exception {
        return new String(decrypt(hex2byte(data.getBytes()), key.getBytes()));
    }

    /**
     * 使用默认key对数据解密
     *
     * @param data
     *         数据
     * @return 解密结果
     * @throws Exception
     *         抛出异常
     */
    public final static String decrypt(String data) throws Exception {
        return new String(decrypt(hex2byte(data.getBytes()), DEFAULT_KEY));
    }

    /**
     * 二行制转字符串
     *
     * @param b
     * @return
     */
    public static String byte2hex(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; b != null && n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1)
                hs.append('0');
            hs.append(stmp);
        }
        return hs.toString().toUpperCase();
    }

    /**
     * 十六转byte
     *
     * @param b
     * @return
     */
    public static byte[] hex2byte(byte[] b) {
        if ((b.length % 2) != 0)
            throw new IllegalArgumentException();
        byte[] b2 = new byte[b.length / 2];
        for (int n = 0; n < b.length; n += 2) {
            String item = new String(b, n, 2);
            b2[n / 2] = (byte) Integer.parseInt(item, 16);
        }
        return b2;
    }

    /**
     * 得到默认的key
     *
     * @return 默认的key
     */
    private static Key getDefaultKey() {
        try {
            // TODO 到时候删掉
            return getKeyByBytes(new byte[]{-84, -19, 0, 5, 115, 114, 0,
                    20, 106, 97, 118, 97, 46, 115, 101, 99, 117, 114, 105,
                    116, 121, 46, 75, 101, 121, 82, 101, 112, -67, -7, 79,
                    -77, -120, -102, -91, 67, 2, 0, 4, 76, 0, 9, 97, 108,
                    103, 111, 114, 105, 116, 104, 109, 116, 0, 18, 76, 106,
                    97, 118, 97, 47, 108, 97, 110, 103, 47, 83, 116, 114,
                    105, 110, 103, 59, 91, 0, 7, 101, 110, 99, 111, 100,
                    101, 100, 116, 0, 2, 91, 66, 76, 0, 6, 102, 111, 114,
                    109, 97, 116, 113, 0, 126, 0, 1, 76, 0, 4, 116, 121,
                    112, 101, 116, 0, 27, 76, 106, 97, 118, 97, 47, 115,
                    101, 99, 117, 114, 105, 116, 121, 47, 75, 101, 121, 82,
                    101, 112, 36, 84, 121, 112, 101, 59, 120, 112, 116, 0,
                    6, 68, 69, 83, 101, 100, 101, 117, 114, 0, 2, 91, 66,
                    -84, -13, 23, -8, 6, 8, 84, -32, 2, 0, 0, 120, 112, 0,
                    0, 0, 24, -15, 61, 52, 26, 38, 109, 67, -62, 59, 31,
                    42, 62, 49, -105, -2, -50, 25, 121, 62, -29, 52, -70,
                    -15, -56, 116, 0, 3, 82, 65, 87, 126, 114, 0, 25, 106,
                    97, 118, 97, 46, 115, 101, 99, 117, 114, 105, 116, 121,
                    46, 75, 101, 121, 82, 101, 112, 36, 84, 121, 112, 101,
                    0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 120, 114, 0, 14, 106,
                    97, 118, 97, 46, 108, 97, 110, 103, 46, 69, 110, 117,
                    109, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 120, 112, 116,
                    0, 6, 83, 69, 67, 82, 69, 84});
        } catch (Exception e) {
            throw new InitErrorException("Get default crypt key error !", e);
        }
    }
}