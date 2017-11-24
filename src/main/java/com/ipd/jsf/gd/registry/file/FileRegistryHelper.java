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
package com.ipd.jsf.gd.registry.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.ipd.jsf.gd.registry.Provider;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.FileUtils;
import com.ipd.jsf.gd.config.AbstractConsumerConfig;
import com.ipd.jsf.gd.util.JSFContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.error.InitErrorException;
import com.ipd.jsf.gd.util.StringUtils;

/**
 * Title: 文件注册中心辅助类<br>
 * <p/>
 * Description: 保留指定的备份文件，通过jaxb实现文件的读写<br>
 * <p/>
 */
public class FileRegistryHelper {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(FileRegistryHelper.class);

    /**
     * 按项目路径得到的xml备份文件名称
     */
    protected final static String XML_FILENAME = FileUtils.getBaseDirName() + "fileRegistry.xml";
    /**
     * 按项目路径的得到的xml备份文件锁
     */
    private final static String LOCK_FILENAME = FileUtils.getBaseDirName() + "fileRegistry.lock";
    /**
     * 最大备份文件数
     */
    private final static int MAX_FILE_INDEX = 20;//保持20个历史文件

    /**
     * 关键字分隔符
     */
    private static final String KEY_SPERATOR = "@";

    /**
     * Build consumer key.
     *
     * @param consumerConfig
     *         the ConsumerConfig
     * @return 关键字
     */
    public static String buildKey(AbstractConsumerConfig consumerConfig) {
        return StringUtils.trimToEmpty(consumerConfig.getInterfaceId()) + KEY_SPERATOR +
                StringUtils.trimToEmpty(consumerConfig.getAlias()) + KEY_SPERATOR +
                StringUtils.trimToEmpty(consumerConfig.getProtocol());
    }

    /**
     * Build consumer key.
     *
     * @param consumerConfig
     *         the ConsumerVo
     * @return 关键字
     */
    public static String buildKey(ConsumerVo consumerConfig) {
        return StringUtils.trimToEmpty(consumerConfig.getInterfaceId()) + KEY_SPERATOR +
                StringUtils.trimToEmpty(consumerConfig.getAlias()) + KEY_SPERATOR +
                StringUtils.trimToEmpty(consumerConfig.getProtocol());
    }

    /**
     * Parse consumer key.
     *
     * @param key
     *         the key
     * @return the string [ ]
     */
    public static String[] parseKey(String key) {
        String[] ss = key.split(KEY_SPERATOR, -1);
        if (ss.length != 3) {
            throw new IllegalArgumentException("Paese key error, Illeagal key :" + key);
        }
        return ss;
    }

