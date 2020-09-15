///**
// * Description:
// *
// * @author mark
// * Date 2020/9/15
// */
// 第一步： AbstractBootstrap
//public ChannelFuture bind(int inetPort) {
//    return bind(new InetSocketAddress(inetPort));
//}
//第二步： AbstractBootstrap
///**
// * Create a new {@link io.netty.channel.Channel} and bind it.
// */
//public ChannelFuture bind(SocketAddress localAddress) {
//    validate();
//    return doBind(ObjectUtil.checkNotNull(localAddress, "localAddress"));
//}
//第三步：AbstractBootstrap
//private ChannelFuture doBind(final SocketAddress localAddress) {
//    // 调用第四步 initAndRegister 初始化 ChannelFuture
//    final ChannelFuture regFuture = initAndRegister();
//    // 从第十一部步回
//    final Channel channel = regFuture.channel();
//    异常
//    if (regFuture.cause() != null) {
//        return regFuture;
//    }
//    if (regFuture.isDone()) {
//        // At this point we know that the registration was complete and successful.
//        ChannelPromise promise = channel.newPromise();
//         调用doBind0完成对端口的绑定，最终进入第十三步
//        doBind0(regFuture, channel, localAddress, promise);
//        return promise;
//    } else {
//        ...
//    });
//    return promise;
//    }
//}
//
//第四步：AbstractBootstrap
//
//final ChannelFuture initAndRegister() {
//    Channel channel = null;
//    try {
//        // 根据之前传递的类 .channel(NioServerSocketChannel.class) 调用构造函数初始化，也就是第五步的初始化方法
//        channel = channelFactory.newChannel();
//           调用第七步init方法
//        init(channel);
//    } catch (Throwable t) {
//        if (channel != null) {
//            // channel can be null if newChannel crashed (eg SocketException("too many open files"))
//            channel.unsafe().closeForcibly();
//            // as the Channel is not registered yet we need to force the usage of the GlobalEventExecutor
//            return new DefaultChannelPromise(channel, GlobalEventExecutor.INSTANCE).setFailure(t);
//        }
//        // as the Channel is not registered yet we need to force the usage of the GlobalEventExecutor
//        return new DefaultChannelPromise(new FailedChannel(), GlobalEventExecutor.INSTANCE).setFailure(t);
//    }
//
//    ChannelFuture regFuture = config().group().register(channel);
//    if (regFuture.cause() != null) {
//        if (channel.isRegistered()) {
//            channel.close();
//        } else {
//            channel.unsafe().closeForcibly();
//        }
//    }
//    // If we are here and the promise is not failed, it's one of the following cases:
//    // 1) If we attempted registration from the event loop, the registration has been completed at this point.
//    //    i.e. It's safe to attempt bind() or connect() now because the channel has been registered.
//    // 2) If we attempted registration from the other thread, the registration request has been successfully
//    //    added to the event loop's task queue for later execution.
//    //    i.e. It's safe to attempt bind() or connect() now:
//    //         because bind() or connect() will be executed *after* the scheduled registration task is executed
//    //         because register(), bind(), and connect() are all bound to the same thread.
//    return regFuture;
//}
//
//第五步：创建Nio实例 AbstractNioChannel
//protected AbstractNioChannel(Channel parent, SelectableChannel ch, int readInterestOp) {
//    // super实际上调用的就是 AbstractChannel的父类构造方法
//    super(parent);
//    this.ch = ch;
//    this.readInterestOp = readInterestOp;
//    ...
//    // 设置非阻塞
//    ch.configureBlocking(false);
//    ...
//}
//
//protected AbstractChannel(Channel parent) {
//    this.parent = parent;
//    id = newId(); // 设置channelId，全局唯一
//    // 这个调用第六步
//    unsafe = newUnsafe();
//    // 下述方法就是最终实例化一个ChannelPipeline
//    pipeline = newChannelPipeline();
//       返回第四步
//}
//// AbstractChannel
//protected DefaultChannelPipeline newChannelPipeline() {
//    // 实例化DefaultChannelPipeline
//    return new DefaultChannelPipeline(this);
//}
//
//protected DefaultChannelPipeline(Channel channel) {
//    this.channel = ObjectUtil.checkNotNull(channel, "channel");
//    succeededFuture = new SucceededChannelFuture(channel, null);
//    voidPromise =  new VoidChannelPromise(channel, true);
//    // 从这里面可以看出在实例化ChannelPipeline时候就初始化了这个首尾的ChannelHandlerContext
//    tail = new DefaultChannelPipeline.TailContext(this);
//    head = new DefaultChannelPipeline.HeadContext(this);
//    // 从这个设置就可以看出就是一个链表
//    head.next = tail;
//    tail.prev = head;
//}
//
//第六步：返回AbstractNioUnsafe，类AbstractNioMessageChannel
//@Override
//protected AbstractNioUnsafe newUnsafe() {
//    return new NioMessageUnsafe();
//}
//private final class NioMessageUnsafe extends AbstractNioChannel.AbstractNioUnsafe {
//
//    private final List<Object> readBuf = new ArrayList<Object>();
//
//    @Override
//    public void read() {
//        ...
//    }
//}
//
//第七步：ServerBootstrap初始化channel
//@Override
//void init(Channel channel) {
//    // 设置channel的option，这个Channel就是NioServerSocketChannel
//    setChannelOptions(channel, newOptionsArray(), logger);
//    // 设置Channel的option,设置 NioServerSocketChannel 的 TCP 属性
//    setAttributes(channel, attrs0().entrySet().toArray(EMPTY_ATTRIBUTE_ARRAY));
//    ChannelPipeline p = channel.pipeline();
//    // 就是在serverBoot设置的workGroup
//    final EventLoopGroup currentChildGroup = childGroup;
//    // 在serverBootstrap中设置的childHandler
//    final ChannelHandler currentChildHandler = childHandler;
//    final Entry<ChannelOption<?>, Object>[] currentChildOptions;
//    // 由于 LinkedHashMap 是非线程安全的，使用同步进行处理。
//    synchronized (childOptions) {
//        currentChildOptions = childOptions.entrySet().toArray(EMPTY_OPTION_ARRAY);
//    }
//    final Entry<AttributeKey<?>, Object>[] currentChildAttrs = childAttrs.entrySet().toArray(EMPTY_ATTRIBUTE_ARRAY);
//
//        // 对 NioServerSocketChannel 的 ChannelPipeline 添加 ChannelInitializer 处理器
//        // 可以看出， init 的方法的核心作用在和 ChannelPipeline 相关
//        // 这里调用了他的 addLast 方法，也就是将整个 handler 插入到 tail 的 前面，因为 tail 永远会在后面，需要做一些系统的固定工作。
//        /*
//                @Override
//                public final ChannelPipeline addLast(EventExecutorGroup group, String name, ChannelHandler handler) {
//                    final AbstractChannelHandlerContext newCtx;
//                    synchronized (this) {
//                        checkMultiplicity(handler);
//
//                        newCtx = newContext(group, filterName(name, handler), handler);
//                        // 调用 addLast0
//                        addLast0(newCtx);
//                        ...
//                    }
//                    // 就是加入我们的上下文中
//                    callHandlerAdded0(newCtx);
//                    return this;
//                }
//                    调用addLast就是调用下述方法
//                        private void addLast0(AbstractChannelHandlerContext newCtx) {
//                            AbstractChannelHandlerContext prev = tail.prev;
//                            newCtx.prev = prev;
//                            newCtx.next = tail;
//                            prev.next = newCtx;
//                            tail.prev = newCtx;
//                        }
//        */
//    p.addLast(new ChannelInitializer<Channel>() {
//        @Override
//        public void initChannel(final Channel ch) {
//            final ChannelPipeline pipeline = ch.pipeline();
//            ChannelHandler handler = config.handler();
//            if (handler != null) {
//                pipeline.addLast(handler);
//            }
//            // 调用SingleThreadEventExecutor的execute方法，第八步，添加任务，相当如注册任务到当前NioEventLoop中
//            ch.eventLoop().execute(new Runnable() {
//                @Override
//                public void run() {
//                    pipeline.addLast(new ServerBootstrapAcceptor(
//                    ch, currentChildGroup, currentChildHandler, currentChildOptions, currentChildAttrs));
//                }
//            });
//        }
//    });
//}
//
//
//第八步：SingleThreadEventExecutor
//@Override
//public void execute(Runnable task) {
//    ObjectUtil.checkNotNull(task, "task");
//    execute(task, !(task instanceof LazyRunnable) && wakesUpForTask(task));
//}
//private void execute(Runnable task, boolean immediate) {
//    boolean inEventLoop = inEventLoop();
//    // 添加任务
//    addTask(task);
//    if (!inEventLoop) {
//        // 调用第九步（启动过程中或执行这里，因为当前的tread是空的）
//        startThread();
//    ...
//    }
//    if (!addTaskWakesUp && immediate) {
//        wakeup(inEventLoop);
//    }
//}
//
//第九步：SingleThreadEventExecutor
//private void doStartThread() {
//    executor.execute(new Runnable(){
//        @Override
//        public void run(){
//            ...
//               这一步就会执行第十步开始进行循环
//            SingleThreadEventExecutor.this.run();
//            ...
//        }
//    }
//}
//
//
//第十步：NioEventLoop开始执行，最终会执行完毕
//@Override
//protected void run() {
//    int selectCnt = 0;
//    for (;;) {
//        ...
//        processSelectedKeys();
//        // Ensure we always run tasks. 执行所有的task，进入第十一步
//        ranTasks = runAllTasks();
//    }
//  ...
//}
//第十一步：SingleThreadEventExecutor，之前说过NioEventLoop继承了SingleThreadEventExecutor
//protected boolean runAllTasks(long timeoutNanos) {
//    ...
//    for (;;) {
//    // 执行第十二步，真正执行任务
//    safeExecute(task);
//    runTasks ++;
//    // Check timeout every 64 tasks because nanoTime() is relatively expensive.
//    // XXX: Hard-coded value - will make it configurable if it is really a problem.
//    if ((runTasks & 0x3F) == 0) {
//        lastExecutionTime = ScheduledFutureTask.nanoTime();
//        if (lastExecutionTime >= deadline) {
//            break;
//        }
//    }
//    // 获取下一个任务
//    task = pollTask();
//    ...
//    afterRunningAllTasks();
//    this.lastExecutionTime = lastExecutionTime;
//       返回第三步
//    return true;
//}
//
//// 第十二步：AbstractEventExecutor
//protected static void safeExecute(Runnable task) {
//    try {
//        // 这个就是当时注册进来的，开始执行，例如在init方法中我们在eventLoop中execute中添加的任务
//        task.run();
//    } catch (Throwable t) {
//        logger.warn("A task raised an exception. Task: {}", task, t);
//    }
//}
// 第十三步：AbstractChannel的bind
//@Override
//public final void bind(final SocketAddress localAddress, final ChannelPromise promise) {
//    ...
//    // 最终调用下面的doBind
//    doBind(localAddress);
//    ...
//    // 通知各个listener，绑定成功
//    safeSetSuccess(promise);
//}
//// NioServerSocketChannel 最终会调用到NioServerSocketChannel的doBind
//@Override
//protected void doBind(SocketAddress localAddress) throws Exception {
//    if (PlatformDependent.javaVersion() >= 7) {
//        javaChannel().bind(localAddress, config.getBacklog());
//    } else {
//        javaChannel().socket().bind(localAddress, config.getBacklog());
//    }
//}