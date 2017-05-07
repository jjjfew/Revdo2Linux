# 录音权限
targetSdkVerion设定为<=22，即可自动获取权限了，不用下面在代码中获取了，否则会获取不到录音权限

# anddroid7.0下搜索IP会闪退
由于华为android7.0对内存使用的限制，同时并发256个thread，会造成内存溢出，因为分为128个thread不会闪退。
## 方法一：
在android2.2下可以自定义我们的应用需要多大的内存，但是更高的android版本不支持如下的操作：
```
private final static int CWJ_HEAP_SIZE = 6* 1024* 1024 ;
//设置最小heap内存为6MB大小
VMRuntime.getRuntime().setMinimumHeapSize(CWJ_HEAP_SIZE);

```
## 方法二：
在ConnectActivity中将256个并发进程分解为128个，第个进程中ping两个ip。