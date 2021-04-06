1.try-catch 가이드
1.1. API 설계 할 때 Exception만 throw 하도록 하지 말고, 필수적으로 처리되어야 하는 Exception을 나열합니다.
귀찮다는 이유로 Exception 을 throw 하게 디자인 하지 맙시다.
NOK
OK
public interface SomeModule {
void someAPI() throws Exception
}

public interface SomeModule {
void someAPI() throws TimeoutException, OtherException
}

1.2. Runtime Exception은 가급적 throw 하지 않도록 합니다.
Runtime Exception 을 throw 하게 되면
API 사용자가  exception을 예상 못하기 때문에,
예외처리를 제대로 못해 문제가 발생할 수 있습니다.
NOK
OK

public void someAPI() {
throw new RuntimeException("something wrong")
}
public class SomethingWrongException extends Exception {
public SomethingWrongException(String message) {
super(message);
}
}

public void someAPI() throws  {
throw new SomethingWrongException("something wrong")
}

API에  RuntimeException을 throws 하는 것을 명시하는 것은 사실 아무 의미가 없습니다.

public interface SomeModule {
// runtime exception은 throws 에 명시해도,  catch 를 강제하지 않습니다.
void someAPI() throws NoSuchElementException
}
물론 아예 명시 안하는 것 보다는 낫지만,
예외처리를 강제하지 않기 때문에 ,    예외처리가 누락되어 문제가 발생할 수 있습니다.

Runtime Exception 은  프로세스가 강제로 종료되어야만 하는 경우 ( 예를 들어 null pointer 나  divide by zero 같은 ) 에만 발생하여야 하나
실제로는  개발자가 반드시 예외처리해야 하는 일반적인 예외까지도 Runtime Exception 으로 되어 있는 경우가 많습니다.

go로 예를 들자면 ,
error 가 리턴되기를 기대했는데   , panic 이 발생해 버리는 케이스 입니다.

다음은 대표적으로 잘못 설계된 runtime exception 목록입니다.
NoSuchElementException
IllegalArgumentException
BufferOverflowException
IllegalStateException

1.3. 하나의 try 에 모든 코드를 넣지 않습니다.
c 나 go 스타일을 코딩에 익숙하거나,
혹은 귀찮아서 ,  하나의 try 에 모든 코드를 넣는 경우가 있는데,
go 에서 function call 마다  error 체크를 해야 하듯이
exception을 throw 하는 메소드 호출 마다  try 블럭을 새로 만들어야 합니다.
try 블럭을 새로 만들 때는 ,  중첩하여 만들어야 합니다.
NOK
OK
void abc() {
// 마지막에 close 를 해야 하기 때문에
// 변수 선언을 try block 안에서 못하고
// try 바깥에서 한번에 해야 합니다.
A a = null;
B b = null;
C c = null;

    try {
        a = openA();
        b = openB(a);
        c = openC(b);
    } catch( Exception e) {
        // 어느 API에서 error 가 발생했는지 알 수 없습니다.
        // openA, openB 의 예외처리가 한 block 에 섞입니다.
        // 코드가 길어져서 리팩토링 하게 되면
        // exception 처리 쪽에서도 코드 분리를 해야 합니다.
    } finally {
        // 함수가 분리 될 때
        // close 하는 부분도 코드분리가 필요합니다.
         
        // close 할 때 마다 null 체크가 필요합니다.
        if ( c != null ) {
            c.close();
        }
        if (b != null) {
            b.close();
        }
        if (a != null) {
            a.close();
        }
    }
}
void abc() {
try {
// 변수를 try 블럭 안에서 선언할 수 있습니다.
var a = openA();
try {
// try 블럭 안에서 선언함으로서
// b 변수의 scope 를 필요한 만큼만 지정할 수 있습니다.
var b= openB(a);
try {
var c = openC(b);
try {
if ( c== null ) {
// 안쪽 블럭의 예외를 바깥쪽으로 throw 가능
throw new NullPointerException();
}
} finally {
// open 하는 것과 같은 indent level 에서 close 가 호출 됩니다.
c.close();
}
} catch(TimeoutException e) {
// openC 예외 처리
} finally {
// open 하는 것과 같은 indent level 에서 close 가 호출 됩니다.
b.close();
}
} catch ( TimeoutException e) {
// openB 예외 처리
} catch ( NullPointerException e) {
// 안쪽 block 의 예외처리 가능
} finally {
// open 하는 것과 같은 indent level 에서 close 가 호출 됩니다.
a.close();
}
} catch ( IOException e) {
// openA 예외처리
}
}
코드 라인 수는 왼쪽이 짧지만 ,  리팩토링하기가 쉽지 않습니다.

