package cn.cotenite.infrastructure.highavailability.dto.response

class GatewayResult<T>(
    code: Int? = null,
    var message: String? = null,
    var data: T? = null
) {
    var code: Int? = code
        set(value) {
            field = value
            success = value == 200
        }

    var success: Boolean? = code == 200

    fun isSuccess(): Boolean = success == true
}
