package com.example.model

data class SinglePhaseMotor(
    val id: String = "",
    val photoUrl: String = "",
    val companyName: String = "",
    val hp: String = "",
    val capacitor: String = "",
    val runningPitch: String = "",
    val runningTurn: String = "",
    val runningSwg: String = "",
    val runningWeight: String = "",
    val startingPitch: String = "",
    val startingTurn: String = "",
    val startingSwg: String = "",
    val startingWeight: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = "",
    val status: String = "approved"
)

data class ThreePhaseMotor(
    val id: String = "",
    val photoUrl: String = "",
    val name: String = "",
    val slot: String = "",
    val hp: String = "",
    val pitch: String = "",
    val turn: String = "",
    val swg: String = "",
    val weight: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = "",
    val status: String = "approved"
)
