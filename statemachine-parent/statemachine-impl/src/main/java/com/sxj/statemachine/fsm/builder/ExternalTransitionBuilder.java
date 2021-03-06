package com.sxj.statemachine.fsm.builder;

import com.sxj.statemachine.fsm.StateMachine;

/**
 * External transition builder which is used to build a external transition.
 * 
 * @author Henry.He
 *
 * @param <T> type of State Machine
 * @param <S> type of State
 * @param <E> type of Event
 * @param <C> type of Context
 */
public interface ExternalTransitionBuilder<T extends StateMachine<T, S, E, C>, S, E, C> {
    /**
     * Build transition source state.
     * @param stateId id of state
     * @return from clause builder
     */
    From<T, S, E, C> from(S stateId);
}
