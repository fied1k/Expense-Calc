package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CostCalculatorTest {

    @Test
    fun testParseCleanDouble() {
        assertEquals(12.5, parseCleanDouble("12.5")!!, 0.001)
        assertEquals(1234.56, parseCleanDouble("$1,234.56")!!, 0.001)
        assertEquals(0.0, parseCleanDouble("0")!!, 0.001)
        assertEquals(0.0, parseCleanDouble("-15.0")!!, 0.001)
        assertNull(parseCleanDouble(null))
        assertNull(parseCleanDouble(""))
        assertNull(parseCleanDouble("   "))
        assertNull(parseCleanDouble("invalid"))
    }

    @Test
    fun testRoundUpToHalf() {
        assertEquals(0.0, roundUpToHalf(0.0), 0.001)
        assertEquals(0.0, roundUpToHalf(-5.0), 0.001)
        assertEquals(0.5, roundUpToHalf(0.1), 0.001)
        assertEquals(0.5, roundUpToHalf(0.5), 0.001)
        assertEquals(1.0, roundUpToHalf(0.6), 0.001)
        assertEquals(1.5, roundUpToHalf(1.1), 0.001)
        assertEquals(2.0, roundUpToHalf(2.0), 0.001)
    }

    @Test
    fun testFormatHalf() {
        assertEquals("", formatHalf(0.0))
        assertEquals("0.5", formatHalf(0.1))
        assertEquals("0.5", formatHalf(0.5))
        assertEquals("1", formatHalf(0.6))
        assertEquals("1.5", formatHalf(1.2))
        assertEquals("2", formatHalf(2.0))
    }

    @Test
    fun testFormatCleanAddress() {
        val addressFull = NominatimAddress(
            house_number = "100",
            road = "Main St",
            city = "Kansas City",
            state = "MO",
            postcode = "64101",
            country = "United States"
        )
        val clean1 = formatCleanAddress("100 Main St, Kansas City, MO 64101, United States", addressFull)
        assertEquals("100 Main St, Kansas City, MO 64101", clean1)

        val rawDisplayName = "Springfield, Greene County, Missouri, USA"
        val clean2 = formatCleanAddress(rawDisplayName, null)
        assertEquals("Springfield, Missouri", clean2)
    }

    @Test
    fun testViewModelStateUpdates() {
        val viewModel = CostCalculatorViewModel()

        assertEquals("", viewModel.state.value.startQuery)
        viewModel.updateStartQuery("Overland Park")
        assertEquals("Overland Park", viewModel.state.value.startQuery)

        viewModel.updateEndQuery("Kansas City")
        assertEquals("Kansas City", viewModel.state.value.endQuery)

        viewModel.updateMiles("25.3")
        assertEquals("25.3", viewModel.state.value.baseMiles)
        viewModel.formatMilesOnBlur()
        assertEquals("25.5", viewModel.state.value.baseMiles)

        viewModel.updateHours("1.1")
        assertEquals("1.1", viewModel.state.value.baseHours)
        viewModel.formatHoursOnBlur()
        assertEquals("1.5", viewModel.state.value.baseHours)
    }

    @Test
    fun testViewModelExpenses() {
        val viewModel = CostCalculatorViewModel()
        assertTrue(viewModel.state.value.additionalExpenses.isEmpty())

        viewModel.addExpense()
        assertEquals(1, viewModel.state.value.additionalExpenses.size)
        assertEquals("", viewModel.state.value.additionalExpenses[0].name)

        viewModel.updateExpense(0, "Tolls", "5.50")
        assertEquals("Tolls", viewModel.state.value.additionalExpenses[0].name)
        assertEquals("5.50", viewModel.state.value.additionalExpenses[0].cost)

        viewModel.removeExpense(0)
        assertTrue(viewModel.state.value.additionalExpenses.isEmpty())
    }
}
