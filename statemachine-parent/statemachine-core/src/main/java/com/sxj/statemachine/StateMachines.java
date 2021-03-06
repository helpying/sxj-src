package com.sxj.statemachine;

import static org.slf4j.LoggerFactory.getLogger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.slf4j.Logger;

import com.sxj.statemachine.annotations.EnterState;
import com.sxj.statemachine.annotations.Event;
import com.sxj.statemachine.annotations.ExitState;
import com.sxj.statemachine.annotations.State;
import com.sxj.statemachine.annotations.StateMachine;
import com.sxj.statemachine.annotations.Transition;
import com.sxj.statemachine.annotations.Transitions;
import com.sxj.statemachine.exceptions.StateMachineException;
import com.sxj.statemachine.interfaces.IStateMachine;
import com.sxj.statemachine.interfaces.StateMachineDefinition;
import com.sxj.statemachine.strategy.NonReentrantStrategy;
import com.sxj.statemachine.strategy.ReentrantStrategy;

/**
 * Helper class for creating state machines from a state machine definition or
 * from an annotated class.
 * 
 * <p>
 * The annotated class must be annotated with {@link StateMachine}
 */
public class StateMachines<S, E>
{
    protected static Logger l = getLogger(StateMachines.class);
    
    public static IStateMachine newReentrant(StateMachineDefinition definition)
            throws StateMachineException
    {
        return new StateMachineImpl(definition, new ReentrantStrategy());
    }
    
    public static IStateMachine newReentrant(Object instance)
            throws StateMachineException
    {
        return new StateMachineImpl(processAnnotatedController(instance),
                new ReentrantStrategy());
    }
    
    public static IStateMachine newNonReentrant(
            StateMachineDefinition definition) throws StateMachineException
    {
        return new StateMachineImpl(definition, new NonReentrantStrategy());
    }
    
    public static IStateMachine newNonReentrant(Object instance)
            throws StateMachineException
    {
        return new StateMachineImpl(processAnnotatedController(instance),
                new NonReentrantStrategy());
    }
    
    private static void checkClassAnnotation(StateMachineDefinition definition,
            Object instance) throws StateMachineException
    {
        Class<?> clazz = instance.getClass();
        if (!clazz.isAnnotationPresent(com.sxj.statemachine.annotations.StateMachine.class))
        {
            throw new StateMachineException(
                    "All state machines must be annotated with the @AStateMachine annotation");
        }
    }
    
    private static StateMachineDefinitionImpl checkFieldAnnotations(
            StateMachineDefinitionImpl stateMachineDefinition, Object instance)
            throws StateMachineException
    {
        Class<?> clazz = instance.getClass();
        
        // Let's process the events and states first.
        // We look for the State, StartState and Event annotations
        for (Field field : clazz.getDeclaredFields())
        {
            if (field.isAnnotationPresent(State.class))
                checkStateAnnotation(instance,
                        stateMachineDefinition,
                        field,
                        field.getAnnotation(State.class));
            
            if (field.isAnnotationPresent(Event.class))
                checkEventAnnotation(instance,
                        stateMachineDefinition,
                        field,
                        field.getAnnotation(Event.class));
        }
        
        return stateMachineDefinition;
    }
    
    private static StateMachineDefinition processAnnotatedController(
            Object instance) throws StateMachineException
    {
        StateMachineDefinitionImpl stateMachineDefinition = new StateMachineDefinitionImpl();
        
        checkClassAnnotation(stateMachineDefinition, instance);
        checkFieldAnnotations(stateMachineDefinition, instance);
        checkTransitionAnnotations(stateMachineDefinition, instance);
        
        return stateMachineDefinition;
    }
    
    private static void checkTransitionAnnotations(
            StateMachineDefinitionImpl definition, Object instance)
            throws StateMachineException
    {
        // Let's process the transitions
        Class<?> clazz = instance.getClass();
        for (Method method : clazz.getMethods())
        {
            if (method.isAnnotationPresent(Transitions.class))
            {
                Transitions transitions = method.getAnnotation(Transitions.class);
                for (Transition transition : transitions.value())
                    checkTransitionAnnotation(instance,
                            definition,
                            method,
                            transition);
            }
            else if (method.isAnnotationPresent(Transition.class))
            {
                checkTransitionAnnotation(instance,
                        definition,
                        method,
                        method.getAnnotation(Transition.class));
            }
            else if (method.isAnnotationPresent(EnterState.class))
            {
                checkEnterStateAnnotation(instance,
                        definition,
                        method,
                        method.getAnnotation(EnterState.class));
            }
            else if (method.isAnnotationPresent(ExitState.class))
            {
                checkExitStateAnnotation(instance,
                        definition,
                        method,
                        method.getAnnotation(ExitState.class));
            }
        }
    }
    
