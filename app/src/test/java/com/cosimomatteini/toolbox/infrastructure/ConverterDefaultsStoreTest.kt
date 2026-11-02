package com.cosimomatteini.toolbox.infrastructure

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.cosimomatteini.toolbox.domain.ConverterCategory
import com.cosimomatteini.toolbox.domain.LengthUnit
import com.cosimomatteini.toolbox.domain.SpeedUnit
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ConverterDefaultsStoreTest {
    @Test
    fun `initial pairs match converter defaults`() = runTest {
        withStore { store, _ ->
            assertEquals(initialConverterDefaults, store.defaults.first())
        }
    }

    @Test
    fun `round trips updates without changing other values`() = runTest {
        withStore { store, _ ->
            store.update(ConverterCategory.Length, DefaultSide.Source, LengthUnit.Foot.name)

            val defaults = store.defaults.first()
            assertEquals(LengthUnit.Foot.name, defaults[ConverterCategory.Length].source)
            assertEquals(LengthUnit.Kilometer.name, defaults[ConverterCategory.Length].target)
            assertEquals(
                initialConverterDefaults[ConverterCategory.Speed],
                defaults[ConverterCategory.Speed]
            )
        }
    }

    @Test
    fun `unknown persisted identifiers fall back by side`() = runTest {
        withStore { store, dataStore ->
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey("length_source")] = "removed-source"
                preferences[stringPreferencesKey("speed_target")] = "removed-target"
            }

            val defaults = store.defaults.first()
            assertEquals(LengthUnit.Meter.name, defaults[ConverterCategory.Length].source)
            assertEquals(
                SpeedUnit.KilometersPerHour.name,
                defaults[ConverterCategory.Speed].target
            )
        }
    }

    private suspend fun withStore(
        block: suspend (
            DataStoreConverterDefaultsStore,
            androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>
        ) -> Unit
    ) {
        val file = File.createTempFile("converter-defaults", ".preferences_pb").apply { delete() }
        val dataStore = PreferenceDataStoreFactory.create { file }
        try {
            block(DataStoreConverterDefaultsStore(dataStore), dataStore)
        } finally {
            file.delete()
        }
    }
}
