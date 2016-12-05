package server.events;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Maximilian Schirm (denkbares GmbH)
 * @created 05.12.2016
 */

public class General {

    protected class EventProducer{

        public EventProducer(){}

        public void produceEvent1(String payload){
            EventManager.getInstance().publishEvent(new TestEvent1(payload));
        }

        public void produceEvent2(){
            EventManager.getInstance().publishEvent(new TestEvent2());
        }
    }

    /**
     * This class has a dynamic payload
     */
    protected class TestEvent1 implements Event{
        private final String payload;

        public TestEvent1(String payload) {
            this.payload = payload;
        }

        public String getPayload() {
            return payload;
        }
    }

    /**
     * This class has a static payload
     */
    protected class TestEvent2 implements Event{
        public TestEvent2(){
        }

        public String getPayload(){
            return "banana";
        }
    }

    protected class EventConsumer implements EventListener{

        public EventConsumer(){
        }

        @Override
        public void notify(Event toNotify) {
            if(toNotify instanceof TestEvent1){
                TestEvent1 recvd = (TestEvent1)toNotify;
                System.out.println("Received an Event (TE1) with the Payload " + recvd.getPayload());
            }
            else if(toNotify instanceof TestEvent2){
                TestEvent2 recvd = (TestEvent2)toNotify;
                System.out.println("Received an Event (TE2) with the Payload " + recvd.getPayload());
            }
        }

        @Override
        public Set<Class<? extends Event>> getEvents() {
            Set<Class<? extends Event>> evts = new HashSet<>();
            evts.add(TestEvent1.class);
            evts.add(TestEvent2.class);
            return evts;
        }
    }

    public void testSendingEvents(int count){
        log("Creating consumer and registering at Manager");
        EventConsumer consumer = new EventConsumer();
        EventManager.getInstance().registerListener(consumer);
        log("Finished init. Starting sending events.");

        for(int i = 0; i < count; i++){
            log("");
            String payload = "Event Number " + i;
            Event evt = (Math.random() > 0.5)?new TestEvent1(payload):new TestEvent2();
            log("Published event of type " + ((evt instanceof TestEvent1)?"TestEvent1":"TestEvent2"));
            EventManager.getInstance().publishEvent(evt);
            log("");
        }

        log("Finished testing!");
    }

    public void testRegisteringAtManager(){
        log("Creating consumer..");
        EventConsumer consumer = new EventConsumer();
        log("Created consumer! Registering at Manager!");
        EventManager.getInstance().registerListener(consumer);
        log("Registered consumer at manager!");
    }

    private static void log(String msg){
        System.out.println(msg);
    }

    public static void main(String... args){
        General gen = new General();
        log("commencing testing...");

        int COUNT = 5;
        log("Sending " + COUNT + " events...");
        gen.testSendingEvents(COUNT);

        log("Finished testing " + COUNT + " events...");
        log("Testing Registering at Manager...");
        gen.testRegisteringAtManager();

        log("finished testing...");
    }

}
