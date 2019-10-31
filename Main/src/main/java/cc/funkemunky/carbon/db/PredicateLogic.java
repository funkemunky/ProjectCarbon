package cc.funkemunky.carbon.db;

import java.util.function.Predicate;

public class PredicateLogic<T> {

    Predicate<T>[] predicates;
    LogicType type;
    public PredicateLogic(LogicType type, Predicate<T>... predicates) {
        this.type = type;
        this.predicates = predicates;
    }

    public enum LogicType {
        OR, AND
    }
}
