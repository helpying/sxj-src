package com.sxj.redis.advance;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sxj.redis.advance.core.RLock;

public class RedisLockTest
{
    
    RedisConcurrent concurrent;
    
    @Before
    public void before()
    {
        concurrent = RedisConcurrent.create();
    }
    
    @After
    public void after()
    {
        try
        {
            //            redis.flushdb();
        }
        finally
        {
            //redis.shutdown();
        }
    }
    
    @Test
    public void testExpire() throws InterruptedException
    {
        final RLock lock = concurrent.getLock("lock1");
        lock.lock();
        System.out.println("Gain Lock Name: " + lock.getName() + "By Thread: "
                + Thread.currentThread().getId());
        final long startTime = System.currentTimeMillis();
        new Thread()
        {
            public void run()
            {
                RLock lock1 = concurrent.getLock("lock1");
                lock1.lock();
                System.out.println("-------Gain Lock Name: " + lock.getName()
                        + "By Thread: " + Thread.currentThread().getId());
                lock1.unlock();
                System.out.println("--------Trying Unlock "
                        + concurrent.getLock("lock1").getName() + "By Thread:"
                        + currentThread().getId());
            };
        }.start();
        Thread.currentThread().sleep(1000);
        new Thread()
        {
            
            @Override
            public void run()
            {
                System.out.println("Trying Unlock "
                        + concurrent.getLock("lock1").getName() + "By Thread:"
                        + Thread.currentThread().getId());
                concurrent.getLock("lock1").unlock();
            }
            
        }.start();
        lock.unlock();
        System.out.println("Trying Unlock "
                + concurrent.getLock("lock1").getName() + "By Thread:"
                + Thread.currentThread().getId());
        //        lock.unlock();
        while (true)
        {
            
        }
    }
    
    public void testGetHoldCount()
    {
        RLock lock = concurrent.getLock("lock");
        Assert.assertEquals(0, lock.getHoldCount());
        lock.lock();
        Assert.assertEquals(1, lock.getHoldCount());
        lock.unlock();
        Assert.assertEquals(0, lock.getHoldCount());
        
        lock.lock();
        lock.lock();
        Assert.assertEquals(2, lock.getHoldCount());
        lock.unlock();
        Assert.assertEquals(1, lock.getHoldCount());
        lock.unlock();
        Assert.assertEquals(0, lock.getHoldCount());
    }
    
    public void testIsHeldByCurrentThreadOtherThread()
            throws InterruptedException
    {
        RLock lock = concurrent.getLock("lock");
        lock.lock();
        
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread()
        {
            public void run()
            {
                RLock lock = concurrent.getLock("lock");
                Assert.assertFalse(lock.isHeldByCurrentThread());
                latch.countDown();
            };
        }.start();
        
        latch.await();
        lock.unlock();
        
        final CountDownLatch latch2 = new CountDownLatch(1);
        new Thread()
        {
            public void run()
            {
                RLock lock = concurrent.getLock("lock");
                Assert.assertFalse(lock.isHeldByCurrentThread());
                latch2.countDown();
            };
        }.start();
        
        latch2.await();
    }
    
    public void testIsHeldByCurrentThread()
    {
        RLock lock = concurrent.getLock("lock");
        Assert.assertFalse(lock.isHeldByCurrentThread());
        lock.lock();
        Assert.assertTrue(lock.isHeldByCurrentThread());
        lock.unlock();
        Assert.assertFalse(lock.isHeldByCurrentThread());
    }
    
    public void testIsLockedOtherThread() throws InterruptedException
    {
        RLock lock = concurrent.getLock("lock");
        lock.lock();
        
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread()
        {
            public void run()
            {
                RLock lock = concurrent.getLock("lock");
                Assert.assertTrue(lock.isLocked());
                latch.countDown();
            };
        }.start();
        
        latch.await();
        lock.unlock();
        
        final CountDownLatch latch2 = new CountDownLatch(1);
        new Thread()
        {
            public void run()
            {
                RLock lock = concurrent.getLock("lock");
                Assert.assertFalse(lock.isLocked());
                latch2.countDown();
            };
        }.start();
        
        latch2.await();
    }
    
