# Description
This project uses [JMH](https://github.com/openjdk/jmh) to perform benchmarks on various implementation of ContinuousByteStringSplitter.

## ContinuousByteStringSplitter?
```java
import akka.util.ByteString;

class Test {
  public static void main(String[] args) {
    ByteString delimiter = ByteString.fromString("$");
    ContinuousByteStringSplitter splitter = new ContinuousByteStringSplitter(delimiter);
    
    // only 'a' and 'b' are in result1, but 'c' is not.
    Iterable<ByteString> result1 = splitter.apply(ByteString.fromString("a$b$c"));

    // 'cd' is in result2
    Iterable<ByteString> result2 = splitter.apply(ByteString.fromString("d$"));
  }
}
```
## JMH Benchmark Structure
```
Benchmark
|
|___ Dry Run(Warmup Fork)
|    |
|    |___ Warmup Iterations
|    |
|    |___ Measurement Iterations
|
|___ Run(Fork)
     |
     |___ Warmup Iterations
     |
     |___ Measurement Iterations
```