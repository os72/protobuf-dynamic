protobuf-dynamic
================

Protocol Buffers Dynamic Schema - create protobuf schemas programmatically.
Available on Maven Central:
* https://repo.maven.apache.org/maven2/com/github/os72/protobuf-dynamic/1.0.0/
* https://repo.maven.apache.org/maven2/com/github/os72/protobuf-dynamic/0.9.4/

[![Maven Central](https://img.shields.io/badge/maven%20central-1.0.0-brightgreen.svg)](http://search.maven.org/#artifactdetails|com.github.os72|protobuf-dynamic|1.0.0|)
[![Maven Central](https://img.shields.io/badge/maven%20central-0.9.4-brightgreen.svg)](http://search.maven.org/#artifactdetails|com.github.os72|protobuf-dynamic|0.9.4|)

---

Library to simplify working with the Protocol Buffers reflection mechanism, no protoc compiler required.
Supports the major protobuf features: primitive types, complex and nested types, labels, default values, etc
* Dynamic schema creation - at runtime
* Dynamic message creation from schema
* Schema merging
* Schema serialization, deserialization
* Schema parsing from protoc compiler output
* Compatible with `protobuf-java` 2.6.1 or higher
* (Version 0.9.x compatible with `protobuf-java` 2.4.1 or higher)

See the Protocol Buffers site for details: https://github.com/google/protobuf

#### Usage
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
  <version>1.0.0</version>
</dependency>
```
```xml
<dependency>
  <groupId>com.github.os72</groupId>
  <artifactId>protobuf-dynamic</artifactId>
  <version>0.9.4</version>
</dependency>
```
