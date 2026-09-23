package com.one.memorymatch

import com.one.memorymatch.ui.components.MathChallenge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ParentGateTest {

    @Test
    fun mathChallenge_generatesValidAdditionWithin20() {
        for (seed in 0..100) {
            val random = Random(seed)
            val challenge = MathChallenge.generate(random)

            // Verify sum formula
            assertEquals(
                "Answer must equal num1 + num2",
                challenge.num1 + challenge.num2,
                challenge.answer
            )

            // Verify sum <= 20
            assertTrue(
                "Sum must be <= 20, got ${challenge.answer}",
                challenge.answer <= 20
            )

            // Verify 3 distinct options
            assertEquals(
                "Must have exactly 3 options",
                3,
                challenge.options.size
            )
            assertEquals(
                "All 3 options must be distinct",
                3,
                challenge.options.distinct().size
            )

            // Verify correct answer is among options
            assertTrue(
                "Options must contain correct answer",
                challenge.options.contains(challenge.answer)
            )

            // Verify all options are in plausible range 2..20
            for (opt in challenge.options) {
                assertTrue("Option $opt must be between 2 and 20", opt in 2..20)
            }
        }
    }
}
