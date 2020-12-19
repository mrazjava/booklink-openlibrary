package com.github.mrazjava.booklink.openlibrary.dataimport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.mrazjava.booklink.openlibrary.BooklinkUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BooklinkUtilsTest {

	@Test
	public void shouldExtractSample() {
		
		String rawText = "\"&Makesi En'gesi xuan ji zhuan ti zhai lu";
		String sampleText = BooklinkUtils.extractSampleText(rawText);
		
		log.debug("sampleText 1: {}", sampleText);
		
		assertEquals("Makesi En'", sampleText);
		
		rawText = "!3Zabra !!* kadabra";
		sampleText = BooklinkUtils.extractSampleText(rawText);
		
		log.debug("sampleText 2: {}", sampleText);
		
		assertEquals("3Zabra !!*", sampleText);
		
		rawText = "mochito";
		sampleText = BooklinkUtils.extractSampleText(rawText);
		
		log.debug("sampleText 3: {}", sampleText);
		
		assertEquals("mochito", sampleText);

		rawText = "         (-->zorro";
		sampleText = BooklinkUtils.extractSampleText(rawText);
		
		log.debug("sampleText 4: {}", sampleText);
		
		assertEquals("zorro", sampleText);
		
		rawText = "1984";
		sampleText = BooklinkUtils.extractSampleText(rawText);
		
		log.debug("sampleText 5: {}", sampleText);
		
		assertEquals("1984", sampleText);

	}
}
