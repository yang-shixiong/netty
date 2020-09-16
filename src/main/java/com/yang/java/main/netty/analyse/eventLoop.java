//package com.yang.java.main.netty.analyse;
//
//import io.netty.channel.SelectStrategy;
//import io.netty.channel.nio.NioEventLoop;
//import io.netty.util.concurrent.AbstractScheduledEventExecutor;
//import io.netty.util.concurrent.ScheduledFutureTask;
//
//import java.io.IOException;
//import java.nio.channels.CancelledKeyException;
//import java.util.Queue;
//
///**
// * Description:
// *
// * @author mark
// * Date 2020/9/16
// */
//// 第一步： NioEventLoop
//@Override
//protected void run() {
//    NioEventLoop
//    int selectCnt = 0;
//    for (;;) {
//        try {
//            int strategy;
//            try {
//                // 判断事件类型，其中就是selectNowSupplier下述
//                /*
//                private final IntSupplier selectNowSupplier = new IntSupplier() {
//                    @Override
//                    public int get() throws Exception {
//                        return selectNow();
//                    }
//                };
//                hasTasks就是 !taskQueue.isEmpty() ||!tailTasks.isEmpty()
//                而这个方法就是执行：hasTasks ? selectSupplier.get() : SelectStrategy.SELECT;一般都是-1，也即是select
//                初始化执行都是0，因为在继续在进行注册，也就是执行里面的那一个语句，会直接跳过select直接运行所有的task
//                 */
//                strategy = selectStrategy.calculateStrategy(selectNowSupplier, hasTasks());
//                switch (strategy) {
//                    case SelectStrategy.CONTINUE:
//                        continue;
//
//                    case SelectStrategy.BUSY_WAIT:
//                        // fall-through to SELECT since the busy-wait is not supported with NIO
//
//                    case SelectStrategy.SELECT:
//                        // 获取下一个超时事件
//                        long curDeadlineNanos = nextScheduledTaskDeadlineNanos();
//                        if (curDeadlineNanos == -1L) {
//                            curDeadlineNanos = NONE; // nothing on the calendar
//                        }
//                        nextWakeupNanos.set(curDeadlineNanos);
//                        try {
//                            // 如果没有任务就开始select事件，并返回接受数量
//                            if (!hasTasks()) {
//                                strategy = select(curDeadlineNanos);
//                            }
//                        } finally {
//                            // This update is just to help block unnecessary selector wakeups
//                            // so use of lazySet is ok (no race condition)
//                            nextWakeupNanos.lazySet(AWAKE);
//                        }
//                        // fall through
//                    default:
//                }
//            } catch (IOException e) {
//                // 异常处理，重新构建
//                // If we receive an IOException here its because the Selector is messed up. Let's rebuild
//                // the selector and retry. https://github.com/netty/netty/issues/8566
//                rebuildSelector0();
//                selectCnt = 0;
//                handleLoopException(e);
//                continue;
//            }
//
//            selectCnt++;
//            cancelledKeys = 0;
//            needsToSelectAgain = false;
//            // 这个就是io超时的比例，默认时50，也就是50%，可以设置，需要介于0<ioRatio<=100
//            final int ioRatio = this.ioRatio;
//            boolean ranTasks;
//            // 如果等于100执行这个，与默认的区别就是runAllTasks没有超时时间
//            if (ioRatio == 100) {
//                try {
//                    if (strategy > 0) {
//                        // 对selectKey也就是已有连接进行处理（调用过程以及分析过这个源码）
//                        processSelectedKeys();
//                    }
//                } finally {
//                    // 没有超时的执行所有任务
//                    // Ensure we always run tasks.
//                    ranTasks = runAllTasks();
//                }
//            } else if (strategy > 0) {
//                final long ioStartTime = System.nanoTime();
//                try {
//                    processSelectedKeys();
//                } finally {
//                    // Ensure we always run tasks.
//                    final long ioTime = System.nanoTime() - ioStartTime;
//                    // 按照IoRatio的比例执行runAllTasks，默认IO任务的时间与非I/O任务（processSelectedKeys）时间是相同的
//                    // 如果非IO任务很多，那么就将ioRatio调小一点，这一样非IO任务就能执行的时间长一点
//                    ranTasks = runAllTasks(ioTime * (100 - ioRatio) / ioRatio);
//                }
//            } else {
//                // 最小化的执行任务
//                ranTasks = runAllTasks(0); // This will run the minimum number of tasks
//            }
//
//            if (ranTasks || strategy > 0) {
//                if (selectCnt > MIN_PREMATURE_SELECTOR_RETURNS && logger.isDebugEnabled()) {
//                    logger.debug("Selector.select() returned prematurely {} times in a row for Selector {}.",
//                            selectCnt - 1, selector);
//                }
//                selectCnt = 0;
//            } else if (unexpectedSelectorWakeup(selectCnt)) { // Unexpected wakeup (unusual case)
//                selectCnt = 0;
//            }
//        } catch (CancelledKeyException e) {
//            // Harmless exception - log anyway
//            if (logger.isDebugEnabled()) {
//                logger.debug(CancelledKeyException.class.getSimpleName() + " raised by a Selector {} - JDK bug?",
//                        selector, e);
//            }
//        } catch (Throwable t) {
//            handleLoopException(t);
//        }
//        // Always handle shutdown even if the loop processing threw an exception.
//        try {
//            if (isShuttingDown()) {
//                closeAll();
//                if (confirmShutdown()) {
//                    return;
//                }
//            }
//        } catch (Throwable t) {
//            handleLoopException(t);
//        }
//    }
//}
//
//// 第二步： NioEventLoop
//private int select(long deadlineNanos) throws IOException {
//    if (deadlineNanos == NONE) {
//        return selector.select();
//    }
//    // Timeout will only be 0 if deadline is within 5 microsecs
//    long timeoutMillis = deadlineToDelayNanos(deadlineNanos + 995000L) / 1000000L;
//    return timeoutMillis <= 0 ? selector.selectNow() : selector.select(timeoutMillis);
//}
//
//// 第三步：SingleThreadEventExecutor 执行所有任务，设置超时时间
//protected boolean runAllTasks(long timeoutNanos) {
//    // 进入第四步，获取所有待执行任务
//    fetchFromScheduledTaskQueue();
//    Runnable task = pollTask();
//    // 判断是否优待执行任务
//    if (task == null) {
//        afterRunningAllTasks();
//        return false;
//    }
//    // 设置超时时间，也就是超过这个时间，就会终止执行任务
//    final long deadline = timeoutNanos > 0 ? ScheduledFutureTask.nanoTime() + timeoutNanos : 0;
//    long runTasks = 0;
//    long lastExecutionTime;
//    for (;;) {
//        // 执行任务，进入第六步
//        safeExecute(task);
//        runTasks ++;
//
//        // 如果中兴了64个任务，就查看是否已经超时
//        // Check timeout every 64 tasks because nanoTime() is relatively expensive.
//        // XXX: Hard-coded value - will make it configurable if it is really a problem.
//        if ((runTasks & 0x3F) == 0) {
//            lastExecutionTime = ScheduledFutureTask.nanoTime();
//            if (lastExecutionTime >= deadline) {
//                break;
//            }
//        }
//
//        task = pollTask();
//        // 判断是否还有下一个任务
//        if (task == null) {
//            lastExecutionTime = ScheduledFutureTask.nanoTime();
//            break;
//        }
//    }
//
//    afterRunningAllTasks();
//    // 设置最后执行任务的时间
//    this.lastExecutionTime = lastExecutionTime;
//    return true;
//}
//
//// 第三步：SingleThreadEventExecutor 执行所有任务,无超时时间
//protected boolean runAllTasks() {
//    assert inEventLoop();
//    boolean fetchedAll;
//    boolean ranAtLeastOne = false;
//
//    do {
//        // 判断是否还继续向taskQueue添加任务，也就是task是否全部添加到taskQueue，第四步
//        fetchedAll = fetchFromScheduledTaskQueue();
//        // 执行任务，内部时循环执行的。进入第五步
//        if (runAllTasksFrom(taskQueue)) {
//            ranAtLeastOne = true;
//        }
//    } while (!fetchedAll); // 获取所有的scheduled的task
//
//    if (ranAtLeastOne) {
//        // 设置最后一次执行完任务的时间
//        lastExecutionTime = ScheduledFutureTask.nanoTime();
//    }
//    // 任务执行完毕
//    afterRunningAllTasks();
//    // 返回第一步
//    return ranAtLeastOne;
//}
//
//// 第四步： SingleThreadEventExecutor，将scheduledTaskQueue的任务添加到taskQueue
//private boolean fetchFromScheduledTaskQueue() {
//    if (scheduledTaskQueue == null || scheduledTaskQueue.isEmpty()) {
//        return true;
//    }
//    long nanoTime = AbstractScheduledEventExecutor.nanoTime();
//    for (;;) {
//        Runnable scheduledTask = pollScheduledTask(nanoTime);
//        if (scheduledTask == null) {
//            return true;
//        }
//        if (!taskQueue.offer(scheduledTask)) {
//            // 也就是taskQueue没有空间了，先存起来，以后执行
//            // No space left in the task queue add it back to the scheduledTaskQueue so we pick it up again.
//            scheduledTaskQueue.add((ScheduledFutureTask<?>) scheduledTask);
//            return false;
//        }
//    }
//    // 最终返回第三步
//}
//
//// 第五步： SingleThreadEventExecutor 执行所有任务，在指定的队列中
//protected final boolean runAllTasksFrom(Queue<Runnable> taskQueue) {
//    Runnable task = pollTaskFrom(taskQueue);
//    if (task == null) {
//        return false;
//    }
//    for (;;) {
//        // 执行任务，进入第六步
//        safeExecute(task);
//        task = pollTaskFrom(taskQueue);
//        if (task == null) {
//            return true;
//        }
//    }
//    // 任务执行完毕，进入没有超时时间的第三步
//}
//
//// 第六步： AbstractEventExecutor 执行任务
//protected static void safeExecute(Runnable task) {
//    try {
//        task.run();
//    } catch (Throwable t) {
//        logger.warn("A task raised an exception. Task: {}", task, t);
//    }
//}
