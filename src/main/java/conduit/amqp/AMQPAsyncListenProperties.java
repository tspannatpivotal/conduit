package conduit.amqp;


import conduit.transport.TransportListenProperties;

public class AMQPAsyncListenProperties implements TransportListenProperties {
    private AMQPAsyncConsumerCallback callback;
    private String exchange;
    private String queue;
    private int threshold;

    public AMQPAsyncListenProperties(
            AMQPAsyncConsumerCallback callback
          , String exchange
          , String queue
    ) {
        this(callback, exchange, queue, 10);
    }

    public AMQPAsyncListenProperties(
            AMQPAsyncConsumerCallback callback
          , String exchange
          , String queue
          , int threshold
    ) {
        this.callback = callback;
        this.exchange = exchange;
        this.queue = queue;
        this.threshold = threshold;
    }

    public AMQPAsyncConsumerCallback getCallback() {
        return callback;
    }

    public String getExchange() {
        return exchange;
    }

    public String getQueue() {
        return queue;
    }

    public int getThreshold() {
        return threshold;
    }
}