오른쪽의 경우 메소드 분리는 다음과 같이 이루어 집니다.
void abc() {
try {
var a = openA();
try {
// 분리할 부분을 삭제하고  메소드 호출로 변경합니다.
bc(a);

            // 아래쪽의 예외처리 및 close 코드들은 리팩토링 되어도 변경이 되지 않습니다.
        } catch ( TimeoutException e) {
            // openB 예외 처리
        } catch ( NullPointerException e) {
            // 안쪽 block 의 예외처리 가능
        } finally {
            // open 하는 것과 같은 indent level 에서 close 가 호출 됩니다.
            a.close();
        }
    } catch ( IOException e) {
        // openA 예외처리
    }
}

// openB 와 openC 가 참조하던 변수들은 메소드 아규먼트로 받습니다.
void bc(A a) throws TimeoutException {
// openB 부터  block 안의 코드를 그대록 복사해서 붙여 넣으면 됩니다.
// 변경할 게 아무 것도 없습니다.
var b= openB(a);
try {
var c = openC(b);
try {
if ( c== null ) {
// 안쪽 블럭의 예외를 바깥쪽으로 throw 가능
throw new NullPointerException();
}
} finally {
// open 하는 것과 같은 indent level 에서 close 가 호출 됩니다.
c.close();
}
} catch(TimeoutException e) {
// openC 예외 처리
} finally {
// open 하는 것과 같은 indent level 에서 close 가 호출 됩니다.
b.close();
}
}

대신 오른쪽 방법은 indent가 너무 깊어지는 문제가 있습니다.
하지만 이건 문제라기 보다는,  한 메소드안의  코드가 너무 길어졌으니,  분리하여 리팩토링 하라는 신호를 개발자에게 보내는 것이라 생각하면 됩니다.
indent가 깊어질 때 마다   메소드를 적절하게 분리하면,   결과적으로 읽기 좋은 깔끔한 코드를 만들 수 있습니다.

close 를 안하거나,  별도의 예외처리를 하지 않는 경우에는 ,  하나의 try 에 코드를 몰아 넣어도 됩니다.
void abc() throws TimeoutException, IoException {
var a = openA();
try {
var b = openB(a);
var c = openC(b);
} finally {
a.close();
}
}

1.4. exception 을 처리할 필요가 없는 API 여도  close 를 해야 하는 API 라면  try-finally 구문을 사용합니다.
NOK
OK
var a = openA();

// do something

// Runtime예외가 발생해서 close가 호출 되지 않을 수 있습니다.
a.close();
return
var a = openA();
try {
// do something
} finally {
a.close();
}

open 할 때 마다 try-finally 로 close 하는 습관을 가지면 , 자연스럽게  1.3. 하나의 try 에 모든 코드를 넣지 않습니다.  패턴으로 코딩하게 됩니다.
AutoCloseable 을 구현하고 있는 class는   try-with-resource 를 사용하여 좀 더 편하게 코딩할 수 있습니다.
// try 의 () 안에  변수 선언과 open 호출을 넣어 둡니다.
try (var a = openA()) {
bc(a);
} catch (Exception e) {
// AutoCloseable 의 close 함수는 Exception을 throw 하기 때문에,  catch가 추가됩니다.
// 여기서 실제 로직 ( 여기서는 bc(a) 호출 ) 의 exception 처리를 같이 하지 않도록 주의 하세요.
}

AutoCloseable 을 그냥 상속 받고 구현하면  ,  Exception  을 throw 하게 되니 ,
다음과 같이,  전용 exception을 만들고,   throws 부분을 override 하여 구현하세요.
NOK
OK
public class A implements AutoCloseable {
public void close() throws Exception {
}
}
public CloseException extends Exception {
}
public class A implements AutoCloseable {
public void close() throws CloseException {

    }
}
// try 의 () 안에  변수 선언과 open 호출을 넣어 둡니다.
try (var a = openA()) {
bc(a);
} catch (CloseException e) {
// Close 전용 exception이 추가되었기 때문에
// 예외처리 코드가 분리가 됩니다.
}
1.5. exception을 throw 하는 메소드를 오버라이드 할 때 , 공변이 허용됩니다.

