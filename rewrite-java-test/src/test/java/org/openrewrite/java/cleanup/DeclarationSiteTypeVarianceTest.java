/*
 * Copyright 2022 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.cleanup;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.java.Assertions.java;

class DeclarationSiteTypeVarianceTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new DeclarationSiteTypeVariance(
          List.of("java.util.function.Function<IN, OUT>"),
          List.of("java.lang.*")
        ));
    }

    @Test
    void validation() {
        assertThat(new DeclarationSiteTypeVariance(
          List.of("java.util.function.Function<INVALID, OUT>"),
          List.of("java.lang.*")
        ).validate().isInvalid()).isTrue();
    }

    @Test
    void inOutVariance() {
        rewriteRun(
          java(
            """
              interface In {}
              interface Out {}
              """
          ),
          java(
            """
              import java.util.function.Function;
              class Test {
                  void test(Function<In, Out> f) {
                  }
              }
              """,
            """
              import java.util.function.Function;
              class Test {
                  void test(Function<? super In, ? extends Out> f) {
                  }
              }
              """
          )
        );
    }

    @Test
    void invariance() {
        rewriteRun(
          spec -> spec.recipe(new DeclarationSiteTypeVariance(
            List.of("java.util.function.Function<INVARIANT, OUT>"),
            List.of("java.lang.*")
          )),
          java(
            """
              interface In {}
              interface Out {}
              """
          ),
          java(
            """
              import java.util.function.Function;
              class Test {
                  void test(Function<In, Out> f) {
                  }
              }
              """,
            """
              import java.util.function.Function;
              class Test {
                  void test(Function<In, ? extends Out> f) {
                  }
              }
              """
          )
        );
    }

    @Test
    void excludedBounds() {
        rewriteRun(
          java(
            """
              import java.util.function.Function;
              class Test {
                  void test(Function<String, Integer> f) {
                  }
              }
              """
          )
        );
    }

    @Test
    void finalClasses() {
        rewriteRun(
          java(
            """
              final class In {}
              interface Out {}
              """
          ),
          java(
            """
              import java.util.function.Function;
              class Test {
                  void test(Function<In, Out> f) {
                  }
              }
              """,
            """
              import java.util.function.Function;
              class Test {
                  void test(Function<In, ? extends Out> f) {
                  }
              }
              """
          )
        );
    }
}
