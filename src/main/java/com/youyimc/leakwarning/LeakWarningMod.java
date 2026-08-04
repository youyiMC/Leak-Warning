package com.youyimc.leakwarning;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(LeakWarningMod.MOD_ID)
public class LeakWarningMod {
    public static final String MOD_ID = "leakwarning";
    private static MemoryMonitorService monitorService;

    // 关键修改：构造函数接收 ModContainer 参数
    public LeakWarningMod(ModContainer container) {
        // 注册配置文件
        container.registerConfig(Type.COMMON, ModConfig.SPEC, "leakwarning-common.toml");

        // 仅在客户端启动监控
        if (FMLEnvironment.dist == Dist.CLIENT) {
            monitorService = new MemoryMonitorService();
            monitorService.start();
            // 添加 JVM 关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (monitorService != null) {
                    monitorService.stop();
                }
            }));
        }
    }
}