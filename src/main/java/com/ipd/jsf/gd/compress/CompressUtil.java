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
package com.ipd.jsf.gd.compress;

import com.ipd.jsf.gd.compress.snappy.Snappy;
import com.ipd.jsf.gd.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.error.InitErrorException;

/**
 * Title: 压缩工具类<br>
 * <p/>
 * Description: 公共方法<br>
 * <p/>
 */
public class CompressUtil {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(CompressUtil.class);

    /**
     * 全局参数，是否启动压缩
     * @see Constants#SETTING_INVOKE_CP_OPEN
     */
    public static boolean compressOpen = true;

    /**
     * 全局参数，启动压缩的起点大小（大于这个值才进行压缩）
     * @see Constants#SETTING_INVOKE_CP_SIZE
     */
    public static int compressThresholdSize = 2048;

    public static byte[] compress(byte[] src, byte compressType) {
        ICompress compress = getCompressor(compressType);
        if (compress == null) {
            return src;
        } else {
            return compress.compress(src);
        }
    }

    public static byte[] deCompress(byte[] src, byte compressType) {
        ICompress compress = getCompressor(compressType);
        if (compress == null) {
            return src;
        } else {
            return compress.deCompress(src);
        }
    }

    private static ICompress getCompressor(byte compressType) {
        ICompress compress = null;
        switch (Constants.CompressType.valueOf(compressType)) {
            case snappy:
                compress = Snappy.getInstance();
                break;
//            case lzo:
//                LOGGER.info("compress type  lzo is not yet implemented");
//                break;
//            case gzip:
//                LOGGER.info("compress type  gzip is not yet implemented");
//                break;
//            case zlib:
//                LOGGER.info("compress type  zlib is not yet implemented");
//                break;
            case NONE:
                break;
            default:
                throw new InitErrorException("Unkown compress algorithm :" + compressType);
        }
        return compress;
    }

}