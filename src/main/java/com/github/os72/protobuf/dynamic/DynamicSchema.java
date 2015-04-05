/*
 * Copyright 2015 protobuf-dynamic developers
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.os72.protobuf.dynamic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.DynamicMessage;

/**
 * DynamicSchema
 */
public class DynamicSchema
{
	// --- public static ---

	/**
	 * Creates a new dynamic schema builder
	 * 
	 * @return the schema builder
	 */
	public static Builder newBuilder() {
		return new Builder();
	}

	/**
	 * Parses a serialized schema descriptor (from input stream; closes the stream)
	 * 
	 * @param schemaDescIn the descriptor input stream
	 * @return the schema object
	 * @throws DescriptorValidationException
	 * @throws IOException
	 */
	public static DynamicSchema parseFrom(InputStream schemaDescIn) throws DescriptorValidationException, IOException {
		try {
			int len;
			byte[] buf = new byte[4096];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while ((len = schemaDescIn.read(buf)) > 0) baos.write(buf, 0, len);
			return parseFrom(baos.toByteArray());
		}
		finally {
			schemaDescIn.close();
		}
	}

	/**
	 * Parses a serialized schema descriptor (from byte array)
	 * 
	 * @param schemaDescBuf the descriptor byte array
	 * @return the schema object
	 * @throws DescriptorValidationException
	 * @throws IOException
	 */
	public static DynamicSchema parseFrom(byte[] schemaDescBuf) throws DescriptorValidationException, IOException {
		return new DynamicSchema(FileDescriptorSet.parseFrom(schemaDescBuf));
	}

	// --- public ---

	/**
	 * Creates a new dynamic message builder for the given message type
	 * 
	 * @param msgTypeName the message type name
	 * @return the message builder (null if not found)
	 */
	public DynamicMessage.Builder newMessageBuilder(String msgTypeName) {
		Descriptor msgType = mMsgDescriptorMap.get(msgTypeName);
		if (msgType == null) return null;
		return DynamicMessage.newBuilder(msgType);
	}

	/**
	 * Gets the protobuf message descriptor for the given message type
	 * 
	 * @param msgTypeName the message type name
	 * @return the message descriptor (null if not found)
	 */
	public Descriptor getMessageDescriptor(String msgTypeName) {
		return mMsgDescriptorMap.get(msgTypeName);
	}

	/**
	 * Gets the protobuf enum value for the given enum type and name
	 * 
	 * @param enumTypeName the enum type name
	 * @param enumName the enum name
	 * @return the enum value descriptor (null if not found)
	 */
	public EnumValueDescriptor getEnumValue(String enumTypeName, String enumName) {
		EnumDescriptor enumType = mEnumDescriptorMap.get(enumTypeName);
		if (enumType == null) return null;
		return enumType.findValueByName(enumName);
	}

	/**
	 * Gets the protobuf enum value for the given enum type and number
	 * 
	 * @param enumTypeName the enum type name
	 * @param enumNumber the enum number
	 * @return the enum value descriptor (null if not found)
	 */
	public EnumValueDescriptor getEnumValue(String enumTypeName, int enumNumber) {
		EnumDescriptor enumType = mEnumDescriptorMap.get(enumTypeName);
		if (enumType == null) return null;
		return enumType.findValueByNumber(enumNumber);
	}

	/**
	 * Serializes the schema
	 * 
	 * @return the serialized schema descriptor
	 */
	public byte[] toByteArray() {
		return mFileDescSet.toByteArray();
	}

	/**
	 * Returns a string representation of the schema
	 * 
	 * @return the schema string
	 */
	public String toString() {
		Set<String> msgTypes = new TreeSet<String>(mMsgDescriptorMap.keySet());
		Set<String> enumTypes = new TreeSet<String>(mEnumDescriptorMap.keySet());
		return "types: " + msgTypes + " enums: " + enumTypes + "\n" + mFullNameMap + "\n" + mFileDescSet;
	}

	// --- private ---

	private DynamicSchema(FileDescriptorSet fileDescSet) throws DescriptorValidationException {
		// TODO: resolve dependencies
		//
		mFileDescSet = fileDescSet;
		for (FileDescriptorProto fileDescProto : fileDescSet.getFileList()) {
			FileDescriptor fileDesc = FileDescriptor.buildFrom(fileDescProto, new FileDescriptor[0]);
			for (Descriptor msgType : fileDesc.getMessageTypes()) addMessageType(msgType, null);			
		}
	}

	private void addMessageType(Descriptor msgType, String scope) {
		// TODO: check name conflicts, full name, etc
		//
		mFullNameMap.put(msgType.getName(), msgType.getFullName());
		
		String msgTypeName = (scope == null ? msgType.getName() : scope + "." + msgType.getName());
		mMsgDescriptorMap.put(msgTypeName, msgType);
		for (Descriptor nestedType : msgType.getNestedTypes()) addMessageType(nestedType, msgTypeName);
		for (EnumDescriptor enumType : msgType.getEnumTypes()) addEnumType(enumType, msgTypeName);
	}

	private void addEnumType(EnumDescriptor enumType, String scope) {
		String enumTypeName = (scope == null ? enumType.getName() : scope + "." + enumType.getName());
		mEnumDescriptorMap.put(enumTypeName, enumType);
	}

	private FileDescriptorSet mFileDescSet;
	private Map<String,Descriptor> mMsgDescriptorMap = new HashMap<String,Descriptor>();
	private Map<String,EnumDescriptor> mEnumDescriptorMap = new HashMap<String,EnumDescriptor>();
	private Map<String,String> mFullNameMap = new TreeMap<String,String>();

	/**
	 * DynamicSchema.Builder
	 */
	public static class Builder
	{
		// --- public ---

		/**
		 * Builds a dynamic schema
		 * 
		 * @return the schema object
		 * @throws DescriptorValidationException
		 */
		public DynamicSchema build() throws DescriptorValidationException {
			FileDescriptorSet.Builder fileDescSetBuilder = FileDescriptorSet.newBuilder();
			fileDescSetBuilder.addFile(mFileDescProtoBuilder.build());
			fileDescSetBuilder.mergeFrom(mFileDescSetBuilder.build());
			return new DynamicSchema(fileDescSetBuilder.build());
		}

		public Builder setName(String name) {
			mFileDescProtoBuilder.setName(name);
			return this;
		}

		public Builder setPackage(String name) {
			mFileDescProtoBuilder.setPackage(name);
			return this;
		}

		public Builder addMessageDefinition(MessageDefinition msgDef) {
			mFileDescProtoBuilder.addMessageType(msgDef.getMessageType());
			return this;
		}

		public Builder addEnumDefinition(EnumDefinition enumDef) {
			mFileDescProtoBuilder.addEnumType(enumDef.getEnumType());
			return this;
		}

		public Builder addSchema(DynamicSchema schema) {
			mFileDescSetBuilder.mergeFrom(schema.mFileDescSet);
			return this;
		}

		// --- private ---
		
		private Builder() {
			mFileDescProtoBuilder = FileDescriptorProto.newBuilder();
			mFileDescSetBuilder = FileDescriptorSet.newBuilder();
		}

		private FileDescriptorProto.Builder mFileDescProtoBuilder;
		private FileDescriptorSet.Builder mFileDescSetBuilder;
	}
}
