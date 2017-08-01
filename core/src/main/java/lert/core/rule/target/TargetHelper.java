package lert.core.rule.target;

import lert.core.rule.*;
import com.google.inject.*;
import groovy.lang.*;

public class TargetHelper {
    private static Injector injector;

    public static void setInjector(Injector injector) {
        TargetHelper.injector = injector;
    }

    public static void rule(Closure<Void> cl) {
        RuleDelegate instance = injector.getInstance(RuleDelegate.class);
        cl.setDelegate(instance);
        cl.setResolveStrategy(Closure.DELEGATE_FIRST);
        cl.call();
        if (!instance.reactionWasCalled()) {
            throw new IllegalStateException("Reaction hasn't been called. Please ensure that you have a 'reaction {...}' block in your rule.");
        }
    }
}
