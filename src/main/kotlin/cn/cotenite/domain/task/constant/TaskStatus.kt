package cn.cotenite.domain.task.constant

/**
 * @author  yhk
 * Description  
 * Date  2026/4/1 18:30
 */
enum class TaskStatus {

    /**
     * 等待中
     */
    WAITING,

    /**
     * 进行中
     */
    IN_PROGRESS,

    /**
     * 已完成
     */
    COMPLETED,

    /**
     * 失败
     */
    FAILED
    ;

}