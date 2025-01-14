/*
 * Copyright 2023 the original author or authors.
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
import org.openrewrite.java.ChangeType;
import org.openrewrite.java.tree.TypeUtils;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.groovy.Assertions.groovy;

public class ChangeTypeTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ChangeType("a.b.Original", "x.y.Target", true));
    }

    @SuppressWarnings("GrPackage")
    @Test
    void changeImport() {
        rewriteRun(
          groovy(
            """
            package a.b
            class Original {}
            """),
          groovy(
            """
            import a.b.Original
            
            class A {
                Original type
            }
            """,
            """
            import x.y.Target
            
            class A {
                Target type
            }
            """
          )
        );
    }

    @SuppressWarnings("GrPackage")
    @Test
    void changeType() {
        rewriteRun(
          groovy(
            """
            package a.b
            class Original {}
            """),
          groovy(
            """
            class A {
                a.b.Original type
            }
            """,
            """
            class A {
                x.y.Target type
            }
            """
          )
        );
    }

    @Test
    void changeDefinition() {
        rewriteRun(
          spec -> spec.recipe(new ChangeType("file", "newFile", false)),
          groovy(
            """
            class file {
            }
            """,
            """
            class newFile {
            }
            """,
            spec -> spec.path("file.groovy").afterRecipe(cu -> {
              assertThat("newFile.groovy").isEqualTo(cu.getSourcePath().toString());
              assertThat(TypeUtils.isOfClassType(cu.getClasses().get(0).getType(), "newFile")).isTrue();
            })
          )
        );
    }
}
