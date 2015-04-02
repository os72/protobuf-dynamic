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

MessageDefinition msgDef = MessageDefinition.newBuilder("Person") // message Person
		.addField("required", "int32", "id", 1)		// required int32 id = 1
		.addField("required", "string", "name", 2)	// required string name = 2
		.addField("optional", "string", "email", 3)	// optional string email = 3
		.build();

schemaBuilder.addMessageDefinition(msgDef);
DynamicSchema schema = schemaBuilder.build();

// Create dynamic message from schema
DynamicMessage.Builder msgBuilder = schema.newMessageBuilder("Person");
Descriptor msgDesc = msgBuilder.getDescriptorForType();
DynamicMessage msg = msgBuilder
		.setField(msgDesc.findFieldByName("id"), 1)
		.setField(msgDesc.findFieldByName("name"), "Alan Turing")
		.setField(msgDesc.findFieldByName("email"), "at@sis.gov.uk")
		.build();
```

#### Maven dependency
```xml
<dependency>
  <groupId>com.github.os72</groupId>
  <artifactId>protobuf-dynamic</artifactId>
  <version>0.1-SNAPSHOT</version>
</dependency>
```
