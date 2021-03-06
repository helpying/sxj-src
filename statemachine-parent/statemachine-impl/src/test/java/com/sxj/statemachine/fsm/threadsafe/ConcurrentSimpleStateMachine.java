package com.sxj.statemachine.fsm.threadsafe;

import com.sxj.statemachine.fsm.UntypedStateMachine;
import com.sxj.statemachine.fsm.annotation.AsyncExecute;
import com.sxj.statemachine.fsm.annotation.ContextInsensitive;
import com.sxj.statemachine.fsm.annotation.StateMachineParameters;
import com.sxj.statemachine.fsm.impl.AbstractUntypedStateMachine;

@ContextInsensitive
@StateMachineParameters(stateType=String.class, eventType=String.class, contextType=UntypedStateMachine.class)
public class ConcurrentSimpleStateMachine extends AbstractUntypedStateMachine {
    
    StringBuilder logger = new StringBuilder();
    
    StringBuilder logger2 = new StringBuilder();
    
    Thread fromAToBCallThread = null;
    
    Thread fromBToCCallThread = null;

    @AsyncExecute
    protected void fromAToB(String from, String to, String event) {
        logger.append("fromAToB");
        fromAToBCallThread = Thread.currentThread();
        fire("SECOND");
    }
    
    protected void fromBToC(String from, String to, String event) {
        logger.append("fromBToC");
        fromBToCCallThread = Thread.currentThread();
    }
    
    @Override
    protected void beforeActionInvoked(Object fromState, Object toState, Object event, Object context) {
        if (logger.length() > 0) {
            logger.append('.');
        }
        if(logger2.length() > 0) {
            logger2.append(", ");
        }
    }
    
    @Override
    protected void afterActionInvoked(Object fromState, Object toState, Object event, Object context) {
        logger2.append(fromState+"-"+toState);
    }
}
