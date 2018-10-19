package de.hpi.bpt.scylla;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

/**
 * Provides seeds specified as {@link TestSeeds} annotation for ar test method. <br>
 * Also always adds one random seed.
 * @author Leon Bein
 *
 */
public class SeedProvider implements ArgumentsProvider, AnnotationConsumer<TestSeeds>{
	
	long[] seeds;

	@Override
	public void accept(TestSeeds t) {
		seeds = t.value();
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        Random random = new Random();
        Long randomSeed = random.nextLong();
        List<Long> randomRun = new ArrayList<>();
		randomRun.add(randomSeed);
		return Stream.concat(
				Arrays.stream(seeds).boxed(),
				randomRun.stream()
		).map(Arguments::of);
	}
	
}