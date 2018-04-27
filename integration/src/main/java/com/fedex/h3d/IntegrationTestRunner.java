package com.fedex.h3d;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import java.io.File;
import java.util.function.Supplier;

public class IntegrationTestRunner extends BlockJUnit4ClassRunner {


    public IntegrationTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    public void run(RunNotifier notifier) {
        notifier.addListener(new IntegrationTestListener(notifier));
        notifier.fireTestRunStarted(getDescription());
        super.run(notifier);
    }

    class IntegrationTestListener extends RunListener {
        Process serverProcess;
        Process clientBuildProcess;
        RunNotifier notifier;

        IntegrationTestListener(RunNotifier notifier) {
            this.notifier = notifier;
        }

        @Override
        public void testRunStarted(Description description) throws Exception {
            ProcessBuilder buildClient = new ProcessBuilder();
            buildClient.directory(new File("../client"));
            buildClient.command("npm", "run", "build");
            clientBuildProcess = buildClient.start();

            waitFor(() -> !clientBuildProcess.isAlive(), "Waiting for client to build..");

            ProcessBuilder startServer = new ProcessBuilder();
            startServer.directory(new File("../server"));
            startServer.command("./gradlew", "bootRun");
            serverProcess = startServer.start();


            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url("http://localhost:8080/health").build();
            boolean serverCameUp = waitFor(() -> {
                try {
                    Response execute = client.newCall(request).execute();
                    return execute.code() == 200;
                } catch (Exception e) {
                    return false;
                }
            }, "Waiting for app to boot..");

            if (serverCameUp) {
                super.testRunStarted(description);
                return;
            }

            serverProcess.destroy();
            waitFor(() -> !serverProcess.isAlive(), "Waiting for server to DIE..");
            this.notifier.pleaseStop();
            System.out.println("The application never came up!");
        }

        private boolean waitFor(Supplier<Boolean> eventHasHappened, String s) throws InterruptedException {
            int attempts = 20;
            Boolean eventHasNotHappened;
            while ((eventHasNotHappened = !eventHasHappened.get()) && attempts-- > 0) {
                System.out.print("\r" + s + " " + attempts);
                Thread.sleep(1000);
            }
            System.out.println();
            return !eventHasNotHappened;
        }

        @Override
        public void testRunFinished(Result result) throws Exception {
            serverProcess.destroy();
            waitFor(() -> !serverProcess.isAlive(), "Waiting for server to DIE..");
            super.testRunFinished(result);
        }

    }
}


