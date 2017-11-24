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
package com.ipd.jsf.gd.codec.protobuf;

public final class RpcTemplate {
  private RpcTemplate() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface ProtobufInvocationOrBuilder extends
      // @@protoc_insertion_point(interface_extends:com.ipd.jsf.gd.codec.protobuf.ProtobufInvocation)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>required string service = 1;</code>
     */
    boolean hasService();
    /**
     * <code>required string service = 1;</code>
     */
    String getService();
    /**
     * <code>required string service = 1;</code>
     */
    com.google.protobuf.ByteString
        getServiceBytes();

    /**
     * <code>required string alias = 2;</code>
     */
    boolean hasAlias();
    /**
     * <code>required string alias = 2;</code>
     */
    String getAlias();
    /**
     * <code>required string alias = 2;</code>
     */
    com.google.protobuf.ByteString
        getAliasBytes();

    /**
     * <code>required string method = 3;</code>
     */
    boolean hasMethod();
    /**
     * <code>required string method = 3;</code>
     */
    String getMethod();
    /**
     * <code>required string method = 3;</code>
     */
    com.google.protobuf.ByteString
        getMethodBytes();

    /**
     * <code>optional string arg_type = 4;</code>
     */
    boolean hasArgType();
    /**
     * <code>optional string arg_type = 4;</code>
     */
    String getArgType();
    /**
     * <code>optional string arg_type = 4;</code>
     */
    com.google.protobuf.ByteString
        getArgTypeBytes();

    /**
     * <code>optional bytes arg_data = 5;</code>
     */
    boolean hasArgData();
    /**
     * <code>optional bytes arg_data = 5;</code>
     */
    com.google.protobuf.ByteString getArgData();

