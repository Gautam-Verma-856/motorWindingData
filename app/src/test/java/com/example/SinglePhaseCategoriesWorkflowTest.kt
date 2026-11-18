package com.example

import com.example.model.MotorConnectionOptions
import com.example.model.SinglePhaseCategories
import com.example.model.SinglePhaseMotor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SinglePhaseCategoriesWorkflowTest {

    @Test
    fun singlePhaseOptions_preserveFirstThreeAndAddNewThreeInExactOrder() {
        assertEquals(
            listOf(
                "Self Priming (Tullu Pump)",
                "Submersible Motor",
                "Pandubi",
                "Cooler Motor / Exhaust Motor",
                "Washing Machine Motor",
                "Other"
            ),
            SinglePhaseCategories.ALL_OPTIONS
        )
    }

    @Test
    fun categoryMatching_handlesAllSixOptionsAndLegacyOtherRecords() {
        val motors = listOf(
            SinglePhaseMotor(id = "1", category = "Self Priming (Tullu Pump)", companyName = "Crompton", status = "approved"),
            SinglePhaseMotor(id = "2", category = "Submersible Motor", companyName = "Texmo", status = "approved"),
            SinglePhaseMotor(id = "3", category = "Pandubi", companyName = "Lubhi", status = "approved"),
            SinglePhaseMotor(id = "4", category = "Cooler Motor / Exhaust Motor", companyName = "Usha", status = "approved"),
            SinglePhaseMotor(id = "5", category = "Washing Machine Motor", companyName = "LG", status = "approved"),
            SinglePhaseMotor(id = "6", category = "Other", companyName = "Orient", status = "approved"),
            SinglePhaseMotor(id = "7", category = "General / Other Motors", companyName = "LegacyCo", status = "approved"),
            SinglePhaseMotor(id = "8", category = "", companyName = "OldBlank", status = "approved")
        )

        assertEquals(listOf("1"), motors.filter { SinglePhaseCategories.matchesCategory(it.category, SinglePhaseCategories.SELF_PRIMING) }.map { it.id })
        assertEquals(listOf("2"), motors.filter { SinglePhaseCategories.matchesCategory(it.category, SinglePhaseCategories.SUBMERSIBLE) }.map { it.id })
        assertEquals(listOf("3"), motors.filter { SinglePhaseCategories.matchesCategory(it.category, SinglePhaseCategories.PANDUBI) }.map { it.id })
        assertEquals(listOf("4"), motors.filter { SinglePhaseCategories.matchesCategory(it.category, SinglePhaseCategories.COOLER_EXHAUST) }.map { it.id })
        assertEquals(listOf("5"), motors.filter { SinglePhaseCategories.matchesCategory(it.category, SinglePhaseCategories.WASHING_MACHINE) }.map { it.id })
        assertEquals(listOf("6", "7", "8"), motors.filter { SinglePhaseCategories.matchesCategory(it.category, SinglePhaseCategories.OTHER) }.map { it.id })
    }

    @Test
    fun resolveEditableCategory_mapsAllCategoriesAndLegacyValuesProperly() {
        assertEquals("Self Priming (Tullu Pump)", SinglePhaseCategories.resolveEditableCategory("Self Priming (Tullu Pump)"))
        assertEquals("Submersible Motor", SinglePhaseCategories.resolveEditableCategory("Submersible Motor"))
        assertEquals("Pandubi", SinglePhaseCategories.resolveEditableCategory("Pandubi"))
        assertEquals("Cooler Motor / Exhaust Motor", SinglePhaseCategories.resolveEditableCategory("cooler motor / exhaust motor"))
        assertEquals("Washing Machine Motor", SinglePhaseCategories.resolveEditableCategory("Washing Machine Motor"))
        assertEquals("Other", SinglePhaseCategories.resolveEditableCategory("Other"))
        assertEquals("Other", SinglePhaseCategories.resolveEditableCategory("General / Other Motors"))
        assertEquals("Other", SinglePhaseCategories.resolveEditableCategory(""))
    }

    @Test
    fun publicSubmissionApprovalAndSearchFilter_workAcrossNewSinglePhaseCategories() {
        val pendingCooler = SinglePhaseMotor(
            id = "cooler_1",
            category = SinglePhaseCategories.COOLER_EXHAUST,
            companyName = "Symphony",
            hp = "0.25 HP",
            capacitor = "4 mfd",
            rpm = "1440",
            amps = "1.2",
            coilSize = "30 SWG",
            connection = "Parallel",
            runningPitch = "1-4-6",
            runningTurn = "120",
            runningSwg = "30",
            runningWeight = "220g",
            startingPitch = "1-4-6",
            startingTurn = "140",
            startingSwg = "32",
            startingWeight = "180g",
            status = "pending",
            createdBy = "pub_1"
        )
        val approvedWashing = SinglePhaseMotor(
            id = "wash_1",
            category = SinglePhaseCategories.WASHING_MACHINE,
            companyName = "Samsung",
            hp = "0.5 HP",
            capacitor = "10 mfd",
            rpm = "1380",
            amps = "2.1",
            coilSize = "27 SWG",
            connection = "Series",
            status = "approved",
            createdBy = "admin_1"
        )

        // Before approval, public list in Cooler/Exhaust only shows explicitly approved records
        val publicCoolerBeforeApproval = listOf(pendingCooler, approvedWashing)
            .filter { it.isExplicitlyApproved() && SinglePhaseCategories.matchesCategory(it.category, SinglePhaseCategories.COOLER_EXHAUST) }
        assertTrue(publicCoolerBeforeApproval.isEmpty())

        // Admin approves pendingCooler
        val approvedCooler = pendingCooler.copy(status = "approved", adminSeen = true, seen = true)
        val publicCoolerAfterApproval = listOf(approvedCooler, approvedWashing)
            .filter { it.isExplicitlyApproved() && SinglePhaseCategories.matchesCategory(it.category, SinglePhaseCategories.COOLER_EXHAUST) }
        assertEquals(1, publicCoolerAfterApproval.size)
        assertEquals("1440", publicCoolerAfterApproval.first().rpm)
        assertEquals("1.2", publicCoolerAfterApproval.first().amps)
        assertEquals("30 SWG", publicCoolerAfterApproval.first().coilSize)
        assertEquals("Parallel", publicCoolerAfterApproval.first().connection)

        // Search and Connection filters work within categories
        assertTrue(approvedCooler.matchesSearch("Symphony"))
        assertTrue(approvedCooler.matchesSearch("1440"))
        assertTrue(approvedCooler.matchesSearch("30 SWG"))
        assertTrue(MotorConnectionOptions.matchesConnectionFilter(approvedCooler.connection, "Parallel"))
        assertFalse(MotorConnectionOptions.matchesConnectionFilter(approvedWashing.connection, "Parallel"))
        assertTrue(MotorConnectionOptions.matchesConnectionFilter(approvedWashing.connection, "Series"))
    }
}
