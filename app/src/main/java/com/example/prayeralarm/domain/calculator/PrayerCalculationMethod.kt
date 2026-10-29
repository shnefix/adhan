package com.example.prayeralarm.domain.calculator

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.Madhab

/**
 * Named prayer-time calculation convention.
 *
 * Keeping the convention in one explicit place avoids scattering magic angles
 * across the UI, alarm scheduler, and tests. [TUNISIA] preserves the convention
 * that was already configured in this project before the accuracy audit:
 *
 * - Fajr: 18.0 degrees below the horizon
 * - Isha: 18.0 degrees below the horizon
 * - Asr: standard shadow factor (Shafi'i / Maliki / Hanbali / Hanafi-neutral)
 * - High-latitude rule: none (Tunisia is far from the polar circles)
 *
 * This is a named implementation convention, not a claim that it is the only
 * timetable used in Tunisia. See `docs/PRAYER_TIME_CALCULATION.md` for the
 * reference comparison and known differences.
 */
enum class PrayerCalculationMethod(
    val displayName: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val madhab: Madhab,
    val summary: String
) {
    TUNISIA(
        displayName = "Tunisia (18°/18°, standard Asr)",
        fajrAngle = 18.0,
        ishaAngle = 18.0,
        madhab = Madhab.SHAFI,
        summary = "Fajr 18°, Isha 18°, Shafi'i/Maliki standard Asr shadow factor"
    );

    /**
     * Builds a fresh Adhan parameter object for this convention.
     *
     * A new object is returned on every call so callers cannot accidentally
     * mutate the shared convention definition.
     */
    fun createParameters(): CalculationParameters {
        val parameters = CalculationMethod.OTHER.parameters
        parameters.fajrAngle = fajrAngle
        parameters.ishaAngle = ishaAngle
        parameters.madhab = madhab
        return parameters
    }

    companion object {
        /** Convention used by the app unless a caller explicitly supplies another. */
        val DEFAULT: PrayerCalculationMethod = TUNISIA
    }
}
