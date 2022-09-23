/*
 * Copyright 2022 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.kotlin.internal

import io.realm.kotlin.ext.isManaged
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.types.RealmLinkingObjects
import io.realm.kotlin.types.RealmObject
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

internal class RealmLinkingObjectsImpl<T : RealmObject>(
    private val targetProperty: KProperty1<T, *>,
    private val targetClass: KClass<T>
) : RealmLinkingObjects<T> {
    override fun getValue(
        reference: RealmObject,
        referenceProperty: KProperty<*>
    ): RealmResults<T> {
        if (!reference.isManaged()) {
            // throw if unmanaged
        }

        // TODO PROGUARD would break here ask claus, has a solution
        val targetClassName = targetClass.simpleName!!
        val targetPropertyName = targetProperty.name
        val referencePropertyName = referenceProperty.name

        return RealmObjectHelper.getLinkingObjects(
            obj = reference.realmObjectReference!!,
            propertyName = referencePropertyName,
            targetProperty = targetPropertyName,
            targetClass = targetClassName
        )
    }
}