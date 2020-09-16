import io.netty.channel.*;
import io.netty.util.internal.ObjectUtil;

/**
 * Description:
 *
 * @author mark
 * Date 2020/9/15
 */
////第一步：NioEventLoop 连接进来的地方
//private void processSelectedKeys(){
//        ...
//        processSelectedKeysOptimized();
//        }
//// 第二步： NioEventLoop 开始循环
//private void processSelectedKeysOptimized(){
//        // 可以吧selectedKeys当做一个优化的连接
//        for(int i=0;i<selectedKeys.size;++i){
//        ...
//        // 调用执行selectedKey
//        processSelectedKey(k,(AbstractNioChannel)a);
//        ...
//        }
//        }
//// 第三步： NioEventLoop 处理连接
//private void processSelectedKey(SelectionKey k,AbstractNioChannel ch){
//        // 判断这个连接属于什么事件
//        ...
//        if((readyOps&SelectionKey.OP_CONNECT)!=0){
//        ...
//        }
//        // Process OP_WRITE first as we may be able to write some queued buffers and so free memory.
//        if((readyOps&SelectionKey.OP_WRITE)!=0){
//        ...
//        }
//        // 如果是连接或者读事件，就进入这个方法，主要看这个
//        if((readyOps&(SelectionKey.OP_READ|SelectionKey.OP_ACCEPT))!=0||readyOps==0){
//        // 调用NioMessageUnsafe的read方法，我们在源码启动过程中看过这个，当时只是把read方法省略了
//        unsafe.read();
//        }
//        ...
//        }
//
//// 第四步：AbstractNioMessageChannel
//private final class NioMessageUnsafe extends AbstractNioChannel.AbstractNioUnsafe {
//
//    private final List<Object> readBuf = new ArrayList<Object>();
//
//    @Override
//    public void read() {
//        // 判断是否在当前线程
//        assert eventLoop().inEventLoop();
//            ...
//        do {
//            // 读取信息，调用第五步
//            int localRead = doReadMessages(readBuf);
//                ...
//            // 增加已读消息数量
//            allocHandle.incMessagesRead(localRead);
//        } while (allocHandle.continueReading());
//
//        int size = readBuf.size();
//        for (int i = 0; i < size; i++) {
//            readPending = false;
//            // 将消息给下一个ChannelHandler,调用第七步，从这个循环，我们就知道之前使用解码器解析List，为什么会逐个元素进行接收
//            pipeline.fireChannelRead(readBuf.get(i));
//        }
//        // 从第九步切入
//        // 清空buffer
//        readBuf.clear();
//        // 通知读取完成
//        allocHandle.readComplete();
//        // 通知下一个读取完成，执行流程就类似与fireChannelRead，就是不停的调用下一个处理器，并通知完成，最终会调用DefaultChannelPipeline的channelReadComplete，看一下第十步
//        pipeline.fireChannelReadComplete();
//            ...
//    }
//}
//
//    // 第五步： NioServerSocketChannel 读取信息
//    @Override
//    protected int doReadMessages(List<Object> buf) throws Exception {
//        // 之前说的这个channel就是NioServerSocketChannel，这个就是获取连接，accept方法，调用的就是第六步
//        SocketChannel ch = SocketUtils.accept(javaChannel());
//        // 将内容读取到缓冲区
//        buf.add(new NioSocketChannel(this, ch));
//        // 返回第四步
//        return 1;
//    }
//
//    // 第六步：SocketUtils，接受一个连接，并返回
//    public static SocketChannel accept(final ServerSocketChannel serverSocketChannel) throws IOException {
//        return AccessController.doPrivileged(new PrivilegedExceptionAction<SocketChannel>() {
//            @Override
//            public SocketChannel run() throws IOException {
//                return serverSocketChannel.accept();
//            }
//        }
//         ...
//    }
//
//
//    // 第七步：DefaultChannelPipeline，主要作用就是对外接口，并获取下一个处理器
//    @Override
//    public final ChannelPipeline fireChannelRead(Object msg) {
//        // 获取下一个处理器并调用第八步
//          /*
//          private AbstractChannelHandlerContext findContextInbound(int mask) {
//                AbstractChannelHandlerContext ctx = this;
//                EventExecutor currentExecutor = executor();
//                do {
//                    ctx = ctx.next;
//                } while (skipContext(ctx, currentExecutor, mask, MASK_ONLY_INBOUND));
//                return ctx;
//            }
//           */
//        AbstractChannelHandlerContext.invokeChannelRead(head, msg);
//        return this;
//    }
//
//    // 第八步：AbstractChannelHandlerContext，从名字就可以看出这里面AbstractChannelHandlerContext就是下一个ChannelHandlerContext
//    static void invokeChannelRead(final AbstractChannelHandlerContext next, Object msg) {
//        final Object m = next.pipeline.touch(ObjectUtil.checkNotNull(msg, "msg"), next);
//        // 从这里可以知道，这个就是获取下一个执行器
//        EventExecutor executor = next.executor();
//        // 判断是否是当前线程
//        if (executor.inEventLoop()) {
//            // 这里面会进入下一个处理器，会进入下一个处理器的invokeChannelRead，进入第九步
//            next.invokeChannelRead(m);
//        } else {
//            // 会限制性
//            executor.execute(new Runnable() {
//                @Override
//                public void run() {
//                    next.invokeChannelRead(m);
//                }
//            });
//        }
//    }
//
//    // 第九步：AbstractChannelHandlerContext，这里面就是再次进行下一个处理器的ChannelRead
//    private void invokeChannelRead(Object msg) {
//        if (invokeHandler()) {
//            try {
//                // 下属方法进去处理的消息，等到所有的处理器执行完毕（接下来只看ServerBootstrap的ChannelRead，见第九-2步），就会返回第四步
//                ((ChannelInboundHandler) handler()).channelRead(this, msg);
//            } catch (Throwable t) {
//                invokeExceptionCaught(t);
//            }
//        } else {
//            fireChannelRead(msg);
//        }
//    }
//
//    // 第九-2步：ServerBootstrap
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) {
//        // 将msg强转，实际上这个msg的类型就是NioSocketChannel（因为在接受是传递的就是NioSocketChannel）
//        final Channel child = (Channel) msg;
//        // 加入在初始化ServerBootstrap的childHandler
//        child.pipeline().addLast(childHandler);
//        // 设置Option
//        setChannelOptions(child, childOptions, logger);
//        // 设置属性
//        setAttributes(child, childAttrs);
//        ...
//        //childGroup就是当初设置的workGroup，我们将这个注册到workGroup,其实就是调用下述方法，之后进入第九-3步
//        /*
//        @Override
//            public ChannelFuture register(Channel channel) {
//                return next().register(channel);
//            }
//         */
//        childGroup.register(child).addListener(new ChannelFutureListener() {
//            @Override
//            public void operationComplete(ChannelFuture future) throws Exception {
//                if (!future.isSuccess()) {
//                    forceClose(child, future.cause());
//                }
//            }
//        });
//        ...
//
//    }
//
//    // 第九-3步： SingleThreadEventLoop
//    @Override
//    public ChannelFuture register(Channel channel) {
//        // 进入 第九-4步
//        return register(new DefaultChannelPromise(channel, this));
//    }
//
//    // 第九-4步：SingleThreadEventLoop
//    @Override
//    public ChannelFuture register(final ChannelPromise promise) {
//        ObjectUtil.checkNotNull(promise, "promise");
//        // 进入 第九-5步
//        promise.channel().unsafe().register(this, promise);
//        return promise;
//    }
//
//    // 第九-5步 AbstractChannel
//    @Override
//    public final void register(EventLoop eventLoop, final ChannelPromise promise) {
//        ...
//        if (eventLoop.inEventLoop()) {
//            register0(promise);
//        } else {
//            eventLoop.execute(new Runnable() {
//                @Override
//                public void run() {
//                    register0(promise);
//                }
//            });
//            ...
//        }
//    }
//
//    // 第九-6步: AbstractChannel
//    private void register0(ChannelPromise promise) {
//        ...
//        // 进入第九-6步
//        doRegister();
//        neverRegistered = false;
//        registered = true;
//
//        // Ensure we call handlerAdded(...) before we actually notify the promise. This is needed as the
//        // user may already fire events through the pipeline in the ChannelFutureListener.
//        pipeline.invokeHandlerAddedIfNeeded();
//        // 同志成功
//        safeSetSuccess(promise);
//        pipeline.fireChannelRegistered();
//        // Only fire a channelActive if the channel has never been registered. This prevents firing
//        // multiple channel actives if the channel is deregistered and re-registered.
//        if (isActive()) {
//            // 首次注册们就会触发通道活跃
//            if (firstRegistration) {
//                // 这个最终也会调用beginRead
//                pipeline.fireChannelActive();
//            } else if (config().isAutoRead()) {
//                // This channel was registered before and autoRead() is set. This means we need to begin read
//                // again so that we process inbound data.
//                //
//                // See https://github.com/netty/netty/issues/4805
//                // 进入第十一步
//                beginRead();
//            }
//        }
//        ...
//
//    }
//
//    // 第九-7步: AbstractNioChannel
//    @Override
//    protected void doRegister() throws Exception {
//        for (; ; ) {
//            // 这个就是注册监听
//            selectionKey = javaChannel().register(eventLoop().unwrappedSelector(), 0, this);
//        }
//    }
//
//    // 第十步：DefaultChannelPipeline 这个就是默认的ChannelPipeline，也就是我们的头部以及尾部处理器就在这个类中
//    @Override
//    public void channelReadComplete(ChannelHandlerContext ctx) {
//        ctx.fireChannelReadComplete();
//        readIfIsAutoRead();
//    }
//
//    // 第十一步：DefaultChannelPipeline
//    private void readIfIsAutoRead() {
//        // 这个就是在初始化NioServerChannel的设置是否自动接收，netty建议使用true，自动接收，否则还得手动处罚
//        if (channel.config().isAutoRead()) {
//            // 进入第十二步
//            channel.read();
//        }
//    }
//
//    // 第十二步： AbstractChannelHandlerContext
//    @Override
//    public ChannelHandlerContext read() {
//        EventExecutor executor = next.executor();
//        // 判断是否是当前线程
//        if (executor.inEventLoop()) {
//            // 进入十三步
//            next.invokeRead();
//        } else {
//            AbstractChannelHandlerContext.Tasks tasks = next.invokeTasks;
//            if (tasks == null) {
//                next.invokeTasks = tasks = new AbstractChannelHandlerContext.Tasks(next);
//            }
//            executor.execute(tasks.invokeReadTask);
//        }
//
//        return this;
//    }
//
//    // 第十三步：AbstractChannelHandlerContext
//    private void invokeRead() {
//        // 通过下述逐步进行，最终进入第十四步doBeginRead
//        ((ChannelOutboundHandler) handler()).read(this);
//    }
//
//    // DefaultChannelPipeline
//    @Override
//    public void read(ChannelHandlerContext ctx) {
//        unsafe.beginRead();
//    }
//
//    // AbstractChannel
//    @Override
//    public final void beginRead() {
//        doBeginRead();
//    }
//
//    // 第十四步: AbstractNioChannel,这里面判断这个selectionKey是否这是合法，自此就执行完毕，可以监听读事件，进入select循环等待，也就是进入第十五步
//    @Override
//    protected void doBeginRead() throws Exception {
//        // Channel.read() or ChannelHandlerContext.read() was called
//        final SelectionKey selectionKey = this.selectionKey;
//        if (!selectionKey.isValid()) {
//            return;
//        }
//
//        readPending = true;
//        final int interestOps = selectionKey.interestOps();
//        if ((interestOps & readInterestOp) == 0) {
//            selectionKey.interestOps(interestOps | readInterestOp);
//        }
//    }
//
//    // 第十五步：NioEventLoop，这个就是在项目启动时看过的NioEventLoop的run方法
//    protected void run() {
//        int selectCnt = 0;
//        for (; ; ) {
//            ...
//            // 执行select方法进行监听端口
//            strategy = select(curDeadlineNanos);
//            ...
//        }
//    }