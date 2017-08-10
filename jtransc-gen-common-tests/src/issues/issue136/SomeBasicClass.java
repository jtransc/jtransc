package issues.issue136;

import com.jtransc.annotation.JTranscKeep;

@JTranscKeep
public class SomeBasicClass<T> {
	@JTranscKeep
	T key;

	@JTranscKeep
	T[] array;
}
