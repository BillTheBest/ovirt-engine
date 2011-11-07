package org.ovirt.engine.core.utils.timer;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

public class SchedulerUtilQuartsImplTest {

    public static final String onMethod1 = "onTimer1";
    public static final String onMethod2 = "onTimer2";

    // private final Log log = LogFactory.getLog(SchedulerUtilTest.class);
    private static List<String> messages;
    private static SchedulerUtilQuartzImpl scheduler;

    @BeforeClass
    public static void init() {
        // scheduler = SchedulerUtilQuartzImpl.getInstance();
        scheduler = new SchedulerUtilQuartzImpl();
        scheduler.setup();
        messages = new ArrayList<String>();
    }

    @AfterClass
    public static void tearDown() {
        scheduler.teardown();
    }

    @Before
    public void setUp() {
        messages.clear();
    }

    // disabling this test, as it is prone to races and fails periodically.
    // leaving its logic in case someone wants to manually test a similar case
    // while taking into account the potential races
    @Ignore
    @Test
    public void oneTimeJob() throws SchedulerException {
        PrintJob pj = new PrintJob();
        long startTestDate = System.currentTimeMillis();
        System.out.println("Start Time=" + startTestDate);
        scheduler.scheduleAOneTimeJob(pj, "onTimer11", new Class[0], new Object[0], 10, TimeUnit.MILLISECONDS);
        try {
            Thread.sleep(50);
        } catch (InterruptedException ie) {
            // log.error("sleep was interrupted", ie);
        }
        assertEquals("Number of time job was acitivated", 1, messages.size());
    }

    @Ignore
    @Test
    public void deleteAJob() throws SchedulerException {
        PrintJob pj = new PrintJob();
        Date startTestDate = new Date();
        System.out.println("Start Time=" + startTestDate);
        String jobId =
                scheduler.scheduleAFixedDelayJob(pj,
                        "onTimer11",
                        new Class[0],
                        new Object[0],
                        1,
                        3,
                        TimeUnit.MILLISECONDS);
        String[] jobs = scheduler.getRawScheduler().getJobNames(Scheduler.DEFAULT_GROUP);
        assertEquals("Number of scheduled jobs", 1, jobs.length);
        // delete a valid job
        scheduler.deleteJob(jobId);
        jobs = scheduler.getRawScheduler().getJobNames(Scheduler.DEFAULT_GROUP);
        assertEquals("Number of scheduled jobs", 0, jobs.length);

        // delete invalid job
        scheduler.deleteJob("nojob");
    }

