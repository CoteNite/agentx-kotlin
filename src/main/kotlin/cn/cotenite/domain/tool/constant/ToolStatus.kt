package cn.cotenite.domain.tool.constant

import java.util.Set

/**
 * @author  yhk
 * Description  
 * Date  2026/4/6 20:48
 */
enum class ToolStatus {

    WAITING_REVIEW, // 等待审核,

    GITHUB_URL_VALIDATE, // GitHub URL 验证中,

    DEPLOYING, // （原）部署中 - 根据新流程，此状态可能调整或移除，暂时保留,

    FETCHING_TOOLS, // （原）获取工具中 - 根据新流程，此状态可能调整或移除，暂时保留,

    MANUAL_REVIEW, // 人工审核,

    APPROVED, // 已通过,

    FAILED  // 通用失败状态;
    ;

    companion object{

        /** 根据名称获取工具状态枚举。
         *
         * @param name 状态名称
         * @return 对应的工具状态枚举
         * @throws BusinessException 如果找不到对应的状态
         */
        fun fromCode(name: String): ToolStatus? {
            for (status in entries) {
                if (status.name.equals(name, ignoreCase = true)) {
                    return status
                }
            }
            return null
        }

    }

}