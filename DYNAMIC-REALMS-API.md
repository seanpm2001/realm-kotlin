# API Dynamic Realms
This API would provide access to an untyped Realm, that has provides a generic data access objects. Such objects allows the users to access their fields with type unsafe accessors.

There are two main elements in this API the Realm itself, and the Realm objects used to access this Realm.

## DynamicRealm

## API Proposal

The API is a variation of the `Realm` and `MutableRealm`, that only handles `DynamicRealmObjects`.

```
interface UntypedRealm: BaseRealm {
    fun objects(clazz: String): RealmResults<DynamicRealmObject>
    fun query(
        clazz: String,
        query: String = "TRUEPREDICATE",
        vararg args: Any?
    ): RealmQuery<DynamicRealmObject>
}


interface DynamicRealm: UntypedRealm {
    suspend fun <R> write(block: DynamicMutableRealm.() -> R): R 
    fun <R> writeBlocking(block: DynamicMutableRealm.() -> R): R
    fun observe(): Flow<DynamicRealm>
    fun close()
}

interface DynamicMutableRealm: UntypedRealm {
    fun createObject(type: String): DynamicRealmObject
    fun findLatest(obj: DynamicRealmObject): DynamicRealmObject?
    fun cancelWrite()
    fun delete(obj: DynamicRealmObject) // deletes an object
}
```
> Discussion: Better naming?

> Feedback: Any missing functionality?

## DynamicRealmObject

A `DynamicRealmObject` is a type unsafe object, that provides:
-  attribute access via a `String`
- does runtime type check
- support custom types adapters?

> Discussion: Better naming?

### Proposals
### 1. Typed functions (java style)

Because this proposal is statically typed it cannot provide support for custom data types.

```
class DynamicRealmObject {

    fun getByte(fieldName: String): Byte
    fun setByte(fieldName: String, value: Byte)

    fun getShort(fieldName: String): Short
    fun setShort(fieldName: String, value: Short)

    fun getInt(fieldName: String): Int
    fun setInt(fieldName: String, value: Int)

    fun getLong(fieldName: String): Long
    fun setLong(fieldName: String, value: Long)

    fun getFloat(fieldName: String): Float
    fun setFloat(fieldName: String, value: Float)

    fun getDouble(fieldName: String): Double
    fun setDouble(fieldName: String, value: Double)

    fun getString(fieldName: String): String
    fun setString(fieldName: String, value: String)

    fun getRealmInstant(fieldName: String): RealmInstant
    fun setRealmInstant(fieldName: String, value: RealmInstant)

    fun getFloat(fieldName: String): Float
    fun setFloat(fieldName: String, value: Float)

    (...)

    fun getList(fieldName: String): RealmList<DynamicRealmObject>
    inline fun <reified T> getList(fieldName: String): RealmList<T>
    
    // Not a real case, but how would we handle a map?
    inline fun <reified K, reified V> getMap(fieldName: String): RealmMap<K, V>
    
    (...)
}
```

```
// Some examples

val dynamicObject: DynamicRealmObject

// No need to explicity define the type, is defined by the function
val name = dynamicObject.getString("name")

// Exception on mismatching types
assertFailsWith<IllegalStateException> {
    dynamicObject.getFloat("name")
}

// complex types
val myList: RealmList<DynamicRealmObject> = dynamicObject.getList("myList")
val floatList: RealmList<Float> = dynamicObject.getList("floatList")
```


### 2. Dynamic type

```
interface DynamicRealmObject {
    inline fun <reified T> get(fieldName: String): T
    inline fun <reified T> set(fieldName: String, value: T)
    inline fun <reified T> getObjectLinks(fieldName: String): RealmResults<T>
}
```

> Dicussion: could we collapse `getObjectLinks` into `get`?

```
// Some examples

val dynamicObject: DynamicRealmObject

// We have to define the type explicity
val name = dynamicObject.get<String>("name")


// We have to define the type explicity
val name: String = dynamicObject.get("name")

// Exception on mismatching types
assertFailsWith<IllegalStateException> {
    dynamicObject.get<Float>("name")
}

assertFailsWith<IllegalStateException> {
    val name: Float = dynamicObject.get("name")
}

// complex types
val myList: RealmList<DynamicRealmObject> = dynamicObject.get("myList")
val floatList: RealmList<Float> = dynamicObject.get("floatList")
val floatList2 = dynamicObject.get<RealmList<Float>>("floatList")

// custom type adapters
val birthDate: Date = dynamicRealmObject.get("birthdate")
```