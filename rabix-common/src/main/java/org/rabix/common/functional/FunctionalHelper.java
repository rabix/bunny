package org.rabix.common.functional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FunctionalHelper {

  public static class FunctionWrapper<F extends Function<?, ?>> {
    public F func;
  }

  public static class Recursive {

    public static Function<Object, Object> make(Function<Object, Object> f) {
      FunctionWrapper<Function<Object, Object>> recursive = new FunctionWrapper<>();
      recursive.func = s -> {
        if (s instanceof List<?>) {
          return ((List<?>) s).stream().map(x -> recursive.func.apply(x)).collect(Collectors.toList());
        } else if (s instanceof Map<?, ?>) {
          return ((Map<?, ?>) s).entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> recursive.func.apply(e.getValue())));
        } else {
          return f.apply(s);
        }
      };
      return recursive.func;
    }

  }

}
