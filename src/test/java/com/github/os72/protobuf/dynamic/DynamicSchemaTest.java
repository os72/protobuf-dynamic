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

import java.io.FileInputStream;

import org.junit.Test;
import org.junit.Assert;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.DynamicMessage;

public class DynamicSchemaTest
{
	@Test
	public void testDynamicSchema() throws Exception {
		// Create dynamic schema
		DynamicSchema.Builder schemaBuilder = DynamicSchema.newBuilder();
		schemaBuilder.setName("PersonSchemaDynamic.proto");
		
		MessageDefinition msgDef = MessageDefinition.newBuilder("Person") // message Person
				.addField("required", "int32", "id", 1)		// required int32 id = 1
				.addField("required", "string", "name", 2)	// required string name = 2
				.addField("optional", "string", "email", 3)	// optional string email = 3
				.build();
		
		schemaBuilder.addMessageDefinition(msgDef);
		DynamicSchema schema = schemaBuilder.build();
		log(schema);
		
		// Create dynamic message from schema
		DynamicMessage.Builder msgBuilder = schema.newMessageBuilder("Person");
		Descriptor msgDesc = msgBuilder.getDescriptorForType();
		DynamicMessage msg = msgBuilder
				.setField(msgDesc.findFieldByName("id"), 1)
				.setField(msgDesc.findFieldByName("name"), "Alan Turing")
				.setField(msgDesc.findFieldByName("email"), "at@sis.gov.uk")
				.build();
		log(msg);
		
		// Create data object traditional way using generated code 
		PersonSchema.Person person = PersonSchema.Person.newBuilder()
				.setId(1)
				.setName("Alan Turing")
				.setEmail("at@sis.gov.uk")
				.build();
		
		// Should be equivalent
		Assert.assertEquals(person.toString(), msg.toString());
	}

	@Test
	public void testSchemaSerialization() throws Exception {
		// deserialize
		DynamicSchema schema1 = DynamicSchema.parseFrom(new FileInputStream("src/test/resources/PersonSchema.desc"));
		log(schema1);
		
		byte[] descBuf = schema1.toByteArray(); // serialize
		DynamicSchema schema2 = DynamicSchema.parseFrom(descBuf); // deserialize
		
		Assert.assertEquals(schema1.toString(), schema2.toString());
	}

	static void log(Object o) {
		System.out.println(o);
	}
}
