package com.example.prayeralarm.ui.alarms

import com.example.prayeralarm.domain.alarm.AlarmPlanConfig
import com.example.prayeralarm.domain.alarm.AlarmTimingMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test

class AlarmPlanViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        kotlinx.coroutines.Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun defaults_areDocumentedAndRestored() = runTest {
        val store = FakeAlarmPlanPreferences()
        val viewModel = AlarmPlanViewModel(store)

        assertEquals(AlarmPlanConfig.DEFAULT, viewModel.config.value)
        viewModel.setEnabled(true)
        viewModel.setAlarmCount(3)
        viewModel.setIntervalMinutes(15)
        viewModel.setTimingMode(AlarmTimingMode.BEFORE_FAJR)

        testScheduler.advanceUntilIdle()
        assertEquals(viewModel.config.value, store.config.value)
    }

    @Test
    fun invalidValues_areNotPersisted() = runTest {
        val store = FakeAlarmPlanPreferences()
        val viewModel = AlarmPlanViewModel(store)

        viewModel.setAlarmCount(0)
        viewModel.setIntervalMinutes(0)
        testScheduler.advanceUntilIdle()

        assertEquals(AlarmPlanConfig.DEFAULT, store.config.value)
    }
}

private class FakeAlarmPlanPreferences : AlarmPlanPreferences {
    override val config = MutableStateFlow(AlarmPlanConfig.DEFAULT)
    override suspend fun setConfig(config: AlarmPlanConfig) {
        this.config.value = config
    }
}
