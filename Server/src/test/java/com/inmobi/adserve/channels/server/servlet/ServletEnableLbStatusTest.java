package com.inmobi.adserve.channels.server.servlet;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.ServerStatusInfo;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;

@RunWith(PowerMockRunner.class)
public class ServletEnableLbStatusTest {

    @Test
    public void testHandleRequestLocalHost() throws Exception {
        final HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        final ResponseSender mockResponseSender = createMock(ResponseSender.class);

        expect(mockHttpRequestHandler.isRequestFromLocalHost()).andReturn(true).times(1);
        mockResponseSender.sendResponse("OK", null);
        expectLastCall().times(1);

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;

        final ServletEnableLbStatus tested = new ServletEnableLbStatus();
        tested.handleRequest(mockHttpRequestHandler, null, null);
        assertThat(ServerStatusInfo.getStatusCode(), is(equalTo(200)));
        assertThat(ServerStatusInfo.getStatusString(), is(equalTo("OK")));

        verifyAll();
    }

    @Test
    public void testHandleRequestNotFromLocalHost() throws Exception {
        final HttpRequestHandler mockHttpRequestHandler = createMock(HttpRequestHandler.class);
        final ResponseSender mockResponseSender = createMock(ResponseSender.class);

        expect(mockHttpRequestHandler.isRequestFromLocalHost()).andReturn(false).times(1);
        mockResponseSender.sendResponse("NOT AUTHORIZED", null);
        expectLastCall().times(1);

        replayAll();
        mockHttpRequestHandler.responseSender = mockResponseSender;

        final ServletEnableLbStatus tested = new ServletEnableLbStatus();
        tested.handleRequest(mockHttpRequestHandler, null, null);

        verifyAll();
    }

    @Test
    public void testGetName() throws Exception {
        final ServletEnableLbStatus tested = new ServletEnableLbStatus();
        assertThat(tested.getName(), is(IsEqual.equalTo("enablelbstatus")));
    }
}
