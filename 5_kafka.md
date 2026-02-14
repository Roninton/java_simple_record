## 1、Kafka 是什么

Apache Kafka 是一个分布式流处理平台，主要用于构建实时数据管道和流应用。它具有以下核心特性：

**主要功能：**
- 发布和订阅记录流，类似于消息队列或企业消息系统
- 以容错的方式存储记录流
- 在记录流发生时进行处理

1. 订阅记录流（Subscribe to a Stream of Records）
在 Apache Kafka 中，数据是以 主题（Topic） 的形式组织的，每个主题由多个 分区（Partition） 构成。生产者将记录（Record）写入主题，消费者通过 订阅（Subscribe） 主题来读取这些记录，形成一个持续的数据流（Stream）。

2. 容错（Fault Tolerance）
容错是指系统在部分组件（如节点、网络、进程等）发生故障时，仍能继续正常运行或自动恢复的能力。

## 2、压缩（GZIP 或 Snappy）

Kafka支持多种压缩算法来减少网络传输和磁盘存储开销：

**支持的压缩算法：**
- **GZIP**：高压缩比，CPU消耗较高
- **Snappy**：快速压缩解压，压缩比适中
- **LZ4**：极快的压缩速度
- **ZSTD**：平衡了压缩比和速度

## 3、数据文件分段 segment（顺序读写、分段命令、二分查找）

Kafka通过分段存储机制优化性能：

**Segment结构：**
- 每个Partition被分成多个固定大小的Segment文件
- 默认每个Segment大小为1GB
- 文件命名规则：`[base_offset].log`

**优势：**
- **快速定位**：通过文件名直接定位到对应Offset范围
- **高效删除**：可以整段删除过期数据
- **内存映射**：支持操作系统级别的缓存优化

**查找机制：**
```
segment1: [0-10000].log
segment2: [10001-20000].log
segment3: [20001-30000].log
```

## 4、partition 的数据文件（offset， MessageSize， data）

Kafka 并不会把一个分区的所有消息都存到一个文件里，而是将日志（log）切分成多个段（Segment），每个段对应一个日志文件（.log）和索引文件（.index, .timeindex）。
例如，一个分区可能包含以下文件：
```text
00000000000000000000.log      ← 存储 offset 0 ~ 9999 的消息
00000000000000000000.index
00000000000000100000.log      ← 存储 offset 10000 ~ 19999 的消息
00000000000000100000.index
...
```
### Offset 与分段的关系
虽然消息被分散在多个段文件中，但 offset 始终是全局的。Kafka 通过以下方式快速定位消息：
1. 根据 offset 找到对应的段文件
2. 因为段文件名是起始 offset，Kafka 可以通过二分查找快速确定目标 offset 属于哪个段。
3. 在段内计算相对偏移（relative offset）
例如：要读取 offset = 10500:
    - 它属于 00000000000000100000.log 段（起始 offset = 10000）
    - 段内相对 offset = 10500 - 10000 = 500
    - 然后通过 .index 文件（稀疏索引）快速定位到 .log 文件中的字节位置。
    - 从 .log 文件中读取消息

## 5、消费者设计

Kafka消费者采用拉取模式设计：

**核心设计原则：**
- **消费者主动拉取**：消费者主动向Broker请求数据
- **批量处理**：一次拉取多条消息提高效率
- **Offset管理**：跟踪已消费消息的位置

**重要概念：**
- **Consumer Group**：多个消费者组成逻辑组
- **Rebalance**：组内成员变化时重新分配Partition
- **Heartbeat**：定期向Group Coordinator发送心跳

## 6、批量发送

Kafka通过批量发送机制提升吞吐量：

**批量配置参数：**
```properties
# 生产者配置
batch.size=16384          # 批次大小（字节）
linger.ms=0               # 等待时间（毫秒）
buffer.memory=33554432    # 缓冲区内存
max.in.flight.requests.per.connection=5  # 并发请求数
```

**工作原理：**
1. 消息首先放入批次缓冲区
2. 达到批次大小或等待超时后发送
3. 多个批次可以并发发送到不同Partition

## 7、负载均衡（partition 会均衡分布到不同 broker 上）

Kafka通过Partition分布在多个Broker上来实现负载均衡

### 消费者如何知道自己该从哪些分区拉取消息？以及 Kafka 如何保证“消费者-分区”的对应关系？
Kafka 通过“消费者组（Consumer Group）” + “协调器（Coordinator）” + “再均衡（Rebalance）”机制来动态分配分区给消费者，并在拉取时严格遵守这个分配结果。

1. 加入消费者组(Consumer Group)
```java
consumer.subscribe(Arrays.asList("my-topic"));
```
消费者声明自己属于某个 消费者组（group.id），并订阅一个或多个 Topic。

2. 触发“再均衡”（Rebalance）
组内第一个消费者加入、新消费者加入、消费者崩溃、分区数变化等，都会触发 Rebalance。
Kafka 会选举一个 Group Coordinator（协调器）（通常是某个 Broker）来管理这个组。

3. 分区分配策略（Partition Assignment）
Coordinator 使用 分区分配策略（如 Range、RoundRobin、Sticky 等）将 Topic 的所有分区 公平地分配给当前存活的消费者。

例如：
- Topic 有 4 个分区：P0, P1, P2, P3
- 消费者组有 2 个消费者：C1, C2
- 分配结果可能是：C1 → {P0, P1}，C2 → {P2, P3}

**关键点**：每个分区在同一时间只能被组内的一个消费者消费。

4. 消费者获知自己的分区分配
- 每个消费者会收到 Coordinator 发来的 分配结果（assignment）。
- 在 Java 客户端中，你可以通过 consumer.assignment() 查看自己负责哪些分区。

## 8、为什么需要消息系统

**消息系统的优点：**
- **解耦**：生产者和消费者不需要直接交互
- **异步处理**：提高系统响应速度
- **削峰填谷**：缓冲突发流量
- **广播机制**：一条消息可被多个消费者处理
- **高吞吐量**：专为消息传输优化