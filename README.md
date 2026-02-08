[![Build Status](https://github.com/mP1/j2cl-java-util-currency-annotation-processor/workflows/build.yaml/badge.svg)](https://github.com/mP1/j2cl-java-util-currency-annotation-processor/actions/workflows/build.yaml/badge.svg)
[![Coverage Status](https://coveralls.io/repos/github/mP1/j2cl-java-util-currency-annotation-processor/badge.svg)](https://coveralls.io/github/mP1/j2cl-java-util-currency-annotation-processor)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/mP1/j2cl-java-util-currency-annotation-processor.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/mP1/j2cl-java-util-currency-annotation-processor/context:java)
![](https://tokei.rs/b1/github/mP1/j2cl-java-util-currency-annotation-processor)
[![Total alerts](https://img.shields.io/lgtm/alerts/g/mP1/j2cl-java-util-currency-annotation-processor.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/mP1/j2cl-java-util-currency-annotation-processor/alerts/)

# j2cl java-util-currency-annotation-processor

An annotation processor that generates the `CurrencyProvider` used by the emulated `java.util.Currency` in 
[j2cl-java-util-Currency](https://travis-ci.com/mP1/j2cl-java-util-Currency), to support most `Currency` features in javascript
after building with [j2cl-maven-plugin](https://travis-ci.com/mP1/j2cl-maven-plugin).

- Selects currencies for the given [locales](https://travis-ci.com/mP1/j2cl).
- `walkingkooka.j2cl.java.util.Currency` csv of currency codes, with trailing wildcard support. 

```text
-Awalkingkooka.j2cl.java.util.Currency=XXX
-Awalkingkooka.j2cl.java.util.Locale=EN*
```

- This selects all currency data for locales starting with `EN`, and the currency with currency code=`XXX`.

For more details [click here](https://github.com/mP1/j2cl-locale)

## Unsupported features.

See [j2cl-java-util-Currency](https://travis-ci.com/mP1/j2cl-java-util-Currency) for a more comprehensive summary.