    public void testIsLocked()
    {
        RLock lock = concurrent.getLock("lock");
        Assert.assertFalse(lock.isLocked());
        lock.lock();
        Assert.assertTrue(lock.isLocked());
        lock.unlock();
        Assert.assertFalse(lock.isLocked());
    }
    
    //    @Test(expected = IllegalMonitorStateException.class)
    //    public void testUnlockFail() {
    //        Lock lock = redisson.getLock("lock1");
    //        lock.unlock();
    //    }
    //
    //
    public void testLockUnlock()
    {
        Lock lock = concurrent.getLock("lock1");
        lock.lock();
        lock.unlock();
        
        lock.lock();
        lock.unlock();
    }
    
    public void testReentrancy()
    {
        Lock lock = concurrent.getLock("lock1");
        lock.lock();
        lock.lock();
        lock.unlock();
        lock.unlock();
    }
    
    //    @Test
    //    public void testConcurrency_SingleInstance() throws InterruptedException
    //    {
    //        final AtomicInteger lockedCounter = new AtomicInteger();
    //        
    //        int iterations = 15;
    //        testSingleInstanceConcurrency(iterations, new RedissonRunnable()
    //        {
    //            @Override
    //            public void run(Redis redisson)
    //            {
    //                Lock lock = redisson.getLock("testConcurrency_SingleInstance");
    //                System.out.println("lock1 " + Thread.currentThread().getId());
    //                lock.lock();
    //                System.out.println("lock2 " + Thread.currentThread().getId());
    //                lockedCounter.set(lockedCounter.get() + 1);
    //                System.out.println("lockedCounter " + lockedCounter);
    //                System.out.println("unlock1 " + Thread.currentThread().getId());
    //                lock.unlock();
    //                System.out.println("unlock2 " + Thread.currentThread().getId());
    //            }
    //        });
    //        
    //        Assert.assertEquals(iterations, lockedCounter.get());
    //    }
    //    
    //    @Test
    //    public void testConcurrencyLoop_MultiInstance() throws InterruptedException
    //    {
    //        final int iterations = 100;
    //        final AtomicInteger lockedCounter = new AtomicInteger();
    //        
    //        testMultiInstanceConcurrency(16, new RedissonRunnable()
    //        {
    //            @Override
    //            public void run(Redis redisson)
    //            {
    //                for (int i = 0; i < iterations; i++)
    //                {
    //                    redisson.getLock("testConcurrency_MultiInstance1").lock();
    //                    try
    //                    {
    //                        Thread.sleep(10);
    //                    }
    //                    catch (InterruptedException e)
    //                    {
    //                        e.printStackTrace();
    //                    }
    //                    lockedCounter.set(lockedCounter.get() + 1);
    //                    redisson.getLock("testConcurrency_MultiInstance1").unlock();
    //                }
    //            }
    //        });
    //        
    //        Assert.assertEquals(16 * iterations, lockedCounter.get());
    //    }
    //    
    //    @Test
    //    public void testConcurrency_MultiInstance() throws InterruptedException
    //    {
    //        int iterations = 100;
    //        final AtomicInteger lockedCounter = new AtomicInteger();
    //        
    //        testMultiInstanceConcurrency(iterations, new RedissonRunnable()
    //        {
    //            @Override
    //            public void run(Redis redisson)
    //            {
    //                Lock lock = redisson.getLock("testConcurrency_MultiInstance2");
    //                lock.lock();
    //                lockedCounter.set(lockedCounter.get() + 1);
    //                lock.unlock();
    //            }
    //        });
    //        
    //        Assert.assertEquals(iterations, lockedCounter.get());
    //    }
    
}
