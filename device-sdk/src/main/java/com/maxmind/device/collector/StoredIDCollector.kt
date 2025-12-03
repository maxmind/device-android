package com.maxmind.device.collector

import com.maxmind.device.model.StoredID
import com.maxmind.device.storage.StoredIDStorage

/**
 * Collects the server-generated stored ID from local storage.
 *
 * This collector retrieves the stored ID that was previously received
 * from the server and saved to SharedPreferences.
 */
internal class StoredIDCollector(
    private val storage: StoredIDStorage,
) {
    /**
     * Collects the stored ID from local storage.
     *
     * @return [StoredID] containing the ID if available, or empty StoredID if not
     */
    fun collect(): StoredID = StoredID(id = storage.get())
}
