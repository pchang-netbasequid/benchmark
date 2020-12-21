package com.netbase;

import akka.japi.Pair;
import akka.japi.function.Function;
import akka.util.ByteString;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ContinuousByteStringSplitter implements Function<ByteString, Iterable<ByteString>> {
    public static final ByteString END_ELEMENT = ByteString.fromString("NON_EMPTY_STRING");

    private final byte[] delimiter;
    private byte[] remaining = new byte[0];

    public ContinuousByteStringSplitter(ByteString delimiter) {
        this.delimiter = delimiter.toArray();
    }

    @Override
    public Iterable<ByteString> apply(ByteString element) {
        if (element == END_ELEMENT) {
            // fail when remaining is incomplete
            if (remaining.length != 0) {
                throw new IllegalArgumentException("Incomplete byte string:" + ByteString.fromArray(remaining));
            } else {
                return Collections.emptyList();
            }
        } else if (element.isEmpty()) {
            return Collections.emptyList();
        } else {
            // merge remaining with incoming chunk
            byte[] mergedArr = ByteBuffer.allocate(remaining.length + element.size())
                    .put(remaining)
                    .put(element.toArray())
                    .array();

            Pair<List<byte[]>, byte[]> splitResult = chunkSplit(delimiter, mergedArr);
            remaining = splitResult.second();
            return splitResult.first()
                    .stream()
                    .map(ByteString::fromArray)
                    .collect(Collectors.toList());
        }
    }

    private boolean isMatch(byte[] pattern, byte[] input, int pos) {
        for (int i = 0; i < pattern.length; i++) {
            if (pattern[i] != input[pos + i]) {
                return false;
            }
        }
        return true;
    }

    private Pair<List<byte[]>, byte[]> chunkSplit(byte[] delimiter, byte[] chunk) {
        List<byte[]> result = new ArrayList<>();
        byte[] incompleteBlock = new byte[0];

        int blockStart = 0;
        int current = 0;
        while (current <= chunk.length - delimiter.length) {
            if (isMatch(delimiter, chunk, current)) {
                if (blockStart < current) {
                    result.add(Arrays.copyOfRange(chunk, blockStart, current));
                }
                blockStart = current + delimiter.length;
                current = blockStart;
            } else {
                current++;
            }
        }
        // incomplete block (without delimiter at tail)
        if (blockStart < chunk.length) {
            incompleteBlock = Arrays.copyOfRange(chunk, blockStart, chunk.length);
        }
        return new Pair<>(result, incompleteBlock);
    }
}