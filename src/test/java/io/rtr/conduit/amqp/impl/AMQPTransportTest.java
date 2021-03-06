package io.rtr.conduit.amqp.impl;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.impl.AMQImpl;
import io.rtr.conduit.amqp.AMQPConsumerCallback;
import io.rtr.conduit.amqp.AMQPMessageBundle;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class AMQPTransportTest {
    Channel channel;
    AMQPTransport amqpTransport;
    AMQPPublishProperties properties;
    AMQPMessageBundle messageBundle;

    @Before
    public void before() {
        amqpTransport = spy(new AMQPTransport("host", 1234));
        channel = mock(Channel.class);
        amqpTransport.setChannel(channel);

        properties = mock(AMQPPublishProperties.class);
        messageBundle = mock(AMQPMessageBundle.class);
    }

    @Test
    public void testConfirmModeDisabled() throws Exception {
        when(properties.isConfirmEnabled()).thenReturn(false);

        amqpTransport.publishImpl(messageBundle, properties);

        verify(channel, times(0)).confirmSelect();
        verify(channel, times(0)).waitForConfirms(anyLong());
    }

    @Test
    public void testConfirmModeEnabled() throws Exception {
        long timeout = 9876;
        when(properties.getTimeout()).thenReturn(timeout);
        when(properties.isConfirmEnabled()).thenReturn(true);
        when(channel.waitForConfirms(anyLong())).thenReturn(false);

        boolean result = amqpTransport.publishImpl(messageBundle, properties);

        assertFalse(result);
        verify(channel).confirmSelect();
        verify(channel).waitForConfirms(timeout);
    }

    @Test
    public void testListenImplDynamicQueues() throws IOException {
        String randoq = "randoq";
        String exchange = "exchange";
        String router = "router";

        AMQImpl.Queue.DeclareOk ok = mock(AMQImpl.Queue.DeclareOk.class);
        when(ok.getQueue()).thenReturn(randoq);
        when(channel.queueDeclare()).thenReturn(ok);
        amqpTransport.setChannel(channel);

        AMQPConsumerCallback consumerCallback = mock(AMQPConsumerCallback.class);

        AMQPCommonListenProperties commonListenProperties = AMQPSyncConsumerBuilder.builder()
                .callback(consumerCallback)
                .dynamicQueueCreation(true)
                .exchange(exchange)
                .dynamicQueueRoutingKey(router)
                .poisonQueueEnabled(Boolean.TRUE)
                .prefetchCount(1)
                .buildListenProperties();

        amqpTransport.listenImpl(commonListenProperties);
        verify(amqpTransport).getConsumer(consumerCallback, commonListenProperties, "." + randoq);
        verify(amqpTransport).createDynamicQueue(exchange, router, true);
        verify(channel).basicConsume(eq(randoq), eq(false), any(Consumer.class));
        verify(channel).basicQos(1);
    }

    @Test
    public void testListenImplDynamicQueuesPurgeOnConnect() throws IOException {
        String randoq = "randoq";
        String exchange = "exchange";
        String router = "router";

        AMQImpl.Queue.DeclareOk ok = mock(AMQImpl.Queue.DeclareOk.class);
        when(ok.getQueue()).thenReturn(randoq);
        when(channel.queueDeclare()).thenReturn(ok);
        amqpTransport.setChannel(channel);

        AMQPConsumerCallback consumerCallback = mock(AMQPConsumerCallback.class);
        AMQPCommonListenProperties commonListenProperties = AMQPSyncConsumerBuilder.builder()
                .callback(consumerCallback)
                .dynamicQueueCreation(true)
                .exchange(exchange)
                .purgeOnConnect(Boolean.TRUE)
                .dynamicQueueRoutingKey(router)
                .poisonQueueEnabled(Boolean.TRUE)
                .prefetchCount(1)
                .buildListenProperties();

        amqpTransport.listenImpl(commonListenProperties);
        verify(channel).queuePurge(randoq);
        verify(amqpTransport).getConsumer(consumerCallback, commonListenProperties, "." + randoq);
        verify(amqpTransport).createDynamicQueue(exchange, router, true);
        verify(channel).basicConsume(eq(randoq), eq(false), any(Consumer.class));
        verify(channel).basicQos(1);
    }

    @Test
    public void testCloseUsingConnectionFactoryTimeout() throws IOException {
        ConnectionFactory factory = mock(ConnectionFactory.class);
        int expectedTimeout = 5;
        when(factory.getConnectionTimeout()).thenReturn(expectedTimeout);
        Connection connection = mock(Connection.class);
        when(connection.isOpen()).thenReturn(true);

        amqpTransport.setConnectionFactory(factory);
        amqpTransport.setConnection(connection);

        amqpTransport.closeImpl();
        verify(connection).close(eq(expectedTimeout));
    }
}
