package com.maxmind.device.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Manages persistent storage of server-generated stored IDs.
 *
 * Uses SharedPreferences for storage, similar to how the JS implementation
 * uses cookies and localStorage. The stored ID is server-generated and
 * includes an HMAC signature for validation.
 */
internal class StoredIDStorage(
    context: Context,
) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE,
        )

    /**
     * Retrieves the stored ID.
     *
     * @return The stored ID string, or null if not set
     */
    fun get(): String? = prefs.getString(KEY_STORED_ID, null)

    /**
     * Saves a stored ID.
     *
     * @param id The stored ID to save (format: "{uuid}:{hmac}")
     */
    fun save(id: String) {
        prefs.edit { putString(KEY_STORED_ID, id) }
    }

    /**
     * Clears the stored ID.
     */
    fun clear() {
        prefs.edit { remove(KEY_STORED_ID) }
    }

    internal companion object {
        internal const val PREFS_NAME = "com.maxmind.device.storage"
        internal const val KEY_STORED_ID = "stored_id"
    }
}