    /**
     * 备份操作，备份服务列表到本地
     *
     * @param address
     *         备份地址
     * @param cache
     *         备份的内容
     */
    public static synchronized void backup(String address, Map<String, List<Provider>> cache) {
        // 先写一个lock文件，跨进程的锁
        File lockFile = new File(address, LOCK_FILENAME);
        if (lockFile.exists()) {
            LOGGER.warn("Other process is writing fileRegistry.xml, retry after 1s");
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                // ignore
            }
            // 锁住超过一分钟，删掉
            if (lockFile.exists()) {
                if ((JSFContext.systemClock.now() - lockFile.lastModified()) > 60000) {
                    LOGGER.warn("Other process is locking over 60s, force release : {}", lockFile.delete());
                } else {
                    LOGGER.warn("Other process is stilling writing, aborted");
                    return;
                }
            }
        }
        boolean created = false;
        try {
            // 写个锁文件
            created = lockFile.createNewFile();
            if (!created) {
                LOGGER.warn("Create lock file false, may be other process is writing. aborted");
                return;
            }

            // 得到备份的文件内容
            FileRegistryVo fileRegistryVo = buildFileRegistryVo(cache);
            String content = writeToString(fileRegistryVo);

            // 重命名文件，备份旧文件
            File xmlFile = new File(address, XML_FILENAME);
            if (xmlFile.exists()) {
                renameOldBack(xmlFile);
            }

            // 独占锁，写入文件
            if (xmlFile.createNewFile()) {
                RandomAccessFile randomAccessFile = null;
                FileLock lock = null;
                try {
                    randomAccessFile = new RandomAccessFile(xmlFile, "rw");
                    FileChannel fileChannel = randomAccessFile.getChannel();
                    // get an exclusive lock on this channel
                    lock = fileChannel.tryLock();
                    //FileLock lock = fileChannel.tryLock(0, Integer.MAX_VALUE, true);
                    fileChannel.write(Constants.DEFAULT_CHARSET.encode(content));
                    fileChannel.force(false);
                } finally {
                    if (lock != null) {
                        lock.release();
                    }
                    if (randomAccessFile != null) {
                        randomAccessFile.close();
                    }
                }
                LOGGER.info("Write backup file to {}", xmlFile.getAbsolutePath());
                JSFContext.put("provider.backfile", null); // 打上标记
            }
        } catch (Exception e) {
            LOGGER.error("Backup registry file error !", e);
        } finally {
            if (created) {
                boolean deleted = lockFile.delete();
                if (!deleted) {
                    LOGGER.warn("Lock file create by this thread, but failed to delete it," +
                            " may be the elapsed time of this backup is too long");
                }
            }
        }
    }

    private static FileRegistryVo buildFileRegistryVo(Map<String, List<Provider>> cache) {
        FileRegistryVo fileRegistryVo = new FileRegistryVo();
        fileRegistryVo.setJsfVersion(Constants.JSF_VERSION + "");
        fileRegistryVo.setBackupTime(new Date());

        List<ConsumerVo> consumerVos = new ArrayList<ConsumerVo>();
        for (Map.Entry<String, List<Provider>> entry : cache.entrySet()) {
            ConsumerVo vo = new ConsumerVo();
            String[] keys = parseKey(entry.getKey());
            vo.setInterfaceId(keys[0]);
            vo.setAlias(keys[1]);
            vo.setProtocol(keys[2]);
            vo.setProviders(entry.getValue());
            consumerVos.add(vo);
        }
        fileRegistryVo.setConsumerVos(consumerVos);
        return fileRegistryVo;
    }

    private static void renameOldBack(final File xmlFile) {
        String filePath = xmlFile.getAbsolutePath();

        // 保留10个历史文件
        File file = new File(filePath + MAX_FILE_INDEX); // 删掉10
        if (file.exists()) {
            file.delete();
        }
        for (int i = MAX_FILE_INDEX - 1; i > 0; i--) {
            file = new File(filePath + i);
            if (file.exists()) {
                file.renameTo(new File(filePath + (i + 1))); // 2->3 之类的
            }
        }
        // 当前文件变成1
        xmlFile.renameTo(new File(filePath + 1));
    }

    /**
     * Write fileRegistryVo to file.
     *
     * @param <T>
     *         the type parameter
     * @param t
     *         the object vo
     * @throws JAXBException
     *         the jAXB exception
     */
    protected static <T> String writeToString(T t) throws JAXBException, IOException {
        JAXBContext context = JAXBContext.newInstance(t.getClass());
        Marshaller marshaller = context.createMarshaller();
        // marshaller.setProperty(Marshaller.JAXB_ENCODING,"UTF-8");//编码格式
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);//是否格式化生成的xml串
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, false);//是否省略xml头信息

        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(t, stringWriter);
        return stringWriter.toString();
    }

    /**
     * Write fileRegistryVo to file.
     *
     * @param <T>
     *         the type parameter
     * @param t
     *         the object vo
     * @param file
     *         the file
     * @throws JAXBException
     *         the jAXB exception
     */
    protected static <T> void writeToXml(T t, File file) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(t.getClass());
        Marshaller marshaller = context.createMarshaller();
        // marshaller.setProperty(Marshaller.JAXB_ENCODING,"UTF-8");//编码格式
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);//是否格式化生成的xml串
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, false);//是否省略xml头信息
        marshaller.marshal(t, file);
    }

    /**
     * Read FileRegistryVo from xml.
     *
     * @param file
     *         the file
     * @return the file registry vo
     * @throws JAXBException
     *         the jAXB exception
     */
    protected static FileRegistryVo readFromXml(File file) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(FileRegistryVo.class);
        Unmarshaller unMarshaller = context.createUnmarshaller();
        //从最新的文件中解析出provider
        return (FileRegistryVo) unMarshaller.unmarshal(file);
    }

    /**
     * 读取本地备份文件到内存中
     *
     * @param address
     *         the address
     * @param cache
     *         the cache
     * @return the 最后的load时间
     */
    public static Long loadBackupFileToCache(String address, Map<String, List<Provider>> cache) {
        // 从文件夹下读取指定文件
        File xmlFile = new File(address, XML_FILENAME);
        if (!xmlFile.exists()) {
            LOGGER.info("Load backup file failure cause by can't found file: {}", xmlFile.getAbsolutePath());
        } else {
            try {
                FileRegistryVo fileRegistryVo = readFromXml(xmlFile);
                LOGGER.info("Load backup file from {}, backup time is {}",
                        xmlFile.getAbsolutePath(), fileRegistryVo.getBackupTime());
                // 加载到内存中
                List<ConsumerVo> consumerVos = fileRegistryVo.getConsumerVos();
                if (consumerVos != null) {
                    for (ConsumerVo consumerVo : consumerVos) {
                        String key = buildKey(consumerVo);
                        cache.put(key, consumerVo.getProviders());
                    }
                }
            } catch (JAXBException e) {
                throw new InitErrorException("Error when read backup file: " + xmlFile.getAbsolutePath(), e);
            }
        }
        return JSFContext.systemClock.now();
    }

    /**
     * Check file's lastmodified.
     *
     * @param address
     *         the address
     * @param updateDate
     *         the update date
     * @return true被修改，false未被修改
     */
    public static boolean checkModified(String address, long updateDate) {
        // 检查文件是否被修改了
        File xmlFile = new File(address, XML_FILENAME);
        return xmlFile.lastModified() > updateDate;
    }

}