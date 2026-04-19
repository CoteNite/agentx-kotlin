package cn.cotenite.infrastructure.highavailability.dto.request

class SelectInstanceRequest(
    var userId: String? = null,
    var apiIdentifier: String? = null,
    var apiType: String? = null,
    var affinityKey: String? = null,
    var affinityType: String? = null,
    var fallbackChain: List<String>? = null
)
