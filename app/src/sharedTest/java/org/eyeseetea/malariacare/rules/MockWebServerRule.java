package org.eyeseetea.malariacare.rules;

import org.eyeseetea.malariacare.data.file.IFileReader;
import org.eyeseetea.malariacare.data.server.CustomMockServer;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.IOException;

public class MockWebServerRule implements TestRule {

    private CustomMockServer mCustomMockServer;

    private IFileReader fileReader;

    public MockWebServerRule(IFileReader fileReader){
        this.fileReader = fileReader;
    }

    private void before() throws IOException {
        mCustomMockServer = new CustomMockServer(fileReader);
    }

    private void after() throws IOException {
        mCustomMockServer.shutdown();
    }

    public CustomMockServer getMockServer() {
        return mCustomMockServer;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before();

                base.evaluate();

                after();
            }
        };
    }

}