public interface AutoCloseable {
void close() throws Exception;
}

public class A implements AutoCloseable {
// AutoCloseable의 close 메소드는 Exception을 throw 하고 있지만
// Exception을 상속 받는 다른 exception을 throw 하는게 가능합니다.
public void close() throws CloseException {

    }
}


public class B implements AutoCloseable {
// AutoCloseable의 close 메소드는 Exception을 throw 하고 있지만
// 아예 Exception을 throw 하지 않도록  override 하는 것도 가능합니다.
public void close()  {

    }
}

2. 고전적인 try-catch 의 문제점
   java 에는  try-catch  구문이 있어  오랬동안 잘 사용되어 왔는데,
   lambda expression 이 추가된 이후에는 ,    try-catch 가  lambda expression과 잘 맞지 않아
   다른 방법으로 예외처리를 해야 합니다.

try-catch가 lambda expression 과 잘 맞지 않는 이유는,   lambda expression을 위해 추가된  다음 interface 들이   throw 를 허용하지 않기 때문입니다.
public interface Function<T, R> {
R apply(T t);
}

public interface Consumer<T> {
void accept(T t);
}

public interface Supplier<T> {
T get();
}

Optional 클래스의 map 함수를 보면  이런  Functional interface 를 요구하고 있는데
public final class Optional<T> {
public <U> Optional<U> map(Function<? super T, ? extends U> mapper)
}

때문에 Exception을 throw 하는 함수들과 같이 혼용해서 사용할 수가 없습니다.
public final class URI {
public URI(String str) throws URISyntaxException {
new Parser(str).parse(false);
}
}


Optional<String> opt = Optional.of("http://hello.com");
opt.map(s -> {
// new URI 가 URISyntaxException 을 throw 하기 때문에 , compile 되지 않습니다.
return new URI(s);
});

이럴 경우 컴파일 오류를 해결 하기 위해 다음과 같이  RuntimeException을 throw 하게 고치게 되는데,
void someMethod() {
Optional<String> opt = Optional.of("http://hello.com");
opt.map(s -> {
try {
return new URI(s);
} catch (URISyntaxException e) {
// 어쩔 수 없는 경우 아니면 이렇게 하지 마세요.
throw new RuntimeException(e);
}
});
}

try {
someMethod();
} catch ( RuntimeException e) {
// someMethod 가  method 에 정의되지 않은 RuntimeException을 throw 하기 때문에
// catch를 못해, 큰 문제를 일으킬 수 있습니다.
}
Optional 이나 Stream , Future 등의 callback을 코딩할 때는 ,  최악의 선택을 한겁니다.

Optional의  map은 다음과 같이 코딩해야 합니다.
Optional<String> opt = Optional.of("http://hello.com");
Optional<URI> urlOption = opt.flatMap(s -> {
try {
return Optional.of(new URI(s));
} catch (Throwable e) {
return Optional.empty();
}
});

// URI 파싱이 된 경우에만 , request 를 보내도록 try-catch 없이 코딩할 수 있습니다.
urlOption.stream().forEach(u -> {
if (u.getScheme().equals("https")) {
// send request
}
});
URI parse 에 실패한 경우,  Optional 은 empty 로 변환됩니다.

혹은 exception 정보를  유지하고 싶은 경우에는 , 아래와 같이 Optional 을  scala의 Try 에 해당하는 CompletionStage 로 변환 하면 됩니다.
Optional<CompletionStage<URI>> uriOption = opt.map(s -> {
try {
return CompletableFuture.completedStage(new URI(s));
} catch (Throwable e) {
return CompletableFuture.failedStage(e);
}
});

CompletionStage<URI> uriTry = uriOption.orElse(CompletableFuture.failedStage(new NoSuchElementException("no url provided")));
uriTry.thenAccept(uri -> {
System.out.println(uri);
});

lambda expression 안에서  exception 이 발생되는 경우 ,  lambda expression 에서는 CompletionStage 혹은 CompletableFuture 를 리턴해야 합니다.
( scala 의 Try 에 해당하는 것이 CompletionStage 인데
java 의 CompletionStage 는  Optional 로 다시 변환할 수 있는 방법이 없네요. )

