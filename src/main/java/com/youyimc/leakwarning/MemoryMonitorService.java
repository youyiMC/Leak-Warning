package com.youyimc.leakwarning;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.concurrent.*;

public class MemoryMonitorService {
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final long[] recentSamples = new long[5];
    private int sampleCount = 0;
    private int consecutiveRises = 0;
    private long lastWarnTime = 0;
    private volatile boolean running = false;

    // 固定采样间隔（秒）和连续上升次数阈值（不变）
    private static final int SAMPLE_INTERVAL_SECONDS = 5;
    private static final int CONSECUTIVE_RISES_TO_WARN = 4;
    private static final long WARN_COOLDOWN_MS = 300_000; // 5分钟

    public void start() {
        if (running) return;
        running = true;
        sample();
        scheduler.scheduleAtFixedRate(this::checkMemory, SAMPLE_INTERVAL_SECONDS, SAMPLE_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    public void stop() {
        running = false;
        scheduler.shutdownNow();
    }

    private void checkMemory() {
        if (!running) return;
        try {
            long current = memoryBean.getHeapMemoryUsage().getUsed();
            long previous = (sampleCount > 0) ? recentSamples[(sampleCount - 1) % recentSamples.length] : current;
            long delta = current - previous;

            // 存入环形缓冲区
            recentSamples[sampleCount % recentSamples.length] = current;
            sampleCount++;

            // ---------- 1. 单次增量快速报警（独立条件） ----------
            if (ModConfig.ENABLE_SINGLE_RISE_ALERT.get()) {
                long singleThresholdBytes = ModConfig.SINGLE_RISE_THRESHOLD_MB.get() * 1024L * 1024L;
                if (delta > singleThresholdBytes) {
                    sendAlert(delta / (1024 * 1024), "单次异常增加");
                    consecutiveRises = 0; // 重置连续上升计数，避免重复报警
                    return; // 本次检查结束
                }
            }

            // ---------- 2. 连续上升逻辑 ----------
            if (delta <= 0) {
                consecutiveRises = 0;
                return; // 内存下降，重置连续计数
            }

            consecutiveRises++;

            // 检查是否达到连续上升次数
            if (consecutiveRises >= CONSECUTIVE_RISES_TO_WARN) {
                int startIndex = (int)((sampleCount - consecutiveRises) % recentSamples.length);
                long startValue = recentSamples[startIndex];
                long totalIncrease = current - startValue;

                // 判断是否满足报警条件
                boolean shouldAlert = false;

                // 是否启用百分比检查
                if (ModConfig.ENABLE_PERCENT_CHECK.get()) {
                    long max = memoryBean.getHeapMemoryUsage().getMax();
                    if (max > 0) {
                        int percent = (int)(current * 100 / max);
                        if (percent >= ModConfig.MEMORY_PERCENT_THRESHOLD.get()) {
                            shouldAlert = true;
                        }
                        // 如果百分比未达标，不报警
                    } else {
                        // 若 max 为 -1（无限制），则忽略百分比检查，直接允许报警
                        shouldAlert = true;
                    }
                } else {
                    // 未启用百分比检查，直接允许报警
                    shouldAlert = true;
                }

                if (shouldAlert) {
                    long riseThresholdBytes = ModConfig.RISE_THRESHOLD_MB.get() * 1024L * 1024L;
                    if (totalIncrease > riseThresholdBytes) {
                        sendAlert(totalIncrease / (1024 * 1024), "持续增长");
                        consecutiveRises = 0; // 报警后重置计数
                    }
                }
            }
        } catch (Exception e) {
            // 捕获所有异常防止线程终止（可忽略或记录日志）
        }
    }

    private void sample() {
        long current = memoryBean.getHeapMemoryUsage().getUsed();
        recentSamples[0] = current;
        sampleCount = 1;
        consecutiveRises = 0;
    }

    // 发送警告消息（带冷却）
    private void sendAlert(long increaseMB, String type) {
        long now = System.currentTimeMillis();
        if (now - lastWarnTime < WARN_COOLDOWN_MS) {
            return; // 冷却中，不重复发送
        }
        lastWarnTime = now;

        // 必须确保在主线程执行
        Minecraft.getInstance().execute(() -> {
            if (Minecraft.getInstance().player != null) {
                String msg = String.format(
                    "⚠️ [内存警告] %s！内存增加了约 %d MB，请留意游戏性能。",
                    type, increaseMB
                );
                Minecraft.getInstance().player.sendSystemMessage(Component.literal(msg));
            }
        });
    }
}