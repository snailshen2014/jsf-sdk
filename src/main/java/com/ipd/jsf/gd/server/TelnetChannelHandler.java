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
package com.ipd.jsf.gd.server;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.ipd.jsf.gd.server.telnet.*;
import com.ipd.jsf.gd.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.util.NetUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Title:telnet连接处理器<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class TelnetChannelHandler extends ChannelInboundHandlerAdapter {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(TelnetChannelHandler.class);

    /**
     * 命令列表
     */
    private static Map<String, TelnetHandler> telnetMap = Collections.synchronizedMap(new LinkedHashMap<String, TelnetHandler>());
    
    /** 客户端字符集 */
    public final static Map<Channel, String> charsetMap = new ConcurrentHashMap<Channel, String>();

    /**
     * The constant HELP.
     */
    private static final String HELP = "help";
    /**
     * The constant EXIT.
     */
    private static final String EXIT = "exit";

    public TelnetChannelHandler() {

    }

    /**
     * Instantiates a new Telnet channel handler.
     *
     * @param handlers the handlers
     */
    public TelnetChannelHandler(List<TelnetHandler> handlers) {
        registry(handlers);
    }

    /**
     * Init void.
     */
    static {
        registryByClass(HelpTelnetHandler.class); // help
        registryByClass(VersionTelnetHandler.class); // version
        registryByClass(ListTelnetHandler.class); // ls
        registryByClass(PortTelnetHandler.class); // ps
        registryByClass(ConfigTelnetHandler.class);// conf
        registryByClass(SudoTelnetHandler.class); // sudo
        registryByClass(InvokeTelnetHandler.class); // invoke
        registryByClass(ConcurrentTelnetHandler.class); // tp
        registryByClass(DebugModeTelnetHandler.class); // debug
        registryByClass(ResetTelnetHandler.class); // reset
        registryByClass(ServiceInfoTelnetHandler.class); //info
        //registryByClass(MethodInfoTelnetHandler.class); // method
        registryByClass(ExitTelnetHandler.class); // exit
        registryByClass(JVMStatusTelnetHandler.class); // jvm
        registryByClass(CheckTelnetHandler.class); // check
        //registryByClass(EchoTelnetHandler.class);
        //registryByClass(ExecuteTelnetHandler.clas
        //registryByClass(LoadTelnetHandler.class);

        HelpTelnetHandler helpHandler = (HelpTelnetHandler) telnetMap.get(HELP);
        helpHandler.setSupportedCmds(telnetMap);
    }

    /**
     * Registry by class.
     *
     * @param clazz the clazz
     */
    private static void registryByClass(Class<? extends TelnetHandler> clazz) {
        try {
            TelnetHandler obj = clazz.newInstance();
            telnetMap.put(obj.getCommand(), obj);
        } catch (Exception e) {
            LOGGER.warn("Can not registry " + clazz.getCanonicalName() + " to telnet", e);
        }
    }

    /**
     * 注册一组命令和命令处理器
     *
     * @param handlers List<TelnetHandler>
     */
    public static void registry(List<TelnetHandler> handlers) {
        if (handlers != null) {
            for (TelnetHandler handler : handlers) {
                registry(handler);
            }
        }
    }

    /**
     * 注册一个命令和命令处理器
     *
     * @param handler TelnetHandler
     */
    public static void registry(TelnetHandler handler) {
        telnetMap.put(handler.getCommand().toLowerCase(), handler);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String message = msg.toString().trim();
        if (message.length() == 0) {
            return;
        }

        String command = "";
        //判断命令
        if (message.indexOf(" ") > 0) {
            int i = message.indexOf(' ');
            command = message.substring(0, i).trim();
            message = message.substring(i + 1).trim();
        } else {
            command = message;
            message = "";
        }
        //调用指定命令
        String result = "";
        if (telnetMap.containsKey(command)) {
            TelnetHandler handler = telnetMap.get(command.toLowerCase());
            result = handler.telnet(ctx.channel(), message);
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append("ERROR:You input the command:[" + command + " " + message + "] is not exist!!\r\n");
            TelnetHandler handler = telnetMap.get(HELP);
            result = handler.telnet(ctx.channel(), message);
            sb.append(result);
            sb.append("Please input again!\r\n");
            result = sb.toString();
        }
        if (result != null && !"".equals(result.trim())) {
            ctx.writeAndFlush(result + "\r\n");
            ctx.writeAndFlush("jsf>");
        }
        if (EXIT.equalsIgnoreCase(command)) {
            ctx.channel().close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        LOGGER.error("Telnet error", cause);
        ctx.channel().writeAndFlush(cause.getMessage());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        BaseServerHandler.addChannel(ctx.channel());
    }

    /**
     * 允许执行远程invoke命令的连接，前面进行过sudo操作
     */
    public final static Set<Channel> ALLOW_INVOKE_CHANNELS = new ConcurrentHashSet<Channel>();

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        LOGGER.info("Disconnected telnet from {}", NetUtils.channelToString(channel.remoteAddress(), channel.localAddress()));
        BaseServerHandler.removeChannel(channel);
        charsetMap.remove(channel);
        ALLOW_INVOKE_CHANNELS.remove(channel);
    }
}