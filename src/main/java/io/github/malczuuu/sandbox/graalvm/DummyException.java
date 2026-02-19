package io.github.malczuuu.sandbox.graalvm;

import io.github.problem4j.core.ProblemMapping;

@ProblemMapping(
    status = 404,
    detail = "this is error and the param is {help}",
    extensions = {"help"})
public class DummyException extends RuntimeException {

  private final String help;

  public DummyException(String help) {
    this.help = help;
  }
}
