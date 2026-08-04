# Description
This mod will send a warning message to the player when it detects that a memory leak is about to occur.

# How it works
The JVM (Java Virtual Machine) heap memory usage (in bytes) is checked every 5 seconds. This operation only reads a single number, so it is very lightweight. The mod records the results of the most recent measurements. After each measurement, it calculates the difference. If the memory continuously increases for 4 consecutive times (i.e., 20 seconds in total) without decreasing, it subtracts the starting value from the current value for that period to calculate the total increase. If the total continuous increase exceeds 100 MB, the mod considers this more likely a sign of a "memory leak" rather than normal fluctuation. It then uses the main thread to send a yellow warning message to the in-game chat. After sending the warning, it enters a 5-minute cooldown period, during which it will not repeat the warning to prevent frequent pop-ups.
Starting from version 2.0, the detection criteria can be modified via the configuration file.

# Notes
This mod cannot truly 'detect' memory leaks (that requires analyzing object reference chains, which is very complex); it is merely a heuristic warning tool. Its logic assumes: if memory increases by more than 100MB for 20 consecutive seconds during normal gameplay (when the player is not extensively loading new maps or resources), then there is likely a problem. It is like the 'tire pressure warning light' on a car dashboard — it doesn't tell you which tire has a nail, but it alerts you: 'Hey, the pressure is off, you'd better stop and check!' This way, players can save their progress and restart the game in advance, avoiding crashes and data loss due to out-of-memory (OOM) errors.

# 描述
这个模组会在检测到内存泄漏即将发生时向玩家发送一条警告信息。

# 运行逻辑
每5秒检测一次 JVM（Java虚拟机）当前占用的堆内存大小（单位：字节）。这个操作只读取一个数字，非常轻量。
模组会记录最近几次的测量结果，每次测量后，它会计算一次差异值。
如果连续4次（即持续20秒）内存都只涨不跌，它就把这段时期的开始值和当前值相减，算出总增长量。
如果连续上涨的总量超过了100 MB，模组就认为这不像正常的波动，而像“内存泄漏”的征兆。于是它会通过主线程，在游戏聊天栏给你发一条黄字警告。
发完警告后，它会进入5分钟的冷却，期间不再重复警告，防止频繁弹窗。
从2.0版本开始，可以通过配置文件修改检测条件。

# 注意事项
这个模组并不能真正“检测”到内存泄漏（那需要分析对象引用链，非常复杂），它只是一个启发式预警工具。
它的逻辑假设是：如果内存在正常游戏（玩家没有大规模加载新地图或资源）的情况下，连续20秒净增长超过100MB，那大概率“有问题”。
它就像汽车仪表盘上的“胎压报警灯”——它不告诉你是哪个轮胎扎了钉子，但它会提醒你：“嘿，气压不对劲，最好停车检查一下！” 这样玩家就能提前存档并重启游戏，避免因内存溢出（OOM）导致游戏崩溃丢档。
