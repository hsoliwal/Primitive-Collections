/*
 * Copyright 2026 Hitesh Soliwal and contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package speiger.src.collections.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Random;
import org.junit.Test;
import speiger.src.collections.utils.SanityChecks;

/** Boundary proof of the central array offset/length guard. */
public class SanityChecksCapacityTest {
    private static void check(int capacity, int offset, int length) {
        Class<? extends Throwable> expected = null;
        if (offset < 0) expected = IndexOutOfBoundsException.class;
        else if (length < 0) expected = IllegalArgumentException.class;
        else if ((long) offset + length > capacity) expected = IndexOutOfBoundsException.class;
        try {
            SanityChecks.checkArrayCapacity(capacity, offset, length);
            if (expected != null) fail("accepted invalid range " + capacity + "/" + offset + "/" + length);
        } catch (IndexOutOfBoundsException unexpected) {
            if (expected != IndexOutOfBoundsException.class) throw unexpected;
        } catch (IllegalArgumentException unexpected) {
            if (expected != IllegalArgumentException.class) throw unexpected;
        }
    }

    @Test public void overflowAndExactEndAreDifferent() {
        check(Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
        check(Integer.MAX_VALUE, Integer.MAX_VALUE - 1, 1);
        check(Integer.MAX_VALUE, Integer.MAX_VALUE - 1, 2);
        check(Integer.MAX_VALUE, Integer.MAX_VALUE - 1, 16);
        check(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        check(0, 0, 0);
        check(0, 0, 1);
        check(4, 2, 2);
        check(4, 2, 3);
        check(4, -1, 2);
        check(4, 1, -1);
    }

    @Test public void randomRangesMatchLongArithmetic() {
        Random random = new Random(0x4D334152524159L);
        for (int i = 0; i < 20000; i++) {
            int capacity = random.nextInt(10000);
            int offset = random.nextBoolean() ? random.nextInt(12000) : random.nextInt();
            int length = random.nextBoolean() ? random.nextInt(12000) : random.nextInt();
            check(capacity, offset, length);
        }
    }

    @Test public void messageAndExceptionTypesForOrdinaryInvalidRanges() {
        try {
            SanityChecks.checkArrayCapacity(3, 2, 2);
            fail("expected out of bounds");
        } catch (IndexOutOfBoundsException expected) {
            assertEquals("Index (4) is not in size (3)", expected.getMessage());
        }
        try {
            SanityChecks.checkArrayCapacity(3, -1, 1);
            fail("expected negative offset");
        } catch (IndexOutOfBoundsException expected) {
            assertEquals("Offset is negative (-1)", expected.getMessage());
        }
        try {
            SanityChecks.checkArrayCapacity(3, 1, -1);
            fail("expected negative length");
        } catch (IllegalArgumentException expected) {
            assertEquals("Size is negative (-1)", expected.getMessage());
        }
    }
}
