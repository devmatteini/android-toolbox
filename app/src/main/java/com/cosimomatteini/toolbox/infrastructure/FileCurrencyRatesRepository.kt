package com.cosimomatteini.toolbox.infrastructure

import com.cosimomatteini.toolbox.currencyrates.CURRENCY_RATES_FILE_NAME
import com.cosimomatteini.toolbox.currencyrates.CurrencyRates
import com.cosimomatteini.toolbox.currencyrates.CurrencyRatesCodec
import com.cosimomatteini.toolbox.domain.CurrencyRatesRepository
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class FileCurrencyRatesRepository(
    private val directory: File,
    private val writeAtomically: (File, String) -> Unit = ::writeAtomically
) : CurrencyRatesRepository {
    private val ratesFile = File(directory, CURRENCY_RATES_FILE_NAME)

    override fun load(): CurrencyRates? = runCatching {
        CurrencyRatesCodec.decode(ratesFile.readText(StandardCharsets.UTF_8))
    }.getOrNull()

    override fun save(rates: CurrencyRates) {
        writeAtomically(ratesFile, CurrencyRatesCodec.encode(rates))
    }

    private companion object {
        fun writeAtomically(file: File, value: String) {
            file.parentFile?.mkdirs()
            val temporary = File.createTempFile("${file.name}.", ".tmp", file.parentFile)
            try {
                temporary.writeText(value, StandardCharsets.UTF_8)
                try {
                    Files.move(
                        temporary.toPath(),
                        file.toPath(),
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING
                    )
                } catch (_: AtomicMoveNotSupportedException) {
                    Files.move(
                        temporary.toPath(),
                        file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                    )
                }
            } finally {
                temporary.delete()
            }
        }
    }
}