따라서  lambda expression 을 많이 활용할 예정이라면
API 를 설계 할 때  exception 을 throw 하는 것보다  CompletionStage 를 리턴하도록  디자인 하는 것이 중요합니다.


NOK
OK
예외처리
null 리턴 가능성이 있는 API
public class Hello {
public String Hello() {
return null;
}
}


public class Hello {
public Optional<String> Hello() {
return Optional.empty()
}
}
에러가 발생한 경우 Optional.empty() 가 리턴됩니다.
exception 을 throw 하는 API
public class Hello {
public String Hello() throws SomeException {
throw new NoSuchElementException("null");
}
}
public class Hello {
public CompletionStage<String> Hello() {
CompletableFuture.failedStage(
new NoSuchElementException("null");
)
}
}
null을 리턴해야 하는 경우
CompletableFuture.failedStage() 로
NoSuchElementException 이 리턴됩니다.
exception 을 throw 하지만
null 도 리턴할 수 있는 API
public class Hello {
public String Hello() throws SomeException {
return null;
}
}
public class Hello {
public CompletionStage<Optional<String>> Hello() {
CompletableFuture.completedStage(Optional.empty());
}
}
타입이 복잡해지기 때문에
null 일 경우 Optional을 제거하고
NoSuchElementException 을 사용하는게
더 나을 수도 있습니다.


3. Railway Oriented Programming
   try-catch 를 사용하지 않는 경우 ,   예외처리를 할 때는 Railway oriented programming을 사용하는 것이  최신 트렌드 입니다.
   Optional 이나 CompletionStage 을 사용하면 Railway oriented programming 으로 예외 처리하는 것이 가능합니다.
   https://naveenkumarmuguda.medium.com/railway-oriented-programming-a-powerful-functional-programming-pattern-ab454e467f31
   https://blog.logrocket.com/what-is-railway-oriented-programming/

자세한 내용은 위의 페이지를 참조하고 ,
간단히만 설명하자면
Optional 이나 CompletionStage 는   성공 혹은 실패 두가지 값중 하나만을 가질 수 있습니다.
( Optional.empty() 가 실패 에 해당 )

그림으로 그리자면 , 다음과 같은 레일로 그림을 그릴 수 있습니다.
A diagram of railway oriented programming. The green track represents success and the red track represents failure.

Optional 이나 CompletionStage 에서 제공하는 함수들을 이용하여 이 레일들을 결합하여 ,  더 긴 레일을 만들 수 있습니다.
A diagram of a two track system. One track is for successes, one is for failures.

Optional 의 map 함수나 CompletionStage 의 thenApply 같은 함수들은 성공  상태일 때만 실행되고
실패 상태에서는 실행되지 않습니다.

여러개의 함수가  연결되어 있을 때,  모두 성공하면  녹색으로 표시된 성공 레일을 따라 가서  , 마지막으로 성공 상태가 되게 됩니다.

레일의 중간에서 언제든지 ,  실패 레일로 이동하는 것이 가능하고,
실패 레일로 이동하게 되면  map 이나 thenApply 가 실행되지 않으므로 , 계속 실패 상태에 남게 됩니다.

다음은 Optional 사용중에  에러가 발생하면 Optional.empty() 로 이동하는 예제 입니다.
Optional<String> opt = Optional.of("http://hello.com");

Optional<URI> opt2 = opt.flatMap(s -> {
try {
return Optional.of(new URI(s));
} catch (Throwable e) {
return Optional.empty();
}
});

물론 실패레일에서  성공레일로 옮겨가게 해주는 함수들도 제공 됩니다.
Optional 의 or 함수는  실패 상태에서만 실행됩니다.
Optional<String> o = Optional.empty();
o = o.or(() -> Optional.of("hello"));

4. Railway Oriented Programming의 대표적인  컨테이너들
   4.1. Option
   Option 은 Some 과 None 상태중 한가지 상태를 가질 수 있는 컨테이너 입니다.

scala
java

Some
var some : Option[String] = Some("hello")
Optional<String> some = Optional.of("hello");

None
var none : Option[String] = None
Optional<String> none = Optional.empty();

