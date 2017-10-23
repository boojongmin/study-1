### webflux 코드 샘플
(pom.xml은 하단 참조)

```java
public class Server {
    public static void main(String[] args) throws IOException {
        new Server().run();
        System.out.println("Press ENTER to exit.");
        System.in.read();
    }

    private void run() {
        RequestPredicate requestPredicate = RequestPredicates.GET("/hello");
        HandlerFunction<ServerResponse> handlerFunction = request -> ServerResponse.ok().build();
        RouterFunction<ServerResponse> routerFunction = RouterFunctions.route(requestPredicate, handlerFunction);
        HttpWebHandlerAdapter httpWebHandlerAdapter = toHttpHandler(routerFunction);
        ReactorHttpHandlerAdapter reactorHttpHandlerAdapter = new ReactorHttpHandlerAdapter(httpWebHandlerAdapter);
        HttpServer httpServer = HttpServer.create("localhost", 8080);
        httpServer.newHandler(reactorHttpHandlerAdapter).block();
    }
}
```

[nio 참조 링크](https://www.slideshare.net/llj098/epoll-from-the-kernel-side)



### 자주 나오는 Type cheatsheet
- ServerRequest - Server가 생성해서 주입. 익명함수의 인자로 사용.
- ServerResponse - ServerResponse.BodyBuilder로 생성.
- RequestPredicate: ServerRequest -> boolean(RequestPredicates으로 생성.)
- HandlerFunction: ServerRequest -> Mono<T>
- BodyInserters.fromObject(): BodyInserter<T, ReactiveHttpOutputMessage>
- RouterFunction: ServerRequest -> Mono<HandlerFunction<T>>
- RouterFunctins.route(RequestPredict, HandlerFuction): RouterFunction<T extends ServerResponse> -> RouterFunctions.toHttpHandler:HttpWebHandlerAdapter -> 


### POST multipart/form-data 참조
참조할만한 테스트 코드:  org.springframework.web.reactive.function.BodyExtractorsTests

[예제 작성된 github](https://github.com/sdeleuze/webflux-multipart/blob/master/src/main/java/com/example/MultipartRoute.java) -> 예제 코드에서 Part는 tranferTo가 없는데 사용함. 기존 interface가 바뀐듯함. 지금 돌리면 에러 발생할 듯.

Part interface는 뒤의 2 interface가 상속:  FilePart,  FormFieldPart

* TODO: Post Body lenght limit 방법 확인할것 *

### ServletHttpHandlerAdapter
    // Servlet is based on blocking I/O, hence the usage of non-direct, heap-based buffers
    // (i.e. 'false' as constructor argument)

위의 주석을 보면 ServletHttpHandler를 쓰는 것들은 nio의 장점을 제대로 못쓰는 것으로 보임
  1. blocking I/O
  2. 데이터를 OS에서 가지고 있는 것을 직접 쓰는게 아니고 OS에 있는 데이터를  jvm heap으로 copy한 후 사용하므로 copy overhead가 발생

SerletHttpHandlerAdapter 소스에 위와 같은 주석이 있음.
(참고: ServletHttpHandlerAdapter를 상속받은 TomcatHttpHandlerAdapter도 있음 tomcat은 java.nio.ByteBuffer를 사용 음... ServletHttpHandler도 buffer를 사용해서... 흠... 일단 스킵...)


### ReqeustPredicates 코드를 보다가 Optional을 쓰는 부분이 맘에 들어서 가져옴.
```
public static RequestPredicate queryParam(String name, Predicate<String> predicate) {
        return (request) -> {
            Optional<String> s = request.queryParam(name);
            return s.filter(predicate).isPresent();
        };
    }
```


### 좀 더 살펴볼 api들
- [SererCodecConfigurer](https://docs.spring.io/spring/docs/5.0.0.RC1_to_5.0.0.RC2/Spring%20Framework%205.0.0.RC2/org/springframework/http/codec/ServerCodecConfigurer.html)
- [WebHandlerAdapter](https://docs.spring.io/spring/docs/5.0.0.M5_to_5.0.0.RC1/Spring%20Framework%205.0.0.RC1/org/springframework/web/server/adapter/HttpWebHandlerAdapter.html)
- [WebSessionManager](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/server/WebSession.html)
- [DispatcherHandler](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/reactive/DispatcherHandler.html)
- [WebHandler](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/server/WebHandler.html)
- [HttpWebHandlerAdapter](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/server/adapter/HttpWebHandlerAdapter.html)
-[ReactorHttpHandlerAdapter](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/server/reactive/ReactorHttpHandlerAdapter.html)

### 찾아볼 것들
- ServerHttpRequest: HttpRequest, ReactiveHttpInputMessage

- ServerRequest도 있고 ServerHttpResponse도 있음
	- ServerResponse
	- ServerHttpResponse: ReactiveHttpOutputMessage: HttpMessage

    ServerHttpResponse는 Server쪽 코드에서 사용하는 모양새. 그쪽 머리 아픈데...암튼...
(ReactiveHttpOutputMessage를 이해하려면 project-reactor를 더 봐야할 듯.)



### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
		 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		 xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId>study</groupId>
	<artifactId>webflux-sample</artifactId>
	<version>0.1-SNAPSHOT</version>

	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<configuration>
					<source>1.8</source>
					<target>1.8</target>
				</configuration>
			</plugin>
		</plugins>
	</build>

	<dependencyManagement>
        <dependencies>
		<dependency>
			<groupId>io.projectreactor</groupId>
			<artifactId>reactor-bom</artifactId>
			<version>Bismuth-RELEASE</version>
            <type>pom</type>
            <scope>import</scope>
		</dependency>
        </dependencies>
	</dependencyManagement>
	<dependencies>
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-webflux</artifactId>
			<version>5.0.0.RELEASE</version>
		</dependency>
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-context</artifactId>
			<version>5.0.0.RELEASE</version>
		</dependency>
		<dependency>
			<groupId>org.reactivestreams</groupId>
			<artifactId>reactive-streams</artifactId>
		</dependency>
		<dependency>
			<groupId>io.projectreactor</groupId>
			<artifactId>reactor-core</artifactId>
		</dependency>
		<dependency>
			<groupId>io.projectreactor.ipc</groupId>
			<artifactId>reactor-netty</artifactId>
		</dependency>
		<dependency>
			<groupId>com.fasterxml.jackson.core</groupId>
			<artifactId>jackson-databind</artifactId>
			<version>2.9.0.pr2</version>
		</dependency>
		<dependency>
			<groupId>org.apache.logging.log4j</groupId>
			<artifactId>log4j-core</artifactId>
			<version>2.8</version>
		</dependency>
		<dependency>
			<groupId>org.apache.logging.log4j</groupId>
			<artifactId>log4j-jcl</artifactId>
			<version>2.8</version>
		</dependency>
		<dependency>
			<groupId>junit</groupId>
			<artifactId>junit</artifactId>
			<version>4.12</version>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-test</artifactId>
			<version>5.0.0.RELEASE</version>
			<scope>test</scope>
		</dependency>
	</dependencies>
</project>

```



소스 분석중
```
	// router -> handler predicate and get handler
	@Test
	public void t1() throws URISyntaxException {
		List<String> list = Arrays.asList("a", "2");
		Flux<String> flux = Flux.fromIterable(list);
		Flux<String> flux1 = flux.concatMap(m -> Flux.just(m));
		System.out.println(flux1.take(5).collectList().block());

		RouterFunction<ServerResponse> rf = route(GET("/hello"), x -> ServerResponse.ok().build());
		System.out.println(rf);
		MockServerRequest req = MockServerRequest.builder().uri(new URI("/hello")).build();
		Mono<HandlerFunction<ServerResponse>> route = rf.route(req);
		System.out.println(route.block() != null);
		System.out.println(route.subscribe(x -> System.out.println(x.handle(req).block().statusCode())).isDisposed());


		MockServerRequest req2 = MockServerRequest.builder().uri(new URI("/hello2")).build();
		Mono<HandlerFunction<ServerResponse>> route2 = rf.route(req2);
		System.out.println(route2.block() == null);



//		flux.concatMap(m -> Flux.just("asdf", "zxcv"))
//				.next()
//				.switchIfEmpty(Mono.error(new Exception()))
//				.map(x -> "asdfasf")
//				.
//				;
//		System.out.println(block);

	}


```
