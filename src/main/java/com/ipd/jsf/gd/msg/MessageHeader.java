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
package com.ipd.jsf.gd.msg;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ipd.jsf.gd.util.Constants;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class MessageHeader implements Cloneable{

    private Integer length; // 总长度 包含magiccode + header + body

    private Short headerLength;

    private int protocolType = Constants.DEFAULT_PROTOCOL_TYPE.value();

    private int codecType = Constants.DEFAULT_CODEC_TYPE.value();

    private int msgType;

    private int msgId;

    private byte compressType = Constants.CompressType.NONE.value();

    private Map<Byte,Object> keysMap = new ConcurrentHashMap<Byte,Object>();

    public Map<Byte,Object> getAttrMap(){
        return this.keysMap;
    }

	public MessageHeader setValues(int protocolType, int codecType,
			int msgType, int compressType, int msgId) {
		this.msgId = msgId;
		this.codecType = codecType;
		this.msgType = msgType;
		this.protocolType = protocolType;
		this.compressType = (byte) compressType;
		return this;
	}

    public MessageHeader copyHeader(MessageHeader header){
        this.msgId = header.msgId;
        this.codecType = header.codecType;
        this.msgType = header.msgType;
        this.protocolType = header.getProtocolType();
        this.compressType = header.getCompressType();
        this.length = header.getLength();
        this.headerLength = header.getHeaderLength();
        Map<Byte,Object> tempMap = header.getAttrMap();
        for(Map.Entry<Byte,Object> entry:tempMap.entrySet()){
            this.keysMap.put(entry.getKey(),entry.getValue());
        }
        return this;
    }

    public Short getHeaderLength() {
        return headerLength;
    }

    public void setHeaderLength(Short headerLength) {
        this.headerLength = headerLength;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public int getCodecType() {
        return codecType;
    }

    public void setCodecType(int codecType) {
        this.codecType = codecType;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }
    
    /**
	 * @return the compressType
	 */
	public byte getCompressType() {
		return compressType;
	}

	/**
	 * @param compressType the compressType to set
	 */
	public void setCompressType(byte compressType) {
		this.compressType = compressType;
	}

	public int getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(int protocolType) {
        this.protocolType = protocolType;
    }

    public void addHeadKey(Constants.HeadKey key, Object value) {
        if (!key.getType().isInstance(value)) { // 检查类型
            throw new IllegalArgumentException("type mismatch of key:" + key.getNum() + ", expect:"
                    + key.getType().getName() + ", actual:" + value.getClass().getName());
        }
        keysMap.put(key.getNum(), value);
    }

    public Object removeByKey(Constants.HeadKey key){
        return keysMap.remove(key.getNum());
    }

    public Object getAttrByKey(Constants.HeadKey key){
        return keysMap.get(key.getNum());

    }

    public void setValuesInKeyMap(Map<Byte,Object> valueMap){
        this.keysMap.putAll(valueMap);

    }

    public int getAttrMapSize(){
        int mapSize = keysMap.size();
        return mapSize;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageHeader)) return false;

        MessageHeader that = (MessageHeader) o;

        if (codecType != that.codecType) return false;
        if (headerLength != null ? !headerLength.equals(that.headerLength) : that.headerLength != null) return false;
        if (msgId != that.msgId) return false;
        if (msgType != that.msgType) return false;
        if (protocolType != that.protocolType) return false;
        if (compressType != that.compressType) return false;
        if (length != null ? !length.equals(that.length) : that.length != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = msgId;
        result = 31 * result + (length != null ? length.hashCode() : 0);
        result = 31 * result + codecType;
        result = 31 * result + msgType;
        result = 31 * result + protocolType;
        result = 31 * result + compressType;
        result = 31 * result + (headerLength != null ? headerLength.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        String keymapStr = "";
        for(Map.Entry<Byte,Object> entry:keysMap.entrySet()){
            keymapStr = keymapStr+" "+ entry.getKey().toString()+" : "+entry.getValue().toString();
        }

        return "MessageHeader{" +
                "msgId=" + msgId +
                ", length=" + length +
                ", codecType=" + codecType +
                ", msgType=" + msgType +
                ", protocolType=" + protocolType +
                ", compressType=" + compressType +
                ", headerLength=" + headerLength +
                ", keysMap=" + keymapStr +
                "}";
    }

    /**
     * 克隆后和整体原来不是一个对象，
     * 属性相同，修改当前属性不会改变原来的
     * map和原来是一个对象，修改当前map也会改原来的
     *
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
	public MessageHeader clone() {
        MessageHeader header = null;
        try {
            header = (MessageHeader) super.clone();
        } catch (CloneNotSupportedException e) {
            header = new MessageHeader();
            header.copyHeader(this);
        }
        return header;
    }
}