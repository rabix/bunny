package org.rabix.engine.store.model.scatter.impl;

import com.google.common.collect.ImmutableList;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Test(groups = { "functional" })
public class ScatterOrderTest {

  @Test
  public void testOrder() {
    ScatterCartesianStrategy.Combination c1 = new ScatterCartesianStrategy.Combination(1, true, ImmutableList.of(2, 3));
    ScatterCartesianStrategy.Combination c2 = new ScatterCartesianStrategy.Combination(1, true, ImmutableList.of(10, 1));
    List<ScatterCartesianStrategy.Combination> combinations = new ArrayList<>();
    combinations.add(c1);
    combinations.add(c2);
    Collections.sort(combinations, new Comparator<ScatterCartesianStrategy.Combination>() {
      @Override
      public int compare(ScatterCartesianStrategy.Combination o1, ScatterCartesianStrategy.Combination o2) {
        String s1 = o1.indexes.toString();
        String s2 = o2.indexes.toString();
        int cmp = s1.compareTo(s2);
        System.out.println(s1);
        System.out.println(s2);
        System.out.println(cmp);
        return cmp;
      }
    });

  }

}
