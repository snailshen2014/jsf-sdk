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
package com.ipd.jsf.gd.registry.jsf;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ipd.jsf.gd.registry.Provider;
import com.ipd.jsf.gd.util.JSFContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.config.AbstractInterfaceConfig;
import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.config.ProviderConfig;
import com.ipd.jsf.gd.config.ServerConfig;
import com.ipd.jsf.gd.error.InitErrorException;
import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.Constants.ProtocolType;
import com.ipd.jsf.gd.util.FileUtils;
import com.ipd.jsf.gd.util.JsonUtils;
import com.ipd.jsf.gd.util.NetUtils;
import com.ipd.jsf.gd.util.StringUtils;
import com.ipd.jsf.vo.JsfUrl;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public final class JSFRegistryHelper {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(JSFRegistryHelper.class);

    /**
     * 已订阅服务列表的版本号, interfaceId@alias@protocol:dataVersion
     */
    protected static final ConcurrentHashMap<String, Long> subscribed_dataversion = new ConcurrentHashMap<String, Long>();
    /**
     * 已订阅配置的版本号 interfaceId@alias:dataVersion
     */
    protected static final ConcurrentHashMap<String, Long> configed_dataversion = new ConcurrentHashMap<String, Long>();

    /**
     * Convert provider to url.
     *
     * @param providerConfig
     *         the ProviderConfig
     * @return the JsfUrl list
     */
    public static List<JsfUrl> convertProviderToUrl(ProviderConfig providerConfig) {
        List<JsfUrl> jsfUrls = new ArrayList<JsfUrl>();

        // 转换
        List<ServerConfig> serverConfigs = providerConfig.getServer();
        if (serverConfigs != null) {
            for (ServerConfig serverConfig : serverConfigs) {
                try {
                    JsfUrl jsfUrl = new JsfUrl();
                    String host = serverConfig.getVirtualhost(); // 虚拟ip
                    if (host == null) {
                        host = serverConfig.getHost();
                        if (NetUtils.isLocalHost(host) || NetUtils.isAnyHost(host)) {
                            host = JSFContext.getLocalHost();
                        }
                    }
                    jsfUrl.setIp(host);
                    jsfUrl.setPort(serverConfig.getPort());
                    jsfUrl.setPid(Integer.parseInt(JSFContext.PID));
                    jsfUrl.setIface(providerConfig.getInterfaceId());
                    jsfUrl.setAlias(providerConfig.getAlias());
                    jsfUrl.setProtocol(ProtocolType.valueOf(serverConfig.getProtocol()).value());
                    jsfUrl.setRandom(serverConfig.isRandomPort());
                    jsfUrl.setTimeout(providerConfig.getTimeout());
                    //自定义参数
                    Map<String, String> attrs = new HashMap<String, String>();
                    /* string safVersion, string language, string apppath, i32 weight */
                    addCommonAttrs(attrs);
                    attrs.put("id", providerConfig.getId());
                    attrs.put(Constants.CONFIG_KEY_DYNAMIC, providerConfig.isDynamic() + "");
                    attrs.put("ctxpath", serverConfig.getContextpath());
                    attrs.put(Constants.CONFIG_KEY_WEIGHT, providerConfig.getWeight() + "");
                    attrs.put(Constants.CONFIG_KEY_CROSSLANG,
                            providerConfig.getParameter(Constants.CONFIG_KEY_CROSSLANG));
                    jsfUrl.setAttrs(attrs);

                    jsfUrl.setInsKey((String) JSFContext.get(JSFContext.KEY_INSTANCEKEY));
                    jsfUrl.setStTime(JSFContext.START_TIME);

                    jsfUrls.add(jsfUrl);
                } catch (Exception e) {
                    LOGGER.warn("Error when convert provider config and server config to jsf url: "
                            + serverConfig.getId(), e);
                }
            }
        }
        return jsfUrls;
    }

    /**
     * Convert consumer to url.
     *
     * @param consumerConfig
     *         the consumer config
     * @return the list
     */
    public static JsfUrl convertConsumerToUrl(ConsumerConfig consumerConfig) {

        JsfUrl jsfUrl = new JsfUrl();
        jsfUrl.setIp(JSFContext.getLocalHost());
        jsfUrl.setPid(Integer.parseInt(JSFContext.PID));
        jsfUrl.setIface(consumerConfig.getInterfaceId());
        jsfUrl.setAlias(consumerConfig.getAlias());
        jsfUrl.setProtocol(ProtocolType.valueOf(consumerConfig.getProtocol()).value());
        //自定义参数
        Map<String, String> attrs = new HashMap<String, String>();
        /* string dubboVersion ,string safVersion, string language, string apppath, i32 weight */
        /* string safVersion, string language, string appPath, i32 weight */
        addCommonAttrs(attrs);
        String fromGroupFlag = consumerConfig.getParameter("_fromGroup");
        attrs.put("consumer", StringUtils.isNotBlank(fromGroupFlag) ? "2" : "1"); // 代表为服务调用者
        attrs.put("id", consumerConfig.getId());
        attrs.put("timestamp", JSFContext.systemClock.now() + "");
        if (consumerConfig.isGeneric()) { // 本地是Generic调用
            attrs.put(Constants.CONFIG_KEY_GENERIC, "1");
        }
        attrs.put(Constants.CONFIG_KEY_SERIALIZATION, consumerConfig.getSerialization());
        jsfUrl.setAttrs(attrs);

        jsfUrl.setInsKey((String) JSFContext.get(JSFContext.KEY_INSTANCEKEY));
        jsfUrl.setStTime(JSFContext.START_TIME);

        return jsfUrl;
    }

    /**
     * 加入一些公共的额外属性
     *
     * @param attrs
     *         属性
     */
    private static void addCommonAttrs(Map<String, String> attrs) {
        attrs.put("language", "java");
        attrs.put("apppath", (String) JSFContext.get(JSFContext.KEY_APPAPTH));
        attrs.put(Constants.CONFIG_KEY_SAFVERSION, Constants.DEFAULT_SAF_VERSION + "");
        attrs.put(Constants.CONFIG_KEY_JSFVERSION, Constants.JSF_VERSION + "");
        if (JSFContext.get("reg.backfile") != null || JSFContext.get("provider.backfile") != null) {
            attrs.put("backfile", "false");
        }
        putIfContextNotEmpty(attrs, "appId");
        putIfContextNotEmpty(attrs, "appName");
        putIfContextNotEmpty(attrs, "appInsId");
    }

    /**
     * 从上下文中拿到值，如果不为空，放入注册的属性列表中
     *
     * @param attrs
     *         属性
     * @param key
     *         关键字
     */
    private static void putIfContextNotEmpty(Map<String, String> attrs, String key) {
        Object object = JSFContext.get(key);
        if (object != null) {
            attrs.put(key, object.toString());
        }
    }

    /**
     * Convert url to providers.
     *
     * @param urls
     *         the urls
     * @param interfaceId
     *         th interfaceId
     * @return the list
     */
    public static List<Provider> convertUrlToProviders(List<JsfUrl> urls, String interfaceId) {
        List<Provider> providers = new ArrayList<Provider>(urls.size());
        for (JsfUrl url : urls) {
            Provider provider = new Provider();
            provider.setIp(url.getIp());
            provider.setPort(url.getPort());
            provider.setProtocolType(ProtocolType.valueOf(url.getProtocol()));
            provider.setInterfaceId(url.getIface() == null ? interfaceId : url.getIface());
            provider.setAlias(url.getAlias());
            Map<String, String> attrs = url.getAttrs();
            if (attrs != null) {
                String safVersion = attrs.get(Constants.CONFIG_KEY_SAFVERSION);
                if (safVersion != null) {
                    try {
                        provider.setSafVersion(Integer.parseInt(safVersion));
                    } catch (Exception e) {
                        LOGGER.warn("can't get saf version from string {} to int", safVersion);
                    }
                }
                String jsfVersion = attrs.get(Constants.CONFIG_KEY_JSFVERSION);
                if (jsfVersion != null) {
                    try {
                        provider.setJsfVersion(Integer.parseInt(jsfVersion));
                    } catch (Exception e) {
                        LOGGER.warn("can't get jsf version from string {} to int", safVersion);
                    }
                }
                String weight = attrs.get(Constants.CONFIG_KEY_WEIGHT);
                if (weight != null) {
                    try {
                        provider.setWeight(Integer.parseInt(weight));
                    } catch (Exception e) {
                        LOGGER.warn("can't get weight from string {} to int", weight);
                    }
                }
                String serialization = attrs.get(Constants.CONFIG_KEY_SERIALIZATION);
                if (serialization != null) {
                    try {
                        provider.setCodecType(Constants.CodecType.valueOf(serialization));
                    } catch (Exception e) {
                        LOGGER.warn("can't parse serialization " + serialization, e);
                    }
                }
            }
            providers.add(provider);
        }

        return providers;
    }

    /**
     * 全局配置变化需要的jsfUrl
     *
     * @return JsfUrl
     */
    public static JsfUrl buildConfigJsfUrl() {
        JsfUrl jsfUrl = new JsfUrl();
        jsfUrl.setIp(JSFContext.getLocalHost());
        jsfUrl.setPid(Integer.parseInt(JSFContext.PID));
        jsfUrl.setInsKey((String) JSFContext.get(JSFContext.KEY_INSTANCEKEY));
        Map<String, String> attrs = new HashMap<String, String>();
        putIfContextNotEmpty(attrs, "appId");
        putIfContextNotEmpty(attrs, "appName");
        putIfContextNotEmpty(attrs, "appInsId");
        jsfUrl.setAttrs(attrs);
        jsfUrl.setStTime(JSFContext.START_TIME);

        return jsfUrl;
    }

    /**
     * 更新订阅服务列表的数据版本
     *
     * @param jsfUrl
     *         订阅的jsfUrl
     * @param dataVersion
     *         服务列表数据版本
     */
    protected static void updateProviderDataVersion(JsfUrl jsfUrl, long dataVersion) {
        String key = buildSubscribedKey(jsfUrl);
        Long old = subscribed_dataversion.get(key);
        if (old != null) {
            if (dataVersion > old) {
                LOGGER.debug("{} provider data version change to {}", key, dataVersion);
                subscribed_dataversion.put(key, dataVersion);
            }
        } else {
            LOGGER.debug("{} provider data version set to {}", key, dataVersion);
            subscribed_dataversion.put(key, dataVersion);
        }
    }

    /**
     * 更新订阅配置的数据版本
     *
     * @param jsfUrl
     *         订阅的jsfUrl
     * @param dataVersion
     *         配置数据版本
     */
    protected static void updateConfigDataVersion(JsfUrl jsfUrl, long dataVersion) {
        String key = buildConfigKey(jsfUrl);
        Long old = configed_dataversion.get(key);
        if (old != null) {
            if (dataVersion > old) {
                LOGGER.debug("{} config data version change to {}", key, dataVersion);
                configed_dataversion.put(key, dataVersion);
            }
        } else {
            LOGGER.debug("{} config data version set to {}", key, dataVersion);
            configed_dataversion.put(key, dataVersion);
        }
    }

    /**
     * 更新订阅服务列表的数据版本
     *
     * @param jsfUrl
     *         订阅的jsfUrl
     */
    protected static void removeProviderDataVersion(JsfUrl jsfUrl) {
        String key = buildSubscribedKey(jsfUrl);
        subscribed_dataversion.remove(key);
    }

    /**
     * 更新订阅配置的数据版本
     *
     * @param jsfUrl
     *         订阅的jsfUrl
     */
    protected static void removeConfigDataVersion(JsfUrl jsfUrl) {
        String key = buildConfigKey(jsfUrl);
        configed_dataversion.remove(key);
    }

    /**
     * 批量更新订阅配置的数据版本
     *
     * @param jsfUrls
     *         订阅的jsfUrl
     */
    protected static void removeConfigDataVersions(List<JsfUrl> jsfUrls) {
        for (JsfUrl jsfUrl : jsfUrls) {
            removeConfigDataVersion(jsfUrl);
        }
    }

    /**
     * 更新订阅服务列表的数据版本
     *
     * @param jsfUrl
     *         订阅的jsfUrl
     * @return 数据版本
     */
    protected static long getProviderDataVersion(JsfUrl jsfUrl) {
        String key = buildSubscribedKey(jsfUrl);
        Long v = subscribed_dataversion.get(key);
        return v == null ? -1 : v;
    }


    /**
     * 更新订阅配置的数据版本
     *
     * @param jsfUrl
     *         订阅的jsfUrl
     * @return 数据版本
     */
    protected static long getConfigDataVersion(JsfUrl jsfUrl) {
        String key = buildConfigKey(jsfUrl);
        Long v = configed_dataversion.get(key);
        return v == null ? -1 : v;
    }

    /**
     * Build key.
     *
     * @param jsfUrl
     *         the jsf url
     * @return the string
     */
    protected static String buildSubscribedKey(JsfUrl jsfUrl) {
        String interfaceId = jsfUrl.getIface();
        return interfaceId == null ? Constants.GLOBAL_SETTING : interfaceId + "@" + jsfUrl.getAlias() + "@" + jsfUrl.getProtocol();
    }


    /**
     * Build key.
     *
     * @param jsfUrl
     *         the jsf url
     * @return the string
     */
    protected static String buildConfigKey(JsfUrl jsfUrl) {
        String interfaceId = jsfUrl.getIface();
        return interfaceId == null ? Constants.GLOBAL_SETTING : interfaceId + "@" + jsfUrl.getAlias();
    }

    /**
     * 匹配接口相同
     *
     * @param config
     *         配置
     * @param jsfUrl
     *         JsfUrl对象
     * @return 是否相同
     */
    protected static boolean matchInterface(AbstractInterfaceConfig config, JsfUrl jsfUrl) {
        String sourceitf = jsfUrl.getIface() == null ? Constants.GLOBAL_SETTING : jsfUrl.getIface();
        return config.getInterfaceId().equals(sourceitf);
    }

    /**
     * 匹配服务端相同
     *
     * @param config
     *         配置
     * @param jsfUrl
     *         JsfUrl对象
     * @return 是否相同
     */
    protected static boolean matchProvider(ProviderConfig config, JsfUrl jsfUrl) {
        return config.getInterfaceId().equals(jsfUrl.getIface())
                && config.getAlias().equals(jsfUrl.getAlias());
    }

    /**
     * 匹配客户端相同
     *
     * @param config
     *         配置
     * @param jsfUrl
     *         JsfUrl对象
     * @return 是否相同
     */
    protected static boolean matchConsumer(ConsumerConfig config, JsfUrl jsfUrl) {
        return config.getInterfaceId().equals(jsfUrl.getIface())
                && config.getAlias().equals(jsfUrl.getAlias())
                && Constants.ProtocolType.valueOf((config).getProtocol()).value() == jsfUrl.getProtocol();
    }

    /**
     * 备份文件地址
     */
    private static String backfilePath = getBackfilePath("jsfRegistry.json");

    /**
     * 从备份文件拿注册中心地址
     *
     * @return 注册中心地址
     */
    protected static String getAddressFromFile() {
        try {
            LOGGER.info("Read registry address from back file {}", backfilePath);
            File backfile = new File(backfilePath);
            if (!backfile.exists() || !backfile.isFile() || !backfile.canRead()) {
                LOGGER.warn("[JSF-20010]Can not found jsf registry address from backup file {}", backfilePath);
            } else {
                String json = FileUtils.file2String(backfile);
                Map tmpmap = JsonUtils.parseObject(json, Map.class);
                return StringUtils.trimToEmpty((String) tmpmap.get("address"));
            }
        } catch (Exception e) {
            LOGGER.warn("[JSF-20011]Failed to load registry address from back file " + backfilePath, e);
        }
        return StringUtils.EMPTY;
    }

    /**
     * 备份注册中心地址到备份文件
     *
     * @param registryAddress
     *         注册中心地址
     */
    protected static void writeAddressToFile(String registryAddress) {
        try {
            File backfile = new File(backfilePath);
            Map tmpmap = new HashMap();
            tmpmap.put("protocol", Constants.REGISTRY_PROTOCOL_JSF);
            tmpmap.put("address", registryAddress);
            boolean ok = FileUtils.string2File(backfile, JsonUtils.toJSONString(tmpmap));
            JSFContext.put("reg.backfile", ok ? null : "false"); // 打上标记
            LOGGER.info("Write registry address to back file {}, result: {}", backfilePath, ok);
        } catch (Exception e) {
            LOGGER.error("[JSF-20012]Failed to write registry address to back file " + backfilePath, e);
        }
    }

    /**
     * 备份文件地址
     */
    private static String globalConfigPath = getBackfilePath("globalConfig.json");

    /**
     * 从备份文件拿全局配置
     *
     * @return 全局配置
     */
    public static Map<String, String> readGlobalConfigFromFile() {
        try {
            LOGGER.info("Read global config from back file {}", globalConfigPath);
            File backfile = new File(globalConfigPath);
            if (!backfile.exists() || !backfile.isFile() || !backfile.canRead()) {
                LOGGER.warn("[JSF-20013]Can not found global config from backup file {}", globalConfigPath);
            } else {
                String json = FileUtils.file2String(backfile);
                Map tmpmap = JsonUtils.parseObject(json, Map.class);
                return tmpmap;
            }
        } catch (Exception e) {
            LOGGER.error("[JSF-20014]Failed to load global config from back file " + globalConfigPath, e);
        }
        return null;
    }

    /**
     * 备份全局配置到备份文件
     */
    protected static void writeGlobalConfigToFile() {
        try {
            File backfile = new File(globalConfigPath);
            boolean ok = FileUtils.string2File(backfile,
                    JsonUtils.toJSONString(JSFContext.getConfigMap(Constants.GLOBAL_SETTING)));
            JSFContext.put("reg.backfile", ok ? null : "false"); // 打上标记
            LOGGER.info("Write global config to back file {}, result: {}", globalConfigPath, ok);
        } catch (Exception e) {
            LOGGER.error("[JSF-20015]Failed to write global config to back file " + globalConfigPath, e);
        }
    }

    /**
     * 得到HOME目录下的备份名
     *
     * @param fileName
     *         文件名
     * @return 备份地址
     */
    private static String getBackfilePath(String fileName) {
        String backfile = FileUtils.getUserHomeDir(Constants.DEFAULT_PROTOCOL) + File.separator
                + FileUtils.getBaseDirName() + fileName;
        File home = new File(backfile).getParentFile();
        if (!home.exists()) {
            JSFContext.put("reg.backfile", "false"); // 打上标记
            LOGGER.error("User home dir {} is not exists!", home.getAbsolutePath());
        }
        return backfile;
    }

    /**
     * 备份文件地址
     */
    private static String interfaceMapPath = getBackfilePath("interfaceNameId.json");

    /**
     * 从备份文件拿接口名称和ID映射关系
     *
     * @return 接口名称和ID映射关系
     */
    public static Map<String, String> readInterfaceMapFromFile() {
        try {
            File backfile = new File(interfaceMapPath);
            if (!backfile.exists() || !backfile.isFile() || !backfile.canRead()) {
                //LOGGER.warn("can not load map of interface name and id from backup file {}", interfaceMapPath);
            } else {
                LOGGER.info("Read map of interface name and id from back file {}", interfaceMapPath);
                String json = FileUtils.file2String(backfile);
                Map tmpmap = JsonUtils.parseObject(json, Map.class);
                return tmpmap;
            }
        } catch (Exception e) {
            LOGGER.error("[JSF-20016]Failed to load map of interface name and id from back file " + interfaceMapPath, e);
        }
        return null;
    }

    /**
     * 备份接口名称和ID映射关系到备份文件
     */
    public static void writeInterfaceMapToFile() {
        try {
            File backfile = new File(interfaceMapPath);
            boolean ok = FileUtils.string2File(backfile, JsonUtils.toJSONString(JSFContext.getClassNameIfaceIdMap()));
            JSFContext.put("reg.backfile", ok ? null : "false"); // 打上标记
            LOGGER.info("Write map of interface name and id to back file {}, result: {}", interfaceMapPath, ok);
        } catch (Exception e) {
            LOGGER.error("[JSF-20017]Failed to write map of interface name and id to back file " + interfaceMapPath, e);
        }
    }

    /**
     * 新列表是否包含(大于等于)旧列表
     *
     * @param newProviders
     *         新列表
     * @param oldProviders
     *         老列表
     * @return 是否包含
     */
    protected static boolean containsProvider(Collection<Provider> newProviders, Collection<Provider> oldProviders) {
        if (newProviders.size() < oldProviders.size()) {
            return false;
        }
        for (Provider oldProvider : oldProviders) {
            boolean isContains = false;
            for (Provider newProvider : newProviders) { // 遍历新的
                String ip = newProvider.getIp();
                int port = newProvider.getPort();
                if (StringUtils.isEmpty(ip)) {
                    continue;
                }
                if (ip.equals(oldProvider.getIp()) && port == oldProvider.getPort()) {
                    isContains = true;
                    break;
                }
            }
            if (!isContains) {
                return false;
            }
        }
        return true;
    }
}