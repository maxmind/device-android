package com.maxmind.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Telephony context information from TelephonyManager.
 */
@Serializable
public data class TelephonyInfo(
    @SerialName("network_operator_name")
    val networkOperatorName: String? = null,
    @SerialName("sim_state")
    val simState: Int? = null,
    @SerialName("phone_type")
    val phoneType: Int? = null,
    @SerialName("has_icc_card")
    val hasIccCard: Boolean? = null,
)
