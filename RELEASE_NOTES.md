protobuf-dynamic release notes
==============================

#### 0.9 (6-Apr-2015)
* Initial release - introduce DynamicSchema, MessageDefinition, EnumDefinition
* Supports the major protobuf features: primitive types, complex and nested types, labels, default values, etc
* Supports schema serialization and deserialization
* Limitation: no dependency resolution yet (schema imports)
* Limitation: no message name collision handling yet (during schema merging)
