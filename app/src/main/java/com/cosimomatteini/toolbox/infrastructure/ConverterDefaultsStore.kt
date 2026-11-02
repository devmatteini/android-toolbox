package com.cosimomatteini.toolbox.infrastructure

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cosimomatteini.toolbox.domain.AreaUnit
import com.cosimomatteini.toolbox.domain.ConverterCategory
import com.cosimomatteini.toolbox.domain.LengthUnit
import com.cosimomatteini.toolbox.domain.MassUnit
import com.cosimomatteini.toolbox.domain.SpeedUnit
import com.cosimomatteini.toolbox.domain.TemperatureUnit
import com.cosimomatteini.toolbox.domain.VolumeUnit
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

data class DefaultUnitIds(val source: String, val target: String)

data class ConverterDefaults(val values: Map<ConverterCategory, DefaultUnitIds>) {
    operator fun get(category: ConverterCategory): DefaultUnitIds = values.getValue(category)
}

enum class DefaultSide { Source, Target }

interface ConverterDefaultsStore {
    val defaults: Flow<ConverterDefaults>

    suspend fun update(category: ConverterCategory, side: DefaultSide, unitId: String)
}

val initialConverterDefaults = ConverterDefaults(
    mapOf(
        ConverterCategory.Length to DefaultUnitIds(
            LengthUnit.Meter.name,
            LengthUnit.Kilometer.name
        ),
        ConverterCategory.Currency to DefaultUnitIds("EUR", "USD"),
        ConverterCategory.Speed to DefaultUnitIds(
            SpeedUnit.MetersPerSecond.name,
            SpeedUnit.KilometersPerHour.name
        ),
        ConverterCategory.Temperature to DefaultUnitIds(
            TemperatureUnit.Celsius.name,
            TemperatureUnit.Fahrenheit.name
        ),
        ConverterCategory.Mass to DefaultUnitIds(MassUnit.Gram.name, MassUnit.Kilogram.name),
        ConverterCategory.Volume to
            DefaultUnitIds(VolumeUnit.Millilitre.name, VolumeUnit.Litre.name),
        ConverterCategory.Area to
            DefaultUnitIds(AreaUnit.SquareMeter.name, AreaUnit.SquareKilometer.name)
    )
)

private val Context.converterDefaultsDataStore by preferencesDataStore(name = "converter_defaults")

class DataStoreConverterDefaultsStore(private val dataStore: DataStore<Preferences>) :
    ConverterDefaultsStore {
    constructor(context: Context) : this(context.converterDefaultsDataStore)

    override val defaults: Flow<ConverterDefaults> = dataStore.data.map { preferences ->
        ConverterDefaults(
            initialConverterDefaults.values.mapValues { (category, initial) ->
                DefaultUnitIds(
                    source = preferences[key(category, DefaultSide.Source)] ?: initial.source,
                    target = preferences[key(category, DefaultSide.Target)] ?: initial.target
                ).validated(category)
            }
        )
    }.catch { error ->
        if (error is IOException) emit(initialConverterDefaults) else throw error
    }

    override suspend fun update(category: ConverterCategory, side: DefaultSide, unitId: String) {
        val validId = unitId.validated(category, side)
        dataStore.edit { preferences -> preferences[key(category, side)] = validId }
    }

    private fun key(category: ConverterCategory, side: DefaultSide) =
        stringPreferencesKey("${category.name.lowercase()}_${side.name.lowercase()}")
}

private fun DefaultUnitIds.validated(category: ConverterCategory): DefaultUnitIds = DefaultUnitIds(
    source = source.validated(category, DefaultSide.Source),
    target = target.validated(category, DefaultSide.Target)
)

private fun String.validated(category: ConverterCategory, side: DefaultSide): String {
    if (category == ConverterCategory.Currency || category.hasUnitId(this)) return this
    val initial = initialConverterDefaults[category]
    return if (side == DefaultSide.Source) initial.source else initial.target
}

private fun ConverterCategory.hasUnitId(unitId: String): Boolean = when (this) {
    ConverterCategory.Length -> LengthUnit.entries.any { it.name == unitId }
    ConverterCategory.Speed -> SpeedUnit.entries.any { it.name == unitId }
    ConverterCategory.Temperature -> TemperatureUnit.entries.any { it.name == unitId }
    ConverterCategory.Mass -> MassUnit.entries.any { it.name == unitId }
    ConverterCategory.Volume -> VolumeUnit.entries.any { it.name == unitId }
    ConverterCategory.Area -> AreaUnit.entries.any { it.name == unitId }
    ConverterCategory.Currency -> true
}
