protobuf-dynamic release notes
==============================

#### 0.9.4 (14-Jan-2018)
* Check for missing dependencies when parsing schema descriptor (issue #8)

#### 0.9.3 (5-Jan-2016)
* DynamicSchema: getMessageTypes(), getEnumTypes()

#### 0.9.2 (19-Oct-2015)
* Fix compatibility issue with protobuf-java 2.6.1, 3.0.0

#### 0.9.1 (12-Apr-2015)
* Support type lookup both by short and fully qualified names
* Implement dependency resolution (schema imports)
* Message name collision handling (during schema merging)

#### 0.9 (6-Apr-2015)
* Initial release - introduce DynamicSchema, MessageDefinition, EnumDefinition
* Supports the major protobuf features: primitive types, complex and nested types, labels, default values, etc
* Supports schema serialization and deserialization
* Limitation: no dependency resolution yet (schema imports)
* Limitation: no message name collision handling yet (during schema merging)
