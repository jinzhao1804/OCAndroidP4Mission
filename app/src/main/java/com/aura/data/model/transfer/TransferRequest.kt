package com.aura.data.model.transfer

data class TransferRequest(
    val sender: String,
    val recipient: String,
    val amount: Double
)