    /*
     * Schedule one fixed delay job with 3 seconds between successive executions
     */
    @Ignore
    @Test
    public void fixedDelayJob() {
        PrintJob pj = new PrintJob();
        long startTestDate = System.currentTimeMillis();
        System.out.println("Start Time=" + startTestDate);
        String jobId =
                scheduler.scheduleAFixedDelayJob(pj,
                        "onTimer11",
                        new Class[0],
                        new Object[0],
                        0,
                        10,
                        TimeUnit.MILLISECONDS);
        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
            // log.error("sleep was interrupted", ie);
        }
        scheduler.pauseJob(jobId);
        long endTestDate = System.currentTimeMillis();
        System.out.println("Done Time=" + endTestDate);
        // int numberOfCalls = (int)(endTestDate.getTime() -
        // startTestDate.getTime())/(3);
        // assertEquals("Number of scheduled jobs", numberOfCalls+1,
        // messages.size());
        boolean test = messages.size() > 1;
        assertEquals("Number of scheduled jobs should be at least 2 but is " + messages.size(), true, test);

    }

    /*
     * Schedule 2 fixed delay job with 3 seconds between successive executions
     */
    @Ignore
    @Test
    public void fixedDelayJob2() {
        PrintJob pj = new PrintJob();
        long startTestDate = System.currentTimeMillis();
        System.out.println("Start Time=" + startTestDate);
        String jobId1 =
                scheduler.scheduleAFixedDelayJob(pj,
                        "onTimer11",
                        new Class[0],
                        new Object[0],
                        0,
                        3,
                        TimeUnit.MILLISECONDS);
        String jobId2 =
                scheduler.scheduleAFixedDelayJob(pj,
                        "onTimer2",
                        new Class[0],
                        new Object[0],
                        1,
                        3,
                        TimeUnit.MILLISECONDS);
        try {
            Thread.sleep(50);
        } catch (InterruptedException ie) {
            // log.error("sleep was interrupted", ie);
        }
        scheduler.pauseJob(jobId1);
        scheduler.pauseJob(jobId2);
        long endTestDate = System.currentTimeMillis();
        System.out.println("Done Time=" + endTestDate);
        boolean test = messages.size() > 3;
        assertEquals("Number of scheduled jobs should be at least 4 but is " + messages.size(), true, test);

        boolean runOnTimer1 = messages.contains(onMethod1);
        assertEquals("The first timer run ", true, runOnTimer1);
        messages.remove(onMethod1);
        runOnTimer1 = messages.contains(onMethod1);
        assertEquals("The first timer run at least twice", true, runOnTimer1);

        boolean runOnTimer2 = messages.contains(onMethod2);
        assertEquals("The second timer run ", true, runOnTimer2);
        messages.remove(onMethod2);
        runOnTimer2 = messages.contains(onMethod2);
        assertEquals("The second timer run at least twice", true, runOnTimer2);

    }

    /*
     * test the pause and resume functionality
     */
    // disabling this test, as it is prone to races and fails periodically.
    // leaving its logic in case someone wants to manually test a similar case
    // while taking into account the potential races
    // @Test
    public void testPauseResumeJob() {
        PrintJob pj = new PrintJob();
        long startTestDate = System.currentTimeMillis();
        System.out.println("Start Time=" + startTestDate);
        String jobId =
                scheduler.scheduleAFixedDelayJob(pj,
                        "onTimer11",
                        new Class[0],
                        new Object[0],
                        1,
                        8,
                        TimeUnit.MILLISECONDS);
        try {
            Thread.sleep(20);
        } catch (InterruptedException ie) {
            // log.error("sleep was interrupted", ie);
        }
        scheduler.pauseJob(jobId);
        int numberOfRunStart = messages.size();
        try {
            Thread.sleep(20);
        } catch (InterruptedException ie) {
            // log.error("sleep was interrupted", ie);
        }
        int numberOfRunEnd = messages.size();
        assertEquals("Number of scheduled jobs", numberOfRunStart, numberOfRunEnd);

        scheduler.resumeJob(jobId);
        try {
            Thread.sleep(20);
        } catch (InterruptedException ie) {
            // log.error("sleep was interrupted", ie);
        }
        scheduler.pauseJob(jobId);
        int numberOfRunAfterResume = messages.size();

        // test if during resume the job was executed again
        boolean testResume = numberOfRunAfterResume > numberOfRunEnd;
        assertEquals("Number of scheduled jobs after resume should be higher than before", true, testResume);
    }

    // @Test
    @SuppressWarnings("unchecked")
    public void tryReflection() {
        try {
            PrintJob job = new PrintJob();
            String methodName = "onTimer2";
            Class[] inputType = new Class[1];
            inputType[0] = String.class;

            Object[] input = new Object[1];
            input[0] = new String("livnat");

            Method methodToRun = job.getClass().getDeclaredMethod(methodName, inputType);

            methodToRun.invoke(job, input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    // disabling this test, as it is prone to races and fails periodically.
    // leaving its logic in case someone wants to manually test a similar case
    // while taking into account the potential races
    // @Test
    public void aOneTimeJobWithMethodParams() {
        PrintJob pj = new PrintJob();
        Class[] inputType = new Class[2];
        inputType[0] = String.class;
        inputType[1] = Integer.class;

        Object[] input = new Object[2];
        input[0] = new String("msg1");
        input[1] = new Integer(5);
        scheduler.scheduleAOneTimeJob(pj, "onTimerWithParam", inputType, input, 1, TimeUnit.MILLISECONDS);
        try {
            Thread.sleep(50);
        } catch (InterruptedException ie) {
            // log.error("sleep was interrupted", ie);
        }
        boolean testNumberOfMessages = messages.size() >= 2;
        assertEquals("Number of messages should be at least 2 but was " + messages.size(), true, testNumberOfMessages);
        assertEquals("The first msg should be msg1 but is " + messages.get(0), "msg1", messages.get(0));
        assertEquals("The second msg should be 5 but is " + messages.get(1), "5", messages.get(1));
    }

    @SuppressWarnings("unchecked")
    // disabling this test, as it is prone to races and fails periodically.
    // leaving its logic in case someone wants to manually test a similar case
    // while taking into account the potential races
    @Ignore
    @Test
    public void aFixedDelayJobWithMethodParams() {
        PrintJob pj = new PrintJob();
        Class[] inputType = new Class[2];
        inputType[0] = String.class;
        inputType[1] = int.class;

        Object[] input = new Object[2];
        input[0] = new String("msg1");
        input[1] = 5;
        scheduler.scheduleAFixedDelayJob(pj, "onTimerWithParam", inputType, input, 1, 1, TimeUnit.MILLISECONDS);
        try {
            Thread.sleep(50);
        } catch (InterruptedException ie) {
            // log.error("sleep was interrupted", ie);
        }
        boolean testNumberOfMessages = messages.size() >= 2;
        assertEquals("Number of messages should be at least 2 but was " + messages.size(), true, testNumberOfMessages);
        assertEquals("The first msg should be msg1 but is " + messages.get(0), "msg1", messages.get(0));
        assertEquals("The second msg should be 5 but is " + messages.get(1), "5", messages.get(1));
    }

    private class PrintJob {
        private AtomicInteger state;

        public PrintJob() {
            state = new AtomicInteger();
        }

        @OnTimerMethodAnnotation("onTimer11")
        public void onTimer1() {
            int i = state.incrementAndGet();
            String msg = "onTimer1 state=" + i + " Time=" + System.currentTimeMillis();
            System.out.println(msg);
            messages.add(onMethod1);
        }

        @OnTimerMethodAnnotation("onTimer2")
        public void onTimer2() {
            int i = state.incrementAndGet();
            String msg = "onTimer2 state=" + i + " Time=" + System.currentTimeMillis();
            System.out.println(msg);
            messages.add(onMethod2);
        }

        @OnTimerMethodAnnotation("onTimerWithParam")
        public void onTimer3(String name1, Integer num) {
            state.incrementAndGet();
            messages.add(name1);
            messages.add(String.valueOf(num));
            System.out.println("in Timer 3 name: " + name1 + " num:" + num);
        }
    }
}
