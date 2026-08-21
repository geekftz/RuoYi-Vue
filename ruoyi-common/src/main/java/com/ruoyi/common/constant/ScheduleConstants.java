package com.ruoyi.common.constant;

/**
 * 任务调度通用常量
 * <p>
 * 【架构位置】common模块 → constant，ruoyi-quartz定时任务模块的专用常量。
 * 【MISFIRE说明】定时任务的"错过触发"策略：应用停机期间错过的执行时机，恢复后怎么补偿：
 *   DEFAULT(0)用Cron表达式的默认策略；IGNORE_MISFIRES(1)立即补执行所有错过的；
 *   FIRE_AND_PROCEED(2)只立即执行一次然后按计划走；DO_NOTHING(3)错过的就算了，等下次触发。
 * 【使用场景】后台"定时任务"菜单配置任务时的"错误策略"下拉框对应这四个值。
 * 
 * @author ruoyi
 */
public class ScheduleConstants
{
    public static final String TASK_CLASS_NAME = "TASK_CLASS_NAME";

    /** 执行目标key */
    public static final String TASK_PROPERTIES = "TASK_PROPERTIES";

    /** 默认 */
    public static final String MISFIRE_DEFAULT = "0";

    /** 立即触发执行 */
    public static final String MISFIRE_IGNORE_MISFIRES = "1";

    /** 触发一次执行 */
    public static final String MISFIRE_FIRE_AND_PROCEED = "2";

    /** 不触发立即执行 */
    public static final String MISFIRE_DO_NOTHING = "3";

    public enum Status
    {
        /**
         * 正常
         */
        NORMAL("0"),
        /**
         * 暂停
         */
        PAUSE("1");

        private String value;

        private Status(String value)
        {
            this.value = value;
        }

        public String getValue()
        {
            return value;
        }
    }
}
