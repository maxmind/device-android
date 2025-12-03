package com.maxmind.device.storage

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class StoredIDStorageTest {
    private lateinit var mockContext: Context
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var storage: StoredIDStorage

    @BeforeEach
    internal fun setUp() {
        mockContext = mockk(relaxed = true)
        mockPrefs = mockk(relaxed = true)
        mockEditor = mockk(relaxed = true)

        every { mockContext.applicationContext } returns mockContext
        every {
            mockContext.getSharedPreferences(StoredIDStorage.PREFS_NAME, Context.MODE_PRIVATE)
        } returns mockPrefs
        every { mockPrefs.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.remove(any()) } returns mockEditor
        every { mockEditor.apply() } returns Unit

        storage = StoredIDStorage(mockContext)
    }

    @Test
    internal fun `get returns null when no stored ID exists`() {
        every { mockPrefs.getString(StoredIDStorage.KEY_STORED_ID, null) } returns null

        val result = storage.get()

        assertNull(result)
    }

    @Test
    internal fun `get returns stored ID when it exists`() {
        val expectedId = "test-uuid:test-hmac"
        every { mockPrefs.getString(StoredIDStorage.KEY_STORED_ID, null) } returns expectedId

        val result = storage.get()

        assertEquals(expectedId, result)
    }

    @Test
    internal fun `save stores the ID in SharedPreferences`() {
        val idToSave = "new-uuid:new-hmac"
        val keySlot = slot<String>()
        val valueSlot = slot<String>()
        every { mockEditor.putString(capture(keySlot), capture(valueSlot)) } returns mockEditor

        storage.save(idToSave)

        assertEquals(StoredIDStorage.KEY_STORED_ID, keySlot.captured)
        assertEquals(idToSave, valueSlot.captured)
        verify { mockEditor.apply() }
    }

    @Test
    internal fun `clear removes the stored ID from SharedPreferences`() {
        val keySlot = slot<String>()
        every { mockEditor.remove(capture(keySlot)) } returns mockEditor

        storage.clear()

        assertEquals(StoredIDStorage.KEY_STORED_ID, keySlot.captured)
        verify { mockEditor.apply() }
    }

    @Test
    internal fun `uses correct SharedPreferences name`() {
        assertEquals("com.maxmind.device.storage", StoredIDStorage.PREFS_NAME)
    }

    @Test
    internal fun `uses correct key for stored ID`() {
        assertEquals("stored_id", StoredIDStorage.KEY_STORED_ID)
    }
}