    /**
     * <code>map&lt;string, string&gt; attachments = 6;</code>
     */
    java.util.Map<String, String>
    getAttachments();
  }
  /**
   * Protobuf type {@code com.ipd.jsf.gd.codec.protobuf.ProtobufInvocation}
   */
  public  static final class ProtobufInvocation extends
      com.google.protobuf.GeneratedMessage implements
      // @@protoc_insertion_point(message_implements:com.ipd.jsf.gd.codec.protobuf.ProtobufInvocation)
      ProtobufInvocationOrBuilder {
    // Use ProtobufInvocation.newBuilder() to construct.
    private ProtobufInvocation(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
    }
    private ProtobufInvocation() {
      service_ = "";
      alias_ = "";
      method_ = "";
      argType_ = "";
      argData_ = com.google.protobuf.ByteString.EMPTY;
    }

    @Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private ProtobufInvocation(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry) {
      this();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 10: {
              com.google.protobuf.ByteString bs = input.readBytes();
              bitField0_ |= 0x00000001;
              service_ = bs;
              break;
            }
            case 18: {
              com.google.protobuf.ByteString bs = input.readBytes();
              bitField0_ |= 0x00000002;
              alias_ = bs;
              break;
            }
            case 26: {
              com.google.protobuf.ByteString bs = input.readBytes();
              bitField0_ |= 0x00000004;
              method_ = bs;
              break;
            }
            case 34: {
              com.google.protobuf.ByteString bs = input.readBytes();
              bitField0_ |= 0x00000008;
              argType_ = bs;
              break;
            }
            case 42: {
              bitField0_ |= 0x00000010;
              argData_ = input.readBytes();
              break;
            }
            case 50: {
              if (!((mutable_bitField0_ & 0x00000020) == 0x00000020)) {
                attachments_ = com.google.protobuf.MapField.newMapField(
                    AttachmentsDefaultEntryHolder.defaultEntry);
                mutable_bitField0_ |= 0x00000020;
              }
              com.google.protobuf.MapEntry<String, String>
              attachments = input.readMessage(
                  AttachmentsDefaultEntryHolder.defaultEntry.getParserForType(), extensionRegistry);
              attachments_.getMutableMap().put(attachments.getKey(), attachments.getValue());
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw new RuntimeException(e.setUnfinishedMessage(this));
      } catch (java.io.IOException e) {
        throw new RuntimeException(
            new com.google.protobuf.InvalidProtocolBufferException(
                e.getMessage()).setUnfinishedMessage(this));
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return RpcTemplate.internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufInvocation_descriptor;
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMapField(
        int number) {
      switch (number) {
        case 6:
          return internalGetAttachments();
        default:
          throw new RuntimeException(
              "Invalid map field number: " + number);
      }
    }
    protected FieldAccessorTable
        internalGetFieldAccessorTable() {
      return RpcTemplate.internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufInvocation_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              ProtobufInvocation.class, Builder.class);
    }

    private int bitField0_;
    public static final int SERVICE_FIELD_NUMBER = 1;
    private volatile Object service_;
    /**
     * <code>required string service = 1;</code>
     */
    public boolean hasService() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>required string service = 1;</code>
     */
    public String getService() {
      Object ref = service_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          service_ = s;
        }
        return s;
      }
    }
    /**
     * <code>required string service = 1;</code>
     */
    public com.google.protobuf.ByteString
        getServiceBytes() {
      Object ref = service_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (String) ref);
        service_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int ALIAS_FIELD_NUMBER = 2;
    private volatile Object alias_;
    /**
     * <code>required string alias = 2;</code>
     */
    public boolean hasAlias() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>required string alias = 2;</code>
     */
    public String getAlias() {
      Object ref = alias_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          alias_ = s;
        }
        return s;
      }
    }
    /**
     * <code>required string alias = 2;</code>
     */
    public com.google.protobuf.ByteString
        getAliasBytes() {
      Object ref = alias_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (String) ref);
        alias_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int METHOD_FIELD_NUMBER = 3;
    private volatile Object method_;
    /**
     * <code>required string method = 3;</code>
     */
    public boolean hasMethod() {
      return ((bitField0_ & 0x00000004) == 0x00000004);
    }
    /**
     * <code>required string method = 3;</code>
     */
    public String getMethod() {
      Object ref = method_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          method_ = s;
        }
        return s;
      }
    }
    /**
     * <code>required string method = 3;</code>
     */
    public com.google.protobuf.ByteString
        getMethodBytes() {
      Object ref = method_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (String) ref);
        method_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int ARG_TYPE_FIELD_NUMBER = 4;
    private volatile Object argType_;
    /**
     * <code>optional string arg_type = 4;</code>
     */
    public boolean hasArgType() {
      return ((bitField0_ & 0x00000008) == 0x00000008);
    }
    /**
     * <code>optional string arg_type = 4;</code>
     */
    public String getArgType() {
      Object ref = argType_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        if (bs.isValidUtf8()) {
          argType_ = s;
        }
        return s;
      }
    }
    /**
     * <code>optional string arg_type = 4;</code>
     */
    public com.google.protobuf.ByteString
        getArgTypeBytes() {
      Object ref = argType_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (String) ref);
        argType_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    public static final int ARG_DATA_FIELD_NUMBER = 5;
    private com.google.protobuf.ByteString argData_;
    /**
     * <code>optional bytes arg_data = 5;</code>
     */
    public boolean hasArgData() {
      return ((bitField0_ & 0x00000010) == 0x00000010);
    }
    /**
     * <code>optional bytes arg_data = 5;</code>
     */
    public com.google.protobuf.ByteString getArgData() {
      return argData_;
    }

    public static final int ATTACHMENTS_FIELD_NUMBER = 6;
    private static final class AttachmentsDefaultEntryHolder {
      static final com.google.protobuf.MapEntry<
          String, String> defaultEntry =
              com.google.protobuf.MapEntry
              .<String, String>newDefaultInstance(
                  RpcTemplate.internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufInvocation_AttachmentsEntry_descriptor,
                  com.google.protobuf.WireFormat.FieldType.STRING,
                  "",
                  com.google.protobuf.WireFormat.FieldType.STRING,
                  "");
    }
    private com.google.protobuf.MapField<
        String, String> attachments_;
    private com.google.protobuf.MapField<String, String>
    internalGetAttachments() {
      if (attachments_ == null) {
        return com.google.protobuf.MapField.emptyMapField(
            AttachmentsDefaultEntryHolder.defaultEntry);
     }
      return attachments_;
    }
    /**
     * <code>map&lt;string, string&gt; attachments = 6;</code>
     */

    public java.util.Map<String, String> getAttachments() {
      return internalGetAttachments().getMap();
    }

    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      if (!hasService()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasAlias()) {
        memoizedIsInitialized = 0;
        return false;
      }
      if (!hasMethod()) {
        memoizedIsInitialized = 0;
        return false;
      }
      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        com.google.protobuf.GeneratedMessage.writeString(output, 1, service_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        com.google.protobuf.GeneratedMessage.writeString(output, 2, alias_);
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        com.google.protobuf.GeneratedMessage.writeString(output, 3, method_);
      }
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        com.google.protobuf.GeneratedMessage.writeString(output, 4, argType_);
      }
      if (((bitField0_ & 0x00000010) == 0x00000010)) {
        output.writeBytes(5, argData_);
      }
      for (java.util.Map.Entry<String, String> entry
           : internalGetAttachments().getMap().entrySet()) {
        com.google.protobuf.MapEntry<String, String>
        attachments = AttachmentsDefaultEntryHolder.defaultEntry.newBuilderForType()
            .setKey(entry.getKey())
            .setValue(entry.getValue())
            .build();
        output.writeMessage(6, attachments);
      }
      unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.GeneratedMessage.computeStringSize(1, service_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.GeneratedMessage.computeStringSize(2, alias_);
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        size += com.google.protobuf.GeneratedMessage.computeStringSize(3, method_);
      }
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        size += com.google.protobuf.GeneratedMessage.computeStringSize(4, argType_);
      }
      if (((bitField0_ & 0x00000010) == 0x00000010)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(5, argData_);
      }
      for (java.util.Map.Entry<String, String> entry
           : internalGetAttachments().getMap().entrySet()) {
        com.google.protobuf.MapEntry<String, String>
        attachments = AttachmentsDefaultEntryHolder.defaultEntry.newBuilderForType()
            .setKey(entry.getKey())
            .setValue(entry.getValue())
            .build();
        size += com.google.protobuf.CodedOutputStream
            .computeMessageSize(6, attachments);
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    public static ProtobufInvocation parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static ProtobufInvocation parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static ProtobufInvocation parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static ProtobufInvocation parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static ProtobufInvocation parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static ProtobufInvocation parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static ProtobufInvocation parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static ProtobufInvocation parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static ProtobufInvocation parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static ProtobufInvocation parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(ProtobufInvocation prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @Override
    protected Builder newBuilderForType(
        BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code com.ipd.jsf.gd.codec.protobuf.ProtobufInvocation}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:com.ipd.jsf.gd.codec.protobuf.ProtobufInvocation)
        ProtobufInvocationOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return RpcTemplate.internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufInvocation_descriptor;
      }

      @SuppressWarnings({"rawtypes"})
      protected com.google.protobuf.MapField internalGetMapField(
          int number) {
        switch (number) {
          case 6:
            return internalGetAttachments();
          default:
            throw new RuntimeException(
                "Invalid map field number: " + number);
        }
      }
      @SuppressWarnings({"rawtypes"})
      protected com.google.protobuf.MapField internalGetMutableMapField(
          int number) {
        switch (number) {
          case 6:
            return internalGetMutableAttachments();
          default:
            throw new RuntimeException(
                "Invalid map field number: " + number);
        }
      }
      protected FieldAccessorTable
          internalGetFieldAccessorTable() {
        return RpcTemplate.internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufInvocation_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                ProtobufInvocation.class, Builder.class);
      }

      // Construct using RpcTemplate.ProtobufInvocation.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      public Builder clear() {
        super.clear();
        service_ = "";
        bitField0_ = (bitField0_ & ~0x00000001);
        alias_ = "";
        bitField0_ = (bitField0_ & ~0x00000002);
        method_ = "";
        bitField0_ = (bitField0_ & ~0x00000004);
        argType_ = "";
        bitField0_ = (bitField0_ & ~0x00000008);
        argData_ = com.google.protobuf.ByteString.EMPTY;
        bitField0_ = (bitField0_ & ~0x00000010);
        internalGetMutableAttachments().clear();
        return this;
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return RpcTemplate.internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufInvocation_descriptor;
      }

      public ProtobufInvocation getDefaultInstanceForType() {
        return ProtobufInvocation.getDefaultInstance();
      }

      public ProtobufInvocation build() {
        ProtobufInvocation result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public ProtobufInvocation buildPartial() {
        ProtobufInvocation result = new ProtobufInvocation(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.service_ = service_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.alias_ = alias_;
        if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
          to_bitField0_ |= 0x00000004;
        }
        result.method_ = method_;
        if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
          to_bitField0_ |= 0x00000008;
        }
        result.argType_ = argType_;
        if (((from_bitField0_ & 0x00000010) == 0x00000010)) {
          to_bitField0_ |= 0x00000010;
        }
        result.argData_ = argData_;
        result.attachments_ = internalGetAttachments();
        result.attachments_.makeImmutable();
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof ProtobufInvocation) {
          return mergeFrom((ProtobufInvocation)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(ProtobufInvocation other) {
        if (other == ProtobufInvocation.getDefaultInstance()) return this;
        if (other.hasService()) {
          bitField0_ |= 0x00000001;
          service_ = other.service_;
          onChanged();
        }
        if (other.hasAlias()) {
          bitField0_ |= 0x00000002;
          alias_ = other.alias_;
          onChanged();
        }
        if (other.hasMethod()) {
          bitField0_ |= 0x00000004;
          method_ = other.method_;
          onChanged();
        }
        if (other.hasArgType()) {
          bitField0_ |= 0x00000008;
          argType_ = other.argType_;
          onChanged();
        }
        if (other.hasArgData()) {
          setArgData(other.getArgData());
        }
        internalGetMutableAttachments().mergeFrom(
            other.internalGetAttachments());
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      public final boolean isInitialized() {
        if (!hasService()) {
          return false;
        }
        if (!hasAlias()) {
          return false;
        }
        if (!hasMethod()) {
          return false;
        }
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        ProtobufInvocation parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (ProtobufInvocation) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private Object service_ = "";
      /**
       * <code>required string service = 1;</code>
       */
      public boolean hasService() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>required string service = 1;</code>
       */
      public String getService() {
        Object ref = service_;
        if (!(ref instanceof String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          String s = bs.toStringUtf8();
          if (bs.isValidUtf8()) {
            service_ = s;
          }
          return s;
        } else {
          return (String) ref;
        }
      }
      /**
       * <code>required string service = 1;</code>
       */
      public com.google.protobuf.ByteString
          getServiceBytes() {
        Object ref = service_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (String) ref);
          service_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>required string service = 1;</code>
       */
      public Builder setService(
          String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
        service_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required string service = 1;</code>
       */
      public Builder clearService() {
        bitField0_ = (bitField0_ & ~0x00000001);
        service_ = getDefaultInstance().getService();
        onChanged();
        return this;
      }
      /**
       * <code>required string service = 1;</code>
       */
      public Builder setServiceBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
        service_ = value;
        onChanged();
        return this;
      }

      private Object alias_ = "";
      /**
       * <code>required string alias = 2;</code>
       */
      public boolean hasAlias() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      /**
       * <code>required string alias = 2;</code>
       */
      public String getAlias() {
        Object ref = alias_;
        if (!(ref instanceof String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          String s = bs.toStringUtf8();
          if (bs.isValidUtf8()) {
            alias_ = s;
          }
          return s;
        } else {
          return (String) ref;
        }
      }
      /**
       * <code>required string alias = 2;</code>
       */
      public com.google.protobuf.ByteString
          getAliasBytes() {
        Object ref = alias_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (String) ref);
          alias_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>required string alias = 2;</code>
       */
      public Builder setAlias(
          String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
        alias_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required string alias = 2;</code>
       */
      public Builder clearAlias() {
        bitField0_ = (bitField0_ & ~0x00000002);
        alias_ = getDefaultInstance().getAlias();
        onChanged();
        return this;
      }
      /**
       * <code>required string alias = 2;</code>
       */
      public Builder setAliasBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
        alias_ = value;
        onChanged();
        return this;
      }

      private Object method_ = "";
      /**
       * <code>required string method = 3;</code>
       */
      public boolean hasMethod() {
        return ((bitField0_ & 0x00000004) == 0x00000004);
      }
      /**
       * <code>required string method = 3;</code>
       */
      public String getMethod() {
        Object ref = method_;
        if (!(ref instanceof String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          String s = bs.toStringUtf8();
          if (bs.isValidUtf8()) {
            method_ = s;
          }
          return s;
        } else {
          return (String) ref;
        }
      }
      /**
       * <code>required string method = 3;</code>
       */
      public com.google.protobuf.ByteString
          getMethodBytes() {
        Object ref = method_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (String) ref);
          method_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>required string method = 3;</code>
       */
      public Builder setMethod(
          String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000004;
        method_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>required string method = 3;</code>
       */
      public Builder clearMethod() {
        bitField0_ = (bitField0_ & ~0x00000004);
        method_ = getDefaultInstance().getMethod();
        onChanged();
        return this;
      }
      /**
       * <code>required string method = 3;</code>
       */
      public Builder setMethodBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000004;
        method_ = value;
        onChanged();
        return this;
      }

      private Object argType_ = "";
      /**
       * <code>optional string arg_type = 4;</code>
       */
      public boolean hasArgType() {
        return ((bitField0_ & 0x00000008) == 0x00000008);
      }
      /**
       * <code>optional string arg_type = 4;</code>
       */
      public String getArgType() {
        Object ref = argType_;
        if (!(ref instanceof String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          String s = bs.toStringUtf8();
          if (bs.isValidUtf8()) {
            argType_ = s;
          }
          return s;
        } else {
          return (String) ref;
        }
      }
      /**
       * <code>optional string arg_type = 4;</code>
       */
      public com.google.protobuf.ByteString
          getArgTypeBytes() {
        Object ref = argType_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (String) ref);
          argType_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <code>optional string arg_type = 4;</code>
       */
      public Builder setArgType(
          String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000008;
        argType_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional string arg_type = 4;</code>
       */
      public Builder clearArgType() {
        bitField0_ = (bitField0_ & ~0x00000008);
        argType_ = getDefaultInstance().getArgType();
        onChanged();
        return this;
      }
      /**
       * <code>optional string arg_type = 4;</code>
       */
      public Builder setArgTypeBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000008;
        argType_ = value;
        onChanged();
        return this;
      }

      private com.google.protobuf.ByteString argData_ = com.google.protobuf.ByteString.EMPTY;
      /**
       * <code>optional bytes arg_data = 5;</code>
       */
      public boolean hasArgData() {
        return ((bitField0_ & 0x00000010) == 0x00000010);
      }
      /**
       * <code>optional bytes arg_data = 5;</code>
       */
      public com.google.protobuf.ByteString getArgData() {
        return argData_;
      }
      /**
       * <code>optional bytes arg_data = 5;</code>
       */
      public Builder setArgData(com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000010;
        argData_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional bytes arg_data = 5;</code>
       */
      public Builder clearArgData() {
        bitField0_ = (bitField0_ & ~0x00000010);
        argData_ = getDefaultInstance().getArgData();
        onChanged();
        return this;
      }

      private com.google.protobuf.MapField<
          String, String> attachments_;
      private com.google.protobuf.MapField<String, String>
      internalGetAttachments() {
        if (attachments_ == null) {
          return com.google.protobuf.MapField.emptyMapField(
              AttachmentsDefaultEntryHolder.defaultEntry);
       }
        return attachments_;
      }
      private com.google.protobuf.MapField<String, String>
      internalGetMutableAttachments() {
        onChanged();;
        if (attachments_ == null) {
          attachments_ = com.google.protobuf.MapField.newMapField(
              AttachmentsDefaultEntryHolder.defaultEntry);
        }
        if (!attachments_.isMutable()) {
          attachments_ = attachments_.copy();
        }
        return attachments_;
      }
      /**
       * <code>map&lt;string, string&gt; attachments = 6;</code>
       */
      public java.util.Map<String, String> getAttachments() {
        return internalGetAttachments().getMap();
      }
      /**
       * <code>map&lt;string, string&gt; attachments = 6;</code>
       */
      public java.util.Map<String, String>
      getMutableAttachments() {
        return internalGetMutableAttachments().getMutableMap();
      }
      /**
       * <code>map&lt;string, string&gt; attachments = 6;</code>
       */
      public Builder putAllAttachments(
          java.util.Map<String, String> values) {
        getMutableAttachments().putAll(values);
        return this;
      }

      // @@protoc_insertion_point(builder_scope:com.ipd.jsf.gd.codec.protobuf.ProtobufInvocation)
    }

    // @@protoc_insertion_point(class_scope:com.ipd.jsf.gd.codec.protobuf.ProtobufInvocation)
    private static final ProtobufInvocation DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new ProtobufInvocation();
    }

    public static ProtobufInvocation getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    @Deprecated public static final com.google.protobuf.Parser<ProtobufInvocation>
        PARSER = new com.google.protobuf.AbstractParser<ProtobufInvocation>() {
      public ProtobufInvocation parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        try {
          return new ProtobufInvocation(input, extensionRegistry);
        } catch (RuntimeException e) {
          if (e.getCause() instanceof
              com.google.protobuf.InvalidProtocolBufferException) {
            throw (com.google.protobuf.InvalidProtocolBufferException)
                e.getCause();
          }
          throw e;
        }
      }
    };

    public static com.google.protobuf.Parser<ProtobufInvocation> parser() {
      return PARSER;
    }

    @Override
    public com.google.protobuf.Parser<ProtobufInvocation> getParserForType() {
      return PARSER;
    }

    public ProtobufInvocation getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  public interface ProtobufResultOrBuilder extends
      // @@protoc_insertion_point(interface_extends:com.ipd.jsf.gd.codec.protobuf.ProtobufResult)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Value value = 1;</code>
     */
    boolean hasValue();
    /**
     * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Value value = 1;</code>
     */
    ProtobufResult.Value getValue();
    /**
     * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Value value = 1;</code>
     */
    ProtobufResult.ValueOrBuilder getValueOrBuilder();

    /**
     * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Exception exception = 2;</code>
     */
    boolean hasException();
    /**
     * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Exception exception = 2;</code>
     */
    ProtobufResult.Exception getException();
    /**
     * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Exception exception = 2;</code>
     */
    ProtobufResult.ExceptionOrBuilder getExceptionOrBuilder();

    public ProtobufResult.ResultOneofCase getResultOneofCase();
  }
  /**
   * Protobuf type {@code com.ipd.jsf.gd.codec.protobuf.ProtobufResult}
   */
  public  static final class ProtobufResult extends
      com.google.protobuf.GeneratedMessage implements
      // @@protoc_insertion_point(message_implements:com.ipd.jsf.gd.codec.protobuf.ProtobufResult)
      ProtobufResultOrBuilder {
    // Use ProtobufResult.newBuilder() to construct.
    private ProtobufResult(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
    }
    private ProtobufResult() {
    }

    @Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private ProtobufResult(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry) {
      this();
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
            case 10: {
              Value.Builder subBuilder = null;
              if (resultOneofCase_ == 1) {
                subBuilder = ((Value) resultOneof_).toBuilder();
              }
              resultOneof_ =
                  input.readMessage(Value.parser(), extensionRegistry);
              if (subBuilder != null) {
                subBuilder.mergeFrom((Value) resultOneof_);
                resultOneof_ = subBuilder.buildPartial();
              }
              resultOneofCase_ = 1;
              break;
            }
            case 18: {
              Exception.Builder subBuilder = null;
              if (resultOneofCase_ == 2) {
                subBuilder = ((Exception) resultOneof_).toBuilder();
              }
              resultOneof_ =
                  input.readMessage(Exception.parser(), extensionRegistry);
              if (subBuilder != null) {
                subBuilder.mergeFrom((Exception) resultOneof_);
                resultOneof_ = subBuilder.buildPartial();
              }
              resultOneofCase_ = 2;
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw new RuntimeException(e.setUnfinishedMessage(this));
      } catch (java.io.IOException e) {
        throw new RuntimeException(
            new com.google.protobuf.InvalidProtocolBufferException(
                e.getMessage()).setUnfinishedMessage(this));
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return RpcTemplate.internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_descriptor;
    }

    protected FieldAccessorTable
        internalGetFieldAccessorTable() {
      return RpcTemplate.internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              ProtobufResult.class, Builder.class);
    }

    public interface ValueOrBuilder extends
        // @@protoc_insertion_point(interface_extends:com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Value)
        com.google.protobuf.MessageOrBuilder {

      /**
       * <code>required string type = 1;</code>
       */
      boolean hasType();
      /**
       * <code>required string type = 1;</code>
       */
      String getType();
      /**
       * <code>required string type = 1;</code>
       */
      com.google.protobuf.ByteString
          getTypeBytes();

      /**
       * <code>optional bytes data = 2;</code>
       */
      boolean hasData();
      /**
       * <code>optional bytes data = 2;</code>
       */
      com.google.protobuf.ByteString getData();
    }
    /**
     * Protobuf type {@code com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Value}
     */
    public  static final class Value extends
        com.google.protobuf.GeneratedMessage implements
        // @@protoc_insertion_point(message_implements:com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Value)
        ValueOrBuilder {
      // Use Value.newBuilder() to construct.
      private Value(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
        super(builder);
      }
      private Value() {
        type_ = "";
        data_ = com.google.protobuf.ByteString.EMPTY;
      }

      @Override
      public final com.google.protobuf.UnknownFieldSet
      getUnknownFields() {
        return this.unknownFields;
      }
      private Value(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry) {
        this();
        int mutable_bitField0_ = 0;
        com.google.protobuf.UnknownFieldSet.Builder unknownFields =
            com.google.protobuf.UnknownFieldSet.newBuilder();
        try {
          boolean done = false;
          while (!done) {
            int tag = input.readTag();
            switch (tag) {
              case 0:
                done = true;
                break;
              default: {
                if (!parseUnknownField(input, unknownFields,
                                       extensionRegistry, tag)) {
                  done = true;
                }
                break;
              }
              case 10: {
                com.google.protobuf.ByteString bs = input.readBytes();
                bitField0_ |= 0x00000001;
                type_ = bs;
                break;
              }
              case 18: {
                bitField0_ |= 0x00000002;
                data_ = input.readBytes();
                break;
              }
            }
          }
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          throw new RuntimeException(e.setUnfinishedMessage(this));
        } catch (java.io.IOException e) {
          throw new RuntimeException(
              new com.google.protobuf.InvalidProtocolBufferException(
                  e.getMessage()).setUnfinishedMessage(this));
        } finally {
          this.unknownFields = unknownFields.build();
          makeExtensionsImmutable();
        }
      }
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return RpcTemplate.internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_Value_descriptor;
      }

      protected FieldAccessorTable
          internalGetFieldAccessorTable() {
        return RpcTemplate.internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_Value_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                Value.class, Builder.class);
      }

      private int bitField0_;
      public static final int TYPE_FIELD_NUMBER = 1;
      private volatile Object type_;
      /**
       * <code>required string type = 1;</code>
       */
      public boolean hasType() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>required string type = 1;</code>
       */
      public String getType() {
        Object ref = type_;
        if (ref instanceof String) {
          return (String) ref;
        } else {
          com.google.protobuf.ByteString bs = 
              (com.google.protobuf.ByteString) ref;
          String s = bs.toStringUtf8();
          if (bs.isValidUtf8()) {
            type_ = s;
          }
          return s;
        }
      }
      /**
       * <code>required string type = 1;</code>
       */
      public com.google.protobuf.ByteString
          getTypeBytes() {
        Object ref = type_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (String) ref);
          type_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }

      public static final int DATA_FIELD_NUMBER = 2;
      private com.google.protobuf.ByteString data_;
      /**
       * <code>optional bytes data = 2;</code>
       */
      public boolean hasData() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      /**
       * <code>optional bytes data = 2;</code>
       */
      public com.google.protobuf.ByteString getData() {
        return data_;
      }

      private byte memoizedIsInitialized = -1;
      public final boolean isInitialized() {
        byte isInitialized = memoizedIsInitialized;
        if (isInitialized == 1) return true;
        if (isInitialized == 0) return false;

        if (!hasType()) {
          memoizedIsInitialized = 0;
          return false;
        }
        memoizedIsInitialized = 1;
        return true;
      }

      public void writeTo(com.google.protobuf.CodedOutputStream output)
                          throws java.io.IOException {
        if (((bitField0_ & 0x00000001) == 0x00000001)) {
          com.google.protobuf.GeneratedMessage.writeString(output, 1, type_);
        }
        if (((bitField0_ & 0x00000002) == 0x00000002)) {
          output.writeBytes(2, data_);
        }
        unknownFields.writeTo(output);
      }

      public int getSerializedSize() {
        int size = memoizedSize;
        if (size != -1) return size;

        size = 0;
        if (((bitField0_ & 0x00000001) == 0x00000001)) {
          size += com.google.protobuf.GeneratedMessage.computeStringSize(1, type_);
        }
        if (((bitField0_ & 0x00000002) == 0x00000002)) {
          size += com.google.protobuf.CodedOutputStream
            .computeBytesSize(2, data_);
        }
        size += unknownFields.getSerializedSize();
        memoizedSize = size;
        return size;
      }

      private static final long serialVersionUID = 0L;
      public static Value parseFrom(
          com.google.protobuf.ByteString data)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
      }
      public static Value parseFrom(
          com.google.protobuf.ByteString data,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
      }
      public static Value parseFrom(byte[] data)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
      }
      public static Value parseFrom(
          byte[] data,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
      }
      public static Value parseFrom(java.io.InputStream input)
          throws java.io.IOException {
        return PARSER.parseFrom(input);
      }
      public static Value parseFrom(
          java.io.InputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        return PARSER.parseFrom(input, extensionRegistry);
      }
      public static Value parseDelimitedFrom(java.io.InputStream input)
          throws java.io.IOException {
        return PARSER.parseDelimitedFrom(input);
      }
      public static Value parseDelimitedFrom(
          java.io.InputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        return PARSER.parseDelimitedFrom(input, extensionRegistry);
      }
      public static Value parseFrom(
          com.google.protobuf.CodedInputStream input)
          throws java.io.IOException {
        return PARSER.parseFrom(input);
      }
      public static Value parseFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        return PARSER.parseFrom(input, extensionRegistry);
      }

      public Builder newBuilderForType() { return newBuilder(); }
      public static Builder newBuilder() {
        return DEFAULT_INSTANCE.toBuilder();
      }
      public static Builder newBuilder(Value prototype) {
        return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
      }
      public Builder toBuilder() {
        return this == DEFAULT_INSTANCE
            ? new Builder() : new Builder().mergeFrom(this);
      }

      @Override
      protected Builder newBuilderForType(
          BuilderParent parent) {
        Builder builder = new Builder(parent);
        return builder;
      }
      /**
       * Protobuf type {@code com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Value}
       */
      public static final class Builder extends
          com.google.protobuf.GeneratedMessage.Builder<Builder> implements
          // @@protoc_insertion_point(builder_implements:com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Value)
          ValueOrBuilder {
        public static final com.google.protobuf.Descriptors.Descriptor
            getDescriptor() {
          return RpcTemplate.internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_Value_descriptor;
        }

        protected FieldAccessorTable
            internalGetFieldAccessorTable() {
          return RpcTemplate.internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_Value_fieldAccessorTable
              .ensureFieldAccessorsInitialized(
                  Value.class, Builder.class);
        }

        // Construct using RpcTemplate.ProtobufResult.Value.newBuilder()
        private Builder() {
          maybeForceBuilderInitialization();
        }

        private Builder(
            BuilderParent parent) {
          super(parent);
          maybeForceBuilderInitialization();
        }
        private void maybeForceBuilderInitialization() {
          if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
          }
        }
        public Builder clear() {
          super.clear();
          type_ = "";
          bitField0_ = (bitField0_ & ~0x00000001);
          data_ = com.google.protobuf.ByteString.EMPTY;
          bitField0_ = (bitField0_ & ~0x00000002);
          return this;
        }

        public com.google.protobuf.Descriptors.Descriptor
            getDescriptorForType() {
          return RpcTemplate.internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_Value_descriptor;
        }

        public Value getDefaultInstanceForType() {
          return Value.getDefaultInstance();
        }

        public Value build() {
          Value result = buildPartial();
          if (!result.isInitialized()) {
            throw newUninitializedMessageException(result);
          }
          return result;
        }

        public Value buildPartial() {
          Value result = new Value(this);
          int from_bitField0_ = bitField0_;
          int to_bitField0_ = 0;
          if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
            to_bitField0_ |= 0x00000001;
          }
          result.type_ = type_;
          if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
            to_bitField0_ |= 0x00000002;
          }
          result.data_ = data_;
          result.bitField0_ = to_bitField0_;
          onBuilt();
          return result;
        }

        public Builder mergeFrom(com.google.protobuf.Message other) {
          if (other instanceof Value) {
            return mergeFrom((Value)other);
          } else {
            super.mergeFrom(other);
            return this;
          }
        }

        public Builder mergeFrom(Value other) {
          if (other == Value.getDefaultInstance()) return this;
          if (other.hasType()) {
            bitField0_ |= 0x00000001;
            type_ = other.type_;
            onChanged();
          }
          if (other.hasData()) {
            setData(other.getData());
          }
          this.mergeUnknownFields(other.unknownFields);
          onChanged();
          return this;
        }

        public final boolean isInitialized() {
          if (!hasType()) {
            return false;
          }
          return true;
        }

        public Builder mergeFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
          Value parsedMessage = null;
          try {
            parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
          } catch (com.google.protobuf.InvalidProtocolBufferException e) {
            parsedMessage = (Value) e.getUnfinishedMessage();
            throw e;
          } finally {
            if (parsedMessage != null) {
              mergeFrom(parsedMessage);
            }
          }
          return this;
        }
        private int bitField0_;

        private Object type_ = "";
        /**
         * <code>required string type = 1;</code>
         */
        public boolean hasType() {
          return ((bitField0_ & 0x00000001) == 0x00000001);
        }
        /**
         * <code>required string type = 1;</code>
         */
        public String getType() {
          Object ref = type_;
          if (!(ref instanceof String)) {
            com.google.protobuf.ByteString bs =
                (com.google.protobuf.ByteString) ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
              type_ = s;
            }
            return s;
          } else {
            return (String) ref;
          }
        }
        /**
         * <code>required string type = 1;</code>
         */
        public com.google.protobuf.ByteString
            getTypeBytes() {
          Object ref = type_;
          if (ref instanceof String) {
            com.google.protobuf.ByteString b = 
                com.google.protobuf.ByteString.copyFromUtf8(
                    (String) ref);
            type_ = b;
            return b;
          } else {
            return (com.google.protobuf.ByteString) ref;
          }
        }
        /**
         * <code>required string type = 1;</code>
         */
        public Builder setType(
            String value) {
          if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
          type_ = value;
          onChanged();
          return this;
        }
        /**
         * <code>required string type = 1;</code>
         */
        public Builder clearType() {
          bitField0_ = (bitField0_ & ~0x00000001);
          type_ = getDefaultInstance().getType();
          onChanged();
          return this;
        }
        /**
         * <code>required string type = 1;</code>
         */
        public Builder setTypeBytes(
            com.google.protobuf.ByteString value) {
          if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
          type_ = value;
          onChanged();
          return this;
        }

        private com.google.protobuf.ByteString data_ = com.google.protobuf.ByteString.EMPTY;
        /**
         * <code>optional bytes data = 2;</code>
         */
        public boolean hasData() {
          return ((bitField0_ & 0x00000002) == 0x00000002);
        }
        /**
         * <code>optional bytes data = 2;</code>
         */
        public com.google.protobuf.ByteString getData() {
          return data_;
        }
        /**
         * <code>optional bytes data = 2;</code>
         */
        public Builder setData(com.google.protobuf.ByteString value) {
          if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
          data_ = value;
          onChanged();
          return this;
        }
        /**
         * <code>optional bytes data = 2;</code>
         */
        public Builder clearData() {
          bitField0_ = (bitField0_ & ~0x00000002);
          data_ = getDefaultInstance().getData();
          onChanged();
          return this;
        }

        // @@protoc_insertion_point(builder_scope:com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Value)
      }

      // @@protoc_insertion_point(class_scope:com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Value)
      private static final Value DEFAULT_INSTANCE;
      static {
        DEFAULT_INSTANCE = new Value();
      }

      public static Value getDefaultInstance() {
        return DEFAULT_INSTANCE;
      }

      @Deprecated public static final com.google.protobuf.Parser<Value>
          PARSER = new com.google.protobuf.AbstractParser<Value>() {
        public Value parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          try {
            return new Value(input, extensionRegistry);
          } catch (RuntimeException e) {
            if (e.getCause() instanceof
                com.google.protobuf.InvalidProtocolBufferException) {
              throw (com.google.protobuf.InvalidProtocolBufferException)
                  e.getCause();
            }
            throw e;
          }
        }
      };

      public static com.google.protobuf.Parser<Value> parser() {
        return PARSER;
      }

      @Override
      public com.google.protobuf.Parser<Value> getParserForType() {
        return PARSER;
      }

      public Value getDefaultInstanceForType() {
        return DEFAULT_INSTANCE;
      }

    }

    public interface ExceptionOrBuilder extends
        // @@protoc_insertion_point(interface_extends:com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Exception)
        com.google.protobuf.MessageOrBuilder {

      /**
       * <code>required string type = 1;</code>
       */
      boolean hasType();
      /**
       * <code>required string type = 1;</code>
       */
      String getType();
      /**
       * <code>required string type = 1;</code>
       */
      com.google.protobuf.ByteString
          getTypeBytes();

      /**
       * <code>optional string msg = 2;</code>
       */
      boolean hasMsg();
      /**
       * <code>optional string msg = 2;</code>
       */
      String getMsg();
      /**
       * <code>optional string msg = 2;</code>
       */
      com.google.protobuf.ByteString
          getMsgBytes();
    }
    /**
     * Protobuf type {@code com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Exception}
     */
    public  static final class Exception extends
        com.google.protobuf.GeneratedMessage implements
        // @@protoc_insertion_point(message_implements:com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Exception)
        ExceptionOrBuilder {
      // Use Exception.newBuilder() to construct.
      private Exception(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
        super(builder);
      }
      private Exception() {
        type_ = "";
        msg_ = "";
      }

      @Override
      public final com.google.protobuf.UnknownFieldSet
      getUnknownFields() {
        return this.unknownFields;
      }
      private Exception(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry) {
        this();
        int mutable_bitField0_ = 0;
        com.google.protobuf.UnknownFieldSet.Builder unknownFields =
            com.google.protobuf.UnknownFieldSet.newBuilder();
        try {
          boolean done = false;
          while (!done) {
            int tag = input.readTag();
            switch (tag) {
              case 0:
                done = true;
                break;
              default: {
                if (!parseUnknownField(input, unknownFields,
                                       extensionRegistry, tag)) {
                  done = true;
                }
                break;
              }
              case 10: {
                com.google.protobuf.ByteString bs = input.readBytes();
                bitField0_ |= 0x00000001;
                type_ = bs;
                break;
              }
              case 18: {
                com.google.protobuf.ByteString bs = input.readBytes();
                bitField0_ |= 0x00000002;
                msg_ = bs;
                break;
              }
            }
          }
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          throw new RuntimeException(e.setUnfinishedMessage(this));
        } catch (java.io.IOException e) {
          throw new RuntimeException(
              new com.google.protobuf.InvalidProtocolBufferException(
                  e.getMessage()).setUnfinishedMessage(this));
        } finally {
          this.unknownFields = unknownFields.build();
          makeExtensionsImmutable();
        }
      }
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return RpcTemplate.internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_Exception_descriptor;
      }

      protected FieldAccessorTable
          internalGetFieldAccessorTable() {
        return RpcTemplate.internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_Exception_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                Exception.class, Builder.class);
      }

      private int bitField0_;
      public static final int TYPE_FIELD_NUMBER = 1;
      private volatile Object type_;
      /**
       * <code>required string type = 1;</code>
       */
      public boolean hasType() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      /**
       * <code>required string type = 1;</code>
       */
      public String getType() {
        Object ref = type_;
        if (ref instanceof String) {
          return (String) ref;
        } else {
          com.google.protobuf.ByteString bs = 
              (com.google.protobuf.ByteString) ref;
          String s = bs.toStringUtf8();
          if (bs.isValidUtf8()) {
            type_ = s;
          }
          return s;
        }
      }
      /**
       * <code>required string type = 1;</code>
       */
      public com.google.protobuf.ByteString
          getTypeBytes() {
        Object ref = type_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (String) ref);
          type_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }

      public static final int MSG_FIELD_NUMBER = 2;
      private volatile Object msg_;
      /**
       * <code>optional string msg = 2;</code>
       */
      public boolean hasMsg() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      /**
       * <code>optional string msg = 2;</code>
       */
      public String getMsg() {
        Object ref = msg_;
        if (ref instanceof String) {
          return (String) ref;
        } else {
          com.google.protobuf.ByteString bs = 
              (com.google.protobuf.ByteString) ref;
          String s = bs.toStringUtf8();
          if (bs.isValidUtf8()) {
            msg_ = s;
          }
          return s;
        }
      }
      /**
       * <code>optional string msg = 2;</code>
       */
      public com.google.protobuf.ByteString
          getMsgBytes() {
        Object ref = msg_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (String) ref);
          msg_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }

      private byte memoizedIsInitialized = -1;
      public final boolean isInitialized() {
        byte isInitialized = memoizedIsInitialized;
        if (isInitialized == 1) return true;
        if (isInitialized == 0) return false;

        if (!hasType()) {
          memoizedIsInitialized = 0;
          return false;
        }
        memoizedIsInitialized = 1;
        return true;
      }

      public void writeTo(com.google.protobuf.CodedOutputStream output)
                          throws java.io.IOException {
        if (((bitField0_ & 0x00000001) == 0x00000001)) {
          com.google.protobuf.GeneratedMessage.writeString(output, 1, type_);
        }
        if (((bitField0_ & 0x00000002) == 0x00000002)) {
          com.google.protobuf.GeneratedMessage.writeString(output, 2, msg_);
        }
        unknownFields.writeTo(output);
      }

      public int getSerializedSize() {
        int size = memoizedSize;
        if (size != -1) return size;

        size = 0;
        if (((bitField0_ & 0x00000001) == 0x00000001)) {
          size += com.google.protobuf.GeneratedMessage.computeStringSize(1, type_);
        }
        if (((bitField0_ & 0x00000002) == 0x00000002)) {
          size += com.google.protobuf.GeneratedMessage.computeStringSize(2, msg_);
        }
        size += unknownFields.getSerializedSize();
        memoizedSize = size;
        return size;
      }

      private static final long serialVersionUID = 0L;
      public static Exception parseFrom(
          com.google.protobuf.ByteString data)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
      }
      public static Exception parseFrom(
          com.google.protobuf.ByteString data,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
      }
      public static Exception parseFrom(byte[] data)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
      }
      public static Exception parseFrom(
          byte[] data,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
      }
      public static Exception parseFrom(java.io.InputStream input)
          throws java.io.IOException {
        return PARSER.parseFrom(input);
      }
      public static Exception parseFrom(
          java.io.InputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        return PARSER.parseFrom(input, extensionRegistry);
      }
      public static Exception parseDelimitedFrom(java.io.InputStream input)
          throws java.io.IOException {
        return PARSER.parseDelimitedFrom(input);
      }
      public static Exception parseDelimitedFrom(
          java.io.InputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        return PARSER.parseDelimitedFrom(input, extensionRegistry);
      }
      public static Exception parseFrom(
          com.google.protobuf.CodedInputStream input)
          throws java.io.IOException {
        return PARSER.parseFrom(input);
      }
      public static Exception parseFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        return PARSER.parseFrom(input, extensionRegistry);
      }

      public Builder newBuilderForType() { return newBuilder(); }
      public static Builder newBuilder() {
        return DEFAULT_INSTANCE.toBuilder();
      }
      public static Builder newBuilder(Exception prototype) {
        return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
      }
      public Builder toBuilder() {
        return this == DEFAULT_INSTANCE
            ? new Builder() : new Builder().mergeFrom(this);
      }

      @Override
      protected Builder newBuilderForType(
          BuilderParent parent) {
        Builder builder = new Builder(parent);
        return builder;
      }
      /**
       * Protobuf type {@code com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Exception}
       */
      public static final class Builder extends
          com.google.protobuf.GeneratedMessage.Builder<Builder> implements
          // @@protoc_insertion_point(builder_implements:com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Exception)
          ExceptionOrBuilder {
        public static final com.google.protobuf.Descriptors.Descriptor
            getDescriptor() {
          return RpcTemplate.internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_Exception_descriptor;
        }

        protected FieldAccessorTable
            internalGetFieldAccessorTable() {
          return RpcTemplate.internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_Exception_fieldAccessorTable
              .ensureFieldAccessorsInitialized(
                  Exception.class, Builder.class);
        }

        // Construct using RpcTemplate.ProtobufResult.Exception.newBuilder()
        private Builder() {
          maybeForceBuilderInitialization();
        }

        private Builder(
            BuilderParent parent) {
          super(parent);
          maybeForceBuilderInitialization();
        }
        private void maybeForceBuilderInitialization() {
          if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
          }
        }
        public Builder clear() {
          super.clear();
          type_ = "";
          bitField0_ = (bitField0_ & ~0x00000001);
          msg_ = "";
          bitField0_ = (bitField0_ & ~0x00000002);
          return this;
        }

        public com.google.protobuf.Descriptors.Descriptor
            getDescriptorForType() {
          return RpcTemplate.internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_Exception_descriptor;
        }

        public Exception getDefaultInstanceForType() {
          return Exception.getDefaultInstance();
        }

        public Exception build() {
          Exception result = buildPartial();
          if (!result.isInitialized()) {
            throw newUninitializedMessageException(result);
          }
          return result;
        }

        public Exception buildPartial() {
          Exception result = new Exception(this);
          int from_bitField0_ = bitField0_;
          int to_bitField0_ = 0;
          if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
            to_bitField0_ |= 0x00000001;
          }
          result.type_ = type_;
          if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
            to_bitField0_ |= 0x00000002;
          }
          result.msg_ = msg_;
          result.bitField0_ = to_bitField0_;
          onBuilt();
          return result;
        }

        public Builder mergeFrom(com.google.protobuf.Message other) {
          if (other instanceof Exception) {
            return mergeFrom((Exception)other);
          } else {
            super.mergeFrom(other);
            return this;
          }
        }

        public Builder mergeFrom(Exception other) {
          if (other == Exception.getDefaultInstance()) return this;
          if (other.hasType()) {
            bitField0_ |= 0x00000001;
            type_ = other.type_;
            onChanged();
          }
          if (other.hasMsg()) {
            bitField0_ |= 0x00000002;
            msg_ = other.msg_;
            onChanged();
          }
          this.mergeUnknownFields(other.unknownFields);
          onChanged();
          return this;
        }

        public final boolean isInitialized() {
          if (!hasType()) {
            return false;
          }
          return true;
        }

        public Builder mergeFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
          Exception parsedMessage = null;
          try {
            parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
          } catch (com.google.protobuf.InvalidProtocolBufferException e) {
            parsedMessage = (Exception) e.getUnfinishedMessage();
            throw e;
          } finally {
            if (parsedMessage != null) {
              mergeFrom(parsedMessage);
            }
          }
          return this;
        }
        private int bitField0_;

        private Object type_ = "";
        /**
         * <code>required string type = 1;</code>
         */
        public boolean hasType() {
          return ((bitField0_ & 0x00000001) == 0x00000001);
        }
        /**
         * <code>required string type = 1;</code>
         */
        public String getType() {
          Object ref = type_;
          if (!(ref instanceof String)) {
            com.google.protobuf.ByteString bs =
                (com.google.protobuf.ByteString) ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
              type_ = s;
            }
            return s;
          } else {
            return (String) ref;
          }
        }
        /**
         * <code>required string type = 1;</code>
         */
        public com.google.protobuf.ByteString
            getTypeBytes() {
          Object ref = type_;
          if (ref instanceof String) {
            com.google.protobuf.ByteString b = 
                com.google.protobuf.ByteString.copyFromUtf8(
                    (String) ref);
            type_ = b;
            return b;
          } else {
            return (com.google.protobuf.ByteString) ref;
          }
        }
        /**
         * <code>required string type = 1;</code>
         */
        public Builder setType(
            String value) {
          if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
          type_ = value;
          onChanged();
          return this;
        }
        /**
         * <code>required string type = 1;</code>
         */
        public Builder clearType() {
          bitField0_ = (bitField0_ & ~0x00000001);
          type_ = getDefaultInstance().getType();
          onChanged();
          return this;
        }
        /**
         * <code>required string type = 1;</code>
         */
        public Builder setTypeBytes(
            com.google.protobuf.ByteString value) {
          if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
          type_ = value;
          onChanged();
          return this;
        }

        private Object msg_ = "";
        /**
         * <code>optional string msg = 2;</code>
         */
        public boolean hasMsg() {
          return ((bitField0_ & 0x00000002) == 0x00000002);
        }
        /**
         * <code>optional string msg = 2;</code>
         */
        public String getMsg() {
          Object ref = msg_;
          if (!(ref instanceof String)) {
            com.google.protobuf.ByteString bs =
                (com.google.protobuf.ByteString) ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
              msg_ = s;
            }
            return s;
          } else {
            return (String) ref;
          }
        }
        /**
         * <code>optional string msg = 2;</code>
         */
        public com.google.protobuf.ByteString
            getMsgBytes() {
          Object ref = msg_;
          if (ref instanceof String) {
            com.google.protobuf.ByteString b = 
                com.google.protobuf.ByteString.copyFromUtf8(
                    (String) ref);
            msg_ = b;
            return b;
          } else {
            return (com.google.protobuf.ByteString) ref;
          }
        }
        /**
         * <code>optional string msg = 2;</code>
         */
        public Builder setMsg(
            String value) {
          if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
          msg_ = value;
          onChanged();
          return this;
        }
        /**
         * <code>optional string msg = 2;</code>
         */
        public Builder clearMsg() {
          bitField0_ = (bitField0_ & ~0x00000002);
          msg_ = getDefaultInstance().getMsg();
          onChanged();
          return this;
        }
        /**
         * <code>optional string msg = 2;</code>
         */
        public Builder setMsgBytes(
            com.google.protobuf.ByteString value) {
          if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000002;
          msg_ = value;
          onChanged();
          return this;
        }

        // @@protoc_insertion_point(builder_scope:com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Exception)
      }

      // @@protoc_insertion_point(class_scope:com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Exception)
      private static final Exception DEFAULT_INSTANCE;
      static {
        DEFAULT_INSTANCE = new Exception();
      }

      public static Exception getDefaultInstance() {
        return DEFAULT_INSTANCE;
      }

      @Deprecated public static final com.google.protobuf.Parser<Exception>
          PARSER = new com.google.protobuf.AbstractParser<Exception>() {
        public Exception parsePartialFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
          try {
            return new Exception(input, extensionRegistry);
          } catch (RuntimeException e) {
            if (e.getCause() instanceof
                com.google.protobuf.InvalidProtocolBufferException) {
              throw (com.google.protobuf.InvalidProtocolBufferException)
                  e.getCause();
            }
            throw e;
          }
        }
      };

      public static com.google.protobuf.Parser<Exception> parser() {
        return PARSER;
      }

      @Override
      public com.google.protobuf.Parser<Exception> getParserForType() {
        return PARSER;
      }

      public Exception getDefaultInstanceForType() {
        return DEFAULT_INSTANCE;
      }

    }

    private int bitField0_;
    private int resultOneofCase_ = 0;
    private Object resultOneof_;
    public enum ResultOneofCase
        implements com.google.protobuf.Internal.EnumLite {
      VALUE(1),
      EXCEPTION(2),
      RESULTONEOF_NOT_SET(0);
      private int value = 0;
      private ResultOneofCase(int value) {
        this.value = value;
      }
      public static ResultOneofCase valueOf(int value) {
        switch (value) {
          case 1: return VALUE;
          case 2: return EXCEPTION;
          case 0: return RESULTONEOF_NOT_SET;
          default: throw new IllegalArgumentException(
            "Value is undefined for this oneof enum.");
        }
      }
      public int getNumber() {
        return this.value;
      }
    };

    public ResultOneofCase
    getResultOneofCase() {
      return ResultOneofCase.valueOf(
          resultOneofCase_);
    }

    public static final int VALUE_FIELD_NUMBER = 1;
    /**
     * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Value value = 1;</code>
     */
    public boolean hasValue() {
      return resultOneofCase_ == 1;
    }
    /**
     * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Value value = 1;</code>
     */
    public Value getValue() {
      if (resultOneofCase_ == 1) {
         return (Value) resultOneof_;
      }
      return Value.getDefaultInstance();
    }
    /**
     * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Value value = 1;</code>
     */
    public ValueOrBuilder getValueOrBuilder() {
      if (resultOneofCase_ == 1) {
         return (Value) resultOneof_;
      }
      return Value.getDefaultInstance();
    }

    public static final int EXCEPTION_FIELD_NUMBER = 2;
    /**
     * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Exception exception = 2;</code>
     */
    public boolean hasException() {
      return resultOneofCase_ == 2;
    }
    /**
     * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Exception exception = 2;</code>
     */
    public Exception getException() {
      if (resultOneofCase_ == 2) {
         return (Exception) resultOneof_;
      }
      return Exception.getDefaultInstance();
    }
    /**
     * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Exception exception = 2;</code>
     */
    public ExceptionOrBuilder getExceptionOrBuilder() {
      if (resultOneofCase_ == 2) {
         return (Exception) resultOneof_;
      }
      return Exception.getDefaultInstance();
    }

    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      if (hasValue()) {
        if (!getValue().isInitialized()) {
          memoizedIsInitialized = 0;
          return false;
        }
      }
      if (hasException()) {
        if (!getException().isInitialized()) {
          memoizedIsInitialized = 0;
          return false;
        }
      }
      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (resultOneofCase_ == 1) {
        output.writeMessage(1, (Value) resultOneof_);
      }
      if (resultOneofCase_ == 2) {
        output.writeMessage(2, (Exception) resultOneof_);
      }
      unknownFields.writeTo(output);
    }

    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (resultOneofCase_ == 1) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(1, (Value) resultOneof_);
      }
      if (resultOneofCase_ == 2) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(2, (Exception) resultOneof_);
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    public static ProtobufResult parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static ProtobufResult parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static ProtobufResult parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static ProtobufResult parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static ProtobufResult parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static ProtobufResult parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static ProtobufResult parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static ProtobufResult parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static ProtobufResult parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static ProtobufResult parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(ProtobufResult prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @Override
    protected Builder newBuilderForType(
        BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code com.ipd.jsf.gd.codec.protobuf.ProtobufResult}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:com.ipd.jsf.gd.codec.protobuf.ProtobufResult)
        ProtobufResultOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return RpcTemplate.internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_descriptor;
      }

      protected FieldAccessorTable
          internalGetFieldAccessorTable() {
        return RpcTemplate.internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                ProtobufResult.class, Builder.class);
      }

      // Construct using RpcTemplate.ProtobufResult.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      public Builder clear() {
        super.clear();
        resultOneofCase_ = 0;
        resultOneof_ = null;
        return this;
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return RpcTemplate.internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_descriptor;
      }

      public ProtobufResult getDefaultInstanceForType() {
        return ProtobufResult.getDefaultInstance();
      }

      public ProtobufResult build() {
        ProtobufResult result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public ProtobufResult buildPartial() {
        ProtobufResult result = new ProtobufResult(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (resultOneofCase_ == 1) {
          if (valueBuilder_ == null) {
            result.resultOneof_ = resultOneof_;
          } else {
            result.resultOneof_ = valueBuilder_.build();
          }
        }
        if (resultOneofCase_ == 2) {
          if (exceptionBuilder_ == null) {
            result.resultOneof_ = resultOneof_;
          } else {
            result.resultOneof_ = exceptionBuilder_.build();
          }
        }
        result.bitField0_ = to_bitField0_;
        result.resultOneofCase_ = resultOneofCase_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof ProtobufResult) {
          return mergeFrom((ProtobufResult)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(ProtobufResult other) {
        if (other == ProtobufResult.getDefaultInstance()) return this;
        switch (other.getResultOneofCase()) {
          case VALUE: {
            mergeValue(other.getValue());
            break;
          }
          case EXCEPTION: {
            mergeException(other.getException());
            break;
          }
          case RESULTONEOF_NOT_SET: {
            break;
          }
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      public final boolean isInitialized() {
        if (hasValue()) {
          if (!getValue().isInitialized()) {
            return false;
          }
        }
        if (hasException()) {
          if (!getException().isInitialized()) {
            return false;
          }
        }
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        ProtobufResult parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (ProtobufResult) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int resultOneofCase_ = 0;
      private Object resultOneof_;
      public ResultOneofCase
          getResultOneofCase() {
        return ResultOneofCase.valueOf(
            resultOneofCase_);
      }

      public Builder clearResultOneof() {
        resultOneofCase_ = 0;
        resultOneof_ = null;
        onChanged();
        return this;
      }

      private int bitField0_;

      private com.google.protobuf.SingleFieldBuilder<
          Value, Value.Builder, ValueOrBuilder> valueBuilder_;
      /**
       * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Value value = 1;</code>
       */
      public boolean hasValue() {
        return resultOneofCase_ == 1;
      }
      /**
       * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Value value = 1;</code>
       */
      public Value getValue() {
        if (valueBuilder_ == null) {
          if (resultOneofCase_ == 1) {
            return (Value) resultOneof_;
          }
          return Value.getDefaultInstance();
        } else {
          if (resultOneofCase_ == 1) {
            return valueBuilder_.getMessage();
          }
          return Value.getDefaultInstance();
        }
      }
      /**
       * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Value value = 1;</code>
       */
      public Builder setValue(Value value) {
        if (valueBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          resultOneof_ = value;
          onChanged();
        } else {
          valueBuilder_.setMessage(value);
        }
        resultOneofCase_ = 1;
        return this;
      }
      /**
       * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Value value = 1;</code>
       */
      public Builder setValue(
          Value.Builder builderForValue) {
        if (valueBuilder_ == null) {
          resultOneof_ = builderForValue.build();
          onChanged();
        } else {
          valueBuilder_.setMessage(builderForValue.build());
        }
        resultOneofCase_ = 1;
        return this;
      }
      /**
       * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Value value = 1;</code>
       */
      public Builder mergeValue(Value value) {
        if (valueBuilder_ == null) {
          if (resultOneofCase_ == 1 &&
              resultOneof_ != Value.getDefaultInstance()) {
            resultOneof_ = Value.newBuilder((Value) resultOneof_)
                .mergeFrom(value).buildPartial();
          } else {
            resultOneof_ = value;
          }
          onChanged();
        } else {
          if (resultOneofCase_ == 1) {
            valueBuilder_.mergeFrom(value);
          }
          valueBuilder_.setMessage(value);
        }
        resultOneofCase_ = 1;
        return this;
      }
      /**
       * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Value value = 1;</code>
       */
      public Builder clearValue() {
        if (valueBuilder_ == null) {
          if (resultOneofCase_ == 1) {
            resultOneofCase_ = 0;
            resultOneof_ = null;
            onChanged();
          }
        } else {
          if (resultOneofCase_ == 1) {
            resultOneofCase_ = 0;
            resultOneof_ = null;
          }
          valueBuilder_.clear();
        }
        return this;
      }
      /**
       * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Value value = 1;</code>
       */
      public Value.Builder getValueBuilder() {
        return getValueFieldBuilder().getBuilder();
      }
      /**
       * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Value value = 1;</code>
       */
      public ValueOrBuilder getValueOrBuilder() {
        if ((resultOneofCase_ == 1) && (valueBuilder_ != null)) {
          return valueBuilder_.getMessageOrBuilder();
        } else {
          if (resultOneofCase_ == 1) {
            return (Value) resultOneof_;
          }
          return Value.getDefaultInstance();
        }
      }
      /**
       * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Value value = 1;</code>
       */
      private com.google.protobuf.SingleFieldBuilder<
          Value, Value.Builder, ValueOrBuilder>
          getValueFieldBuilder() {
        if (valueBuilder_ == null) {
          if (!(resultOneofCase_ == 1)) {
            resultOneof_ = Value.getDefaultInstance();
          }
          valueBuilder_ = new com.google.protobuf.SingleFieldBuilder<
              Value, Value.Builder, ValueOrBuilder>(
                  (Value) resultOneof_,
                  getParentForChildren(),
                  isClean());
          resultOneof_ = null;
        }
        resultOneofCase_ = 1;
        onChanged();;
        return valueBuilder_;
      }

      private com.google.protobuf.SingleFieldBuilder<
          Exception, Exception.Builder, ExceptionOrBuilder> exceptionBuilder_;
      /**
       * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Exception exception = 2;</code>
       */
      public boolean hasException() {
        return resultOneofCase_ == 2;
      }
      /**
       * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Exception exception = 2;</code>
       */
      public Exception getException() {
        if (exceptionBuilder_ == null) {
          if (resultOneofCase_ == 2) {
            return (Exception) resultOneof_;
          }
          return Exception.getDefaultInstance();
        } else {
          if (resultOneofCase_ == 2) {
            return exceptionBuilder_.getMessage();
          }
          return Exception.getDefaultInstance();
        }
      }
      /**
       * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Exception exception = 2;</code>
       */
      public Builder setException(Exception value) {
        if (exceptionBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          resultOneof_ = value;
          onChanged();
        } else {
          exceptionBuilder_.setMessage(value);
        }
        resultOneofCase_ = 2;
        return this;
      }
      /**
       * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Exception exception = 2;</code>
       */
      public Builder setException(
          Exception.Builder builderForValue) {
        if (exceptionBuilder_ == null) {
          resultOneof_ = builderForValue.build();
          onChanged();
        } else {
          exceptionBuilder_.setMessage(builderForValue.build());
        }
        resultOneofCase_ = 2;
        return this;
      }
      /**
       * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Exception exception = 2;</code>
       */
      public Builder mergeException(Exception value) {
        if (exceptionBuilder_ == null) {
          if (resultOneofCase_ == 2 &&
              resultOneof_ != Exception.getDefaultInstance()) {
            resultOneof_ = Exception.newBuilder((Exception) resultOneof_)
                .mergeFrom(value).buildPartial();
          } else {
            resultOneof_ = value;
          }
          onChanged();
        } else {
          if (resultOneofCase_ == 2) {
            exceptionBuilder_.mergeFrom(value);
          }
          exceptionBuilder_.setMessage(value);
        }
        resultOneofCase_ = 2;
        return this;
      }
      /**
       * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Exception exception = 2;</code>
       */
      public Builder clearException() {
        if (exceptionBuilder_ == null) {
          if (resultOneofCase_ == 2) {
            resultOneofCase_ = 0;
            resultOneof_ = null;
            onChanged();
          }
        } else {
          if (resultOneofCase_ == 2) {
            resultOneofCase_ = 0;
            resultOneof_ = null;
          }
          exceptionBuilder_.clear();
        }
        return this;
      }
      /**
       * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Exception exception = 2;</code>
       */
      public Exception.Builder getExceptionBuilder() {
        return getExceptionFieldBuilder().getBuilder();
      }
      /**
       * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Exception exception = 2;</code>
       */
      public ExceptionOrBuilder getExceptionOrBuilder() {
        if ((resultOneofCase_ == 2) && (exceptionBuilder_ != null)) {
          return exceptionBuilder_.getMessageOrBuilder();
        } else {
          if (resultOneofCase_ == 2) {
            return (Exception) resultOneof_;
          }
          return Exception.getDefaultInstance();
        }
      }
      /**
       * <code>optional .com.ipd.jsf.gd.codec.protobuf.ProtobufResult.Exception exception = 2;</code>
       */
      private com.google.protobuf.SingleFieldBuilder<
          Exception, Exception.Builder, ExceptionOrBuilder>
          getExceptionFieldBuilder() {
        if (exceptionBuilder_ == null) {
          if (!(resultOneofCase_ == 2)) {
            resultOneof_ = Exception.getDefaultInstance();
          }
          exceptionBuilder_ = new com.google.protobuf.SingleFieldBuilder<
              Exception, Exception.Builder, ExceptionOrBuilder>(
                  (Exception) resultOneof_,
                  getParentForChildren(),
                  isClean());
          resultOneof_ = null;
        }
        resultOneofCase_ = 2;
        onChanged();;
        return exceptionBuilder_;
      }

      // @@protoc_insertion_point(builder_scope:com.ipd.jsf.gd.codec.protobuf.ProtobufResult)
    }

    // @@protoc_insertion_point(class_scope:com.ipd.jsf.gd.codec.protobuf.ProtobufResult)
    private static final ProtobufResult DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new ProtobufResult();
    }

    public static ProtobufResult getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    @Deprecated public static final com.google.protobuf.Parser<ProtobufResult>
        PARSER = new com.google.protobuf.AbstractParser<ProtobufResult>() {
      public ProtobufResult parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        try {
          return new ProtobufResult(input, extensionRegistry);
        } catch (RuntimeException e) {
          if (e.getCause() instanceof
              com.google.protobuf.InvalidProtocolBufferException) {
            throw (com.google.protobuf.InvalidProtocolBufferException)
                e.getCause();
          }
          throw e;
        }
      }
    };

    public static com.google.protobuf.Parser<ProtobufResult> parser() {
      return PARSER;
    }

    @Override
    public com.google.protobuf.Parser<ProtobufResult> getParserForType() {
      return PARSER;
    }

    public ProtobufResult getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufInvocation_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufInvocation_fieldAccessorTable;
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufInvocation_AttachmentsEntry_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufInvocation_AttachmentsEntry_fieldAccessorTable;
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_fieldAccessorTable;
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_Value_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_Value_fieldAccessorTable;
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_Exception_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_Exception_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    String[] descriptorData = {
      "\n\nrpc3.proto\022\034com.ipd.jsf.gd.codec.protob" +
      "uf\"\364\001\n\022ProtobufInvocation\022\017\n\007service\030\001 \002" +
      "(\t\022\r\n\005alias\030\002 \002(\t\022\016\n\006method\030\003 \002(\t\022\020\n\010arg" +
      "_type\030\004 \001(\t\022\020\n\010arg_data\030\005 \001(\014\022V\n\013attachm" +
      "ents\030\006 \003(\0132A.com.ipd.jsf.gd.codec.protobu" +
      "f.ProtobufInvocation.AttachmentsEntry\0322\n" +
      "\020AttachmentsEntry\022\013\n\003key\030\001 \001(\t\022\r\n\005value\030" +
      "\002 \001(\t:\0028\001\"\377\001\n\016ProtobufResult\022C\n\005value\030\001 " +
      "\001(\01322.com.ipd.jsf.gd.codec.protobuf.Proto" +
      "bufResult.ValueH\000\022K\n\texception\030\002 \001(\01326.c",
      "om.ipd.jsf.gd.codec.protobuf.ProtobufResu" +
      "lt.ExceptionH\000\032#\n\005Value\022\014\n\004type\030\001 \002(\t\022\014\n" +
      "\004data\030\002 \001(\014\032&\n\tException\022\014\n\004type\030\001 \002(\t\022\013" +
      "\n\003msg\030\002 \001(\tB\016\n\014result_oneofB\rB\013RpcTempla" +
      "te"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufInvocation_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufInvocation_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessage.FieldAccessorTable(
        internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufInvocation_descriptor,
        new String[] { "Service", "Alias", "Method", "ArgType", "ArgData", "Attachments", });
    internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufInvocation_AttachmentsEntry_descriptor =
      internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufInvocation_descriptor.getNestedTypes().get(0);
    internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufInvocation_AttachmentsEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessage.FieldAccessorTable(
        internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufInvocation_AttachmentsEntry_descriptor,
        new String[] { "Key", "Value", });
    internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessage.FieldAccessorTable(
        internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_descriptor,
        new String[] { "Value", "Exception", "ResultOneof", });
    internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_Value_descriptor =
      internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_descriptor.getNestedTypes().get(0);
    internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_Value_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessage.FieldAccessorTable(
        internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_Value_descriptor,
        new String[] { "Type", "Data", });
    internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_Exception_descriptor =
      internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_descriptor.getNestedTypes().get(1);
    internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_Exception_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessage.FieldAccessorTable(
        internal_static_com_ipd_jsf_gd_codec_protobuf_ProtobufResult_Exception_descriptor,
        new String[] { "Type", "Msg", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}