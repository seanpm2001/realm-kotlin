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

package io.realm.kotlin.ext

import io.realm.kotlin.internal.ManagedRealmList
import io.realm.kotlin.internal.UnmanagedRealmList
import io.realm.kotlin.internal.asRealmList
import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.types.BaseRealmObject
import io.realm.kotlin.types.RealmList

/**
 * Instantiates an **unmanaged** [RealmList].
 */
public fun <T> realmListOf(vararg elements: T): RealmList<T> =
    if (elements.isNotEmpty()) elements.asRealmList() else UnmanagedRealmList()

/**
 * Returns a [RealmQuery] matching the predicate represented by [query].
 *
 * @param query the Realm Query Language predicate to append.
 * @param args Realm values for the predicate.
 */
@Suppress("unchecked")
public fun <E : BaseRealmObject> RealmList<E>.query(query: String, vararg args: Any?): RealmQuery<E> {
    when (this) {
        is ManagedRealmList -> {
            return this.objectQuery(query, *args) as RealmQuery<E>
        }
        else -> throw IllegalStateException("Can't query on unmanaged objects")
    }
}
