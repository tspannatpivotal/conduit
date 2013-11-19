package conduit.amqp;

import conduit.transport.TransportPublishProperties;

public class AMQPPublishProperties implements TransportPublishProperties {
    private String exchange;
    private String routingKey;
    private long timeout;

    public AMQPPublishProperties(String exchange, String routingKey, long timeout) {
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.timeout = timeout;
    }

    public AMQPPublishProperties(String exchange, String routingKey) {
        this(exchange, routingKey, 100);
    }

    public String getExchange() {
        return exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public long getTimeout() {
        return timeout;
    }
}
