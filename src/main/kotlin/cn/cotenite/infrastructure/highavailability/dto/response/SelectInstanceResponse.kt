package cn.cotenite.infrastructure.highavailability.dto.response

class SelectInstanceResponse(
    var instanceId: String? = null,
    var businessId: String? = null,
    var reason: String? = null,
    var success: Boolean? = null,
    var errorMessage: String? = null
) {
    constructor(instanceId: String, businessId: String, reason: String) : this(
        instanceId = instanceId,
        businessId = businessId,
        reason = reason,
        success = true
    )
}
