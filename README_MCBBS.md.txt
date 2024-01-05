[postbg]bg5.png[/postbg][markdown]

https://github.com/ApliNi/PerPlayerWorld

---

# PerPlayerWorld `v1.0`
**[定制插件]** 为每个玩家创建单独的维度, 通过末影箱传送

---

## 使用方法
1. 当玩家第一次打开末影箱时会通过配置中的指令复制 `ppw_world` 地图到 `world_${玩家UUID}`.
2. 之后再次打开末影箱将传送到 `world_${玩家UUID}`.
3. 玩家在这个地图中点击末影箱可传送到原来的位置.

## 前置条件
1. 安装一个多世界插件, 默认配置中使用 `MyWorlds`, 使用其他多世界插件则需要修改配置.
2. 准备 `ppw_world` 地图用于复制. 或者修改 `command.copyWorld` 为创建地图的指令.

### 配置
```yaml

message:
  loadWorld: '维度正在加载... 请稍后再试一次'
  tp: '正在传送...'

command:
  copyWorld: 'world copy ppw_world ${worldName}'
  loadWorld: 'world load ${worldName}'

```


---

### MCBBS
本插件所用所有代码均为原创,不存在借用/抄袭等行为
本插件为非盈利性插件，免费发布，严禁销售和转卖

[/markdown]
