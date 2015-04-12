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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
		Descriptor msgType = getMessageDescriptor(msgTypeName);
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
		Descriptor msgType = mMsgDescriptorMapShort.get(msgTypeName);
		if (msgType == null) msgType = mMsgDescriptorMapFull.get(msgTypeName);
		return msgType;
	}

	/**
	 * Gets the enum value for the given enum type and name
	 * 
	 * @param enumTypeName the enum type name
	 * @param enumName the enum name
	 * @return the enum value descriptor (null if not found)
	 */
	public EnumValueDescriptor getEnumValue(String enumTypeName, String enumName) {
		EnumDescriptor enumType = getEnumDescriptor(enumTypeName);
		if (enumType == null) return null;
		return enumType.findValueByName(enumName);
	}

	/**
	 * Gets the enum value for the given enum type and number
	 * 
	 * @param enumTypeName the enum type name
	 * @param enumNumber the enum number
	 * @return the enum value descriptor (null if not found)
	 */
	public EnumValueDescriptor getEnumValue(String enumTypeName, int enumNumber) {
		EnumDescriptor enumType = getEnumDescriptor(enumTypeName);
		if (enumType == null) return null;
		return enumType.findValueByNumber(enumNumber);
	}

	/**
	 * Gets the protobuf enum descriptor for the given enum type
	 * 
	 * @param enumTypeName the enum type name
	 * @return the enum descriptor (null if not found)
	 */
	public EnumDescriptor getEnumDescriptor(String enumTypeName) {
		EnumDescriptor enumType = mEnumDescriptorMapShort.get(enumTypeName);
		if (enumType == null) enumType = mEnumDescriptorMapFull.get(enumTypeName);
		return enumType;
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
		Set<String> msgTypes = new TreeSet<String>(mMsgDescriptorMapFull.keySet());
		Set<String> enumTypes = new TreeSet<String>(mEnumDescriptorMapFull.keySet());
		return "types: " + msgTypes + "\nenums: " + enumTypes + "\n" + mFileDescSet;
	}

	// --- private ---

	private DynamicSchema(FileDescriptorSet fileDescSet) throws DescriptorValidationException {
		mFileDescSet = fileDescSet;
		Map<String,FileDescriptor> fileDescMap = init(fileDescSet);
		
		Set<String> msgDupes = new HashSet<String>();
		Set<String> enumDupes = new HashSet<String>();
		for (FileDescriptor fileDesc : fileDescMap.values()) {
			for (Descriptor msgType : fileDesc.getMessageTypes()) addMessageType(msgType, null, msgDupes, enumDupes);			
			for (EnumDescriptor enumType : fileDesc.getEnumTypes()) addEnumType(enumType, null, enumDupes);						
		}
		
		for (String msgName : msgDupes) mMsgDescriptorMapShort.remove(msgName);
		for (String enumName : enumDupes) mEnumDescriptorMapShort.remove(enumName);
	}

	private Map<String,FileDescriptor> init(FileDescriptorSet fileDescSet) throws DescriptorValidationException {
		// check for dupes
		Map<String,FileDescriptor> fileDescMap = new HashMap<String,FileDescriptor>();
		for (FileDescriptorProto fdProto : fileDescSet.getFileList()) {
			if (fileDescMap.containsKey(fdProto.getName()))  throw new IllegalArgumentException("duplicate name: " + fdProto.getName());
			fileDescMap.put(fdProto.getName(), null);
		}
		fileDescMap.clear();
		
		// build FileDescriptors, resolve dependencies (imports) if any
		while (fileDescMap.size() < fileDescSet.getFileCount()) {
			for (FileDescriptorProto fdProto : fileDescSet.getFileList()) {
				if (fileDescMap.containsKey(fdProto.getName())) continue;
				
				List<String> dependencyList = fdProto.getDependencyList();
				List<FileDescriptor> fdList = new ArrayList<FileDescriptor>();
				for (String depName : dependencyList) {
					FileDescriptor fd = fileDescMap.get(depName);
					if (fd != null) fdList.add(fd);
				}
				
				if (fdList.size() == dependencyList.size()) { // dependencies resolved
					FileDescriptor[] fds = new FileDescriptor[fdList.size()];
					FileDescriptor fd = FileDescriptor.buildFrom(fdProto, fdList.toArray(fds));
					fileDescMap.put(fdProto.getName(), fd);
				}
			}
		}
		
		return fileDescMap;
	}

	private void addMessageType(Descriptor msgType, String scope, Set<String> msgDupes, Set<String> enumDupes) {
		String msgTypeNameFull = msgType.getFullName();
		String msgTypeNameShort = (scope == null ? msgType.getName() : scope + "." + msgType.getName());
		
		if (mMsgDescriptorMapFull.containsKey(msgTypeNameFull)) throw new IllegalArgumentException("duplicate name: " + msgTypeNameFull);
		if (mMsgDescriptorMapShort.containsKey(msgTypeNameShort)) msgDupes.add(msgTypeNameShort);
		
		mMsgDescriptorMapFull.put(msgTypeNameFull, msgType);
		mMsgDescriptorMapShort.put(msgTypeNameShort, msgType);
		
		for (Descriptor nestedType : msgType.getNestedTypes()) addMessageType(nestedType, msgTypeNameShort, msgDupes, enumDupes);
		for (EnumDescriptor enumType : msgType.getEnumTypes()) addEnumType(enumType, msgTypeNameShort, enumDupes);
	}

	private void addEnumType(EnumDescriptor enumType, String scope, Set<String> enumDupes) {
		String enumTypeNameFull = enumType.getFullName();
		String enumTypeNameShort = (scope == null ? enumType.getName() : scope + "." + enumType.getName());
		
		if (mEnumDescriptorMapFull.containsKey(enumTypeNameFull)) throw new IllegalArgumentException("duplicate name: " + enumTypeNameFull);
		if (mEnumDescriptorMapShort.containsKey(enumTypeNameShort)) enumDupes.add(enumTypeNameShort);
		
		mEnumDescriptorMapFull.put(enumTypeNameFull, enumType);
		mEnumDescriptorMapShort.put(enumTypeNameShort, enumType);
	}

	private FileDescriptorSet mFileDescSet;
	private Map<String,Descriptor> mMsgDescriptorMapFull = new HashMap<String,Descriptor>();
	private Map<String,Descriptor> mMsgDescriptorMapShort = new HashMap<String,Descriptor>();
	private Map<String,EnumDescriptor> mEnumDescriptorMapFull = new HashMap<String,EnumDescriptor>();
	private Map<String,EnumDescriptor> mEnumDescriptorMapShort = new HashMap<String,EnumDescriptor>();

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
