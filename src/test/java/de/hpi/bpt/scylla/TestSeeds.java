package de.hpi.bpt.scylla;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

/**
 * Parameterizes a test method with specified and one random seeds
 * @see {@link SeedProvider}
 * @author Leon Bein
 *
 */
@ParameterizedTest
@Retention(RetentionPolicy.RUNTIME)
@ArgumentsSource(SeedProvider.class)
public @interface TestSeeds {
	long[] value();
}