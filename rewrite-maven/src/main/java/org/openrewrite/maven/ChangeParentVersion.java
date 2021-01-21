/*
 * Copyright 2020 the original author or authors.
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
package org.openrewrite.maven;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeProcessor;
import org.openrewrite.Validated;
import org.openrewrite.xml.ChangeTagValueProcessor;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.tree.Xml;

import static org.openrewrite.Validated.required;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChangeParentVersion extends Recipe {

    private static final XPathMatcher PARENT_VERSION_MATCHER = new XPathMatcher("/project/parent/version");

    private final String groupId;
    private final String artifactId;
    private final String toVersion;

    @Override
    protected TreeProcessor<?, ExecutionContext> getProcessor() {
        return new ChangeParentVersionProcessor();
    }

    @Override
    public Validated validate() {
        return required("groupId", groupId)
                .and(required("artifactId", artifactId))
                .and(required("toVersion", toVersion));
    }

    private class ChangeParentVersionProcessor extends MavenProcessor<ExecutionContext> {

        private ChangeParentVersionProcessor() {
            setCursoringOn();
        }

        @Override
        public Xml visitTag(Xml.Tag tag, ExecutionContext ctx) {
            if (PARENT_VERSION_MATCHER.matches(getCursor())) {
                Xml.Tag parent = getCursor().getParentOrThrow().getTree();
                if (groupId.equals(parent.getChildValue("groupId").orElse(null)) &&
                        artifactId.equals(parent.getChildValue("artifactId").orElse(null)) &&
                        !toVersion.equals(tag.getValue().orElse(null))) {
                    doAfterVisit(new ChangeTagValueProcessor<>(tag, toVersion));
                }
            }
            return super.visitTag(tag, ctx);
        }
    }
}
