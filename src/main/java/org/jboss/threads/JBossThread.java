/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.threads;

import org.jboss.logging.Logger;

/**
 * A JBoss thread.  Supports logging and extra operations.
 */
public final class JBossThread extends Thread {
    private static final Logger log = Logger.getLogger("org.jboss.threads");
    private static final Logger ihlog = Logger.getLogger("org.jboss.threads.interrupt-handler");

    private volatile InterruptHandler interruptHandler;
    private ThreadNameInfo threadNameInfo;

    /**
     * Construct a new instance.
     *
     * @param target the runnable target
     * @see #Thread(Runnable)
     */
    public JBossThread(final Runnable target) {
        super(target);
    }

    /**
     * Construct a new instance.
     *
     * @param target the runnable target
     * @param name the initial thread name
     * @see #Thread(Runnable, String)
     */
    public JBossThread(final Runnable target, final String name) {
        super(target, name);
    }

    /**
     * Construct a new instance.
     *
     * @param group the parent thread group
     * @param target the runnable target
     * @see #Thread(ThreadGroup, Runnable)
     * @throws SecurityException if the current thread cannot create a thread in the specified thread group
     */
    public JBossThread(final ThreadGroup group, final Runnable target) throws SecurityException {
        super(group, target);
    }

    /**
     * Construct a new instance.
     *
     * @param group the parent thread group
     * @param target the runnable target
     * @param name the initial thread name
     * @see #Thread(ThreadGroup,Runnable,String)
     * @throws SecurityException if the current thread cannot create a thread in the specified thread group
     */
    public JBossThread(final ThreadGroup group, final Runnable target, final String name) throws SecurityException {
        super(group, target, name);
    }

    /**
     * Construct a new instance.
     *
     * @param group the parent thread group
     * @param target the runnable target
     * @param name the initial thread name
     * @see #Thread(ThreadGroup,Runnable,String,long)
     * @throws SecurityException if the current thread cannot create a thread in the specified thread group
     */
    public JBossThread(final ThreadGroup group, final Runnable target, final String name, final long stackSize) throws SecurityException {
        super(group, target, name, stackSize);
    }

    /**
     * Interrupt this thread.  Logs a trace message and calls the current interrupt handler, if any.  The interrupt
     * handler is called from the <em>calling</em> thread, not the thread being interrupted.
     */
    public void interrupt() {
        ihlog.tracef("Interrupting thread \"%s\"", this);
        try {
            super.interrupt();
        } finally {
            final InterruptHandler interruptHandler = this.interruptHandler;
            if (interruptHandler != null) {
                try {
                    interruptHandler.handleInterrupt(this);
                } catch (Throwable t) {
                    ihlog.errorf(t, "Interrupt handler %s threw an exception", interruptHandler);
                }
            }
        }
    }

    /**
     * Execute the thread's {@code Runnable}.  Logs a trace message at the start and end of execution.
     */
    public void run() {
        log.tracef("Thread \"%s\" starting execution", this);
        try {
            super.run();
        } finally {
            log.tracef("Thread \"%s\" exiting", this);
        }
    }

    /**
     * Get the current {@code JBossThread}, or {@code null} if the current thread is not a {@code JBossThread}.
     *
     * @return the current thread, or {@code null}
     */
    public static JBossThread currentThread() {
        final Thread thread = Thread.currentThread();
        return thread instanceof JBossThread ? (JBossThread) thread : null;
    }

    /**
     * Start the thread.
     *
     * @throws IllegalThreadStateException if the thread was already started.
     */
    public void start() {
        super.start();
        log.tracef("Started thread \"%s\"", this);
    }

    /**
     * Change the uncaught exception handler for this thread.
     *
     * @param eh the new handler
     */
    public void setUncaughtExceptionHandler(final UncaughtExceptionHandler eh) {
        super.setUncaughtExceptionHandler(eh);
        log.tracef("Changed uncaught exception handler for \"%s\" to %s", this, eh);
    }

    /**
     * Swap the current thread's active interrupt handler.  Most callers should restore the old handler in a {@code finally}
     * block like this:
     * <pre>
     * InterruptHandler oldHandler = JBossThread.getAndSetInterruptHandler(newHandler);
     * try {
     *     ...execute interrupt-sensitive operation...
     * } finally {
     *     JBossThread.getAndSetInterruptHandler(oldHandler);
     * }
     * </pre>
     *
     * @param newInterruptHandler the new interrupt handler
     * @return the old interrupt handler
     */
    public static InterruptHandler getAndSetInterruptHandler(final InterruptHandler newInterruptHandler) {
        final JBossThread thread = currentThread();
        if (thread == null) {
            throw new IllegalStateException("The current thread does not support interrupt handlers");
        }
        try {
            return thread.interruptHandler;
        } finally {
            thread.interruptHandler = newInterruptHandler;
        }
    }

    /**
     * Get the thread name information.  This includes information about the thread's sequence number and so forth.
     *
     * @return the thread name info
     */
    public ThreadNameInfo getThreadNameInfo() {
        return threadNameInfo;
    }

    /**
     * Set the thread name information.  This includes information about the thread's sequence number and so forth.
     *
     * @param threadNameInfo the new thread name info
     * @throws SecurityException if the calling thread is not allowed to modify this thread
     */
    public void setThreadNameInfo(final ThreadNameInfo threadNameInfo) throws SecurityException {
        checkAccess();
        this.threadNameInfo = threadNameInfo;
    }
}
