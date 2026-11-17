package com.example

import com.example.model.MotorConnectionOptions
import com.example.model.SinglePhaseMotor
import com.example.model.ThreePhaseMotor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MotorNewFieldsWorkflowTest {

    @Test
    fun legacyMotorRecordsWithoutNewFields_defaultSafelyToEmptyStrings() {
        val legacySingle = SinglePhaseMotor(
            id = "legacy_sp_1",
            category = "Submersible Motor",
            companyName = "Crompton",
            hp = "1 HP",
            capacitor = "36 mfd",
            runningPitch = "1-8-10",
            runningTurn = "45",
            runningSwg = "21",
            runningWeight = "450g",
            startingPitch = "1-10",
            startingTurn = "60",
            startingSwg = "23",
            startingWeight = "250g",
            status = "approved"
        )

        val legacyThree = ThreePhaseMotor(
            id = "legacy_tp_1",
            category = "Monoblock",
            name = "Kirloskar",
            slot = "24",
            hp = "5 HP",
            pitch = "1-10-12",
            turn = "32",
            swg = "19",
            weight = "2.4 kg",
            status = "approved"
        )

        assertEquals("", legacySingle.rpm)
        assertEquals("", legacySingle.amps)
        assertEquals("", legacySingle.coilSize)
        assertEquals("", legacySingle.connection)
        assertEquals("Crompton", legacySingle.companyName)
        assertEquals("36 mfd", legacySingle.capacitor)

        assertEquals("", legacyThree.rpm)
        assertEquals("", legacyThree.amps)
        assertEquals("", legacyThree.coilSize)
        assertEquals("", legacyThree.connection)
        assertEquals("Kirloskar", legacyThree.name)
        assertEquals("24", legacyThree.slot)
    }

    @Test
    fun connectionDropdownOptions_containExpectedChoices() {
        assertEquals(
            listOf("Star", "Delta", "Star-Delta", "Series", "Parallel", "Other"),
            MotorConnectionOptions.DROPDOWN_OPTIONS
        )
    }

    @Test
    fun connectionSaveAndEditResolution_handlesPresetsAndCustomOther() {
        assertEquals("Star", MotorConnectionOptions.resolveForSave("Star", ""))
        assertEquals("Star-Delta", MotorConnectionOptions.resolveForSave("Star-Delta", "ignored"))
        assertEquals("2-Pole Parallel", MotorConnectionOptions.resolveForSave("Other", "  2-Pole Parallel  "))
        assertEquals("", MotorConnectionOptions.resolveForSave("", ""))

        assertEquals("Delta" to "", MotorConnectionOptions.resolveSavedSelection("delta"))
        assertEquals("Parallel" to "", MotorConnectionOptions.resolveSavedSelection("Parallel"))
        assertEquals("Other" to "2-Pole Parallel", MotorConnectionOptions.resolveSavedSelection("2-Pole Parallel"))
        assertEquals("" to "", MotorConnectionOptions.resolveSavedSelection(""))
    }

    @Test
    fun publicSubmissionApproval_preservesAllNewAndExistingFields() {
        val publicSingle = SinglePhaseMotor(
            id = "sp_pub_1",
            category = "Pandubi",
            companyName = "Havells",
            hp = "1.5 HP",
            capacitor = "50 mfd",
            rpm = "1440",
            amps = "6.8",
            coilSize = "22 SWG + 24 SWG",
            connection = "Series",
            runningPitch = "1-8-10-12",
            runningTurn = "40",
            runningSwg = "22",
            runningWeight = "520g",
            startingPitch = "1-10-12",
            startingTurn = "55",
            startingSwg = "24",
            startingWeight = "300g",
            status = "pending",
            createdBy = "public_uid_1",
            createdByName = "Technician A"
        )

        val approvedSingle = publicSingle.copy(
            status = "approved",
            reviewedBy = "admin_uid_1",
            reviewedByName = "Admin",
            adminSeen = true,
            seen = true
        )

        assertTrue(approvedSingle.isExplicitlyApproved())
        assertEquals("1440", approvedSingle.rpm)
        assertEquals("6.8", approvedSingle.amps)
        assertEquals("22 SWG + 24 SWG", approvedSingle.coilSize)
        assertEquals("Series", approvedSingle.connection)
        assertEquals("Havells", approvedSingle.companyName)

        val publicThree = ThreePhaseMotor(
            id = "tp_pub_1",
            category = "Submersible",
            name = "Texmo",
            slot = "36",
            hp = "7.5 HP",
            rpm = "2880",
            amps = "11.2",
            coilSize = "1.1 mm",
            connection = "Star-Delta",
            pitch = "1-10-12",
            turn = "28",
            swg = "18",
            weight = "3.5 kg",
            status = "pending",
            createdBy = "public_uid_1"
        )

        val approvedThree = publicThree.copy(status = "approved", adminSeen = true, seen = true)
        assertTrue(approvedThree.isExplicitlyApproved())
        assertEquals("2880", approvedThree.rpm)
        assertEquals("11.2", approvedThree.amps)
        assertEquals("1.1 mm", approvedThree.coilSize)
        assertEquals("Star-Delta", approvedThree.connection)
    }

    @Test
    fun searchAndConnectionFilters_matchNewFieldsAndHandleLegacyRecordsSafely() {
        val legacySingle = SinglePhaseMotor(id = "sp_old", companyName = "Usha", hp = "0.5 HP", status = "approved")
        val newSingle = SinglePhaseMotor(
            id = "sp_new",
            companyName = "Crompton",
            hp = "2 HP",
            rpm = "1440",
            amps = "8.2",
            coilSize = "21 SWG",
            connection = "Parallel",
            status = "approved"
        )
        val customConnThree = ThreePhaseMotor(
            id = "tp_custom",
            name = "ABB",
            slot = "24",
            hp = "10 HP",
            rpm = "960",
            amps = "15.0",
            coilSize = "17 SWG Dual",
            connection = "Double Delta",
            status = "approved"
        )

        // Search matches RPM, Amps, Coil Size, Connection
        assertTrue(newSingle.matchesSearch("1440"))
        assertTrue(newSingle.matchesSearch("8.2"))
        assertTrue(newSingle.matchesSearch("21 SWG"))
        assertTrue(newSingle.matchesSearch("parallel"))
        assertFalse(legacySingle.matchesSearch("1440"))

        assertTrue(customConnThree.matchesSearch("960"))
        assertTrue(customConnThree.matchesSearch("15.0"))
        assertTrue(customConnThree.matchesSearch("17 SWG"))
        assertTrue(customConnThree.matchesSearch("Double Delta"))

        // Connection filters
        assertTrue(MotorConnectionOptions.matchesConnectionFilter(legacySingle.connection, "All"))
        assertFalse(MotorConnectionOptions.matchesConnectionFilter(legacySingle.connection, "Star"))
        assertFalse(MotorConnectionOptions.matchesConnectionFilter(legacySingle.connection, "Other"))

        assertTrue(MotorConnectionOptions.matchesConnectionFilter(newSingle.connection, "Parallel"))
        assertFalse(MotorConnectionOptions.matchesConnectionFilter(newSingle.connection, "Delta"))

        assertTrue(MotorConnectionOptions.matchesConnectionFilter(customConnThree.connection, "Other"))
        assertFalse(MotorConnectionOptions.matchesConnectionFilter(newSingle.connection, "Other"))
    }
}
