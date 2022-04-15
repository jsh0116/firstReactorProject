package com.bithumb.firstReactorProject;

import com.bithumb.firstReactorProject.model.Person;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;

@SpringBootTest
class FirstReactorProjectApplicationTests {

	/**
	 * 1. ["Blenders", "Old", "Johnnie"] 와 "[Pride", "Monk", "Walker”] 를
	 * 순서대로 하나의 스트림으로 처리되는 로직 검증
	 */
	@Test
	void concatWithDelay() {
		Flux<String> names1 = Flux.just("Blenders", "Old", "Johnnie").delayElements(Duration.ofSeconds(1));
		Flux<String> names2 = Flux.just("Pride", "Monk", "Walker").delayElements(Duration.ofSeconds(1));
		Flux<String> names = Flux.concat(names1, names2).log();

		StepVerifier.create(names)
				.expectSubscription()
				.expectNext("Blenders", "Old", "Johnnie", "Pride", "Monk", "Walker")
				.verifyComplete();
	}

	/**
	 * 2. 1~100 까지의 자연수 중 짝수만 출력하는 로직 검증
	 */
	@Test
	void testEvenNumber() {
		Flux<Integer> evenNumbers = Flux.range(1,100).filter(number -> number % 2 == 0);

		StepVerifier.create(evenNumbers)
				.expectSubscription()
				.expectNextCount(50) // 짝수 개수 검증
				.thenConsumeWhile(number -> {
					// 2로 나눈 나머지가 0인지 검증
					Assertions.assertEquals(0, number % 2);
					System.out.println(number);
					return number % 2 == 0;
				})
				.verifyComplete();
	}

	/**
	 * “hello”, “there” 를 순차적으로 publish 하여 순서대로 나오는지 검증
	 */
	@Test
	void publishTest() {
		Flux<String> names = Flux.just("hello", "there")
				.publishOn(Schedulers.boundedElastic())
				.log();

		StepVerifier.create(names)
				.expectSubscription()
				.expectNext("hello")
				.expectNext("there")
				.verifyComplete();
	}


	/**
	 * 4. 이름이 대문자로 변환되어 출력되는 로직 검증
	 */
	@Test
	void toUpperCaseTest() {
		Person john = new Person("John", "[john@gmail.com](mailto:john@gmail.com)", "12345678");
		Person jack = new Person("Jack", "[jack@gmail.com](mailto:jack@gmail.com)", "12345678");
		Flux<Person> flux = Flux.just(john, jack)
				.map(person -> {
					person.setUserName(person.getUserName().toUpperCase());
					return person;
				})
				.log();

		StepVerifier.create(flux)
				.assertNext(person -> assertThat(person.getUserName()).isEqualTo("JOHN"))
				.assertNext(person -> assertThat(person.getUserName()).isEqualTo("JACK"))
				.verifyComplete();
	}

	/**
	 * ["Blenders", "Old", "Johnnie"] 와 "[Pride", "Monk", "Walker”]를 압축하여 스트림으로 처리 검증
	 * Flux<List<String>>으로 해결할 수 없나?
	 */
	@Test
	void zipStreamTest() {
		Flux<String> flux1 = Flux.just("Blenders","Old","Johnnie");
		Flux<String> flux2 = Flux.just("Pride", "Monk", "Walker");

		Flux<String> zippedFlux = Flux.zip(flux1, flux2, (f1, f2) -> f1 + " " + f2).log();

		StepVerifier.create(zippedFlux)
				.expectNext("Blenders Pride")
				.expectNext("Old Monk")
				.expectNext("Johnnie Walker")
				.verifyComplete();
	}

	/**
	 * 6. ["google", "abc", "fb", "stackoverflow"] 의 문자열 중 5자 이상 되는 문자열만 대문자로 비동기로 치환하여 1번 반복하는 스트림으로 처리하는 로직 검증
	 * return : 예상되는 스트림 결과값 ["GOOGLE", "STACKOVERFLOW", "GOOGLE", "STACKOVERFLOW"]
	 */
	@Test
	void stringLengthUpperCaseTest() {
		Flux<String> flux = Flux.just("google", "abc", "fb", "stackoverflow");
		Flux<String> resultFlux  = flux.filter(str -> str.length() >= 5)
				.flatMap(str -> Mono.just(str.toUpperCase()))
				.repeat(1)
				.log();

		StepVerifier.create(resultFlux)
				.expectSubscription()
				.expectNext("GOOGLE", "STACKOVERFLOW", "GOOGLE", "STACKOVERFLOW")
				.verifyComplete();
	}
}
