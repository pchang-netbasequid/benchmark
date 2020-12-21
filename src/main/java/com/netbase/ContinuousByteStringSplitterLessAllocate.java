package com.netbase;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import akka.japi.Pair;
import akka.japi.function.Function;
import akka.util.ByteString;

public class ContinuousByteStringSplitterLessAllocate implements Function<ByteString, Iterable<ByteString>> {
    public static final ByteString END_ELEMENT = ByteString.fromString("NON_EMPTY_STRING");

    private final byte[] delimiter;
    private transient ByteBuffer chunk = ByteBuffer.allocate(0);

    public ContinuousByteStringSplitterLessAllocate(ByteString delimiter) {
        this.delimiter = delimiter.toArray();
    }

    @Override
    public Iterable<ByteString> apply(ByteString element) {
        if (element == END_ELEMENT) {
            if (chunk.hasRemaining()) {
                throw new IllegalArgumentException(
                        "Incomplete byte string:" + ByteString.fromByteBuffer(chunk.slice()));
            } else {
                return Collections.emptyList();
            }
        } else if (element.isEmpty()) {
            return Collections.emptyList();
        } else {
            // merge remaining with incoming
            if (chunk.remaining() + element.size() > chunk.capacity()) {
                chunk = ByteBuffer.allocate(chunk.remaining() + element.size())
                        .put(chunk);
            } else {
                chunk.compact();
            }
            chunk.put(element.asByteBuffer());

            chunk.flip();
            return chunkSplit();
        }
    }

    private List<ByteString> chunkSplit() {
        List<ByteString> result = new ArrayList<>();

        int chunkLimit = chunk.limit();
        chunk.mark();
        while (chunk.position() <= chunkLimit - delimiter.length) {
            // try to match delimiter at current position
            int delimiterPosition = chunk.position();
            for (int i = 0; i < delimiter.length; i++) {
                if (delimiter[i] != chunk.get()) {
                    chunk.position(delimiterPosition);
                    break;
                }
            }

            if (chunk.position() == delimiterPosition) {
                chunk.position(delimiterPosition + 1);
            } else {
                chunk.reset();
                chunk.limit(delimiterPosition);
                if (chunk.hasRemaining()) {
                    result.add(ByteString.fromByteBuffer(chunk));
                }

                // position for next possible block
                chunk.limit(chunkLimit);
                chunk.position(delimiterPosition + delimiter.length);
                chunk.mark();
            }
        }
        chunk.reset();
        return result;
    }
}