// 혹은  java 11
var none = Optional.<String>empty();

Option
val s : String = null
var option = Option(s)
String s = null
Optional<String> option = Optional.ofNullable(s);
값을 null check 해서 null 이면 None
아니면 Some 이 만들어 집니다.
Map
option = option.map(s => s + " world")
option = option.map(s -> s + " world");
map 은 Some 상태에서만 실행됩니다.
FlatMap
option = option.flatMap(s => {
s match {
case "hello" => Some("hello world")
case _ => None
}
})
option= option.flatMap(s -> {
if ( s.equals("hello") ) {
return Optional.of("hello world");
} else {
return Optional.empty();
}
});
FlatMap 은 Some 상태에서 None 상태로
레일을 갈아탈 수 있게 해줍니다.
OrElse
option = option.orElse(Some("hello"))
option = option.or(() -> Optional.of("hello"));
orElse 는 None 상태를 Some 상태로
레일을 갈아 탈 수 있게 해줍니다.


4.2. Try
Try 는 Success 와 Failure  상태중 한가지 상태만을 가질 수 있는 컨테이너 입니다.
Java 에는 Try 에 해당하는 class 는 없지만  CompletionStage 라는 interface 를 사용할 수 있습니다.
CompletionStage 과 Future 와 얽혀 있어서,  API 가 좀 모자른 느낌이 나는데,
scala 에서도 Future 의 onComplete 나 transform  callback 의 아규먼트로 사용하기 위해서 Try 를 사용하며 Try 만 단독으로 사용하는 경우는 거의 없기 때문에 ,     
java에서도 CompletionStage 를 Try 대신 사용해도 될 것 같습니다.


scala
java

Success
var success : Try[String] = Success("hello")
CompletionStage<String> success = CompletableFuture.completedStage("hello");

Failure
var failure : Try[String] = Failure(new Exception())
CompletionStage<String> failure = CompletableFuture.failedStage(new Exception());

// 혹은  java 11
var failure = CompletableFuture.<String>failedStage(new Exception());

Try
var t : Try[String] = Try {
throw new Exception()
}
Java 는 제공되는 함수는 없고 다음과 같이 만들어서 사용가능
@FunctionalInterface
public interface ExceptionSupplier<T> {
public T supply() throws Throwable;
}

public <T> CompletionStage<T> Try( ExceptionSupplier<T> sup) {
try {
var ret = sup.supply();
return CompletableFuture.completedStage(ret);
} catch (Throwable e ) {
return CompletableFuture.failedStage(e);
}
}

CompletionStage<String> t = Try(() -> {
throw new Exception();
});

Map
t = t.map(s => s + " world")
t= t.thenApply(s ->
s = s + " world"
);
map 과 thenApply 는
Success 상태에서만 실행됩니다.
FlatMap
t = t.flatMap(s => {
s match {
case "hello" => Success("hello world")
case _ => Failure(new Exception())
}
})
t = t.thenCompose(s -> {
if (s.equals("hello")) {
return CompletableFuture.completedStage("hello world");
} else {
return CompletableFuture.failedFuture(new Exception());
}
});
flatMap 과 thenCompose 함수를 사용하면
Failure 상태로 레일을 갈아탈 수 있습니다.
Recover
t = t.recover{
case e : NoSuchElementException=>
"hello recover"
}
CompletionStage<CompletionStage<String>> t2 = t.handle((s, throwable) -> {
if (throwable == null) {
return CompletableFuture.completedStage(s);
}

    if (throwable.getCause() instanceof NoSuchElementException) {
        return CompletableFuture.completedStage("hello recover");
    }
    return CompletableFuture.failedStage(throwable);
});

t = t2.thenCompose(completionStage -> completionStage);
recover 함수나 handle 함수를 사용하면
Failure 상태에서 Success 상태로
레일을 갈아 탈 수 있습니다.

java의 경우 exceptionally 가 비슷한 함수인데
항상 Success 를 리턴해야 하는 문제가 있어
사용이 불가능합니다.

handle 함수는 flatMap 처럼 동작하지 않기 때문에
thenCompose 로 flatten 시켜야 합니다.
4.3. Future
Future 는  Try에 아직 완료되지 않은 상태가 추가된 컨테이너 입니다.
Future 에도 Try 와 동일한 메소드들이 있는데
완료되지 않은 상태에서는 실행되지 않고 , 완료된 이후에만  실행이 됩니다.
완료된 이후에는  성공 혹은 실패 상태를 가질 수 있습니다.

