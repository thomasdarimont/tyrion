/*
 * Copyright (c) 2013, Pierre Laporte
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this work; if not, see <http://www.gnu.org/licenses/>.
 */

package fr.pingtimeout.tyrion;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class LockProfilingAgent {

    /**
     * JVM hook to statically load the javaagent at startup.
     * <p/>
     * After the Java Virtual Machine (JVM) has initialized, the premain method
     * will be called. Then the real application main method will be called.
     *
     * @param args The agent's arguments, not used
     * @param inst The instrumentation class that will be used
     * @throws Exception
     */
    public static void premain(String args, Instrumentation inst) throws Exception {
        new LockInterceptor();
        String arguments = args == null ? "" : args;
        Logger.info("Tyrion agent starting with arguments '%s'", arguments);

        if (arguments.startsWith("outputFile=")) {
            final String outputFile = arguments.substring("outputFile=".length());

//            if (LocksStatisticsCollector.createInstanceAndRegisterAsMXBean()) {
//                Logger.info("Statistics succesfully registered as JMX bean");
//            }

            clearOutputFile(outputFile);
            scheduleLocksWrite(outputFile);

            addLocksTransformer(inst);
        } else {
            Logger.warn("No output file was provided, agent is disabled");
        }

    }

    private static void clearOutputFile(String outputFile) {
        try(FileOutputStream erasor = new FileOutputStream(outputFile)) {
            erasor.write("".getBytes(Charset.forName("UTF-8")));
        } catch (IOException e) {
            Logger.warn("Output file could not be cleared. Cause : %s", e.getMessage());
            Logger.debug(e);
        }
    }

    private static void addLocksTransformer(Instrumentation inst) {
        inst.addTransformer(new LocksTransformer());
    }

    private static void scheduleLocksWrite(String outputFile) {
        final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "Tyrion locks writer");
                t.setDaemon(true);
                return t;
            }
        });
        executorService.scheduleAtFixedRate(new EventsWriter(outputFile), 1, 10, TimeUnit.SECONDS);
    }
}