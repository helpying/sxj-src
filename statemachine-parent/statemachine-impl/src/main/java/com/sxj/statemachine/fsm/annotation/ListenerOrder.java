package com.sxj.statemachine.fsm.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
/**
 * ListenerOrder can be used to insure listener invoked orderly
 * @author Henry.He
 */
public @interface ListenerOrder {
    int value();
}