java 의 CompleteableFuture 는 Future 에 해당하는 클래스인데 ,
특이하게도 Promise 와 Future 를 합친 구조로 되어있고
CompletionStage 인터페이스를 구현하고 있습니다.
그래서 CompletionStage의 API 를 그대로 사용할 수 있습니다.

Future 의 생성 방법에 대해서만 정리하겠습니다.
transform 방법에 대해서는 Try 를 참조하면 됩니다.

scala
java

Future 의 생성
val promise = Promise[String]()

val future = promise.future
CompletableFuture<String> future = new CompletableFuture();

성공 상태로 이동
promise.success("hello")
future.complete("hello");

실패 상태로 이동
promise.failure(new Exception())
future.completeExceptionally(new Exception());

코드블럭으로 Future 생성
val future = Future {
"hello"
}
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
return "hello";
});


4.4. Either
Either 는 Try 대신에 많이 사용하는 컨테이너인데 ,  java 에서  기본으로 제공해 주고 있지 않습니다.

Try 와 비교했을 때 Either 타입의 장점은  ,   어떤 타입의 에러가 발생하는지  명시해 줄 수 있다는 점입니다.

다음은 예제코드입니다.
// try 는 성공했을 때 String 을 리턴한다는 것만 알려주지 , 에러의 종류를 명시해 주지는 않습니다.
def doSomething() : Try[String]

// either 는  실패했을 때 IOException 을 리턴한다는 것을 알려줍니다.
def doSomething() : Either[IOException, String]


// 두개 이상의 exception을 리턴할 수 있는 경우에는 다음과 같이 infix notation 으로 표시할 수도 있습니다.
def doSomething() : IOException Either TimeoutException Either String

// 왼쪽 부터 계산 하니까  (IOException Either TimeoutException) Either String 과 동일하고
// 결국 다음과 동일합니다.  Either[Either[IOException,TimeoutException] , String]



5. Railway Oriented Programming의 장점
   5.1. 함수의 합성을 할 때 가독성이 좋아집니다.
   다음과 같이 세 함수가 있는 경우
   func AtoB( a A ) B {
   return B{a}
   }

func BtoC (b B) C {
return C{b}
}

func CtoD (c C) D {
return D{c}
}

우리는 다음과 같이 함수 합성을 할 수 있습니다.
d := CtoD( BtoC ( AtoB ( A{} ) ) )

코드는 CtoD , BtoC , AtoB 순으로 적혀 있지만 , 실제 실행은  AtoB -> BtoC -> CtoD 순서로 실행됩니다.
이 때 Option 컨테이너를 사용하면  다음과 같이 코드를 바꿔 쓸 수 있습니다.
d := Option( A{} )
.Map(AtoB)
.Map(BtoC)
.Map(CtoD)
.Get()


실행되는 순서로 코드를 읽을 수 있습니다.

5.2. 예외처리 코드가 짧아집니다.
Try 를 사용하지 않는 경우에는 , 다음과 같이  매 함수 호출 마다 에러검사를 해야 합니다.
func AtoB( a A ) (B, error) {
return B{a}, nil
}

func BtoC (b B) (C, error) {
return C{b},nil
}

func CtoD (c C) (D,error) {
return D{c},nil
}

func AtoD(a A) (D, error) {
b , err := AtoB( a )
// 함수 호출 마다 err 검사하고 리턴하는 코드 세줄이 추가 됩니다.
if err != nil {
return nil, err
}
c , err := BtoC( b )
if err != nil {
return nil, err
}
return CtoD ( c )
}

Try 를 사용한다면 다음과 같이 , 에러 검사할 필요 없이  flatMap 으로 chain 만 만들어 주면 됩니다.
func AtoB( a A ) Try[B] {
return Success(B{a})
}

func BtoC (b B) Try[C] {
return Success(C{b})
}

func CtoD (c C) Try[D] {
return Success(D{c})
}

func AtoD(a A) Try[D] {
d := Succcess(a)
.flatMap(AtoB)
.flatMap(BtoC)
.flatMap(CtoD)
return d
}


