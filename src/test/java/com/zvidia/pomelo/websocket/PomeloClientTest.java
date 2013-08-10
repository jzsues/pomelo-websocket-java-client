package com.zvidia.pomelo.websocket;

import com.zvidia.pomelo.exception.PomeloException;
import com.zvidia.pomelo.protocol.PomeloMessage;
import org.json.JSONObject;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: jiangzm
 * Date: 13-8-8
 * Time: 下午10:14
 * To change this template use File | Settings | File Templates.
 */
public class PomeloClientTest {
    public static void assertConcurrent(final String message, final List<? extends Runnable> runnables, final int maxTimeoutSeconds) throws InterruptedException {
        final int numThreads = runnables.size();
        final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<Throwable>());
        final ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
        try {
            final CountDownLatch allExecutorThreadsReady = new CountDownLatch(numThreads);
            final CountDownLatch afterInitBlocker = new CountDownLatch(1);
            final CountDownLatch allDone = new CountDownLatch(numThreads);
            for (final Runnable submittedTestRunnable : runnables) {
                threadPool.submit(new Runnable() {
                    public void run() {
                        allExecutorThreadsReady.countDown();
                        try {
                            afterInitBlocker.await();
                            submittedTestRunnable.run();
                        } catch (final Throwable e) {
                            exceptions.add(e);
                        } finally {
                            allDone.countDown();
                        }
                    }
                });
            }
            // wait until all threads are ready
            assertTrue("Timeout initializing threads! Perform long lasting initializations before passing runnables to assertConcurrent", allExecutorThreadsReady.await(runnables.size() * 10, TimeUnit.MILLISECONDS));
            // start all test runners
            afterInitBlocker.countDown();
            assertTrue(message + " timeout! More than" + maxTimeoutSeconds + "seconds", allDone.await(maxTimeoutSeconds, TimeUnit.SECONDS));
        } finally {
            threadPool.shutdownNow();
        }
        assertTrue(message + "failed with exception(s)" + exceptions, exceptions.isEmpty());
    }

    @Test
    public void testConnect() throws InterruptedException {
        try {
            PomeloClient client = new PomeloClient(new URI("ws://localhost:3014"));
//            List<Runnable> runs = new ArrayList<Runnable>();
//            runs.add(client);
//            PomeloClientTest.assertConcurrent("test websocket client", runs, 200);
            client.connect();
            int i = 0;
            while (i < 10) {
                try {
                    boolean connected = client.isConnected();
                    if (connected) {
                        JSONObject json = new JSONObject();
                        json.put("uid", 1);
                        client.request("gate.gateHandler.queryEntry", json.toString(), new OnDataHandler() {
                            @Override
                            public void onData(PomeloMessage.Message message) {
                                System.out.println(message.toString());
                            }
                        });
                    }
                } catch (PomeloException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                i++;
                Thread.sleep(100);
            }
            client.close();
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
