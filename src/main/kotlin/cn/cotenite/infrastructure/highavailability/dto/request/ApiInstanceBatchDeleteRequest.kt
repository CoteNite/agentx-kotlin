package cn.cotenite.infrastructure.highavailability.dto.request

class ApiInstanceBatchDeleteRequest(
    var instances: List<ApiInstanceDeleteItem>? = null
) {
    class ApiInstanceDeleteItem(
        var apiType: String? = null,
        var businessId: String? = null
    )
}