    private static void checkGenericTransitionHasTheRightParameters(
            Method method) throws StateMachineException
    {
        Class<?> paramTypes[] = method.getParameterTypes();
        if (paramTypes == null || paramTypes.length != 1
                || !paramTypes[0].equals(TransitionInfo.class))
            throw new StateMachineException(
                    "Transition for method "
                            + method.getName()
                            + " is not well defined. It should have one and only TransitionEvent paramter");
    }
    
    private static void checkEnterStateAnnotation(Object instance,
            StateMachineDefinitionImpl definition, Method method, EnterState ann)
            throws StateMachineException
    {
        // First of all, we check the parameters
        checkGenericTransitionHasTheRightParameters(method);
        
        // Second, we check the return type is correct
        Class<?> resultType = method.getReturnType();
        if (resultType == null
                || (!resultType.equals(EventInfo.class) && !("void".equals(resultType.getName()))))
        {
            throw new StateMachineException(
                    "Transition for method "
                            + method.getName()
                            + " is not well defined. Enter phase must return a EventInfo or void");
        }
        
        definition.defineEnterState(ann, method, instance);
    }
    
    private static void checkExitStateAnnotation(Object instance,
            StateMachineDefinitionImpl definition, Method method, ExitState ann)
            throws StateMachineException
    {
        // First of all, we check the parameters
        checkGenericTransitionHasTheRightParameters(method);
        // Second, we check the return type is correct
        Class<?> resultType = method.getReturnType();
        if (resultType == null
                || (!resultType.equals(Boolean.class) && !("void".equals(resultType.getName()))))
        {
            throw new StateMachineException(
                    "Transition for method "
                            + method.getName()
                            + " is not well defined. Exit phase must return a boolean or void");
        }
        
        definition.defineExitState(ann, method, instance);
    }
    
    private static void checkStateAnnotation(Object instance,
            StateMachineDefinitionImpl definition, Field field, State ann)
            throws StateMachineException
    {
        if (!isStringAndFinal(field))
            throw new StateMachineException("@State " + field.getName()
                    + " must be declared as public static final");
        
        try
        {
            String stateName = (String) field.get(instance);
            definition.defineState(stateName, ann.isStart(), ann.isFinal());
        }
        catch (IllegalAccessException e)
        {
            l.error("Error. This should never happen as we have checked the conditions before using reflection",
                    e);
        }
    }
    
    private static void checkEventAnnotation(Object instance,
            StateMachineDefinitionImpl definition, Field field, Event ann)
            throws StateMachineException
    {
        if (!isStringAndFinal(field))
            throw new StateMachineException("@Event " + field.getName()
                    + " must be declared as public static final");
        
        try
        {
            String eventName = (String) field.get(instance);
            definition.defineEvent(eventName);
        }
        catch (IllegalAccessException e)
        {
            l.error("ERROR. This should never happen as we have checked the conditions before using reflection",
                    e);
        }
        //        catch (EventAlreadyExistsException e)
        //        {
        //            throw new IllegalEventAnnotationException("@Event "
        //                    + field.getName() + " has been declared twice");
        //        }
    }
    
    /*
     * TODO. Define a set of tests for this functionality
     */
    private static void checkTransitionAnnotation(Object instance,
            StateMachineDefinitionImpl stateMachineDefinition, Method method,
            Transition ann) throws StateMachineException
    {
        // First of all, we check the parameters
        checkGenericTransitionHasTheRightParameters(method);
        stateMachineDefinition.defineTransition(ann, method, instance);
    }
    
    /**
     * We check that the annotated field is a public final String type
     * 
     * @return true if it conforms the condition, false otherwise.
     */
    private static boolean isStringAndFinal(Field field)
    {
        return (field.getType().equals(String.class)
                && Modifier.isFinal(field.getModifiers()) && Modifier.isPublic(field.getModifiers()));
    }
    
}