6. Railway Oriented Programming을 사용해야 하나요?
   multi thread 프로그래밍을 할 때  
   fork join pool 을 사용한 executor 와  Future 를 사용하는 추세가 점점 많아지고 있습니다.

만약 Future 를 사용한다면 ,  Future 가 Railway Oriented Programming을 지원하는 컨테이너이기 때문에
try-catch 대신  Railway Oriented Programming 으로 구현하는 것이 좋습니다.

또 lambda expression 사용 빈도가 높고  java의 stream을 사용한다면 ,   마찬가지로 Railway Oriented Programming 을 사용하는게 좋겠습니다.

하지만  Future , stream ,  lambda expression 을 사용하지 않는다면
굳이 try-catch 를 버리고  Railway Oriented Programming을 사용할 필요는 없다고 생각합니다.

java 1.8 에서 위의 세 기능이 추가되기 전에  try-catch 로도 잘 해왔잖아요?


Try-Catch
Railway Oriented Programming
API 가독성
발생 가능한 exception 명시
Runtime exception은 명시안함
exception 이 발생한다는 사실만 명시
Runtime exception만 발생하는 경우에도 발생한다는 사실 명시
강제 예외처리
예외처리를 강제함
Runtime exception은 예외처리 강제안함
모든 exception에 대해 예외처리 강제함
구조적 문제
Exception 목록이 너무 길어져, 모든 exception을 나열하지 않고 상위 exception으로 대체하는 문제
메소드 상속 받을 때 exception을 추가하는 것이 불가능해서 Runtime exception이 남용됨
try-with-resource 기능 구현이 쉽지 않음

A. 부록  Future 의  Try-With-Resource  구현
def tryWithResource[T <: AutoCloseable,R]( r : Future[T])( f : T => Future[R] ) : Future[R] = {
r.flatMap( r => {
val ret = f(r)
ret.onComplete(_ => Try(r.close()))
ret
})
}

def openA() : Future[A] = ???
def bc( a : A ) : Future[C] = ???

def test() : Future[C] = {
tryWithResource(openA()){
a => bc(a)
}
}


좋아요처음으로 좋아하는 사람이 되볼까요?
레이블 없음 레이블 편집
1 개의 댓글
사용자 아이콘: gura
허재녕
Future example
package com.uangel.test;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class TestFuture {

    class FilePair implements  AutoCloseable{
        File file1;
        File file2;
 
        public FilePair(File file1, File file2) {
            this.file1 = file1;
            this.file2 = file2;
        }
 
        @Override
        public void close() throws Exception {
 
        }
    }
 
 
    class File implements AutoCloseable {
        @Override
        public void close()  {
 
        }
    }
 
    CompletableFuture<File> open(String name) {
        return CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("hello world");
//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            return new File();
});
}




    CompletableFuture<Integer> copyStream(FilePair pair) {
        System.out.println("copy stream");
        //CompletableFuture.failedFuture(new Exception());
        return CompletableFuture.completedFuture(100);
 
    }
 
 
 
    CompletableFuture<FilePair> openSecond( File file1 , String name ) {
        System.out.println("openSecond");
        var f3 =   open(name).thenApply(file2 -> new FilePair(file1, file2));
        return f3;
        //return CompletableFuture.failedFuture(new IOException("no such file"));
    }
 
 
    <T extends AutoCloseable,R> CompletableFuture<R> withResource(T a, Function<T,CompletableFuture<R>> mapFunc) {
        var f = mapFunc.apply(a);
        f.whenComplete((r, throwable) -> {
            try {
                a.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return f;
    }
 
    @Test
    public void test() throws InterruptedException {
        var t1 = open("file1");
        var t2 = t1.thenCompose( file -> {
            return withResource(file, file1 -> {
                var t3 = openSecond(file1, "file2");
                return t3.thenCompose(filePair -> {
                    return withResource(filePair , this::copyStream);
                });
            });
        });
 
 
        var f = open("file1")
                .thenCompose(file1 -> openSecond(file1, "name"))
                .thenCompose(this::copyStream);
 
        f.thenAccept(n -> {
            System.out.printf("file copied %d bytes\n", n);
         });
 
        f.whenComplete((integer, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            }
        });
 
 
        System.out.println("before sleep");
        Thread.sleep(100);
    }
}
