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

import org.junit.jupiter.api.Test;
import walkingkooka.collect.set.Sets;
import walkingkooka.j2cl.locale.WalkingkookaLanguageTag;
import walkingkooka.reflect.ClassTesting;
import walkingkooka.reflect.JavaVisibility;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class CurrencyProviderToolTest implements ClassTesting<CurrencyProviderTool> {

    @Test
    public void testCurrencyDEWildcard() {
        final String expected = "static void register() {\n" +
                "  // CHF=[de, de_AT, de_BE, de_CH, de_DE, de_LI, de_LU]\n" +
                "  Currency.register(\"CHF\",\n" +
                "    2, // defaultFractionDigits\n" +
                "    756, // numericCode\n" +
                "    \"CHF\", // defaultSymbol\n" +
                "    \"de-CH,de-LI\", // locales\n" +
                "    \"CHF,de,de-AT,de-BE,de-CH,de-DE,de-LI,de-LU\" // symbolToLocales\n" +
                "  );\n" +
                "  // EUR=[de_CH, de_LI]\n" +
                "  // €=[de, de_AT, de_BE, de_DE, de_LU]\n" +
                "  Currency.register(\"EUR\",\n" +
                "    2, // defaultFractionDigits\n" +
                "    978, // numericCode\n" +
                "    \"€\", // defaultSymbol\n" +
                "    \"de-AT,de-BE,de-DE,de-LU\", // locales\n" +
                "    \"EUR,de-CH,de-LI\", // symbolToLocales\n" +
                "    \"€,de,de-AT,de-BE,de-DE,de-LU\" // symbolToLocales\n" +
                "  );\n" +
                "}";
        assertEquals(expected, CurrencyProviderTool.generateMethod(WalkingkookaLanguageTag.all("DE*")));
    }

    @Test
    public void testCurrencyENAU() {
        final String expected = "static void register() {\n" +
                "  // $=[en_AU]\n" +
                "  Currency.register(\"AUD\",\n" +
                "    2, // defaultFractionDigits\n" +
                "    36, // numericCode\n" +
                "    \"$\", // defaultSymbol\n" +
                "    \"en-AU\", // locales\n" +
                "    \"$,en-AU\" // symbolToLocales\n" +
                "  );\n" +
                "}";
        assertEquals(expected, CurrencyProviderTool.generateMethod(Sets.of("EN-AU")));
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<CurrencyProviderTool> type() {
        return CurrencyProviderTool.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
