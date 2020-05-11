/*
 * Copyright 2019 Miroslav Pokorny (github.com/mP1)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package walkingkooka.j2cl.java.util.currency.annotationprocessor;

import walkingkooka.collect.set.Sets;
import walkingkooka.j2cl.locale.annotationprocessor.LocaleAwareAnnotationProcessor;
import walkingkooka.text.printer.IndentingPrinter;

import java.io.DataOutput;
import java.util.Set;
import java.util.function.Function;

public final class CurrencyProviderAnnotationProcessor extends LocaleAwareAnnotationProcessor {

    @Override
    protected Set<String> additionalArguments() {
        return Sets.empty();
    }

    @Override
    protected void generate(final Set<String> languageTags,
                            final Function<String, String> arguments,
                            final DataOutput data,
                            final IndentingPrinter comments) throws Exception {
        CurrencyProviderTool.generate(languageTags,
                Sets.of("XXX"),
                data,
                comments); // https://github.com/mP1/j2cl-java-util-currency-annotation-processor/issues/13
    }

    @Override
    protected String generatedClassName() {
        return "walkingkooka.j2cl.java.util.currency.CurrencyProvider";
    }
}
