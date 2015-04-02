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
import java.util.Map;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.DescriptorProtos.FileOptions;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * DynamicSchema
 */
public class DynamicSchema
{
	/**
	 * Creates a new dynamic schema builder
	 * 
	 * @return the schema builder
	 */
	public static Builder newBuilder() {
		return new Builder();
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
		// TODO: hold full fdSet, resolve dependencies, populate descriptorMap
		DescriptorProtos.FileDescriptorSet fdSet = DescriptorProtos.FileDescriptorSet.parseFrom(schemaDescBuf);
		FileDescriptorProto fileDescProto = fdSet.getFileList().get(0);
		return new DynamicSchema(fileDescProto);
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
	 * Creates a new dynamic message builder for the given message name
	 * 
	 * @param msgName the message name
	 * @return the message builder
	 */
	public DynamicMessage.Builder newMessageBuilder(String msgName) {
		Descriptor type = mFileDesc.findMessageTypeByName(msgName);
		return DynamicMessage.newBuilder(type);
	}

	/**
	 * Gets the protobuf message descriptor for the given message name
	 * 
	 * @param msgName the message name
	 * @return the message descriptor
	 */
	public Descriptor getMessageDescriptor(String msgName) {
		return mFileDesc.findMessageTypeByName(msgName);
	}

	/**
	 * Serializes the schema
	 * 
	 * @return the serialized schema descriptor
	 */
	public byte[] toByteArray() {
		// TODO: full descriptorSet
		DescriptorProtos.FileDescriptorSet.Builder fdSetBuilder = DescriptorProtos.FileDescriptorSet.newBuilder();
		fdSetBuilder.addFile(mFileDesc.toProto());
		return fdSetBuilder.build().toByteArray();
	}

	/**
	 * Returns a string representation of the schema
	 * 
	 * @return the schema string
	 */
	public String toString() {
		// TODO: full descriptorSet
		return mFileDesc.toProto().toString();
	}

	/**
	 * Creates a dynamic schema
	 * 
	 * @param fileDescProto the protobuf file descriptor proto
	 * @throws DescriptorValidationException
	 */
	private DynamicSchema(FileDescriptorProto fileDescProto) throws DescriptorValidationException {
		mFileDesc = FileDescriptor.buildFrom(fileDescProto, new FileDescriptor[0]);
	}

	private FileDescriptor mFileDesc;

	// TODO: hold fdSet, descriptorMap
	private DescriptorProtos.FileDescriptorSet mFdSet;
	private Map<String,Descriptor> mDescriptorMap;

	/**
	 * DynamicSchema.Builder
	 */
	public static class Builder
	{
		/**
		 * Builds a dynamic schema
		 * 
		 * @return the schema object
		 * @throws DescriptorValidationException
		 */
		public DynamicSchema build() throws DescriptorValidationException {
			return new DynamicSchema(mFileDescProtoBuilder.build());
		}

		public Builder setName(String name) {
			mFileDescProtoBuilder.setName(name);
			return this;
		}

		public Builder setPackage(String name) {
			mFileDescProtoBuilder.setPackage(name);
			return this;
		}

		public Builder addMessageDefinition(MessageDefinition def) {
			mFileDescProtoBuilder.addMessageType(def.getMsgType());
			return this;
		}

		// TODO
		public Builder addSchema(DynamicSchema schema) {
			// merge descriptorSets
			return this;
		}

		private Builder() {
			mFileDescProtoBuilder = FileDescriptorProto.newBuilder();
		}

		FileDescriptorProto.Builder mFileDescProtoBuilder;
	}
}
