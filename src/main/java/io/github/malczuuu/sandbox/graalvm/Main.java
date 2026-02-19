package io.github.malczuuu.sandbox.graalvm;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemMapper;

public class Main {

  public static void main(String[] args) {
    System.out.println("Hello, GraalVM!");

    for (int i = 1; i <= 5; i++) {
      System.out.println("i = " + i);
    }

    String help = "asdasd";
    System.out.println("Help: " + help);
    DummyException ex = new DummyException(help);

    ProblemMapper mapper = ProblemMapper.create();

    Problem problem = mapper.toProblemBuilder(ex).build();

    System.out.println(problem);
  }
}
