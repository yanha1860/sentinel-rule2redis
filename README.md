# sentinel-rule2redis
通过 Sentinel 控制台配置集群流控规则(自动同步到业务服务)

### 背景
Sentinel Dashboard与业务服务之间本身是可以互通获取最新限流规则的，这在没有整合配置中心来存储限流规则的时候就已经存在这样的机制。最主要的区别是：配置中心的修改都可以实时的刷新到业务服务，从而被Sentinel Dashboard读取到，但是对于这些规则的更新到达各个业务服务之后，并没有一个机制去同步到配置中心，作为配置中心的客户端也不会提供这样的逆向更新方法。
（这里的配置中心其实就是存放sentinel规则的地方，可以是Nacos、Redis）

### 改造方案
![sentinel-redis](https://user-images.githubusercontent.com/5134790/148348074-cb0c7be6-6e11-4c09-9d3e-34093a5e2866.png)

* 代码
