protobuf-dynamic
================

Protocol Buffers Dynamic Schema - *coming soon*

Library to simplify working with the Protocol Buffers reflection mechanism:
* Dynamic schema creation - at runtime
* Dynamic message creation from schema
* Schema serialization, deserialization
* Schema parsing from protoc compiler output

#### Usage:
```java
// Create dynamic schema
DynamicSchema.Builder schemaBuilder = DynamicSchema.newBuilder();
schemaBuilder.setName("PersonSchemaDynamic.proto");

MessageDefinition msgDef;
msgDef = MessageDefinition.newBuilder("Person")		// message Person
		.addField("required", "int32", "id", 1)		// required int32 id = 1
		.addField("required", "string", "name", 2)	// required string name = 2
		.addField("optional", "string", "email", 3)	// optional string email = 3
		.build();
schemaBuilder.addMessageDefinition(msgDef);
DynamicSchema schema = schemaBuilder.build();

// Create messages from schema
Descriptor msgDesc;
DynamicMessage.Builder msgBuilder;
msgBuilder = schema.newMessageBuilder("Person");
msgDesc = msgBuilder.getDescriptorForType();
msgBuilder.setField(msgDesc.findFieldByName("id"), 1);
msgBuilder.setField(msgDesc.findFieldByName("name"), "Alan Turing");
msgBuilder.setField(msgDesc.findFieldByName("email"), "at@sis.gov.uk");
DynamicMessage msg = msgBuilder.build();
```
