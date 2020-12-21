package com.netbase;

import akka.util.ByteString;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Timeout;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@Timeout(time = 60, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 5, warmups = 1)
@Warmup(iterations = 3, time = 10, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 10, timeUnit = TimeUnit.SECONDS)
public class MyBenchmarks {

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        public ByteString input;
        public ContinuousByteStringSplitter splitter;
        public ContinuousByteStringSplitterLessAllocate splitterRevise;

        @Setup(Level.Trial)
        public void setup() {
            ByteString delimiter = ByteString.fromString(System.lineSeparator());
            input = ByteString.fromString("567" + System.lineSeparator() + "1234");
            splitter = new ContinuousByteStringSplitter(delimiter);
            splitterRevise = new ContinuousByteStringSplitterLessAllocate(delimiter);
        }
    }

    @Benchmark
    public void continuousByteStringSplitter(BenchmarkState state) {
        state.splitter.apply(state.input);
    }

    @Benchmark
    public void continuousByteStringSplitterLessAllocate(BenchmarkState state) {
        state.splitterRevise.apply(state.input);
    }


    //@Benchmark
    public void doNothing() {
    }

    //@Benchmark
    public int constantFolding() {
        int a = 1;
        int b = 2;
        int sum = a + b;
        return sum;
    }

    //@Benchmark
    public void doDeadCodeElimination() {
        double a = 5;
        double b = 7;
        double c = a * a + b * b + Math.sqrt(a * b);
    }
}
