package com.programming.study.toby.p20161203.p05;

import lombok.extern.slf4j.*;
import org.reactivestreams.*;

import java.util.function.*;
import java.util.stream.*;

/**
 *
 *
 */

@Slf4j
public class PubSub {
	public static void main(String[] args) {
		Publisher<Integer> publisher = iterPub(Stream.iterate(1, a -> a + 1 ).limit(10).collect(Collectors.toList()));
		Publisher<Integer> mapPub2 = reducePub(publisher, 0, (BiFunction<Integer, Integer, Integer>)(a, b) -> a+b);
		mapPub2.subscribe(logSub());
	}

	// 1,2,3,4,5
	// 0 -> (0,1) -> 0+ 1 =1
	// 1 -> (1,2) -> 1 + 2 = 3
	// 3 -> (3,3) -> 3 + 3 = 6
	// 6 -> (6,4) -> 6 + 4 = 10
	// 10 -> (10,5) -> 10 + 5 = 15

	private static Publisher<Integer> reducePub(Publisher<Integer> publisher, int init, BiFunction<Integer, Integer, Integer> bf) {
		return new Publisher<Integer>() {
			@Override
			public void subscribe(Subscriber<? super Integer> sub) {
				publisher.subscribe(new DelegateSub("reducSub", sub){
					int result = init;

					@Override
					public void onNext(Integer i) {
						result = bf.apply(result, i);
					}

					@Override
					public void onComplete() {
						sub.onNext(result);
						sub.onComplete();
					}
				});
			}
		};
	}

	private static Subscriber<Integer> logSub() {
		return new Subscriber<Integer>() {
			@Override
			public void onSubscribe(Subscription s) {
				log.debug("logSub onSubscribe:");
				s.request(Long.MAX_VALUE);
			}

			@Override
			public void onNext(Integer integer) {
				log.debug("logSub onNext:" + integer);
			}

			@Override
			public void onError(Throwable t) {
				log.debug("logSub onError {}:" + t);
			}

			@Override
			public void onComplete() {
				log.debug("logSub onComplete:");
			}
		};
	}

	private static Publisher<Integer> iterPub(final Iterable<Integer> iter) {
		return new Publisher<Integer>() {
			@Override
			public void subscribe(Subscriber<? super Integer> subscriber) {
				subscriber.onSubscribe(new Subscription() {
					@Override
					public void request(long n) {
						try {
							iter.forEach( s -> subscriber.onNext(s));

							log.debug("iterPub onComplete");
							subscriber.onComplete();
						} catch (Throwable t) {
							t.printStackTrace();
							subscriber.onError(t);
						}
					}

					@Override
					public void cancel() {

					}
				});
			}
		};
	}

}
