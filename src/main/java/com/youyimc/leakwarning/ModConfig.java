package com.youyimc.leakwarning;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ModConfig {
    public static final ModConfigSpec SPEC;

    // 配置项定义
    public static final ModConfigSpec.BooleanValue ENABLE_PERCENT_CHECK;
    public static final ModConfigSpec.IntValue MEMORY_PERCENT_THRESHOLD;
    public static final ModConfigSpec.IntValue RISE_THRESHOLD_MB;
    public static final ModConfigSpec.BooleanValue ENABLE_SINGLE_RISE_ALERT;
    public static final ModConfigSpec.IntValue SINGLE_RISE_THRESHOLD_MB;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("Leak Warning Mod 配置文件").push("general");

        ENABLE_PERCENT_CHECK = builder
                .comment("是否启用内存占比检测。若为true，则只有当内存使用率超过百分比阈值且满足连续上升条件时才报警；若为false，则忽略占比条件。")
                .define("enablePercentCheck", true);

        MEMORY_PERCENT_THRESHOLD = builder
                .comment("内存使用率百分比阈值 (0-100)。当已用内存超过最大堆内存的此百分比时，才可能触发连续上升报警。")
                .defineInRange("memoryPercentThreshold", 50, 0, 100);

        RISE_THRESHOLD_MB = builder
                .comment("连续上升总增量阈值（单位：MB）。当内存连续上升的总量超过此值时触发报警。")
                .defineInRange("riseThresholdMB", 100, 1, Integer.MAX_VALUE);

        ENABLE_SINGLE_RISE_ALERT = builder
                .comment("是否启用单次增量快速报警。若为true，则当单次采样增量超过 'singleRiseThresholdMB' 时立即报警，忽略其他条件。")
                .define("enableSingleRiseAlert", false);

        SINGLE_RISE_THRESHOLD_MB = builder
                .comment("单次增量阈值（单位：MB）。仅当 'enableSingleRiseAlert' 为 true 时生效。")
                .defineInRange("singleRiseThresholdMB", 200, 1, Integer.MAX_VALUE);

        builder.pop();
        SPEC = builder.build();
    }
}