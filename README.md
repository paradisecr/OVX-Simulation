OVX-Java仿真 v1.2-as-dep 版本 

版本说明：

1.2-as-dep version: Can be include as resource allocation computer.

1.2版本拟增加回溯，优化方法调用。

1.1版本每次运行可进行多次资源映射。

1.0版本每次运行只能运行一次映射，下一个版本将增加多次映射功能。



## 1. 运行环境

- 系统环境：Windows / Linux

- 软件环境：Java-1.8 version

- 编译依赖：Maven


## 2. 运行方法


编译方法：

```
mvn clean install
```

运行示例：
```
java -jar ovx-simulation-jar-with-dependencies.jar

Please input physical topology param description file path(e.g, ~/params/phy.json): 物理网络参数文件地址
**************************************************
Please input resource request param description file path: 虚拟网络参数文件地址
Request:
{s3=100, s4=100, s1=1, s2=100}
{(s1 : s2)=100, (s3 : s4)=100, (s1 : s4)=100, (s1 : s3)=100, (s2 : s4)=100}
ResourceAllocation{
vAllocationMap={},
eAllocationMap={},
isAllocationSuccess=false}
Total Cost:0
Run time：168ms
*******
Remain Resource:
Nodes:{s3=100, s4=100, s1=1, s2=100}
Links:{(s1 : s2)=100, (s3 : s4)=100, (s1 : s4)=100, (s1 : s3)=100, (s2 : s4)=100}
**************************************************
Please input resource request param description file path:虚拟网络参数文件地址2
Request:
{s3=100, s4=100, s1=1, s2=100}
{(s1 : s2)=100, (s3 : s4)=100, (s1 : s4)=100, (s1 : s3)=100, (s2 : s4)=100}
ResourceAllocation{
vAllocationMap={},
eAllocationMap={},
isAllocationSuccess=false}
Total Cost:0
Run time：0ms
*******
Remain Resource:
Nodes:{s3=100, s4=100, s1=1, s2=100}
Links:{(s1 : s2)=100, (s3 : s4)=100, (s1 : s4)=100, (s1 : s3)=100, (s2 : s4)=100}
**************************************************
Ctr + D Exit
```

### 2.1 参数：
(1)物理网络参数文件地址：表示物理网络配置文件所在地址，该文件格式如下

```
	* nodes：表示所有的交换机节点
	* 
		* name：交换机节点的名称
		* resource：交换机节点的计算容量
		* cost：交换机节点计算能力单位成本

	* links：表示所有物理链路(表示双向链路，src和dst无特殊意义)
	* 
		* src: 链路一端的交换机节点名称
		* dst: 链路另一端交换机节点名称
		* resource：链路总带宽
		* cost：链路带宽成本
```

```
{
    "nodes":[
        {
            "name":"s1",
            "resource":100,
            "cost":5
        },
        {
            "name":"s2",
            "resource":100,
            "cost":5
        },
        {
            "name":"s3",
            "resource":100,
            "cost":5
        },
        {
            "name":"s4",
            "resource":100,
            "cost":5
        }
    ],
    "links":[
        {
            "src":"s1",
            "dst":"s2",
            "resource":100,
            "cost":5
        },
        {
            "src":"s1",
            "dst":"s3",
            "resource":100,
            "cost":5
        },
        {
            "src":"s2",
            "dst":"s4",
            "resource":100,
            "cost":5
        },
        {
            "src":"s3",
            "dst":"s4",
            "resource":100,
            "cost":5
        }
    ]
}

```

(2) 虚拟网络参数文件地址：表示虚拟网络参数文件的地址，文件格式如下。

	* nodes：表示所有的交换机节点
	* 
		* name：虚拟节点的名称
		* resource：虚拟节点的计算容量需求

	* links：表示所有虚拟链路(表示双向链路，src和dst无特殊意义)
	* 
		* src: 链路一端的虚拟交换机节点名称
		* dst: 链路另一端虚拟交换机节点名称
		* resource：虚拟链路带宽需求


```
{
    "nodes":[
        {
            "name":"s1",
            "resource":100
        },
        {
            "name":"s2",
            "resource":100
        },
        {
            "name":"s3",
            "resource":100
        },
        {
            "name":"s4",
            "resource":100
        }
    ],
    "links":[
        {
            "src":"s1",
            "dst":"s2",
            "resource":100
        },
        {
            "src":"s1",
            "dst":"s3",
            "resource":100
        },
        {
            "src":"s2",
            "dst":"s4",
            "resource":100
        },
        {
            "src":"s3",
            "dst":"s4",
            "resource":100
        }
    ]
}

```

### 2.2 输出说明

````
Request:                         # Resource request params
{s3=100, s4=100, s1=1, s2=100}
{(s1 : s2)=100, (s3 : s4)=100, (s1 : s4)=100, (s1 : s3)=100, (s2 : s4)=100}
ResourceAllocation{              # Resource mapping result
vAllocationMap={},
eAllocationMap={},
isAllocationSuccess=false}
Total Cost:0
Run time：0ms
*******
Remain Resource:                 # Remain source info
Nodes:{s3=100, s4=100, s1=1, s2=100}
Links:{(s1 : s2)=100, (s3 : s4)=100, (s1 : s4)=100, (s1 : s3)=100, (s2 : s4)=100}
```
